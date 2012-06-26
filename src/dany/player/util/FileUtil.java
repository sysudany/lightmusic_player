package dany.player.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import dany.player.bean.Music;
import dany.player.bean.SongInfo;

public class FileUtil {

	public static final void copyFile(File oldLocation, File newLocation)
			throws IOException {
		if(newLocation.exists()){
			newLocation.delete();
		}
		if (oldLocation.exists()) {
			FileInputStream reader = new FileInputStream(oldLocation);
			FileOutputStream writer = new FileOutputStream(newLocation);
			try {
				byte[] buff = new byte[8192];
				int length;
				while ((length = reader.read(buff)) > 0) {
					writer.write(buff, 0, length);
				}
			} catch (IOException ex) {
				throw new IOException("IOException when transferring "
						+ oldLocation.getPath() + " to "
						+ newLocation.getPath());
			} finally {
				try {
					if (reader != null) {
						writer.close();
						reader.close();
					}
				} catch (IOException ex) {
					Log.e("moveFile","Error closing files when transferring "
									+ oldLocation.getPath() + " to "
									+ newLocation.getPath());
				}
			}
		} else {
			throw new IOException(
					"Old location does not exist when transferring "
							+ oldLocation.getPath() + " to "
							+ newLocation.getPath());
		}
	}
	
	
	public static final List<Music> getMusicsFromDir(File directory) throws Exception{
		List<Music> musicList = new ArrayList<Music>();
		File[] files = directory.listFiles();
		RandomAccessFile ran;
		byte[] buffer = new byte[128];
		SongInfo info; 
		for(File file :files){
			if(file.getName().endsWith(".mp3")){
				ran = new RandomAccessFile(file, "r");
				ran.seek(ran.length() - 128);
				ran.read(buffer);
				info = new SongInfo(buffer);
				Music music = new Music();
				music.artist = info.getArtist();
				music.musicName = info.getSongName();
				music.localPath = file.getAbsolutePath();
				System.out.println(music);
				musicList.add(music);
			}
		}
		return musicList;
	}

}
