package xyz.maksimenko.DAO;

import java.sql.SQLException;
import java.util.Collection;

import xyz.maksimenko.feedsreader.feedobject.Category;
import xyz.maksimenko.feedsreader.feedobject.Feed;

public interface FeedDAO {
	public void addFeed(Feed feed) throws SQLException;
	public void updateFeed(Feed feed) throws SQLException;
	public void deleteFeed(Feed feed) throws SQLException;
	public Feed getFeedById(Long feedId) throws SQLException;
	public Feed getFeedByUrl(String url) throws SQLException;
	public Collection getAllDistinctFeeds(byte FeedType) throws SQLException;
	public Collection getFeedsByCategory(Category category) throws SQLException;
	public Collection getFeedByTitle(String title) throws SQLException; //for test purposes
}
