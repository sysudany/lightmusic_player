package dany.player;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import dany.player.util.StreamTools;

public class BaiduMusic {
	public static void main(String[] args) throws Exception {
		String name = "Ìì±ßµÄ¾ìÁµ";
		URL url = new URL("http://mp3.baidu.com/m?word=" + URLEncoder.encode(name, "GBK")
				+ "&lm=-1&f=ms&tn=baidump3&ct=134217728&lf=&rn=");
		URLConnection connection = url.openConnection();
		Source source = new Source(connection);
		System.out.println(connection.getContentLength());
		List<Element> elements = source.getAllElementsByClass("down");

		List<String> paths = new ArrayList<String>();

		for (Element element : elements) {
			String href = element.getChildElements().get(0).getAttributeValue("href");
			if (href != null) {
				paths.add(href);
			}
		}
		Thread.sleep(1000);
		for (String path : paths) {
			System.out.println("-----------------------");
			URL href = new URL(path);
			URLConnection conn = href.openConnection();
			InputStream inputStream = conn.getInputStream();
			String html = StreamTools.getString(inputStream);
			Pattern pattern = Pattern.compile("(?<=urlM\\().+?(?=\\))");
			Matcher matcher = pattern.matcher(html);
			while (matcher.find()) {
				String src = matcher.group();
				if (src.length() < 10 || src.contains("...")) {
					continue;
				} else {
					String finalPath = src.replaceAll("['+ ]+", "");
					if (finalPath.endsWith(".mp3")) {
						System.out.println(finalPath);
					}
				}
			}
			Thread.sleep(1000);
		}
	}
}
