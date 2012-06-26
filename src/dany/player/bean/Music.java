package dany.player.bean;

public class Music {
	public int _id;
	public String musicName;
	public String artist;
	public String url;
	public String localPath;
	@Override
	public String toString() {
		return "Music [_id=" + _id + ", musicName=" + musicName + ", artist=" + artist + ", url=" + url + ", localPath=" + localPath + "]";
	}
}
