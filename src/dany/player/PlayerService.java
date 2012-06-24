package dany.player;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;

public class PlayerService extends Service {

	private final IBinder mBinder = new PlayerBinder();
	private MediaPlayer mediaPlayer;

	@Override
	public void onCreate() {
		try {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDataSource("http://115.170.153.147:8080/1.mp3");
			mediaPlayer.prepare();
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					mediaPlayer.start();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onCreate();
	}

	public class PlayerBinder extends Binder {
		PlayerService getService() {
			return PlayerService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void play() {
		mediaPlayer.start();
	}

	public void pause() {
		mediaPlayer.pause();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		mediaPlayer.release();
		return super.onUnbind(intent);
	}

	public int getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}


}
