"""Exercise LSPatch signing arguments without ADB or real credentials."""
from pathlib import Path
import os
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]
INSTALLER = ROOT / "installer"

MOCK_APKSIGNER = r'''#!/usr/bin/env bash
set -euo pipefail
[[ "$1" == sign ]]
shift
output=""
source=""
while (($#)); do
    if [[ "$1" == --out ]]; then output="$2"; shift 2; else source="$1"; shift; fi
done
[[ -n "$output" && -n "$source" ]]
cp "$source" "$output"
'''

MOCK_KEYTOOL = r'''#!/usr/bin/env bash
set -euo pipefail
while (($#)); do
    if [[ "$1" == -keystore ]]; then : > "$2"; shift 2; else shift; fi
done
'''


class SigningTest(unittest.TestCase):
    def invoke(self, setup, check, stdin=""):
        with tempfile.TemporaryDirectory(prefix="spotitheme-signing-test-") as temporary:
            root = Path(temporary)
            work = root / "work"
            mock_bin = root / "bin"
            mock_bin.mkdir()
            base = root / "base.apk"
            split = root / "split.apk"
            module = root / "module.apk"
            trace = root / "commands.trace"
            for path in (base, split, module):
                path.write_bytes(path.name.encode())
            apksigner = mock_bin / "apksigner"
            keytool = mock_bin / "keytool"
            apksigner.write_text(MOCK_APKSIGNER)
            keytool.write_text(MOCK_KEYTOOL)
            apksigner.chmod(0o755)
            keytool.chmod(0o755)

            script = '''set -euo pipefail
source "$INSTALLER_ROOT/lib/ui.sh"
source "$INSTALLER_ROOT/lib/lspatch.sh"
INSTALLER="$INSTALLER_ROOT"
WORK="$MOCK_WORK"
MODULE_APK="$MOCK_MODULE"
APKSIGNER="$MOCK_APKSIGNER"
INPUT_APKS=("$MOCK_BASE" "$MOCK_SPLIT")
python3() {
    if [[ "$1" == *patch_provider_queries.py ]]; then
        printf 'patcher:%s\\n' "$*" >> "$TRACE"
        cp "$2" "$3"
    else
        command python3 "$@"
    fi
}
java() {
    printf 'java:%s\\n' "$*" >> "$TRACE"
''' + check + '''
}
''' + setup + '''
mkdir -p "$WORK/patched"
patch_split_set
'''
            env = dict(
                os.environ,
                INSTALLER_ROOT=str(INSTALLER),
                MOCK_WORK=str(work),
                MOCK_MODULE=str(module),
                MOCK_APKSIGNER=str(apksigner),
                MOCK_BASE=str(base),
                MOCK_SPLIT=str(split),
                TRACE=str(trace),
            )
            result = subprocess.run(
                ["bash", "-c", script], env=env, input=stdin,
                text=True, capture_output=True,
            )
            return result, trace.read_text() if trace.exists() else ""

    def test_default_uses_staged_signed_splits_and_no_custom_key(self):
        result, trace = self.invoke("", '''args=("$@")
[[ "${args[1]}" == "$INSTALLER/vendor/lspatch.jar" ]]
[[ "${args[2]}" == "$WORK/lspatch-input/base.apk" ]]
[[ "${args[3]}" == "$WORK/lspatch-input/split.apk" ]]
[[ "${args[4]}" == -l && "${args[5]}" == 2 ]]
[[ "${args[6]}" == -m && "${args[7]}" == "$MODULE_APK" ]]
[[ "${args[*]}" != *' -k '* ]]''')
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn("patcher:", trace)
        self.assertIn("java:", trace)
        self.assertTrue((Path(os.environ.get("TMPDIR", "/tmp")) / "does-not-exist").parent.exists())
        self.assertNotIn("temporary-pass", result.stdout + result.stderr)

    def test_custom_signing_keeps_arguments_and_reads_passwords_silently(self):
        with tempfile.TemporaryDirectory() as directory:
            key = Path(directory) / "signing key.jks"
            key.touch()
            setup = 'SPOTITHEME_KEYSTORE=' + repr(str(key)) + '\nSPOTITHEME_KEY_ALIAS="test alias"'
            result, trace = self.invoke(setup, '''args=("$@")
[[ "${args[8]}" == -k ]]
[[ "${args[9]}" == "$SPOTITHEME_KEYSTORE" ]]
[[ "${args[10]}" == 'dummy store' ]]
[[ "${args[11]}" == 'test alias' ]]
[[ "${args[12]}" == 'dummy key' ]]''', 'dummy store\ndummy key\n')
            self.assertEqual(result.returncode, 0, result.stderr)
            self.assertNotIn("dummy store", result.stdout + result.stderr)
            self.assertNotIn("dummy key", result.stdout + result.stderr)
            self.assertIn("lspatch-input/base.apk", trace)

    def test_missing_keystore_rejected_before_lspatch(self):
        result, trace = self.invoke('SPOTITHEME_KEYSTORE=/missing/key.jks', 'exit 99')
        self.assertEqual(result.returncode, 1)
        self.assertIn("does not exist", result.stderr)
        self.assertNotIn("java:", trace)

    def test_missing_alias_rejected_before_lspatch(self):
        with tempfile.NamedTemporaryFile() as key:
            result, trace = self.invoke('SPOTITHEME_KEYSTORE=' + repr(key.name), 'exit 99')
            self.assertEqual(result.returncode, 1)
            self.assertIn("SPOTITHEME_KEY_ALIAS", result.stderr)
            self.assertNotIn("java:", trace)


if __name__ == "__main__":
    unittest.main()
