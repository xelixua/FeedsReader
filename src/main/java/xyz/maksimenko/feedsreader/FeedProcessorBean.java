package xyz.maksimenko.feedsreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Stateless;

import xyz.maksimenko.DAO.FeedDAO;
import xyz.maksimenko.DAO.FeedItemDAO;
import xyz.maksimenko.feedsreader.feedobject.Feed;
import xyz.maksimenko.feedsreader.feedobject.FeedItem;
import xyz.maksimenko.feedsreader.parsing.FeedParser;
import xyz.maksimenko.util.DAOfactory;
import xyz.maksimenko.util.ParserFactory;

@Stateless
public class FeedProcessorBean  implements FeedProcessor{
	private final Long DEFAULT_FEED_ID = 1L;
	private final byte UPDATE_INTERVAL = 5; //minutes

	/**
	 * periodical automatic update of all feeds in datebase
	 */	
	
	public FeedProcessorBean(){
		System.out.println("Hello from feed Processor bean!");
	}
	
	@Schedule(second="0", minute="*/10", hour="*")
	@SuppressWarnings("unchecked")
	public void updateFeeds() {
		//select all distinct feeds from database
		System.out.println("Updating feeds");
		FeedDAO dao = DAOfactory.getInstance().getFeedDAO();
		try {
			List<String> feedUrls = (List<String>) dao.getAllDistinctFeeds((byte) 0);
			updateFeeds(feedUrls);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * fetches and parses feed when adding a new one
	 * @param feedUrl
	 * @param parser
	 * @return
	 */
	public Feed processFeed(String feedUrl){
		ProcessedFeed pf = fetchFeed(feedUrl);
		return (Feed) ParserFactory.getInstance().getParser(((String) pf.getData()).substring(0, 100)).parse(pf).getData();
	}
	
	/**
	 * Updates feeds from list
	 * @param feedUrls
	 * @param parser
	 */
	public List<ProcessedFeed> updateFeeds(List<String> feedUrls){
		return feedUrls.stream().map(url -> fetchFeed(url))
				.map(data -> ParserFactory.getInstance().getParser(((String) data.getData()).substring(0, 100)).parse(data))
				.peek(etalonFeed -> storeToDb(etalonFeed))
				.collect(Collectors.toList());
	}
	
	/**
	 * stores feedItems to database for correct feedId
	 * @param feed
	 */
	@SuppressWarnings("unchecked")
	private void storeToDb(ProcessedFeed processedFeed){
		System.out.println("Storing feed to db");
		Feed feed = (Feed) processedFeed.getData();
		String feedUrl = processedFeed.getFeedUrl();
		FeedItemDAO dao = DAOfactory.getInstance().getFeedItemDAO();
		Set<FeedItem> feedItems = feed.getFeedItems();
		try {
			//finds all real feed records with such stores messages for every feedId
			Feed specificFeed = (Feed) DAOfactory.getInstance().getFeedDAO().getFeedByUrl(feedUrl);
			feedItems = feedItems.stream().map(item -> {item.setFeedId(specificFeed.getFeedId()); return item;}).collect(Collectors.toSet());
			feedItems.forEach(feedItem -> {
				String guid = feedItem.getGuid();
				try {
					//it's a new item, store it to db
					FeedItem item;
					if((item = dao.getFeedItemByGuid(guid)) != null){
						dao.deleteFeedItem(item);
					}
				} catch (Exception e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			specificFeed.setFeedItems(feedItems);
			DAOfactory.getInstance().getFeedDAO().updateFeed(specificFeed);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/**
	 * Fetces feed data from network
	 * @param feedUrl
	 * @return
	 * @throws NullPointerException
	 */
	private ProcessedFeed fetchFeed(String feedUrl) throws NullPointerException{
		ProcessedFeed result = new ProcessedFeed();
		result.setFeedUrl(feedUrl);
		HttpURLConnection feedConnection;
		String inputString;
		String responseText = "";
		System.out.println("Updating feed by url: " + feedUrl);
		
		try {
			feedConnection = (HttpURLConnection) new URL(feedUrl).openConnection();
			feedConnection.setRequestProperty("Content-Type", "text/xml");
			feedConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1");
			
			feedConnection.setRequestMethod("GET");
			feedConnection.setDoOutput(true);
			feedConnection.connect();
			
			//reading response
			InputStream inStr = feedConnection.getInputStream();
			InputStreamReader inStrReader = new InputStreamReader(inStr, "UTF-8");
			BufferedReader bufReader = new BufferedReader(inStrReader);
			while ((inputString = bufReader.readLine()) != null) {
	            responseText += inputString;
	        }
	        bufReader.close();
			inStrReader.close();
			inStr.close();
			feedConnection.disconnect();
			result.setData(responseText);
		} catch (IOException e) {
			// TODO retry to fetch feed again
			
		}
		return result;
	}
}
