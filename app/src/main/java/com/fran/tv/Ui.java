package com.fran.tv;

import android.app.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import android.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public final class Ui {
 public static final int BG=Color.rgb(4,12,25),CARD=Color.rgb(13,27,43),CYAN=Color.rgb(38,223,241),WHITE=Color.rgb(242,247,252),MUTED=Color.rgb(159,180,200);
 public final Activity a;public final float scale;
 private static final LruCache<String,Bitmap> cache=new LruCache<String,Bitmap>(12*1024*1024){protected int sizeOf(String k,Bitmap b){return b.getByteCount();}};
 private static final ExecutorService images=Executors.newFixedThreadPool(3);
 public Ui(Activity a){this.a=a;DisplayMetrics m=a.getResources().getDisplayMetrics();scale=Math.min(m.widthPixels/960f,m.heightPixels/540f);}
 public int d(float x){return Math.round(x*scale);}
 public TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(a);t.setText(s);t.setTextColor(color);t.setTextSize(TypedValue.COMPLEX_UNIT_PX,d(size));t.setFontFeatureSettings("kern");t.setTypeface(Typeface.create(bold?"sans-serif-medium":"sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));t.setIncludeFontPadding(false);return t;}
 public GradientDrawable bg(int color,int radius,int stroke){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(d(radius));if(stroke!=0)g.setStroke(d(1.5f),stroke);return g;}
 public LinearLayout column(){LinearLayout l=new LinearLayout(a);l.setOrientation(LinearLayout.VERTICAL);l.setClipChildren(false);l.setClipToPadding(false);return l;}
 public LinearLayout row(){LinearLayout l=new LinearLayout(a);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);l.setClipChildren(false);l.setClipToPadding(false);return l;}
 public TextView button(String s,Runnable action){TextView t=text(s,14,WHITE,false);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(d(12),0,d(12),0);focus(t,false);t.setOnClickListener(v->action.run());return t;}
 public void focus(View v,boolean selected){v.setFocusable(true);v.setBackground(bg(selected?0xff123e50:CARD,10,selected?0xff20778a:0xff203449));v.setOnFocusChangeListener((w,has)->{w.setBackground(bg(has?0xff123c50:(selected?0xff123e50:CARD),10,has?CYAN:(selected?0xff20778a:0xff203449)));w.animate().scaleX(has?1.025f:1).scaleY(has?1.025f:1).setDuration(130).start();w.setElevation(has?d(7):0);});}
 public ImageView brand(int size){ImageView i=new ImageView(a);i.setImageResource(R.drawable.brand);i.setScaleType(ImageView.ScaleType.FIT_CENTER);i.setLayoutParams(new LinearLayout.LayoutParams(d(size),d(size)));return i;}
 public void immersive(){a.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);}
 public void logo(ImageView view,Channel c){
  view.setScaleType(ImageView.ScaleType.FIT_CENTER);view.setTag(c.id);Bitmap cached=cache.get(c.id);if(cached!=null){applyLogo(view,cached);return;}
  view.setImageDrawable(new NameLogo(c.name));
  images.execute(()->{Bitmap b=null;
   try(InputStream in=a.getAssets().open("logos/"+c.id+".png")){b=BitmapFactory.decodeStream(in);}catch(Exception ignored){}
   if(b==null&&(c.logo.startsWith("https://")||c.logo.startsWith("http://"))){HttpURLConnection con=null;try{con=(HttpURLConnection)new URL(c.logo).openConnection();con.setConnectTimeout(5000);con.setReadTimeout(5000);try(InputStream in=con.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1){if(out.size()+n>2_000_000)throw new IOException();out.write(buf,0,n);}byte[] bytes=out.toByteArray();BitmapFactory.Options opts=new BitmapFactory.Options();opts.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(bytes,0,bytes.length,opts);opts.inSampleSize=Math.max(1,Math.max(opts.outWidth,opts.outHeight)/400);opts.inJustDecodeBounds=false;b=BitmapFactory.decodeByteArray(bytes,0,bytes.length,opts);}}catch(Exception ignored){}finally{if(con!=null)con.disconnect();}}
   if(b!=null){cache.put(c.id,b);Bitmap image=b;a.runOnUiThread(()->{if(!a.isDestroyed()&&c.id.equals(view.getTag()))applyLogo(view,image);});}
  });
 }
 void applyLogo(ImageView v,Bitmap b){v.setImageBitmap(b);long lum=0;int n=0;for(int y=0;y<b.getHeight();y+=5)for(int x=0;x<b.getWidth();x+=5){int c=b.getPixel(x,y);if(Color.alpha(c)>128){lum+=(Color.red(c)+Color.green(c)+Color.blue(c))/3;n++;}}v.setBackground(bg(n>0&&lum/n<115?0xffe3edf4:Color.TRANSPARENT,8,0));}
 static class NameLogo extends Drawable{final String name;final Paint p=new Paint(3);NameLogo(String n){name=n;}public void draw(Canvas c){p.setColor(WHITE);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(Math.min(32,getBounds().width()/Math.max(1,name.length())*1.5f));c.drawText(name,getBounds().centerX(),getBounds().centerY(),p);}public void setAlpha(int a){p.setAlpha(a);}public void setColorFilter(ColorFilter f){p.setColorFilter(f);}public int getOpacity(){return android.graphics.PixelFormat.TRANSLUCENT;}}
 public LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:d(w),h<0?h:d(h));}
 public void gap(LinearLayout l,int height){l.addView(new View(a),lp(1,height));}
}
