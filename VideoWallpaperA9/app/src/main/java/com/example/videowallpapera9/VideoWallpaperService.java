package com.example.videowallpapera9;
import android.media.MediaPlayer;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import java.io.File;
public class VideoWallpaperService extends WallpaperService {
 @Override public Engine onCreateEngine(){return new VideoEngine();}
 class VideoEngine extends Engine {
  private MediaPlayer player;
  private boolean visible=false;
  private boolean surfaceReady=false;
  @Override public void onSurfaceCreated(SurfaceHolder holder){super.onSurfaceCreated(holder);surfaceReady=true;startVideo();}
  @Override public void onSurfaceDestroyed(SurfaceHolder holder){surfaceReady=false;release();super.onSurfaceDestroyed(holder);}
  @Override public void onVisibilityChanged(boolean v){visible=v;if(v){if(player==null)startVideo();else {try{player.start();}catch(IllegalStateException ignored){}}}else if(player!=null&&player.isPlaying())player.pause();}
  private void startVideo(){if(!surfaceReady||!visible||player!=null)return;File file=new File(getFilesDir(),"wallpaper.mp4");if(!file.exists())return;try{MediaPlayer p=new MediaPlayer();player=p;p.setDataSource(file.getAbsolutePath());p.setDisplay(getSurfaceHolder());p.setLooping(true);p.setVolume(0,0);p.setVideoScalingMode(getSharedPreferences("MainActivity",MODE_PRIVATE).getInt("fit",0)==0?MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING:MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);p.setOnPreparedListener(mp->{if(visible)mp.start();});p.setOnErrorListener((mp,what,extra)->{release();return true;});p.prepareAsync();}catch(Exception e){release();}}
  private void release(){if(player!=null){MediaPlayer p=player;player=null;try{p.reset();p.release();}catch(Exception ignored){}}}
  @Override public void onDestroy(){release();super.onDestroy();}
 }
}