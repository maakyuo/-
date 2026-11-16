package com.example.videowallpapera9;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import java.io.*;
public class MainActivity extends Activity {
 TextView status,diagnostic;
 Button apply;
 static final int PICK_VIDEO=100;
 @Override public void onCreate(Bundle b){super.onCreate(b);
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(40,65,40,30);root.setGravity(Gravity.CENTER_HORIZONTAL);
  TextView title=new TextView(this);title.setText("Video Wallpaper A9");title.setTextSize(26);root.addView(title);
  TextView hint=new TextView(this);hint.setText("동영상 선택 후 배경화면을 적용하세요.\nMP4(H.264) 영상 권장 · 무음 반복 재생");hint.setTextSize(16);hint.setPadding(0,30,0,30);root.addView(hint);
  Button pick=new Button(this);pick.setText("동영상 선택");root.addView(pick);
  Spinner mode=new Spinner(this);String[] modes={"화면 채우기","전체 영상 표시"};mode.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,modes));mode.setSelection(getPreferences(0).getInt("fit",0));root.addView(mode);
  mode.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,android.view.View v,int pos,long id){getPreferences(0).edit().putInt("fit",pos).apply();}});
  apply=new Button(this);apply.setText("배경화면 적용");root.addView(apply);
  status=new TextView(this);status.setPadding(0,28,0,0);root.addView(status);
  diagnostic=new TextView(this);diagnostic.setPadding(0,20,0,0);root.addView(diagnostic);
  Button refresh=new Button(this);refresh.setText("재생 상태 확인");root.addView(refresh);
  setContentView(root);
  pick.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("video/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_VIDEO);});
  apply.setOnClickListener(v->{File f=new File(getFilesDir(),"wallpaper.mp4");if(!f.isFile()||f.length()==0){Toast.makeText(this,"먼저 동영상을 선택하세요",Toast.LENGTH_SHORT).show();return;}Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new android.content.ComponentName(this,VideoWallpaperService.class));try{startActivity(i);}catch(Exception e){status.setText("적용 화면 열기 실패: "+e.getMessage());}});
  refresh.setOnClickListener(v->refresh());refresh();
 }
 void refresh(){File f=new File(getFilesDir(),"wallpaper.mp4");status.setText(f.isFile()?"저장된 동영상: "+f.length()+" 바이트":"선택된 동영상이 없습니다.");diagnostic.setText("재생 상태: "+getSharedPreferences("wallpaper_status",MODE_PRIVATE).getString("status","아직 재생 기록 없음"));}
 @Override protected void onResume(){super.onResume();if(status!=null)refresh();}
 @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(req!=PICK_VIDEO||res!=RESULT_OK||data==null)return;Uri uri=data.getData();status.setText("동영상 복사 중...");apply.setEnabled(false);new Thread(()->{
  File target=new File(getFilesDir(),"wallpaper.mp4"),temp=new File(getFilesDir(),"wallpaper.tmp");
  try(InputStream in=getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(temp)){
   byte[] buf=new byte[65536];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);
   if(temp.length()==0)throw new IOException("빈 파일입니다");
   java.nio.file.Files.move(temp.toPath(),target.toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
   runOnUiThread(()->{apply.setEnabled(true);refresh();Toast.makeText(this,"영상 선택 완료",Toast.LENGTH_SHORT).show();});
  }catch(Exception e){temp.delete();runOnUiThread(()->{apply.setEnabled(true);status.setText("복사 오류: "+e.getMessage());});}
 }).start();}
}