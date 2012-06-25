package dany.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import dany.player.bean.Music;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;


/**
 * The main ideas of the following class is :
 * when you first start the player with a music url
 * there open two thread:
 * one for downloading the file,the other for monitor the downloadprogress
 * when downloaded size is enough,start a mediaplayer
 * when mediaplayer almost ended,then transfer to another mediaplayer to play it 
 * it may cause some suspend,i am trying to minimize it
 */
public class CacheMediaPlayer {
	private Context context;
	private File cacheFile;
	private Music music;
	private MediaPlayer mediaPlayer1;
	private MediaPlayer mediaPlayer2;
	private MediaPlayer currentMediaPlayer;
	private MediaPlayer lastMediaPlayer;

	public CacheMediaPlayer(Context context) {
		this.context = context;
		mediaPlayer1 = new MediaPlayer();
		mediaPlayer2 = new MediaPlayer();
		cacheFile = new File(context.getCacheDir(), "cache.dat");
	}

	public void pause() {
		currentMediaPlayer.pause();
	}

	public void release() {
		mediaPlayer1.release();
		mediaPlayer2.release();
	}

	public void start() {
		currentMediaPlayer.start();
	}

	public boolean isPlaying;
	public void play(Music music) {
		this.music = music;
		new DownLoadThread().start();
		isPlaying = true;
	}

	private int totalReadBytes = 0;
	public boolean isDownloading;

	private class DownLoadThread extends Thread {
		@Override
		public void run() {
			if (music.url != null) {
				try {
					URL url = new URL(music.url);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setConnectTimeout(5000);
					connection.setRequestMethod("GET");
					InputStream inputStream = connection.getInputStream();
					System.out.println(connection.getContentLength()
							+ "---------------------------------");
					if (cacheFile.exists()) {
						cacheFile.delete();
					}
					FileOutputStream fos = new FileOutputStream(cacheFile);
					int length = 0;
					byte[] buffer = new byte[1024];
					isDownloading = true;
					new PlayThread().start();
					while ((length = inputStream.read(buffer)) > 0) {
						fos.write(buffer, 0, length);
						fos.flush();
						totalReadBytes += length;
					}
					inputStream.close();
					fos.close();
					//下载完成的处理
					isDownloading = false;
					lastMediaPlayer = currentMediaPlayer;
					currentMediaPlayer = (currentMediaPlayer == mediaPlayer1 ? mediaPlayer2	: mediaPlayer1);
					try {
						prepareMediaPlayer();
						currentMediaPlayer.seekTo(lastMediaPlayer.getCurrentPosition());
						currentMediaPlayer.start();
						lastMediaPlayer.pause();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	private void prepareMediaPlayer() throws Exception {
		FileInputStream fis = new FileInputStream(cacheFile);
		currentMediaPlayer.reset();
		currentMediaPlayer.setDataSource(fis.getFD());
		currentMediaPlayer.prepare();
	}

	private class PlayThread extends Thread {
		@Override
		public void run() {
			System.out.println("playthread");
			while (isDownloading) {
				if (currentMediaPlayer == null && totalReadBytes > 256 * 1024) {
					System.out
							.println("adfadsfasdf----------------------------------------");
					try {
						currentMediaPlayer = mediaPlayer1;
						prepareMediaPlayer();
						currentMediaPlayer.start();
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
						Log.i("PlayThread while loop", "error");
					}
				} else if (currentMediaPlayer != null
						&& currentMediaPlayer.getDuration()
								- currentMediaPlayer.getCurrentPosition() < 200) {
					lastMediaPlayer = currentMediaPlayer;
					currentMediaPlayer = (currentMediaPlayer == mediaPlayer1 ? mediaPlayer2
							: mediaPlayer1);
					try {
						prepareMediaPlayer();
						currentMediaPlayer.seekTo(lastMediaPlayer
								.getCurrentPosition());
						currentMediaPlayer.start();
						lastMediaPlayer.pause();
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private class PreparedListener implements OnPreparedListener {
		@Override
		public void onPrepared(MediaPlayer mp) {
			// TODO Auto-generated method stub
			mp.start();
		}

	}
}
