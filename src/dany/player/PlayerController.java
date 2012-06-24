package dany.player;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

public class PlayerController extends Thread {
	private Context context;
	private int loadProgress;
	private int currentPosition;
	
	public int getLoadProgress() {
		return loadProgress;
	}

	public int getCurrentPosition() {
		return currentPosition;
	}


	private File savePath;
	private String url;

	public PlayerController(Context context, File savePath, String url) {
		super();
		this.context = context;
		this.savePath = savePath;
		this.url = url;
	}

	private MediaPlayer mediaPlayer;
	private final Handler handler = new Handler();
	private int counter = 0;
	private long mediaLengthInKb = 5208;
	private File cacheMediaFile;
	private FileOutputStream out;
	private int totalKbRead;

	public void startStreaming(final String mediaUrl, long mediaLengthInKb,
			long mediaLengthInSeconds) {
		this.mediaLengthInKb = mediaLengthInKb;
		Runnable r = new Runnable() {
			public void run() {
				try {
					downloadAudioIncrement(mediaUrl);
				} catch (IOException e) {
					Log.e(getClass().getName(),
							"Unable to initialize the MediaPlayer for fileurl="
									+ mediaUrl, e);
					return;
				}
			}
		};
		new Thread(r).start();
	}


	public void downloadAudioIncrement(String mediaUrl) throws IOException {
		URLConnection cn = new URL(mediaUrl).openConnection();
		cn.connect();
		InputStream stream = cn.getInputStream();
		if (stream == null) {
			Log.e(getClass().getName(),
					"Unable to get input stream for mediaUrl:" + mediaUrl);
		}
		cacheMediaFile = new File(context.getCacheDir(),
				"downloadingMediaFile.dat");
		if (cacheMediaFile.exists()) {
			cacheMediaFile.delete();
		}
		out = new FileOutputStream(cacheMediaFile);
		byte buf[] = new byte[16384];
		int totalBytesRead = 0;
		do {
			int numread = stream.read(buf);
			if (numread <= 0) {
				break;
			}
			out.write(buf, 0, numread);
			totalBytesRead += numread;
			totalKbRead = totalBytesRead / 1024;
			testMediaBuffer();
			fireDataLoadUpdate();
		} while (validateNotInterrupted());
		stream.close();
		if (validateNotInterrupted()) {
			fireDataFullyLoaded();
		}
	}

	private boolean validateNotInterrupted() {
		if (isInterrupted()) {
			if (mediaPlayer != null) {
				mediaPlayer.pause();
			}
			return false;
		} else {
			return true;
		}
	}

