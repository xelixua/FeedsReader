package xyz.maksimenko.DAO;

import java.sql.SQLException;
import java.util.Collection;

import xyz.maksimenko.feedsreader.feedobject.Feed;
import xyz.maksimenko.feedsreader.feedobject.FeedItem;

public interface FeedItemDAO {
	public void addFeedItem(FeedItem feedItem) throws SQLException;
	public void updateFeedItem(FeedItem feedItem) throws SQLException;
	public FeedItem getFeedItemById(Long itemId) throws SQLException;
	public FeedItem getFeedItemByGuid(String guid) throws SQLException;
	public Collection getFeedItemByTitle(String title) throws SQLException;
	public Collection getAllFeedItems() throws SQLException;
	public void deleteFeedItem(FeedItem feedItem) throws SQLException;
	public void deleteOldItems(Long threshold) throws SQLException;
	public Collection getFeedItemsByFeed(Feed feed) throws SQLException;
}
