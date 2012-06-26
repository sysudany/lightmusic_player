package dany.player;

import java.io.File;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import dany.player.bean.Music;
import dany.player.util.FileUtil;

public class PlayerService extends Service {

	public static final String ERROR = "getList";
	
	private final IBinder mBinder = new PlayerBinder();
	
	private MediaPlayer mPlayer;
	private List<Music> musicList;
	private int musicIndex;

	@Override
	public void onCreate() {
		mPlayer = new MediaPlayer();
		File directory  = new File(Environment.getExternalStorageDirectory(),"music");
		System.out.println(directory.getAbsolutePath());
		try {
			musicList = FileUtil.getMusicsFromDir(directory);
			mPlayer.setDataSource(musicList.get(musicIndex).localPath);
			mPlayer.prepare();
			mPlayer.setOnCompletionListener(new CompletionListener());
		} catch (Exception e) {
			e.printStackTrace();
			Intent intent = new Intent();
			intent.setAction(PlayerActivity.UPDATE_UI);
			intent.putExtra(ERROR, true );
			sendBroadcast(intent);
			stopSelf(1);
		}
	}

	private class CompletionListener implements OnCompletionListener{
		@Override
		public void onCompletion(MediaPlayer mp) {

			next();
		}
		
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

	@Override
	public boolean onUnbind(Intent intent) {
		mPlayer.release();
		return super.onUnbind(intent);
	}

	
	public void pause() {
		mPlayer.pause();
	}

	public void start() {
		mPlayer.start();
	}
	
	//得到总时长
	public String getDuration(){
		int duration = mPlayer.getDuration();
		int minute = duration/1000/60;
	    int second = duration/1000%60;
	    return appendZero(minute).concat(appendZero(second));
	}
	
	//得到当前播放位置
	public String getCurrentPosition(){
		int currentPosition = mPlayer.getCurrentPosition();
		int minute = currentPosition/1000/60;
	    int second = currentPosition/1000%60;
	    return appendZero(minute).concat(appendZero(second));
	}
	
	public String getMusicName(){
		return musicList.get(musicIndex).musicName;
	}
	
	public String getArtist(){
		return musicList.get(musicIndex).artist;
	}
	
	//自动补零函数
	private String appendZero(int num){
		if(num<10)
			return String.valueOf(num).concat("0");
		else
			return String.valueOf(num);
	}
	
	public void next() {
		Intent intent = new Intent();
		intent.setAction(PlayerActivity.UPDATE_UI);
		sendBroadcast(intent);
		if(musicIndex == musicList.size()-1){
			musicIndex = -1;
		}
		musicIndex++;
		try {
			mPlayer.reset();
			mPlayer.setDataSource(musicList.get(musicIndex).localPath);
			mPlayer.prepare();
			mPlayer.start();
		} catch (Exception e) {
			intent.setAction(PlayerActivity.UPDATE_UI);
			intent.putExtra(ERROR, true );
			sendBroadcast(intent);
			stopSelf(1);
			e.printStackTrace();
		}
	}
	
}
