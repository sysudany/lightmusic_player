package dany.player.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileUtil {
	public static final void copyFile(FileInputStream inputStream,FileOutputStream outputStream)throws Exception{
		int length = 0;
		byte[] buffer = new byte[16384];
		while((length = inputStream.read(buffer))>0){
			outputStream.write(buffer,0,length);
			outputStream.flush();
		}
		inputStream.close();
		outputStream.close();
	}
}
