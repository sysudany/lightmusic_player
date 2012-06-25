package dany.player.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileUtil {
	public static final void copyFile(File srcFile,File desFile)throws Exception{
		FileInputStream inputStream = new FileInputStream(srcFile);
		FileOutputStream outputStream = new FileOutputStream(desFile);
		
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
