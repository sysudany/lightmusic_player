package dany.player;

import dany.player.util.SocketMail;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MailActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	public void sendMail(View view) {
		SocketMail mail = new SocketMail();
		mail.mailServer = "smtp.163.com";
		mail.from = "for__test@163.com";
		mail.to = "chdany@qq.com";
		mail.content = "hello,this is a test mail!";
		boolean boo = mail.sendMail();
		if (boo)
			Toast.makeText(this, "邮件发送成功！", 1).show();
		else {
			Toast.makeText(this, "邮件发送失败！", 1).show();
		}
		
	}
}