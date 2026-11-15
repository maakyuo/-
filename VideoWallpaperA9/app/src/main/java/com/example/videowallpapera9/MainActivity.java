package com.example.videowallpapera9;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.*;
public class MainActivity extends Activity {
 private TextView status;
 private static final int PICK_VIDEO=100;
 @Override public void onCreate(Bundle b){super.onCreate(b);
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(40,65,40,30);root.setGravity(Gravity.CENTER_HORIZONTAL);
  TextView title=new TextView(this);title.setText("Video Wallpaper A9");title.setTextSize(26);root.addView(title);
  TextView hint=new TextView(this);hint.setText("동영상을 선택한 뒤 배경화면을 적용하세요.\n영상은 무음으로 반복 재생됩니다.");hint.setTextSize(16);hint.setPadding(0,30,0,30);root.addView(hint);
  Button pick=new Button(this);pick.setText("동영상 선택");root.addView(pick);
  TextView ratio=new TextView(this);ratio.setText("화면 맞춤 방식");ratio.setPadding(0,22,0,0);root.addView(ratio);
  Spinner mode=new Spinner(this);String[] modes={"화면 채우기 (가장자리 잘림)","전체 영상 표시 (여백 가능)"};mode.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,modes));mode.setSelection(getPreferences(0).getInt("fit",0));root.addView(mode);mode.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> parent){}public void onItemSelected(android.widget.AdapterView<?> parent,View view,int position,long id){getPreferences(0).edit().putInt("fit",position).apply();}});
  Button apply=new Button(this);apply.setText("배경화면 적용");root.addView(apply);
  status=new TextView(this);status.setPadding(0,28,0,0);root.addView(status);
  setContentView(root);
  pick.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("video/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_VIDEO);});
  apply.setOnClickListener(v->{if(!new File(getFilesDir(),"wallpaper.mp4").exists()){Toast.makeText(this,"먼저 동영상을 선택하세요",Toast.LENGTH_SHORT).show();return;}Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new android.content.ComponentName(this,VideoWallpaperService.class));startActivity(i);});
  status.setText(new File(getFilesDir(),"wallpaper.mp4").exists()?"저장된 동영상이 있습니다.":"선택된 동영상이 없습니다.");
 }
 @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(req!=PICK_VIDEO||res!=RESULT_OK||data==null)return;Uri uri=data.getData();status.setText("동영상 복사 중...");new Thread(()->{File target=new File(getFilesDir(),"wallpaper.mp4");File temp=new File(getFilesDir(),"wallpaper.tmp");try(InputStream in=getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(temp)){byte[] buf=new byte[65536];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);java.nio.file.Files.move(temp.toPath(),target.toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);runOnUiThread(()->status.setText("동영상 준비 완료. 배경화면 적용을 누르세요."));}catch(Exception e){temp.delete();runOnUiThread(()->status.setText("오류: "+e.getMessage()));}}).start();}
}