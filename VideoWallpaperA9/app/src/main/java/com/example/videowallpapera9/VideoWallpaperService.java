package com.example.videowallpapera9;
import android.media.MediaPlayer;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import java.io.File;
public class VideoWallpaperService extends WallpaperService {
 @Override public Engine onCreateEngine(){return new VideoEngine();}
 class VideoEngine extends Engine {
  private MediaPlayer player;
  private boolean visible=false, surfaceReady=false, prepared=false;
  private long loadedModified=-1;
  @Override public void onSurfaceCreated(SurfaceHolder holder){super.onSurfaceCreated(holder);surfaceReady=true;syncPlayback();}
  @Override public void onSurfaceChanged(SurfaceHolder holder,int format,int width,int height){super.onSurfaceChanged(holder,format,width,height);syncPlayback();}
  @Override public void onSurfaceDestroyed(SurfaceHolder holder){surfaceReady=false;release();super.onSurfaceDestroyed(holder);}
  @Override public void onVisibilityChanged(boolean v){visible=v;syncPlayback();}
  private void syncPlayback(){
   if(!surfaceReady){return;}
   File file=new File(getFilesDir(),"wallpaper.mp4");
   if(!file.exists()){release();return;}
   if(player!=null && loadedModified!=file.lastModified())release();
   if(player==null){if(visible)startVideo(file);return;}
   if(!prepared)return;
   try{if(visible){if(!player.isPlaying())player.start();}else if(player.isPlaying())player.pause();}
   catch(IllegalStateException e){release();}
  }
  private void startVideo(File file){
   try{
    MediaPlayer p=new MediaPlayer();player=p;prepared=false;loadedModified=file.lastModified();
    p.setDataSource(file.getAbsolutePath());
    p.setDisplay(getSurfaceHolder());
    p.setLooping(true);p.setVolume(0f,0f);
    p.setVideoScalingMode(getSharedPreferences("MainActivity",MODE_PRIVATE).getInt("fit",0)==0?MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING:MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
    p.setOnPreparedListener(mp->{if(player!=mp)return;prepared=true;syncPlayback();});
    p.setOnErrorListener((mp,what,extra)->{if(player==mp)release();return true;});
    p.prepareAsync();
   }catch(Exception e){release();}
  }
  private void release(){MediaPlayer p=player;player=null;prepared=false;loadedModified=-1;if(p!=null){try{p.release();}catch(Exception ignored){}}}
  @Override public void onDestroy(){release();super.onDestroy();}
 }
}