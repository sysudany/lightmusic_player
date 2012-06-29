package dany.player.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * ͨ��socket��smtpЭ������������ʼ�
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
	 * ��ʼ������
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
				System.out.println("��������ʧ�ܣ�" + isConnect);
				boo = false;
			}
		} catch (UnknownHostException e) {
			System.out.println("��������ʧ�ܣ�");
			e.printStackTrace();
			boo = false;
		} catch (IOException e) {
			System.out.println("��ȡ��ʧ�ܣ�");
			e.printStackTrace();
			boo = false;
		}
		return boo;
	}

	/**
	 * ����smtpָ�� �����ط�������Ӧ��Ϣ
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
	 * ��ȡ����������Ӧ��Ϣ
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
	 * �����ʼ�
	 * 
	 * @return
	 */
	public boolean sendMail() {
		// ��ʼ��
		if (client == null) {
			if (init()) {

			} else {
				return false;
			}
		}
		// �ж�from,to
		if (from == null || from.length()==0 || to == null || to.length()==0) {
			return false;
		}
		// ��������
		String result = sendCommand("HELO " + mailServer + lineFeet);
		if (!result.startsWith("250")) {
			System.out.println("����ʧ�ܣ�" + result);
			return false;
		}
		// ��֤��������Ϣ
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

		// ����ָ��
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
