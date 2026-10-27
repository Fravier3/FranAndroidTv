package com.fran.tv;

import org.json.*;
import java.util.*;
import java.text.Normalizer;

public final class Channel {
 public final String id,name,url,logo,category,country; public final int number;
 Channel(String id,String name,String url,String logo,String category,String country,int number){this.id=id;this.name=name;this.url=url;this.logo=logo;this.category=category;this.country=country;this.number=number;}
 public String numberText(){return String.format(Locale.US,"%03d",number);}
 public static String normalize(String s){return Normalizer.normalize(s,Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT);}
 public boolean matches(String q){return normalize(name).contains(normalize(q))||numberText().contains(q)||Integer.toString(number).equals(q);}
 public static List<Channel> parse(String text) throws JSONException {
  JSONArray a=new JSONArray(text); List<Channel> result=new ArrayList<>(); Set<String> ids=new HashSet<>();
  for(int i=0;i<a.length();i++){
   JSONObject o=a.optJSONObject(i);if(o==null)continue;
   String url=o.optString("url","").trim(),name=o.optString("name","").trim();
   if(name.isEmpty()||!(url.startsWith("https://")||url.startsWith("http://")))continue;
   String id=o.optString("id",url).trim(); if(id.isEmpty())id=url;if(!ids.add(id))continue;
   String cat=o.optString("category","Otros").trim();
   result.add(new Channel(id,name,url,o.optString("logo",""),cat.isEmpty()?"Otros":cat,o.optString("country",""),result.size()+1));
  }
  if(result.isEmpty())throw new JSONException("Empty catalog");return result;
 }
}
