package dany.player;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import dany.player.bean.Music;

public class PlayerService extends Service {

	private final IBinder mBinder = new PlayerBinder();
	private CacheMediaPlayer player;
	private List<Music> musicList;

	@Override
	public void onCreate() {
		player = new CacheMediaPlayer(this);
		// player.play(music);
	}

	// 绑定时返回自身对象
	public class PlayerBinder extends Binder {
		PlayerService getService() {
			return PlayerService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void pause() {
		player.pause();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		player.release();
		return super.onUnbind(intent);
	}

	public void start() {
		if (!player.isPlaying) {
			Music music = new Music();
			music.musicName = "天边的眷恋";
			music.url = "http://192.168.1.105:8080/1.mp3";
			player.play(music);
		}else{
			player.start();
		}

	}

	public void play(Music music) {
		player.play(music);
	}

	public void next() {

	}
}
