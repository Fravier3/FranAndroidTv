import hashlib,json,os,re,shutil,subprocess
from pathlib import Path
root=Path(__file__).resolve().parents[1]
apk=root/'app/build/outputs/apk/release/app-release.apk'
verify=subprocess.check_output([str(Path(os.environ['ANDROID_HOME'])/'build-tools/35.0.0/apksigner'),'verify','--print-certs',str(apk)],text=True)
if 'Android Debug' in verify:raise SystemExit('Release signing required')
cert=re.search(r'Signer #1 certificate SHA-256 digest: (\w+)',verify).group(1)
pin=root/'signing-certificate.sha256'
if pin.exists() and pin.read_text().strip()!=cert:raise SystemExit('Signing certificate changed')
pin.write_text(cert+'\n')
gradle=(root/'app/build.gradle').read_text();code=int(re.search(r'versionCode (\d+)',gradle).group(1));version=re.search(r"versionName '([^']+)'",gradle).group(1)
(root/'releases').mkdir(exist_ok=True);name=f'FranTV-{version}.apk'
shutil.copy2(apk,root/'FranTV.apk');shutil.copy2(apk,root/'releases'/name)
sha=hashlib.sha256(apk.read_bytes()).hexdigest()
(root/'update.json').write_text(json.dumps(dict(versionCode=code,versionName=version,apkUrl=f'https://raw.githubusercontent.com/Fravier3/FranAndroidTv/main/releases/{name}',sha256=sha,notes='Nueva experiencia FranTV, canales actualizados, favoritos y mejoras de reproducción.'),indent=2,ensure_ascii=False)+'\n')
print(f'{name}: {sha}')
