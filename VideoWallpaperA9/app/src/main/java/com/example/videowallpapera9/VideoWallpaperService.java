package com.example.videowallpapera9;
import android.media.MediaPlayer;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.view.Surface;
import android.util.Log;
import java.io.File;
public class VideoWallpaperService extends WallpaperService {
 static final String TAG="VideoWallpaperA9";
 @Override public Engine onCreateEngine(){return new VideoEngine();}
 class VideoEngine extends Engine {
  MediaPlayer player;
  boolean visible, surfaceReady, prepared;
  long modified=-1;
  void status(String msg){Log.i(TAG,msg);getSharedPreferences("wallpaper_status",MODE_PRIVATE).edit().putString("status",msg).apply();}
  @Override public void onSurfaceCreated(SurfaceHolder h){super.onSurfaceCreated(h);surfaceReady=true;sync();}
  @Override public void onSurfaceChanged(SurfaceHolder h,int format,int w,int height){super.onSurfaceChanged(h,format,w,height);surfaceReady=true;sync();}
  @Override public void onVisibilityChanged(boolean v){visible=v;sync();}
  @Override public void onSurfaceDestroyed(SurfaceHolder h){surfaceReady=false;release();super.onSurfaceDestroyed(h);}
  void sync(){
   if(!surfaceReady)return;
   File f=new File(getFilesDir(),"wallpaper.mp4");
   if(!f.isFile()||f.length()==0){status("영상 파일이 없거나 비어 있습니다.");release();return;}
   if(player!=null&&modified!=f.lastModified())release();
   if(player==null){startVideo(f);return;}
   if(!prepared)return;
   try{if(visible){if(!player.isPlaying())player.start();}else if(player.isPlaying())player.pause();}
   catch(Exception e){status("재생 전환 실패: "+e.getMessage());release();}
  }
  void startVideo(File f){
   try{
    MediaPlayer p=new MediaPlayer();player=p;prepared=false;modified=f.lastModified();
    p.setDataSource(f.getAbsolutePath());
    // WallpaperService surfaces reject the keep-screen-on flag used by setDisplay().
    // A plain Surface avoids SurfaceHolder.setKeepScreenOn().
    p.setSurface(getSurfaceHolder().getSurface());
    p.setLooping(true);p.setVolume(0,0);
    p.setOnPreparedListener(mp->{
     if(player!=mp)return;
     try{
      int fit=getSharedPreferences("MainActivity",MODE_PRIVATE).getInt("fit",0);
      mp.setVideoScalingMode(fit==0?MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING:MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
      prepared=true;status("영상 준비 완료: "+mp.getVideoWidth()+"x"+mp.getVideoHeight());
      sync();
     }catch(Exception e){status("영상 준비 실패: "+e.getMessage());release();}
    });
    p.setOnErrorListener((mp,what,extra)->{status("재생 오류: "+what+" / "+extra);if(player==mp)release();return true;});
    p.prepareAsync();
    status("영상 불러오는 중...");
   }catch(Exception e){status("파일 열기 실패: "+e.getMessage());release();}
  }
  void release(){MediaPlayer p=player;player=null;prepared=false;modified=-1;if(p!=null){try{p.release();}catch(Exception ignored){}}}
  @Override public void onDestroy(){release();super.onDestroy();}
 }
}