	private void testMediaBuffer() {
		Runnable updater = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mediaPlayer == null) {
					if (totalKbRead >= 16) {
						try {
							startMediaPlayer();
						} catch (Exception e) {
							Log.e(getClass().getName(),
									"Error copying buffered content.", e);
						}
					}
				} else if (mediaPlayer.getDuration()
						- mediaPlayer.getCurrentPosition() <= 1000) {
					transferBufferToMediaPlayer();
				}
			}
		};
		handler.post(updater);

	}

	private void startMediaPlayer() {
		try {

			File bufferedFile = new File(context.getCacheDir(), "PlayingMedia"
					+ (counter++) + ".dat");
			moveFile(cacheMediaFile, bufferedFile);
			Log.e(getClass().getName(),
					"Buffered File Path:" + bufferedFile.getAbsolutePath());
			Log.e(getClass().getName(),
					"Buffered File Length:" + bufferedFile.length() + "");
			mediaPlayer = createMediaPlayer(bufferedFile);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			System.out.println("123456");
			mediaPlayer.start();
			startPlayProgressUpdater();
		} catch (IOException e) {
			Log.e(getClass().getName(), "Error initializing the MediaPlayer", e);
		}
	}

	private MediaPlayer createMediaPlayer(File mediaFile) throws IOException {
		MediaPlayer mPlayer = new MediaPlayer();
		mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// TODO Auto-generated method stub
				Log.e(getClass().getName(), "Error in MediaPlayer:(" + what
						+ ")with extra(" + extra + ")");
				return false;
			}
		});
		FileInputStream fis = new FileInputStream(mediaFile);
		mPlayer.setDataSource(fis.getFD());
		mPlayer.prepare();

		return mPlayer;
	}

	private void transferBufferToMediaPlayer() {
		try {
			boolean wasPlaying = mediaPlayer.isPlaying();
			int curPosition = mediaPlayer.getCurrentPosition();
			File oldBufferedFile = new File(context.getCacheDir(),
					"playingMedia" + counter + ".dat");
			File bufferedFile = new File(context.getCacheDir(), "playingMedia"
					+ (counter++) + ".dat");
			bufferedFile.deleteOnExit();
			moveFile(cacheMediaFile, bufferedFile);
			mediaPlayer.pause();
			mediaPlayer = createMediaPlayer(bufferedFile);
			mediaPlayer.seekTo(curPosition);
			boolean atEndOfFile = mediaPlayer.getDuration()
					- mediaPlayer.getCurrentPosition() <= 1000;
			if (wasPlaying || atEndOfFile) {
				mediaPlayer.start();
			}
			oldBufferedFile.delete();
		} catch (Exception e) {
			Log.e(getClass().getName(),
					"Error updating to newly loaded content", e);
		}
	}

	private void fireDataLoadUpdate() {
		Runnable updater = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				float loadProgress = ((float) totalKbRead / (float) mediaLengthInKb);
				PlayerController.this.loadProgress = (int) (loadProgress * 100);
			}
		};
		handler.post(updater);
	}

	private void fireDataFullyLoaded() {
		Runnable updater = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				transferBufferToMediaPlayer();
				cacheMediaFile.delete();
			}
		};
		handler.post(updater);
	}

	public void startPlayProgressUpdater() {
/*		float progress = (((float) mediaPlayer.getCurrentPosition() / 1000) / mediaLengthInSeconds);
		progressBar.setProgress((int) (progress * 100));
		int pos = mediaPlayer.getCurrentPosition();
		int min = (pos / 1000) / 60;
		int sec = (pos / 1000) % 60;
		if (sec < 10) {
			playTime.setText("" + min + ":0" + sec);
		} else {
			playTime.setText("" + min + ":" + sec);
		}
*/
		currentPosition = mediaPlayer.getCurrentPosition();
		if (mediaPlayer.isPlaying()) {
			Runnable notification = new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					startPlayProgressUpdater();
				}
			};
			handler.postDelayed(notification, 1000);
		}
	}

	public void moveFile(File oldLocation, File newLocation) throws IOException {
		if (oldLocation.exists()) {
			BufferedInputStream reader = new BufferedInputStream(
					new FileInputStream(oldLocation));
			BufferedOutputStream writer = new BufferedOutputStream(
					new FileOutputStream(newLocation, false));
			try {
				byte buff[] = new byte[8192];
				int numChars;
				while ((numChars = reader.read(buff, 0, buff.length)) != -1) {
					writer.write(buff, 0, numChars);
				}
			} catch (IOException e) {
				throw new IOException("IOException when transferring "
						+ oldLocation.getPath() + " to "
						+ newLocation.getPath());
			} finally {
				try {
					if (reader != null) {
						writer.close();
						reader.close();
					}
				} catch (IOException e) {
					Log.e(getClass().getName(),
							"Error closing files when transferring "
									+ oldLocation.getPath() + " to "
									+ newLocation.getPath());
				}
			}
		} else {
			throw new IOException(
					"Old location does not exist when transferring"
							+ oldLocation.getPath() + " to "
							+ newLocation.getPath());
		}

	}

	public void pause(){
		if(mediaPlayer.isPlaying())
			mediaPlayer.pause();
	}
	
	public void start(){
		mediaPlayer.start();
		startPlayProgressUpdater();
	}
	
	public void release(){
		mediaPlayer.release();
	}
}
