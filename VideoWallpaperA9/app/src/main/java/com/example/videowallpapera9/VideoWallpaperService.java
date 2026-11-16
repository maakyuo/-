package com.example.videowallpapera9;
import android.graphics.*;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;
import java.io.File;
public class VideoWallpaperService extends WallpaperService {
 static final String TAG="VideoWallpaperA9";
 @Override public Engine onCreateEngine(){return new VideoEngine();}
 class VideoEngine extends Engine {
  final Handler handler=new Handler(Looper.getMainLooper());
  final Runnable frame=new Runnable(){@Override public void run(){drawFrame();}};
  MediaPlayer player; Bitmap bitmap; Movie movie;
  boolean visible,surfaceReady,prepared;
  long modified=-1,animationStart=0;
  String activeType="";
  void status(String msg){Log.i(TAG,msg);getSharedPreferences("wallpaper_status",MODE_PRIVATE).edit().putString("status",msg).apply();}
  @Override public void onSurfaceCreated(SurfaceHolder h){super.onSurfaceCreated(h);surfaceReady=true;sync();}
  @Override public void onSurfaceChanged(SurfaceHolder h,int format,int w,int height){super.onSurfaceChanged(h,format,w,height);surfaceReady=true;sync();}
  @Override public void onVisibilityChanged(boolean v){visible=v;sync();}
  @Override public void onSurfaceDestroyed(SurfaceHolder h){surfaceReady=false;release();super.onSurfaceDestroyed(h);}
  void sync(){
   handler.removeCallbacks(frame);
   if(!surfaceReady)return;
   File f=new File(getFilesDir(),"wallpaper_media");
   if(!f.isFile()||f.length()==0){status("선택된 파일이 없습니다");release();return;}
   String type=getSharedPreferences("wallpaper_media",MODE_PRIVATE).getString("type","video");
   if(modified!=f.lastModified()||!type.equals(activeType)){release();activeType=type;modified=f.lastModified();load(f,type);}
   if("video".equals(type)){
    if(player!=null&&prepared){try{if(visible&&!player.isPlaying())player.start();else if(!visible&&player.isPlaying())player.pause();}catch(Exception e){status("재생 실패: "+e.getMessage());release();}}
   }else if(visible)drawFrame();
  }
  void load(File f,String type){
   try{
    if("video".equals(type)){startVideo(f);return;}
    if("gif".equals(type)){movie=Movie.decodeFile(f.getAbsolutePath());if(movie==null)throw new Exception("GIF 디코딩 실패");animationStart=SystemClock.uptimeMillis();status("GIF 준비 완료");}
    else {bitmap=BitmapFactory.decodeFile(f.getAbsolutePath());if(bitmap==null)throw new Exception("이미지 디코딩 실패");status("이미지 준비 완료");}
   }catch(Exception e){status("파일 열기 실패: "+e.getMessage());release();}
  }
  void drawFrame(){
   handler.removeCallbacks(frame);
   if(!visible||!surfaceReady||(bitmap==null&&movie==null))return;
   Canvas c=null;
   try{
    c=getSurfaceHolder().lockCanvas();if(c==null)return;
    c.drawColor(Color.BLACK);
    int iw=bitmap!=null?bitmap.getWidth():movie.width(),ih=bitmap!=null?bitmap.getHeight():movie.height();
    if(iw<=0||ih<=0)return;
    int fit=getSharedPreferences("MainActivity",MODE_PRIVATE).getInt("fit",0);
    float sx=(float)c.getWidth()/iw,sy=(float)c.getHeight()/ih;
    float scale=fit==0?Math.max(sx,sy):Math.min(sx,sy);
    float left=(c.getWidth()-iw*scale)/2f,top=(c.getHeight()-ih*scale)/2f;
    if(bitmap!=null){Paint p=new Paint(Paint.FILTER_BITMAP_FLAG);c.drawBitmap(bitmap,null,new RectF(left,top,left+iw*scale,top+ih*scale),p);}
    else{
     int duration=movie.duration();if(duration<=0)duration=1000;
     movie.setTime((int)((SystemClock.uptimeMillis()-animationStart)%duration));
     c.save();c.translate(left,top);c.scale(scale,scale);movie.draw(c,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));c.restore();
    }
   }catch(Exception e){status("화면 그리기 실패: "+e.getMessage());}
   finally{if(c!=null)try{getSurfaceHolder().unlockCanvasAndPost(c);}catch(Exception ignored){}}
   if(movie!=null&&visible&&surfaceReady)handler.postDelayed(frame,33);
  }
  void startVideo(File f){
   try{
    MediaPlayer p=new MediaPlayer();player=p;prepared=false;
    p.setDataSource(f.getAbsolutePath());
    p.setSurface(getSurfaceHolder().getSurface());
    p.setLooping(true);p.setVolume(0,0);
    p.setOnPreparedListener(mp->{if(player!=mp)return;try{
     int fit=getSharedPreferences("MainActivity",MODE_PRIVATE).getInt("fit",0);
     mp.setVideoScalingMode(fit==0?MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING:MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
     prepared=true;status("동영상 준비 완료: "+mp.getVideoWidth()+"x"+mp.getVideoHeight());sync();
    }catch(Exception e){status("동영상 준비 실패: "+e.getMessage());release();}});
    p.setOnErrorListener((mp,what,extra)->{status("재생 오류: "+what+" / "+extra);if(player==mp)release();return true;});
    p.prepareAsync();status("동영상 불러오는 중...");
   }catch(Exception e){status("동영상 열기 실패: "+e.getMessage());release();}
  }
  void release(){
   handler.removeCallbacks(frame);
   MediaPlayer p=player;player=null;prepared=false;if(p!=null)try{p.release();}catch(Exception ignored){}
   if(bitmap!=null){bitmap.recycle();bitmap=null;}movie=null;modified=-1;activeType="";
  }
  @Override public void onDestroy(){release();super.onDestroy();}
 }
}