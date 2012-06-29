package dany.player;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import dany.player.PlayerService.PlayerBinder;
import dany.player.view.WmtRatingBar;
import dany.player.view.WmtRatingBar.OnRatingBarChanging;

public class PlayerActivity extends Activity implements OnClickListener {
	//实际情况可以加上某种权限才能调用
	public static final String UPDATE_UI = "dany.player.UPDATE_UI";
	public static final String EXIT = "dany.player.EXIT";
	private PlayerService mService;
	boolean mBound = false;
	private BroadcastReceiver updateReceiver;
	private AudioManager audioManager;
	SharedPreferences sharedPreferences;

	private int maxVolum,currentVolum;

	private Button bt_control;
	//测试用按钮
	private Button bt_next;
	private TextView tv_title;
	private Animation alphaAnimation;
	private WmtRatingBar mVoluemRatingBar;
	private LinearLayout ll_vol;
	private RelativeLayout rv_bg;
	
	
	private Handler handler = new Handler();
	private int[] bgPics= {R.drawable.bg_01,R.drawable.bg_02,R.drawable.bg_03,R.drawable.bg_04,R.drawable.bg_05,
						R.drawable.bg_06,R.drawable.bg_07,R.drawable.bg_08,R.drawable.bg_09,R.drawable.bg_10,};
	private int bgPicIndex = 0;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initViews();
		
