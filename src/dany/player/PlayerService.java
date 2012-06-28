package dany.player;

import java.io.File;
import java.util.List;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import dany.player.bean.Music;
import dany.player.util.FileUtil;

public class PlayerService extends Service {

	public static final String ERROR = "getList";

	private final IBinder mBinder = new PlayerBinder();

	private MediaPlayer mPlayer;
	private List<Music> musicList;
	private int musicIndex;

	private PhoneComingReceiver receiver;

	private NotificationManager notificationManager;
	private Notification notification;
	
	private class PhoneComingReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
				pause();
			} else {
				// 如果是来电
				TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
				switch (tm.getCallState()) {
				case TelephonyManager.CALL_STATE_RINGING:
					pause();
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					pause();
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					start();
					break;
				}
			}
		}
	}

	@Override
	public void onCreate() {
		initReceiver();
		initMediaPlayer();
		initNotification();
	}

	private void initReceiver() {
		receiver = new PhoneComingReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.intent.action.PHONE_STATE");
		intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
		registerReceiver(receiver, intentFilter);
	}

	private void initMediaPlayer() {
		mPlayer = new MediaPlayer();
		File directory = new File(Environment.getExternalStorageDirectory(), "music");
		try {
			musicList = FileUtil.getMusicsFromDir(directory);
			mPlayer.reset();
			mPlayer.setDataSource(musicList.get(musicIndex).localPath);
			mPlayer.prepare();
			mPlayer.setOnCompletionListener(new CompletionListener());
		} catch (Exception e) {
			e.printStackTrace();
			Intent intent = new Intent();
			intent.setAction(PlayerActivity.UPDATE_UI);
			intent.putExtra(ERROR, true);
			sendBroadcast(intent);
			stopSelf(1);
		}
	}
	
	private void initNotification(){
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification = new Notification(R.drawable.icon,getMusicName(),System.currentTimeMillis());
		notification.flags = Notification.FLAG_INSISTENT;
		Intent intent = new Intent(this,PlayerActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		notification.setLatestEventInfo(getApplicationContext(), "轻音乐播放器", getMusicName(), contentIntent);
		notificationManager.notify(1, notification);
	}
	
	private class CompletionListener implements OnCompletionListener {
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
		notificationManager.cancel(1);
		return super.onUnbind(intent);
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	public void pause() {
		mPlayer.pause();
	}

	public void start() {
		mPlayer.start();
	}

	// 得到总时长
	public String getDuration() {
		int duration = mPlayer.getDuration();
		int minute = duration / 1000 / 60;
		int second = duration / 1000 % 60;
		return appendZero(minute).concat(appendZero(second));
	}

	// 得到当前播放位置
	public String getCurrentPosition() {
		int currentPosition = mPlayer.getCurrentPosition();
		int minute = currentPosition / 1000 / 60;
		int second = currentPosition / 1000 % 60;
		return appendZero(minute).concat(appendZero(second));
	}

	public String getMusicName() {
		return musicList.get(musicIndex).musicName;
	}

	public String getArtist() {
		return musicList.get(musicIndex).artist;
	}

	// 自动补零函数
	private String appendZero(int num) {
		if (num < 10)
			return String.valueOf(num).concat("0");
		else
			return String.valueOf(num);
	}

	public void next() {
		Intent intent = new Intent();
		intent.setAction(PlayerActivity.UPDATE_UI);
		sendBroadcast(intent);
		if (musicIndex == musicList.size() - 1) {
			musicIndex = -1;
		}
		musicIndex++;
		Random random = new Random();
		Music temp = null;
		//随机循环播放的逻辑实现
		int next = musicIndex+random.nextInt(musicList.size()-musicIndex);
		temp = musicList.get(next);
		musicList.set(next, musicList.get(musicIndex));
		musicList.set(musicIndex, temp);
		
		try {
			mPlayer.reset();
			mPlayer.setDataSource(musicList.get(musicIndex).localPath);
			mPlayer.prepare();
			mPlayer.start();
		} catch (Exception e) {
			intent.setAction(PlayerActivity.UPDATE_UI);
			intent.putExtra(ERROR, true);
			sendBroadcast(intent);
			stopSelf(1);
			e.printStackTrace();
		}
	}

}
