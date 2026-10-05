from pathlib import Path
import subprocess, os, json, tempfile, shutil
root=Path(tempfile.mkdtemp(prefix='spotitheme-installer-tests-'))
installer=Path(__file__).resolve().parents[1]
script=r'''set -euo pipefail
source "$INSTALLER_ROOT/lib/ui.sh"
source "$INSTALLER_ROOT/lib/vector.sh"
MODULE_APK=/tmp/mock.apk
confirm() { return 0; }
adb_device() { printf 'adb:%s\n' "$*" >> "$TRACE"; }
vector_cli() {
 printf 'vector:%s\n' "$*" >> "$TRACE"
 case "$*" in
 'modules --json ls')
   if [[ "$CASE" == bad_modules ]]; then echo broken; return; fi
   if [[ "$CASE" == absent ]]; then echo '{"success":true,"data":[{"PACKAGE":"com.spotitheme","STATUS":"enabled"}]}';
   else echo '{"success":true,"data":[{"PACKAGE":"com.lenerd46.spotifyplus","STATUS":"enabled"},{"PACKAGE":"com.spotitheme","STATUS":"enabled"}]}'; fi ;;
 'scope --json ls com.lenerd46.spotifyplus')
   if [[ "$CASE" == bad_scope ]]; then echo '{"success":false,"data":[]}';
   else echo '{"success":true,"data":[{"APP_PACKAGE":"com.spotify.music","USER_ID":0}]}'; fi ;;
 'scope --json ls com.spotitheme')
   if [[ "$CASE" == failed_target ]]; then echo '{"success":true,"data":[]}';
   else echo '{"success":true,"data":[{"APP_PACKAGE":"com.spotify.music","USER_ID":0}]}'; fi ;;
 esac
}
install_root
'''
results=[]
for case in ['bad_modules','bad_scope','failed_target','absent','success']:
 trace=root/(case+'.trace');trace.write_text('')
 env=dict(os.environ,CASE=case,TRACE=str(trace),INSTALLER_ROOT=str(installer))
 p=subprocess.run(['bash','-c',script],env=env,text=True,capture_output=True)
 calls=trace.read_text().splitlines()
 removed=any('scope rm' in c for c in calls)
 if case in ['bad_modules','bad_scope']:
  assert p.returncode and not any(c.startswith('adb:') for c in calls) and not removed
 elif case=='failed_target': assert p.returncode and not removed
 elif case=='absent': assert not p.returncode and not removed and not any('ls com.lenerd46.spotifyplus' in c for c in calls)
 else:
  assert not p.returncode and removed
  assert calls.index('vector:scope --json ls com.spotitheme')<calls.index('vector:scope rm com.lenerd46.spotifyplus com.spotify.music/0')
 results.append({'case':case,'passed':True,'exit_code':p.returncode})
shutil.rmtree(root)
print('5 mocked installer cases passed; no device commands executed.')
