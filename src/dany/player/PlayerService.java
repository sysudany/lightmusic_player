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
	private PlayerController playerController;

	@Override
	public void onCreate() {
		try {
			/*mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDataSource("http://192.168.1.125:8080/1.mp3");
			mediaPlayer.prepare();
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					mediaPlayer.start();
				}
			});*/
			playerController = new PlayerController(this, "http://192.168.1.125:8080/1.mp3", getExternalCacheDir().getAbsolutePath(), "111");
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
		playerController.play();
	}

	public void pause() {
		playerController.pause();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		playerController.release();
		return super.onUnbind(intent);
	}

	public int getCurrentPosition() {
		return playerController.getCurrentPosition();
	}
	
	public int getAccurateDuration(){
		return playerController.getAccurateDuration();
	}
	
	public int getEstimateDuration(){
		return playerController.getEstimateDuration();
	}


}
