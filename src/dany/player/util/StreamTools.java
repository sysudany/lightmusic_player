package dany.player.util;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class StreamTools {
	public static byte[] getBytes(InputStream is) throws Exception{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len =0;
		while((len =is.read(buffer))!=-1){
			baos.write(buffer, 0, len);
		}
		is.close();
		baos.flush();
		return baos.toByteArray();
	}
	
	public static String getString(InputStream is) throws Exception{
		Reader reader = new InputStreamReader(is,"GBK");
		StringBuffer sb = new StringBuffer();
		char[] buff = new char[1024];
		int length = 0;
		while((length = reader.read(buff))!=-1){
			sb.append(buff);
		}
		return sb.toString();
	}
}
