package com.qingjiao.service;

import java.io.IOException;

import com.qingjiao.common.LogUtils;
import com.testplayer.R;
import com.testplayer.R.string;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;
import android.widget.SeekBar;
import android.widget.Toast;

public class Controller extends Service implements OnPreparedListener,OnCompletionListener{
	private final IBinder mBinder=new ControllerBinder();
	private String url;
	private String uri;
	private MediaPlayer mp;
	private Handler handler;
	
	
	private int block=0;
	public boolean canPlayer=false;
	public boolean isPlaying=false;
	public boolean updateTime=false;
	
	private static final String PLAYER_CURRENT = "com.testplayer.currentTime";
	private static final String PLAYER_DURATION = "com.testplayer.duration";
	private static final String PLAYER_NEXT = "com.testplayer.next";
	private static final String PLAYER_UPDATE = "com.testplayer.update";
	private final String PLAYER_COMPLETION = "com.testplayer.completion";
	
	private int currentPosition=0;
	private int duration;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mp=new MediaPlayer();
	}
	

	@Override
	public IBinder onBind(Intent it) {
		// TODO Auto-generated method stub
		uri=it.getStringExtra("uri");
		init();
		return mBinder;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		updateTime=false;
		if(handler!=null){
			handler.removeMessages(1);
		}
		if(mp!=null){
			mp.release();
		}
		
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	private void init(){
		
		String path = url!=null?url:uri;
		try {
			mp.setOnPreparedListener(this);
			mp.setOnCompletionListener(this);
			mp.setDataSource(path);
			mp.prepareAsync();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void setDisplay(SurfaceHolder holder){
		mp.setDisplay(holder);
		mp.setScreenOnWhilePlaying(true);
	}
	
	public void play(){
		if(canPlayer){
			isPlaying=true;
			mp.seekTo(currentPosition);
			mp.start();
			if(handler==null){
				handler=new Handler(){

					@Override
					public void handleMessage(Message msg) {
						// TODO Auto-generated method stub
						super.handleMessage(msg);
						if (msg.what == 1) {
							Intent intent=new Intent();
							intent.setAction(PLAYER_CURRENT);
							if(mp.isPlaying()){
								currentPosition = mp.getCurrentPosition();
							}
							
							intent.putExtra("currentPosition", currentPosition);
							System.out.println("发送广播");
							sendBroadcast(intent);

						}
						if(updateTime){
							System.out.println("发送空消息");
							handler.sendEmptyMessageDelayed(1, 1000);// 发送空消息持续时间
						}
					}
					
				};
			}
			updateTime=true;
			Message msg=new Message();
			msg.what=1;
			handler.sendMessage(msg);
			System.out.println("发送第一个消息");
		}else{
			Toast.makeText(getApplicationContext(), R.string.waiting,Toast.LENGTH_SHORT);
		}
	}
	
	public void pause(){
		updateTime=false;
		isPlaying=false;
		currentPosition=mp.getCurrentPosition();
		mp.pause();
	}
	
	public void forward(){
		updateTime=false;
		if(isPlaying){
			mp.pause();
		}
		currentPosition=mp.getCurrentPosition()+block;
	}
	
	public void back(){
		updateTime=false;
		if(isPlaying){
			mp.pause();
		}
		currentPosition=mp.getCurrentPosition()-block;
	};
	
	public void reset(){
		if(isPlaying){
			updateTime=true;
			mp.seekTo(currentPosition);
			mp.start();
			Message msg=new Message();
			msg.what=1;
			handler.sendMessage(msg);
		}else{
			mp.seekTo(currentPosition);
		}
	}
	
	public class ControllerBinder extends Binder{
		public Controller getService(){
			return Controller.this;
		}
	}

	public int getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}


	@Override
	public void onPrepared(MediaPlayer mp) {
		canPlayer=true;
		duration=mp.getDuration();
		block=duration/100;
		Intent intent = new Intent();
		intent.setAction(PLAYER_DURATION);
		intent.putExtra("duration", duration);
		sendBroadcast(intent);// 将Intent对象信息用广播发送出去
	}


	@Override
	public void onCompletion(MediaPlayer mp) {
		LogUtils.e("onCompletion!!!!!");
		updateTime=false;
		handler.removeMessages(1);
		if(mp!=null){
			mp.release();
		}
		Intent intent = new Intent();
		intent.setAction(PLAYER_COMPLETION);
		sendBroadcast(intent);
	}
}
