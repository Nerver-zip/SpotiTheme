"""Exercise signing argument assembly without Java, ADB or real credentials."""
import pathlib
import subprocess
import tempfile
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]

class SigningTest(unittest.TestCase):
    def invoke(self, setup, check, stdin=''):
        script = '''set -euo pipefail
source "$1/lib/ui.sh"
source "$1/lib/lspatch.sh"
INSTALLER="$1"
WORK=/tmp/mock-output
MODULE_APK=/tmp/mock-module.apk
INPUT_APKS=(/tmp/base.apk /tmp/split.apk)
''' + setup + '\njava() {\n' + check + '\n}\npatch_split_set\n'
        return subprocess.run(['bash', '-c', script, 'test', str(ROOT)], input=stdin,
                              text=True, capture_output=True)

    def test_default_embeds_all_splits_without_custom_key(self):
        result = self.invoke('', '''[[ "$*" == *'/tmp/base.apk /tmp/split.apk -l 2 -m /tmp/mock-module.apk'* ]]
[[ "$*" != *' -k '* ]]''')
        self.assertEqual(result.returncode, 0, result.stderr)

    def test_custom_signing_preserves_spaces_and_reads_passwords(self):
        with tempfile.TemporaryDirectory() as directory:
            key = pathlib.Path(directory) / 'signing key.jks'
            key.touch()
            setup = 'SPOTITHEME_KEYSTORE=' + repr(str(key)) + '\nSPOTITHEME_KEY_ALIAS="test alias"'
            result = self.invoke(setup, '''args=("$@")
[[ "${args[8]}" == -k ]]
[[ "${args[9]}" == "$SPOTITHEME_KEYSTORE" ]]
[[ "${args[10]}" == 'dummy store' ]]
[[ "${args[11]}" == 'test alias' ]]
[[ "${args[12]}" == 'dummy key' ]]''', 'dummy store\ndummy key\n')
            self.assertEqual(result.returncode, 0, result.stderr)
            self.assertNotIn('dummy store', result.stdout + result.stderr)
            self.assertNotIn('dummy key', result.stdout + result.stderr)

    def test_missing_keystore_rejected_before_java(self):
        result = self.invoke('SPOTITHEME_KEYSTORE=/missing/key.jks', 'exit 99')
        self.assertEqual(result.returncode, 1)
        self.assertIn('does not exist', result.stderr)

    def test_missing_alias_rejected(self):
        with tempfile.NamedTemporaryFile() as key:
            result = self.invoke('SPOTITHEME_KEYSTORE=' + repr(key.name), 'exit 99')
            self.assertEqual(result.returncode, 1)
            self.assertIn('SPOTITHEME_KEY_ALIAS', result.stderr)

if __name__ == '__main__':
    unittest.main()
