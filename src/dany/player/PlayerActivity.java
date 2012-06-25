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
	
    private Button bt_start;
    
	private TextView tv_loadProgress;
	private TextView tv_currentPosition;
	
	Animation alphaAnimation;
	

	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        bt_start = (Button) this.findViewById(R.id.start);
        bt_start.setOnClickListener(this);
        alphaAnimation = AnimationUtils.loadAnimation(this,R.anim.alpha); 
        
        tv_loadProgress = (TextView) this.findViewById(R.id.tv_load);
        tv_currentPosition = (TextView) this.findViewById(R.id.tv_time);
        
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
		if(v == bt_start){
			if(v.getId()==R.id.start){
				v.setId(R.id.pause);
				v.setBackgroundResource(R.drawable.pause_selector);
				v.startAnimation(alphaAnimation);
				mService.start();
			}else{
				v.setId(R.id.start);
				v.setBackgroundResource(R.drawable.play_selector);
				v.startAnimation(alphaAnimation);
				mService.pause();
			}
		}
	}
	

}