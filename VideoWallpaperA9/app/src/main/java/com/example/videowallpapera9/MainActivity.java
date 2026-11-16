package com.example.videowallpapera9;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.widget.*;
import java.io.*;
public class MainActivity extends Activity {
 TextView status,diagnostic; Button apply;
 static final int PICK_MEDIA=100;
 @Override public void onCreate(Bundle b){super.onCreate(b);
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(40,55,40,30);root.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
  TextView title=new TextView(this);title.setText("Video Wallpaper A9");title.setTextSize(26);root.addView(title);
  TextView hint=new TextView(this);hint.setText("동영상 · 일반 이미지 · GIF 배경화면\n동영상 무음 반복 / GIF 반복 재생");hint.setTextSize(16);hint.setPadding(0,24,0,24);root.addView(hint);
  Button pick=new Button(this);pick.setText("동영상 / 이미지 / GIF 선택");root.addView(pick);
  Spinner mode=new Spinner(this);String[] modes={"화면 채우기","전체 화면에 맞추기"};mode.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,modes));mode.setSelection(getPreferences(0).getInt("fit",0));root.addView(mode);
  mode.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,android.view.View v,int pos,long id){getPreferences(0).edit().putInt("fit",pos).apply();}});
  apply=new Button(this);apply.setText("배경화면 적용");root.addView(apply);
  status=new TextView(this);status.setPadding(0,28,0,0);root.addView(status);
  diagnostic=new TextView(this);diagnostic.setPadding(0,20,0,0);root.addView(diagnostic);
  Button refresh=new Button(this);refresh.setText("재생 상태 확인");root.addView(refresh);
  setContentView(root);
  pick.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"video/*","image/*"});i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_MEDIA);});
  apply.setOnClickListener(v->{if(!new File(getFilesDir(),"wallpaper_media").isFile()){Toast.makeText(this,"먼저 파일을 선택하세요",Toast.LENGTH_SHORT).show();return;}Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new android.content.ComponentName(this,VideoWallpaperService.class));try{startActivity(i);}catch(Exception e){status.setText("적용 실패: "+e.getMessage());}});
  refresh.setOnClickListener(v->refresh());refresh();
 }
 void refresh(){File f=new File(getFilesDir(),"wallpaper_media");String type=getSharedPreferences("wallpaper_media",MODE_PRIVATE).getString("type","없음");status.setText(f.isFile()?"선택된 파일: "+type+" / "+f.length()+" 바이트":"선택된 파일 없음");diagnostic.setText("재생 상태: "+getSharedPreferences("wallpaper_status",MODE_PRIVATE).getString("status","기록 없음"));}
 @Override protected void onResume(){super.onResume();if(status!=null)refresh();}
 @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(req!=PICK_MEDIA||res!=RESULT_OK||data==null)return;
  Uri uri=data.getData();String mime=getContentResolver().getType(uri);String name="";
  try(Cursor cur=getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)){if(cur!=null&&cur.moveToFirst())name=cur.getString(0);}
  String lower=name==null?"":name.toLowerCase(java.util.Locale.ROOT);
  final String type;
  if("image/gif".equals(mime)||lower.endsWith(".gif"))type="gif";
  else if(mime!=null&&mime.startsWith("video/"))type="video";
  else if(mime!=null&&mime.startsWith("image/"))type="image";
  else {status.setText("지원하지 않는 파일입니다.");return;}
  status.setText("파일 복사 중...");apply.setEnabled(false);
  new Thread(()->{
   File target=new File(getFilesDir(),"wallpaper_media"),temp=new File(getFilesDir(),"wallpaper_media.tmp");
   try(InputStream in=getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(temp)){
    if(in==null)throw new IOException("파일을 열 수 없습니다");
    byte[] buf=new byte[65536];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);
    if(temp.length()==0)throw new IOException("빈 파일입니다");
    if(type.equals("image")&&android.graphics.BitmapFactory.decodeFile(temp.getAbsolutePath())==null)throw new IOException("지원되지 않는 이미지");
    if(type.equals("gif")&&android.graphics.Movie.decodeFile(temp.getAbsolutePath())==null)throw new IOException("지원되지 않는 GIF");
    java.nio.file.Files.move(temp.toPath(),target.toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    getSharedPreferences("wallpaper_media",MODE_PRIVATE).edit().putString("type",type).apply();
    runOnUiThread(()->{apply.setEnabled(true);refresh();Toast.makeText(this,"파일 선택 완료",Toast.LENGTH_SHORT).show();});
   }catch(Exception e){temp.delete();runOnUiThread(()->{apply.setEnabled(true);status.setText("파일 오류: "+e.getMessage());});}
  }).start();
 }
}