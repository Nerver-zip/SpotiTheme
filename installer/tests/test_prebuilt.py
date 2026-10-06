"""Check the prebuilt bundle boundary without contacting a device."""
import hashlib
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]

class PrebuiltTest(unittest.TestCase):
    def run_bundle(self, corrupt=False, missing=False):
        with tempfile.TemporaryDirectory(prefix='spotitheme-prebuilt-') as directory:
            root = Path(directory)
            shutil.copy2(ROOT / 'scripts/install-prebuilt.sh', root / 'install.sh')
            (root / 'installer').mkdir()
            (root / 'installer/install.sh').write_text(
                '#!/usr/bin/env bash\nset -eu\n'
                '[[ "$SPOTITHEME_MODULE_APK" == "$PWD/SpotiTheme.apk" ]]\n'
                '[[ "$1" == --rootless && "$2" == --check ]]\n'
                'echo delegated\n')
            data = b'fixture-apk'
            if not missing:
                (root / 'SpotiTheme.apk').write_bytes(data + (b'bad' if corrupt else b''))
            (root / 'SHA256SUMS').write_text(hashlib.sha256(data).hexdigest() + '  SpotiTheme.apk\n')
            return subprocess.run(['bash', str(root / 'install.sh'), '--rootless', '--check'],
                                  cwd='/tmp', capture_output=True, text=True)

    def test_verified_apk_and_arguments_reach_existing_installer(self):
        result = self.run_bundle()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn('delegated', result.stdout)

    def test_corrupt_apk_never_reaches_installer(self):
        result = self.run_bundle(corrupt=True)
        self.assertNotEqual(result.returncode, 0)
        self.assertNotIn('delegated', result.stdout)

    def test_missing_apk_never_reaches_installer(self):
        result = self.run_bundle(missing=True)
        self.assertNotEqual(result.returncode, 0)
        self.assertNotIn('delegated', result.stdout)
