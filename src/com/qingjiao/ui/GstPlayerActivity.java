package com.qingjiao.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.gstreamer.GStreamer;
import com.qingjiao.view.GStreamerSurfaceView;
import com.testplayer.R;

import android.R.string;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class GstPlayerActivity extends Activity implements OnClickListener, OnSeekBarChangeListener, SurfaceHolder.Callback {
	private native void nativeInit(); // Initialize native code, build pipeline,
										// etc

	private native void nativeFinalize(); // Destroy pipeline and shutdown
											// native code

	private native void nativeSetUri(String uri); // Set the URI of the media to
													// play

	private native void nativePlay(); // Set pipeline to PLAYING

	private native void nativeSetPosition(int milliseconds); // Seek to the
																// indicated
																// position, in
																// milliseconds

	private native void nativePause(); // Set pipeline to PAUSED

	private static native boolean nativeClassInit(); // Initialize native class:
														// cache Method IDs for
														// callbacks

	private native void nativeSurfaceInit(Object surface); // A new surface is
															// available

	private native void nativeSurfaceFinalize(); // Surface about to be
													// destroyed

	private long native_custom_data; // Native code will use this to keep
										// private data

	private boolean is_playing_desired; // Whether the user asked to go to
										// PLAYING
	private int position; // Current position, reported by native code
	private int duration; // Current clip duration, reported by native code
	private boolean is_local_media; // Whether this clip is stored locally or is
									// being streamed
	private int desired_position; // Position where the users wants to seek to
	private String mediaUri; // URI of the clip being played

	public ImageButton play;
	public ImageButton pause;

	static {
		System.loadLibrary("gstreamer_android");
		System.loadLibrary("qingjiao");
		nativeClassInit();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialize GStreamer and warn if it fails
		try {
			GStreamer.init(this);
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		setContentView(R.layout.activity_gst_player);
		initViews();

		GStreamerSurfaceView sv = (GStreamerSurfaceView) this.findViewById(R.id.sv_player);
		SurfaceHolder sh = sv.getHolder();
		sh.addCallback(this);

		SeekBar sb = (SeekBar) this.findViewById(R.id.seekbar);
		sb.setOnSeekBarChangeListener(this);

		// Retrieve our previous state, or initialize it to default values
		if (savedInstanceState != null) {
			is_playing_desired = savedInstanceState.getBoolean("playing");
			position = savedInstanceState.getInt("position");
			duration = savedInstanceState.getInt("duration");
			mediaUri = savedInstanceState.getString("mediaUri");
			Log.i("GStreamer", "Activity created with saved state:");
		} else {
			setUri();
			is_playing_desired = true;
			position = duration = 0;
			// mediaUri = defaultMediaUri;
			Log.i("GStreamer", "Activity created with no saved state:");
		}

		nativeInit();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d("GStreamer", "Saving state, playing:" + is_playing_desired + " position:" + position + " duration: " + duration + " uri: " + mediaUri);
		outState.putBoolean("playing", is_playing_desired);
		outState.putInt("position", position);
		outState.putInt("duration", duration);
		outState.putString("mediaUri", mediaUri);
	}

	@Override
	protected void onDestroy() {
		nativeFinalize();
		super.onDestroy();
	}

	private void initViews() {
		play = (ImageButton) findViewById(R.id.play);
		pause = (ImageButton) findViewById(R.id.pause);
		play.setEnabled(false);
		pause.setEnabled(false);
	}

	private void setUri() {
		mediaUri = getIntent().getStringExtra("uri");
		if (mediaUri.startsWith("file")) {
			is_local_media = true;
		} else {
			is_local_media = false;
		}
	}

	private void changePlayAndPauseButtonBg(boolean isPlay) {
		if (isPlay) {
			play.setVisibility(View.VISIBLE);
			pause.setVisibility(View.GONE);
		} else {
			play.setVisibility(View.GONE);
			pause.setVisibility(View.VISIBLE);
		}
	}

	// Called from native code. Native code calls this once it has created its
	// pipeline and
	// the main loop is running, so it is ready to accept commands.
	private void onGStreamerInitialized() {
		Log.i("GStreamer", "GStreamer initialized:");
		Log.i("GStreamer", "  playing:" + is_playing_desired + " position:" + position + " uri: " + mediaUri);

		// Restore previous playing state
		nativeSetUri(mediaUri);
		nativeSetPosition(position);
		if (is_playing_desired) {
			nativePlay();
		} else {
			nativePause();
		}

		// Re-enable buttons, now that GStreamer is initialized
		runOnUiThread(new Runnable() {
			public void run() {
				play.setEnabled(true);
				pause.setEnabled(true);
			}
		});
	}
	
	// Called from native code
	private void showToast(String message){
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();;
	}

	// Called from native code
	private void setCurrentPosition(final int position, final int duration) {
		final SeekBar sb = (SeekBar) this.findViewById(R.id.seekbar);

		// Ignore position messages from the pipeline if the seek bar is being
		// dragged
		if (sb.isPressed())
			return;

		runOnUiThread(new Runnable() {
			public void run() {
				sb.setMax(duration);
				sb.setProgress(position);
				updateTimeWidget();
			}
		});
		this.position = position;
		this.duration = duration;
	}

	private void updateTimeWidget() {
		TextView tv_cur = (TextView) this.findViewById(R.id.tv_current);
		TextView tv_dur = (TextView) this.findViewById(R.id.tv_duration);
		SeekBar sb = (SeekBar) this.findViewById(R.id.seekbar);
		final int pos = sb.getProgress();

		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		// final String message = df.format(new Date (pos)) + " / " +
		// df.format(new Date (duration));
		tv_cur.setText(df.format(new Date(pos)));
		tv_dur.setText(df.format(new Date(duration)));
	}
	
	// Called from native code when the size of the media changes or is first detected.
    // Inform the video surface about the new size and recalculate the layout.
    private void onMediaSizeChanged (int width, int height) {
        Log.i ("GStreamer", "Media size changed to " + width + "x" + height);
        final GStreamerSurfaceView gsv = (GStreamerSurfaceView) this.findViewById(R.id.sv_player);
        gsv.media_width = width;
        gsv.media_height = height;
        runOnUiThread(new Runnable() {
            public void run() {
                gsv.requestLayout();
            }
        });
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser == false) return;
        desired_position = progress;
        // If this is a local file, allow scrub seeking, this is, seek as soon as the slider is moved.
        if (is_local_media) nativeSetPosition(desired_position);
        updateTimeWidget();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		nativePause();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (!is_local_media)
			nativeSetPosition(desired_position);
		if (is_playing_desired)
			nativePlay();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			break;

		case R.id.play:
			is_playing_desired = true;
			nativePlay();
			break;

		case R.id.pause:
			is_playing_desired = false;
			nativePause();
			break;

		case R.id.forward:
			break;

		default:
			break;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i("GStreamer", "Surface created: " + holder.getSurface());
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.i("GStreamer", "Surface changed to format " + format + " width " + width + " height " + height);
		nativeSurfaceInit(holder.getSurface());
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("GStreamer", "Surface destroyed");
		nativeSurfaceFinalize();
	}

}
