package xyz.maksimenko.feedsreader;

public class ProcessedFeed {
	private String feedUrl;
	private Object data;
	
	public String getFeedUrl() {
		return feedUrl;
	}
	public void setFeedUrl(String feedUrl) {
		this.feedUrl = feedUrl;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
	
}