		Intent intent = new Intent(this, PlayerService.class);
		getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		sharedPreferences = getSharedPreferences("config", 0);
		Editor editor = sharedPreferences.edit();
		editor.putInt("num_exit", Integer.MAX_VALUE);
		editor.commit();
		
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		maxVolum = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		currentVolum = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		mVoluemRatingBar.setRating(15*currentVolum/maxVolum);
		mVoluemRatingBar.setOnRatingBarChange(new OnRatingBarChanging() {
			@Override
			public void onRatingChanging(float f) {
				handler.removeCallbacks(dismissRunnable);
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (f*maxVolum/15), 0);
				handler.postDelayed(dismissRunnable, 5000);
			}
		});
	}

	private void initViews() {
		bt_control = (Button) this.findViewById(R.id.bt_control);
		//测试用按钮	
		bt_next = (Button) this.findViewById(R.id.bt_next);
		bt_next.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mService.next();
			}
		});
		bt_control.setOnClickListener(this);
		alphaAnimation = AnimationUtils.loadAnimation(this, R.anim.alpha);
		tv_title = (TextView) this.findViewById(R.id.tv_title);
		ll_vol = (LinearLayout) this.findViewById(R.id.ll_vol);
		rv_bg = (RelativeLayout) this.findViewById(R.id.rv_bg);
		mVoluemRatingBar = (WmtRatingBar) findViewById(R.id.volume_ratingBar);
	}

	protected void onStart() {
		super.onStart();
		updateReceiver = new UpdateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_UI);
		filter.addAction(EXIT);
		registerReceiver(updateReceiver, filter);
	}

	@Override
	protected void onStop() {
		unregisterReceiver(updateReceiver);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBound) {
			getApplicationContext().unbindService(mConnection);
			mBound = false;
		}
	}

	
	//收听服务的广播
	private class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(EXIT)){
				System.out.println("exit");
				finish();
				return;
			}
			if (intent.getBooleanExtra(PlayerService.ERROR, false)) {
				Toast.makeText(PlayerActivity.this, "没有列表", 1).show();
				getApplicationContext().unbindService(mConnection);
			} else {
				tv_title.setText(mService.getMusicName());
				bgPicIndex ++;
				rv_bg.setBackgroundResource(bgPics[bgPicIndex%10]);
			}
		}
	}
	
	//服务连接
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			PlayerBinder binder = (PlayerBinder) service;
			mService = binder.getService();
			mBound = true;
			tv_title.setText(mService.getMusicName());
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

	// 创建菜单
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	
	
	// 菜单的点击事件
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.settings:
			showSettingDialog();
			return true;
		case R.id.exit:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	//处理对话框逻辑
	private Dialog dialog;
	private View dialogView;
	private void showSettingDialog() {
		 dialog = new Dialog(this, R.style.MyDialog);	 
		 dialogView = View.inflate(this, R.layout.settings, null);
		 dialog.setContentView(dialogView);
		 dialog.show();
		 initDialogControl();
	}

	private EditText et;
	private void initDialogControl() {
		et = (EditText) dialogView.findViewById(R.id.et_num_exit);
		Button bt_num_exit_3 = (Button) dialogView.findViewById(R.id.bt_num_exit_3);
		Button bt_num_exit_5 = (Button) dialogView.findViewById(R.id.bt_num_exit_5);
		Button bt_num_exit_10 = (Button) dialogView.findViewById(R.id.bt_num_exit_10);
		Button bt_num_exit_20 = (Button) dialogView.findViewById(R.id.bt_num_exit_20);
		Button bt_confirm = (Button) dialogView.findViewById(R.id.bt_dialog_confirm);
		Button bt_quit = (Button) dialogView.findViewById(R.id.bt_dialog_quit);
		OnClickListener listener = new DialogOnlickListener();
		bt_num_exit_3.setOnClickListener(listener);
		bt_num_exit_5.setOnClickListener(listener);
		bt_num_exit_10.setOnClickListener(listener);
		bt_num_exit_20.setOnClickListener(listener);
		bt_confirm.setOnClickListener(listener);
		bt_quit.setOnClickListener(listener);
	}

	private class DialogOnlickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_dialog_confirm:
				Editor editor = sharedPreferences.edit();
				int num_exit  = Integer.parseInt(et.getText().toString());
				editor.putInt("num_exit",num_exit );
				editor.commit();
				et = null;
				Toast.makeText(PlayerActivity.this, "保存成功，将在"+num_exit+"首歌后退出程序", 1).show();
				dialog.dismiss();
				break;
			case R.id.bt_dialog_quit:
				et = null;
				dialog.dismiss();
				break;

			default:
				Button button = (Button) v;
				et.setText(button.getText().toString().trim());
				break;
			}
		}
	}
	
	//播放控制
	private boolean isPlaying;
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_control:
			if (isPlaying) {
				mService.pause();
				v.setBackgroundResource(R.drawable.play_selector);
				v.setAnimation(alphaAnimation);
				isPlaying = false;
			} else {
				mService.start();
				v.setBackgroundResource(R.drawable.pause_selector);
				v.setAnimation(alphaAnimation);
				isPlaying = true;
			}
			break;
		default:
			break;
		}
	}

	// 复写返回键
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);
			mHomeIntent.addCategory(Intent.CATEGORY_HOME);
			mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			this.startActivity(mHomeIntent);
			overridePendingTransition(R.anim.scale_in, R.anim.scale_out);
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
			ll_vol.setVisibility(View.VISIBLE);
			handler.removeCallbacks(dismissRunnable);
			mVoluemRatingBar.setRating(mVoluemRatingBar.getRating()-1);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (maxVolum*mVoluemRatingBar.getRating()/15), 0);
			handler.postDelayed(dismissRunnable, 5000);
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			ll_vol.setVisibility(View.VISIBLE);
			handler.removeCallbacks(dismissRunnable);
			mVoluemRatingBar.setRating(mVoluemRatingBar.getRating()+1);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (maxVolum*mVoluemRatingBar.getRating()/15), 0);
			handler.postDelayed(dismissRunnable, 5000);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// 实现音量控制板的显示和消失
	private long timeWhenYouTouch = 0;
	private DismissRunnable dismissRunnable = new DismissRunnable();
	class DismissRunnable implements Runnable{
		@Override
		public void run() {
			ll_vol.setVisibility(View.INVISIBLE);
			timeWhenYouTouch = 0;
		}
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (ll_vol.getVisibility() == View.INVISIBLE) {
				if (timeWhenYouTouch != 0) {
					long interval = System.currentTimeMillis() - timeWhenYouTouch;
					if (interval < 1000) {
						ll_vol.setVisibility(View.VISIBLE);
						handler.postDelayed(dismissRunnable, 5000);
					}
				}
				timeWhenYouTouch = System.currentTimeMillis();
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

}