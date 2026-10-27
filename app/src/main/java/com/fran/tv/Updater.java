package com.fran.tv;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.content.FileProvider;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.Arrays;
import org.json.JSONObject;

final class Updater {
 static final String URL="https://raw.githubusercontent.com/Fravier3/FranAndroidTv/main/update.json";
 final Activity a; final Ui ui; boolean externalFlow=false,closed=false,busy=false; File pending;
 Updater(Activity a,Ui ui){this.a=a;this.ui=ui;}
 static final class Release {
  final int code;final String version,url,sha,notes;
  Release(String raw)throws Exception{JSONObject j=new JSONObject(raw);code=j.getInt("versionCode");version=j.getString("versionName");url=j.getString("apkUrl");sha=j.getString("sha256").toLowerCase(java.util.Locale.ROOT);notes=j.optString("notes","Mejoras de rendimiento y estabilidad.");if(code<1||!url.startsWith("https://")||!sha.matches("[a-f0-9]{64}"))throw new IOException("Invalid release");}
 }
 void post(Runnable r){a.runOnUiThread(()->{if(!closed&&!a.isFinishing()&&!a.isDestroyed())r.run();});}
 void check(boolean manual){if(busy||closed)return;busy=true;Catalog.IO.execute(()->{try{Release r=new Release(Catalog.get(URL,64000));post(()->{busy=false;if(r.code>BuildConfig.VERSION_CODE)new AlertDialog.Builder(a).setTitle("FranTV "+r.version+" disponible").setMessage(r.notes+"\n\n¿Quieres actualizar ahora?").setPositiveButton("Actualizar",(d,w)->download(r)).setNegativeButton("Más tarde",null).show();else if(manual)notice("Tienes la versión más reciente.");});}catch(Exception e){post(()->{busy=false;if(manual)notice("No se pudo comprobar la actualización. Inténtalo más tarde.");});}});}
 void notice(String s){new AlertDialog.Builder(a).setTitle("FranTV").setMessage(s).setPositiveButton("Aceptar",null).show();}
 void download(Release r){if(busy)return;busy=true;ProgressDialog progress=new ProgressDialog(a);progress.setTitle("Actualizando FranTV");progress.setMessage("Descargando…");progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);progress.setCancelable(false);progress.show();Catalog.IO.execute(()->{HttpURLConnection con=null;File f=null;try{File dir=new File(a.getCacheDir(),"updates");if(!dir.isDirectory()&&!dir.mkdirs())throw new IOException();f=new File(dir,"FranTV.apk");con=(HttpURLConnection)new URL(r.url).openConnection();con.setConnectTimeout(15000);con.setReadTimeout(20000);if(con.getResponseCode()!=200||!con.getURL().getProtocol().equals("https"))throw new IOException();int length=con.getContentLength();MessageDigest digest=MessageDigest.getInstance("SHA-256");try(InputStream in=con.getInputStream();FileOutputStream out=new FileOutputStream(f)){byte[] b=new byte[16384];int n,total=0,last=-1;while((n=in.read(b))!=-1){if(closed||(total+=n)>100_000_000)throw new IOException();out.write(b,0,n);digest.update(b,0,n);int percent=length>0?(int)(100L*total/length):0;if(percent!=last){last=percent;post(()->progress.setProgress(percent));}}}if(!hex(digest.digest()).equals(r.sha))throw new IOException();verify(f,r.code);File ready=f;post(()->{busy=false;progress.dismiss();pending=ready;install();});}catch(Exception e){if(f!=null)f.delete();post(()->{busy=false;progress.dismiss();notice("No se pudo descargar o verificar la actualización. Inténtalo más tarde.");});}finally{if(con!=null)con.disconnect();}});}
 @SuppressWarnings("deprecation") void verify(File f,int code)throws Exception{PackageManager pm=a.getPackageManager();PackageInfo next=pm.getPackageArchiveInfo(f.getAbsolutePath(),PackageManager.GET_SIGNATURES),own=pm.getPackageInfo(a.getPackageName(),PackageManager.GET_SIGNATURES);if(next==null||!a.getPackageName().equals(next.packageName)||next.versionCode!=code||next.versionCode<=BuildConfig.VERSION_CODE||next.signatures==null||own.signatures==null||!Arrays.equals(next.signatures,own.signatures))throw new IOException("Invalid package");}
 void install(){if(pending==null||closed)return;try{if(Build.VERSION.SDK_INT>=26&&!a.getPackageManager().canRequestPackageInstalls()){new AlertDialog.Builder(a).setTitle("Permitir actualización").setMessage("Activa el permiso para instalar actualizaciones de FranTV y vuelve a la app.").setPositiveButton("Continuar",(d,w)->{externalFlow=true;a.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+a.getPackageName())));}).setNegativeButton("Más tarde",(d,w)->pending=null).show();return;}Intent i=new Intent(Intent.ACTION_VIEW).setDataAndType(FileProvider.getUriForFile(a,a.getPackageName()+".files",pending),"application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);externalFlow=true;pending=null;a.startActivity(i);}catch(Exception e){externalFlow=false;notice("No se pudo abrir el instalador.");}}
 void resume(){if(externalFlow){externalFlow=false;if(pending!=null){if(Build.VERSION.SDK_INT<26||a.getPackageManager().canRequestPackageInstalls())install();else pending=null;}}}
 static String hex(byte[] b){StringBuilder s=new StringBuilder();for(byte v:b)s.append(String.format(java.util.Locale.ROOT,"%02x",v&255));return s.toString();}
 void close(){closed=true;}
}
