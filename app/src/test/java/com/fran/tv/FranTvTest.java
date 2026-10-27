package com.fran.tv;
import org.junit.*;import static org.junit.Assert.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;import android.content.Context;import android.graphics.*;import android.view.*;import java.io.*;import java.util.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=28)
public class FranTvTest {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();Catalog.prefs(c).edit().clear().commit();}
 @Test public void bundledCatalog()throws Exception{List<Channel> a=Channel.parse(Catalog.read(c.getAssets().open("channels.json"),2000000));assertEquals(80,a.size());assertEquals("001",a.get(0).numberText());}
 @Test(expected=org.json.JSONException.class) public void rejectsEmpty()throws Exception{Channel.parse("[]");}
 @Test(expected=org.json.JSONException.class) public void rejectsBadSchemes()throws Exception{Channel.parse("[{\"name\":\"Bad\",\"url\":\"file:///test\"}]");}
 @Test public void deduplicates()throws Exception{assertEquals(1,Channel.parse("[{\"id\":\"a\",\"name\":\"One\",\"url\":\"https://a\"},{\"id\":\"a\",\"name\":\"Two\",\"url\":\"https://b\"}]").size());}
 @Test public void accentsAndNumbers(){Channel a=new Channel("a","Televisión","https://a","","PR","PR",7);assertTrue(a.matches("television"));assertTrue(a.matches("007"));assertFalse(a.matches("999"));}
 @Test public void favoritesPersistAcrossStreamChange(){Channel a=new Channel("a","A","https://a","","PR","PR",1),b=new Channel("a","A","https://b","","PR","PR",1);assertTrue(Catalog.toggle(c,a));assertTrue(Catalog.favorite(c,b));assertFalse(Catalog.toggle(c,b));}
 @Test public void filters(){List<Channel> a=Catalog.load(c);assertEquals(7,Catalog.filter(c,a,"Puerto Rico","").size());assertEquals(0,Catalog.filter(c,a,"Favoritos","").size());}
 @Test(expected=Exception.class) public void rejectsInsecureUpdate()throws Exception{new Updater.Release("{\"versionCode\":2,\"versionName\":\"2\",\"apkUrl\":\"http://a\",\"sha256\":\""+"a".repeat(64)+"\"}");}
 @Test public void updateManifest()throws Exception{Updater.Release r=new Updater.Release("{\"versionCode\":2,\"versionName\":\"2\",\"apkUrl\":\"https://a\",\"sha256\":\""+"a".repeat(64)+"\"}");assertEquals(2,r.code);}
 @Test @GraphicsMode(GraphicsMode.Mode.NATIVE) @Config(qualifiers="w960dp-h540dp-land-xhdpi") public void rendersHomeAndWelcome()throws Exception{
  MainActivity a=Robolectric.buildActivity(MainActivity.class).create().get();draw(a,"welcome");a.root.removeViewAt(a.root.getChildCount()-1);a.home.setVisibility(View.VISIBLE);a.intro=false;a.category="Puerto Rico";a.renderNav();a.renderGrid(false);Thread.sleep(800);org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();draw(a,"home");assertEquals(7,a.visible.size());a.onDestroy();
 }
 void draw(MainActivity a,String name)throws Exception{a.root.measure(View.MeasureSpec.makeMeasureSpec(1920,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1080,View.MeasureSpec.EXACTLY));a.root.layout(0,0,1920,1080);Bitmap b=Bitmap.createBitmap(1920,1080,Bitmap.Config.ARGB_8888);a.root.draw(new Canvas(b));File d=new File("build/screenshots");d.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(d,name+".png"))){b.compress(Bitmap.CompressFormat.PNG,100,out);}}
}
