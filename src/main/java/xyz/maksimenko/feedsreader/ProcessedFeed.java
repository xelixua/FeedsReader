package xyz.maksimenko.feedsreader;

import xyz.maksimenko.feedsreader.feedobject.Feed;

public class ProcessedFeed {
	private String feedUrl;
	private Object data;
	private Feed	tempFeed;
	
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
	public Feed getTempFeed() {
		return tempFeed;
	}
	public void setTempFeed(Feed tempFeed) {
		this.tempFeed = tempFeed;
	}
	
	
}
