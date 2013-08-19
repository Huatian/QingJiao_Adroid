package com.qingjiao.ui;

import com.qingjiao.common.MediaUtils;
import com.qingjiao.service.Controller;
import com.qingjiao.service.Controller.ControllerBinder;
import com.testplayer.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlayerActivity extends Activity implements OnClickListener,OnSeekBarChangeListener,OnTouchListener{
	public LinearLayout controllerBar;
	public ImageButton forward;
	public ImageButton play;
	public ImageButton pause;
	public ImageButton back;
	public SeekBar seekBar;
	public TextView time_current;
	public TextView time_duration;
	public TextView time_track;
	public SurfaceView sv;
	
	Controller controller;
	MyHandler handler;
	
	public final String PLAYER_CURRENT = "com.testplayer.currentTime";
	public final String PLAYER_DURATION = "com.testplayer.duration";
	public final String PLAYER_NEXT = "com.testplayer.next";
	public final String PLAYER_UPDATE = "com.testplayer.update";
	private final String PLAYER_COMPLETION = "com.testplayer.completion";
	
	public int currentPosition=0;
	public int duration=0;
	public final int HIDENBAR=2;
	public final int DOWN=3;
	public final int UP=4;
	public final int BEGIN_PLAYER=5;
	
	public boolean isBind=false;
	public boolean isSurfaceViewCreated=false;
	public boolean isTrack=false;
	public boolean isMediaPrepare = false;
	
	private ServiceConnection mConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onServiceConnected(ComponentName className, IBinder service) {
				// TODO Auto-generated method stub
				System.out.println("服务连接上了！");
				ControllerBinder cb = (ControllerBinder) service;
				controller = cb.getService();
				controller.setDisplay(sv.getHolder());
				
				isBind=true;
				
				
			}
		};
	//String path = Environment.getExternalStorageDirectory().getPath()+"/vedio/North.America.Part.I.720X400.mp4";
	String path=null;
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Intent it=new Intent(this,Controller.class);
		getSource();
		it.putExtra("uri", path);
		bindService(it, mConnection, Context.BIND_AUTO_CREATE);
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		controllerBar=(LinearLayout) findViewById(R.id.controlbar);
		forward=(ImageButton) findViewById(R.id.forward);
		play=(ImageButton) findViewById(R.id.play);
		pause=(ImageButton) findViewById(R.id.pause);
		back=(ImageButton) findViewById(R.id.back);
		seekBar=(SeekBar) findViewById(R.id.seekbar);
		time_current=(TextView) findViewById(R.id.time_current);
		time_duration=(TextView) findViewById(R.id.time_duration);
		time_track=(TextView) findViewById(R.id.time_track);
		sv=(SurfaceView) findViewById(R.id.medioView);
		handler=new MyHandler();
		
		
		forward.setOnClickListener(this);
		forward.setOnTouchListener(this);
		play.setOnClickListener(this);
		pause.setOnClickListener(this);
		back.setOnClickListener(this);
		back.setOnTouchListener(this);
		seekBar.setOnSeekBarChangeListener(this);
		sv.getHolder().addCallback(new Callback() {
			
			@Override
			public void surfaceDestroyed(SurfaceHolder arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void surfaceCreated(SurfaceHolder arg0) {
				System.out.println("surfaceview 创建成功 ！");
				isSurfaceViewCreated=true;
				
			}
			
			@Override
			public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
		});
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(PLAYER_CURRENT);
		filter.addAction(PLAYER_DURATION);
		filter.addAction(PLAYER_COMPLETION);
		registerReceiver(playerreceiver, filter);
		
		System.out.println("判断服务是否连接。。。");
		
	}
	
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
	}




	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		unregisterReceiver(playerreceiver);//停止界面时，反注册广播接收器
	}

	
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unbindService(mConnection);
	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		controllerBar.setVisibility(View.VISIBLE);
		Message msg=new Message();
		msg.what=HIDENBAR;
		handler.sendMessageDelayed(msg, 5000);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(controllerBar.getVisibility()==View.GONE){
				controllerBar.setVisibility(View.VISIBLE);
			}
			break;
			
		case MotionEvent.ACTION_UP:
			hidinBar();

		default:
			break;
		}
		return super.onTouchEvent(event);
	}




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public class MyHandler extends Handler{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case R.id.back:
				if(msg.arg1==DOWN){
					controller.back();
				}
				if(msg.arg1==UP){
					controller.reset();
				}
				break;
				
			case R.id.play:
				play.setVisibility(8);
				pause.setVisibility(0);
				controller.play();
				break;
			
			case R.id.pause:
				play.setVisibility(0);
				pause.setVisibility(8);
				controller.pause();
				break;
				
			case R.id.forward:
				if(msg.arg1==DOWN){
					controller.forward();
				}
				if(msg.arg1==UP){
					controller.reset();
				}
				break;
			
			case HIDENBAR:
				controllerBar.setVisibility(8);
				break;
				
			case R.id.seekbar:
				controller.setCurrentPosition(msg.arg1);
				controller.reset();
				break;
				
			case BEGIN_PLAYER:
				if(isBind && isSurfaceViewCreated && isMediaPrepare){
					play.setVisibility(8);
					pause.setVisibility(0);
					controller.play();
				}
				break;
			
			default:
				break;
			}
		}
		
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Message msg=new Message();
		switch (v.getId()) {
		case R.id.back:
			msg.what=R.id.back;
			handler.sendMessage(msg);
			break;
			
		case R.id.play:
			msg.what=R.id.play;
			handler.sendMessage(msg);
			break;
		
		case R.id.pause:
			msg.what=R.id.pause;
			//CURRENT_POSITION=mp.getCurrentPosition();
			handler.sendMessage(msg);
			break;
			
		case R.id.forward:
			msg.what=R.id.forward;
			handler.sendMessage(msg);
			break;
			
		default:
			break;
		}
	}


	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		System.out.println("onProgressChanged");
		time_track.setText(MediaUtils.turnNumToTime(progress).toString());
		time_current.setText(MediaUtils.turnNumToTime(progress).toString());
	}


	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		System.out.println("onStartTrackingTouch");
		isTrack=true;
		time_track.setVisibility(View.VISIBLE);
		time_track.setText(MediaUtils.turnNumToTime(seekBar.getProgress()));
	}


	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		System.out.println("onStopTrackingTouch");
		isTrack=false;
		time_track.setVisibility(View.GONE);
		Message msg=new Message();
		msg.what=R.id.seekbar;
		msg.arg1=seekBar.getProgress();
		currentPosition=seekBar.getProgress();
		handler.sendMessage(msg);
	}
	


	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		Message msg=new Message();
		msg.what=arg0.getId();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			handler.removeMessages(HIDENBAR);
			msg.arg1=DOWN;
			handler.sendMessage(msg);
			break;

		case MotionEvent.ACTION_UP:
			msg.arg1=UP;
			handler.sendMessage(msg);
			hidinBar();
			break;
		default:
			break;
		}
		return true;
	}
	
	private void hidinBar(){
		Message msg=new Message();
		msg.what=HIDENBAR;
		handler.sendMessageDelayed(msg, 5000);
	}
	
	// 然后在后台MusicService里使用handler消息机制，不停的向前台发送广播，广播里面的数据是当前mp播放的时间点，前台接收到广播后获得播放时间点来更新进度条
	private BroadcastReceiver playerreceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(PLAYER_CURRENT)) {
				currentPosition = intent.getExtras().getInt("currentPosition");// 获得当前播放位置
				seekBar.setProgress(currentPosition);// 初始化播放进度位置
				
				System.out.println(currentPosition);

			} else if (action.equals(PLAYER_DURATION)) {
				duration = intent.getExtras().getInt("duration");// 获取总时间
				seekBar.setMax(duration);// 进度条设置最大值（传总时间）
				time_duration.setText(MediaUtils.turnNumToTime(duration).toString());
				isMediaPrepare = true;
				Message msg = new Message();
				msg.what = BEGIN_PLAYER;
				handler.sendMessage(msg);
				Log.i("duration", ""+duration);
			}else if(action.equals(PLAYER_COMPLETION)){
				Intent it = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(it);
			}
		}
	};	
	
	private void getSource(){
		path=getIntent().getStringExtra("path");
		if(path==null){
			// Get the intent that started this activity
		    Intent intent = getIntent();
		    Uri data = intent.getData();

		    // Figure out what to do based on the intent type
		    if (intent.getType().indexOf("video/") != -1) {
		        path=data.toString();
		    } 
		}
	}
}
