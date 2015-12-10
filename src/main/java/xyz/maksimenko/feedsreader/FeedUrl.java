package xyz.maksimenko.feedsreader;

public class FeedUrl {
	String url;
	byte type;
	
	public FeedUrl(){};
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public byte getType() {
		return type;
	}
	public void setType(byte type) {
		this.type = type;
	}
}
