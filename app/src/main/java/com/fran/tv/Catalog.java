package com.fran.tv;

import android.content.*;
import android.os.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public final class Catalog {
 public static final String URL="https://raw.githubusercontent.com/Fravier3/IPTv-Fran/refs/heads/main/channels.json";
 public static final ExecutorService IO=Executors.newFixedThreadPool(4);
 private static volatile List<Channel> current=Collections.emptyList();
 public interface Callback {void done(List<Channel> list, boolean fresh);}
 public static List<Channel> load(Context c){
  if(!current.isEmpty())return current;
  try {current=Channel.parse(read(new FileInputStream(new File(c.getFilesDir(),"channels.json")),2_000_000));}catch(Exception ignored){}
  if(current.isEmpty())try{current=Channel.parse(read(c.getAssets().open("channels.json"),2_000_000));}catch(Exception ignored){}
  return current;
 }
 public static void refresh(Context c, Callback cb){Context app=c.getApplicationContext();IO.execute(()->{
  boolean fresh=false;
  try {
   String raw=get(URL,2_000_000); List<Channel> list=Channel.parse(raw);
   android.util.AtomicFile file=new android.util.AtomicFile(new File(app.getFilesDir(),"channels.json"));
   FileOutputStream out=null;try{out=file.startWrite();out.write(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));file.finishWrite(out);}catch(Exception e){file.failWrite(out);throw e;}
   current=Collections.unmodifiableList(list);fresh=true;
  }catch(Exception ignored){}
  final boolean ok=fresh;List<Channel> snapshot=load(app);new Handler(Looper.getMainLooper()).post(()->cb.done(snapshot,ok));
 });}
 public static String get(String url,int max) throws IOException {
  HttpURLConnection con=(HttpURLConnection)new URL(url).openConnection();con.setConnectTimeout(10000);con.setReadTimeout(10000);con.setUseCaches(false);con.setRequestProperty("User-Agent","FranTV/1.0");
  try {int code=con.getResponseCode();if(code!=200)throw new IOException("HTTP "+code);return read(con.getInputStream(),max);}finally{con.disconnect();}
 }
 public static String read(InputStream in,int max)throws IOException{try(InputStream src=in;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=src.read(b))!=-1){if(out.size()+n>max)throw new IOException("Too large");out.write(b,0,n);}return out.toString("UTF-8");}}
 public static SharedPreferences prefs(Context c){return c.getSharedPreferences("frantv",Context.MODE_PRIVATE);}
 public static boolean favorite(Context c,Channel ch){return prefs(c).getStringSet("favorites",Collections.emptySet()).contains(ch.id);}
 public static boolean toggle(Context c,Channel ch){Set<String> s=new HashSet<>(prefs(c).getStringSet("favorites",Collections.emptySet()));boolean added=s.add(ch.id);if(!added)s.remove(ch.id);prefs(c).edit().putStringSet("favorites",s).apply();return added;}
 public static List<Channel> filter(Context c,List<Channel> all,String category,String query){List<Channel> r=new ArrayList<>();for(Channel ch:all){boolean group=category.equals("Todos")||category.equals("Buscar")||(category.equals("Favoritos")?favorite(c,ch):category.equals(ch.category));if(group&&(query.isEmpty()||ch.matches(query)))r.add(ch);}return r;}
}
