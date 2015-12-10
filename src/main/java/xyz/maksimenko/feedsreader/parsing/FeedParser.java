package xyz.maksimenko.feedsreader.parsing;

import org.w3c.dom.Element;

import xyz.maksimenko.feedsreader.ProcessedFeed;
import xyz.maksimenko.feedsreader.feedobject.Feed;
import xyz.maksimenko.feedsreader.feedobject.FeedItem;

public interface FeedParser {
	public static FeedParser getInstance() {
		return null;
	}
	public ProcessedFeed parse(ProcessedFeed data);
	public Feed parse(String data);
	public FeedItem parseItem(Element itemData);
}
