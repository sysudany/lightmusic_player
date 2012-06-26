package dany.player;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import dany.player.PlayerService.PlayerBinder;

public class PlayerActivity extends Activity implements OnClickListener {

	public static final String UPDATE_UI = "dany.player.UPDATE_UI";
	PlayerService mService;
	BroadcastReceiver updateReceiver;
	boolean mBound = false;

	private Button bt_control;
	private TextView tv_title;
	private TextView tv_artist;

	Animation alphaAnimation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		bt_control = (Button) this.findViewById(R.id.bt_control);
		bt_control.setOnClickListener(this);
		alphaAnimation = AnimationUtils.loadAnimation(this, R.anim.alpha);

		tv_title = (TextView) this.findViewById(R.id.tv_title);
		tv_artist = (TextView) this.findViewById(R.id.tv_artist);

	}

	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, PlayerService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		updateReceiver = new UpdateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_UI);
		registerReceiver(updateReceiver, filter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		unregisterReceiver(updateReceiver);
	}

	private class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getBooleanExtra(PlayerService.ERROR, false)) {
				Toast.makeText(PlayerActivity.this, "没有列表", 1).show();
				unbindService(mConnection);
			}else{
				tv_artist.setText(mService.getArtist());
				tv_title.setText(mService.getMusicName());
			}
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			PlayerBinder binder = (PlayerBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
	
	private boolean isPlaying ;
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_control:
			if(isPlaying)
			{
				mService.pause();
				v.setBackgroundResource(R.drawable.play_selector);
				v.setAnimation(alphaAnimation);
			}else{
				mService.start();
				v.setBackgroundResource(R.drawable.pause_selector);
				v.setAnimation(alphaAnimation);
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			 Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);

             mHomeIntent.addCategory(Intent.CATEGORY_HOME);
             mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                             | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
             this.startActivity(mHomeIntent);
             return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}