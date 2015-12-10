package xyz.maksimenko.feedsreader;

import java.util.List;

import javax.ejb.Local;
import javax.websocket.Session;

import xyz.maksimenko.feedsreader.feedobject.Feed;
import xyz.maksimenko.feedsreader.parsing.FeedParser;

@Local
public interface FeedProcessor {
	public void updateFeeds();
	public Feed processFeed(String feedUrl);
	public List<ProcessedFeed> updateFeeds(List<String> feedUrls);	
}
