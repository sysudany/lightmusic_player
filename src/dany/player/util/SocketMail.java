package dany.player.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 通过socket向smtp协议服务器发送邮件
 * 
 * @author fuyanqing
 * 
 */
public class SocketMail {
	public String mailServer;
	public String from;
	public String to;
	public String content;
	String lineFeet = "\r\n";
	private int port = 25;
	Socket client;
	BufferedReader in;
	DataOutputStream os;

	/**
	 * 初始化连接
	 * @return
	 */
	private boolean init() {
		boolean boo = true;
		if (mailServer == null || "".equals(mailServer)) {
			return false;
		}
		try {
			client = new Socket(mailServer, port);
			in = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
			os = new DataOutputStream(client.getOutputStream());
			String isConnect = response();
			if (isConnect.startsWith("220")) {

			} else {
				System.out.println("建立连接失败：" + isConnect);
				boo = false;
			}
		} catch (UnknownHostException e) {
			System.out.println("建立连接失败！");
			e.printStackTrace();
			boo = false;
		} catch (IOException e) {
			System.out.println("读取流失败！");
			e.printStackTrace();
			boo = false;
		}
		return boo;
	}

	/**
	 * 发送smtp指令 并返回服务器响应信息
	 */
	private String sendCommand(String msg) {
		String result = null;
		try {
			os.writeBytes(msg);
			os.flush();
			result = response();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 读取服务器端响应信息
	 * @return
	 */
	private String response() {
		String result = null;
		try {
			result = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 发送邮件
	 * 
	 * @return
	 */
	public boolean sendMail() {
		// 初始化
		if (client == null) {
			if (init()) {

			} else {
				return false;
			}
		}
		// 判断from,to
		if (from == null || from.length()==0 || to == null || to.length()==0) {
			return false;
		}
		// 进行握手
		String result = sendCommand("HELO " + mailServer + lineFeet);
		if (!result.startsWith("250")) {
			System.out.println("握手失败：" + result);
			return false;
		}
		// 验证发信人信息
		String auth = sendCommand("AUTH LOGIN" + lineFeet);
		if (!auth.startsWith("334")) {
			return false;
		}
		String user = sendCommand(new String(Base64.encode("for__test".getBytes()))
				+ lineFeet);
		if (!user.startsWith("334")) {
			return false;
		}
		String pass = sendCommand(new String(Base64.encode("chdany".getBytes()))
				+ lineFeet);
		if (!pass.startsWith("235")) {
			return false;
		}

		// 发送指令
		String f = sendCommand("Mail From:<" + from + ">" + lineFeet);
		if (!f.startsWith("250")) {
			return false;
		}
		String toStr = sendCommand("RCPT TO:<" + to + ">" + lineFeet);
		if (!toStr.startsWith("250")) {

			return false;
		}

		String data = sendCommand("DATA" + lineFeet);
		if (!data.startsWith("354")) {
			return false;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("From:<" + from + ">" + lineFeet);
		sb.append("To:<" + to + ">" + lineFeet);
		sb.append("Subject:test" + lineFeet);
		sb.append("Date:2010/10/27 17:30" + lineFeet);
		sb.append("Content-Type:text/plain;charset=\"GB2312\"" + lineFeet);
		sb.append(lineFeet);
		sb.append(content);
		sb.append(lineFeet + "." + lineFeet);

		String conStr = sendCommand(sb.toString());
		if(!conStr.startsWith("250"))
			return false;
		

		// quit
		String quit = sendCommand("QUIT" + lineFeet);
		if(!quit.startsWith("221"))
			return false;
		try {
			os.close();
			in.close();
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	
}
