package dany.player;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import dany.player.PlayerService.PlayerBinder;

public class PlayerActivity extends Activity implements OnClickListener {

    PlayerService mService;
    boolean mBound = false;
	
    private Button bt_pause;
    
	private TextView tv_loadProgress;
	private TextView tv_currentPosition;
	
	Animation alphaAnimation;
	
	private Handler handler = new Handler();

	private Runnable runnable = new Runnable() {
		public void run() {
			tv_loadProgress.setText(mService.getLoadProgress()+"");
			tv_currentPosition.setText(mService.getCurrentPosition()+"");
			handler.postDelayed(this, 1000);
		}

	};
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        bt_pause = (Button) this.findViewById(R.id.pause);
        bt_pause.setOnClickListener(this);
        alphaAnimation = AnimationUtils.loadAnimation(this,R.anim.alpha); 
        
        tv_loadProgress = (TextView) this.findViewById(R.id.tv_load);
        tv_currentPosition = (TextView) this.findViewById(R.id.tv_time);
        
        handler.postDelayed(runnable, 1000);
    }
    
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, PlayerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        handler.removeCallbacks(runnable);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            PlayerBinder binder = (PlayerBinder) service;
            mService = binder.getService();
            mBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

	@Override
	public void onClick(View v) {
		if(v == bt_pause){
			if(v.getId()==R.id.pause){
				v.setId(R.id.play);
				v.setBackgroundResource(R.drawable.play_selector);
				v.startAnimation(alphaAnimation);
				mService.pause();
			}else{
				v.setId(R.id.pause);
				v.setBackgroundResource(R.drawable.pause_selector);
				v.startAnimation(alphaAnimation);
				mService.play();
			}
		}
	}
	

}