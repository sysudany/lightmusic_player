package dany.player;

import java.io.File;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;


public class PlayerService extends Service{

    private final IBinder mBinder = new PlayerBinder();
    private PlayerController playerController;
    
    @Override
    public void onCreate() {
    	if(playerController != null)
		{
    		playerController.interrupt(); 
		}
    	playerController = new PlayerController(this,new File(""),"");
    	playerController.startStreaming("http://192.168.1.105:8080/1.mp3",5208,216);
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
    
    public void play(){
    	playerController.start();
    }

    public void pause(){
    	playerController.pause();
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
    	playerController.release();
    	return super.onUnbind(intent);
    }
    
    public int getCurrentPosition(){
    	return playerController.getCurrentPosition();
    }

    public int getLoadProgress(){
    	return playerController.getLoadProgress();
    }
}
