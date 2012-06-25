package dany.player;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import dany.player.util.FileUtil;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.widget.Toast;

/**
 * 
 * @author dany
 * 
 */
public class PlayerController {

	/**
	 * assume 96kbps*10secs/8bits per byte
	 */
	private static final int INTIAL_BYTES = 1024*256;

	// ���췽���Ĳ���
	private Context context;
	private String musicUrl;
	private String savePath;
	private String musicName;

	// ��������mediaPlayer��file���ڻ��岥�ŵ��л�
	private MediaPlayer mediaPlayer1;
	private MediaPlayer mediaPlayer2;
	private MediaPlayer currentMediaPlayer;
	private File cachefile1;
	private File cachefile2;

	// ˢ�½�����Ҫ����Ϣ
	private int currentPosition = 0;
	private int downloadProgress = 0;
	private int estimateDuration = 0;
	private int accurateDuration = 0;

	public int getCurrentPosition() {
		return currentMediaPlayer == null ? 0 : currentMediaPlayer
				.getCurrentPosition();
	}

	public int getDownloadProgress() {
		return downloadProgress;
	}

	public int getEstimateDuration() {
		return estimateDuration;
	}

	public int getAccurateDuration() {
		return accurateDuration;
	}

	private boolean isDownloading = false;

	/**
	 * 
	 * @param context
	 * @param musicUrl
	 *            ���ص��ļ�·�� ��http://192.168.1.105:8080/1.mp3
	 * @param savePath
	 *            �����ļ���sdcard·��
	 */
	public PlayerController(Context context, String musicUrl, String savePath,
			String musicName) {
		this.context = context;
		this.musicUrl = musicUrl;
		this.savePath = savePath;
		this.musicName = musicName;
		mediaPlayer1 = new MediaPlayer();
		mediaPlayer2 = new MediaPlayer();
		mediaPlayer1.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer2.setAudioStreamType(AudioManager.STREAM_MUSIC);
		cachefile1 = new File(context.getCacheDir(),"1.dat");
		cachefile2 = new File(context.getCacheDir(),"2.dat");
	}

	public void play() {
		if (!isDownloading) {
			new DownLoadThread().start();
			isDownloading = true;
		} else {
			try {
				FileInputStream inputStream1 = new FileInputStream(cachefile1);
				startPlaying(mediaPlayer1, inputStream1.getFD());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class DownLoadThread extends Thread {
		@Override
		public void run() {
			try {
				URL url = new URL(musicUrl);
				URLConnection connection = url.openConnection();

				int contentLength = connection.getContentLength();
				int downloadContentLength = 0;

				InputStream inputStream = connection.getInputStream();
				FileOutputStream out1 = new FileOutputStream(cachefile1);
				FileOutputStream out2 = new FileOutputStream(cachefile2);
				int length = 0;
				byte[] buffer = new byte[16384];
				while ((length = inputStream.read(buffer)) > 0) {
					out1.write(buffer, 0, length);
					out2.write(buffer, 0, length);
					downloadContentLength += length;
					// �õ����ؽ���
					downloadProgress = downloadContentLength * 100
							/ contentLength;
					if (currentMediaPlayer == null
							&& downloadContentLength > INTIAL_BYTES) {
						currentMediaPlayer = mediaPlayer1;
						currentMediaPlayer.setDataSource(new FileInputStream(cachefile1).getFD());
						currentMediaPlayer.prepare();
						currentMediaPlayer.start();
						// �õ��������ֳ���
//						estimateDuration = currentMediaPlayer.getDuration()
//								* contentLength / downloadProgress;
					} else if (currentMediaPlayer != null&&currentMediaPlayer.getDuration()
							- currentMediaPlayer.getCurrentPosition() < 1000) {
						switchMediaPlayer();
					}
				}
				// �������,������������д��sdcard,ɾ������			
				File sdcardFile = new File(savePath,musicName);
				FileUtil.copyFile(cachefile1, sdcardFile);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * ׼���������̰���ǰ���Ȳ���
	 */
	private class PreparedListener implements OnPreparedListener {
		@Override
		public void onPrepared(MediaPlayer mp) {
			mp.seekTo(currentPosition);
		}
	}

	/**
	 * �����ǰmediaplayer�������Ž�����,����mediaplayer���л�
	 */
	private void switchMediaPlayer() throws Exception {
		if (currentMediaPlayer == mediaPlayer1) {
			startPlaying(mediaPlayer2, new FileInputStream(cachefile2).getFD());
		} else {
			startPlaying(mediaPlayer1, new FileInputStream(cachefile1).getFD());
		}
	}

	/**
	 * �����ض���mediaplayer���ļ���������������
	 * 
	 * @param mediaPlayer
	 * @param fd
	 * @throws Exception
	 */
	private void startPlaying(MediaPlayer mediaPlayer, FileDescriptor fd)
			throws Exception {
		currentPosition = currentMediaPlayer.getCurrentPosition();
		currentMediaPlayer = mediaPlayer;
		currentMediaPlayer.setDataSource(fd);
		currentMediaPlayer.prepareAsync();
		currentMediaPlayer.setOnPreparedListener(new PreparedListener());
	}

	public void pause() {
		currentPosition = currentMediaPlayer.getCurrentPosition();
		currentMediaPlayer.pause();
	}
	
	public void release(){
		mediaPlayer1.release();
		mediaPlayer2.release();
	}

}
