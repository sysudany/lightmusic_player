package dany.player.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;

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

}
