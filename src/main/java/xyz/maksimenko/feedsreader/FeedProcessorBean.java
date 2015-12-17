package xyz.maksimenko.feedsreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.function.Supplier;
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
	private final byte UPDATE_INTERVAL = 10; //minutes
	private final byte OLD_THRESHOLD = 120; //hours

	
	public FeedProcessorBean(){
		System.out.println("Hello from feed Processor bean!");
	}
	
	/**
	 *	Runs regular update for all feeds
	*/
	@Schedule(second="0", minute="*/" + UPDATE_INTERVAL, hour="*")
	@SuppressWarnings("unchecked")
	public void updateFeeds() {
		//select all distinct feeds from database
		System.out.println("Updating feeds");
		FeedDAO dao = DAOfactory.getInstance().getFeedDAO();
		try {
			//List<String> feedUrls = (List<String>) dao.getAllDistinctFeeds((byte) 0);
			List<Feed> feeds = (List<Feed>) dao.getAllFeeds();
			updateFeeds(feeds);
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
		return (Feed) ParserFactory.getInstance().getParser(((String) pf.getData()).substring(0, 1000)).parse(pf).getData();
	}
	
	/**
	 * Updates feeds from list
	 * @param feedUrls
	 * @param parser
	 */
	public List<ProcessedFeed> updateFeeds(List<Feed> feeds){
		deleteOldItems();
		return feeds.stream().map(feed -> fetchFeed(feed)) //fetching feed
				.map(pF -> {	//processing processedFeed
						try {
							String feedSource = (String) ((ProcessedFeed) pF).getData();
							 ((ProcessedFeed) pF).setData(ParserFactory.getInstance().getParser(pF).parse(feedSource));
							 return pF;
						} catch (NullPointerException e) {
							e.printStackTrace();
							Stack<String> encodings = new Stack<String>();
						} catch (StringIndexOutOfBoundsException e) {
							System.out.println("Wrong answer from server");
						}
						return null;
					})
				.peek(pF -> { //storing
					Feed etalonFeed = (Feed) ((ProcessedFeed) pF).getData();
					if(etalonFeed != null) storeToDb(pF);
					})
				.collect(Collectors.toList());
	}
	
	/**
	 * Deletes feed items older then OLD_THREShOLD
	 * @param feed
	 */
	
	private void deleteOldItems(){
		System.out.println("Deleting old feedItems");
		Long currentTime = Calendar.getInstance().getTimeInMillis(),
				threshold = currentTime - OLD_THRESHOLD * 3600 * 1000;
		try {
			DAOfactory.getInstance().getFeedItemDAO().deleteOldItems(threshold);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Stores new feed items to database
	 * @param processedFeed
	 */
	@SuppressWarnings("unchecked")
	private void storeToDb(ProcessedFeed processedFeed){
		System.out.println("Storing feed to db");
		Feed feed = (Feed) processedFeed.getData();
		String feedUrl = processedFeed.getFeedUrl();
		FeedItemDAO dao = DAOfactory.getInstance().getFeedItemDAO();
		SortedSet<FeedItem> feedItems = feed.getFeedItems();
		feed.setUrl(feedUrl);
		//try {
			//finds all real feed records with such stores messages for every feedId
			//Feed specificFeed = (Feed) DAOfactory.getInstance().getFeedDAO().getFeedByUrl(feedUrl);
			Feed specificFeed = processedFeed.getTempFeed();
			feed.setFeedId(specificFeed.getFeedId());
			Supplier<TreeSet<FeedItem>> supplier = () -> new TreeSet<FeedItem>();
			System.out.println("ALL:" + feedItems.size());
			feedItems = feedItems.stream()
					.filter(item-> /*!(specificFeed.getFeedItems().contains(item)*/!specificFeed.getGuids().contains(item.getGuid()))
					.map(item -> {item.setFeedId(specificFeed.getFeedId()); return item;})
					.collect(Collectors.toCollection(supplier));
			System.out.println("NEW:" + feedItems.size());
			if(feedItems.size() > 0) {
				specificFeed.getFeedItems().addAll(feedItems);
				try {
					DAOfactory.getInstance().getFeedDAO().updateFeed(specificFeed);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} /*else {
				if(!feed.equals(specificFeed)){
					System.out.println(feed);
					System.out.println(specificFeed);
					SortedSet<FeedItem> specificItems = specificFeed.getFeedItems();
					Feed sF = feed;
					sF.setFeedItems(specificItems);
					DAOfactory.getInstance().getFeedDAO().updateFeed(sF);
				}
			}*/
		/*} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
	}
	
	/**
	 * Fetches feed data from network by feed. Used for regular updates
	 * @param feed
	 * @return container with this feed and fetched data
	 * @throws NullPointerException
	 */
	private ProcessedFeed fetchFeed(Feed feed) throws NullPointerException{
		String feedUrl = feed.getUrl();
		ProcessedFeed result = fetchFeed(feedUrl);
		result.setTempFeed(feed);
		return result;
		
	}
	
	/**
	 * Fetces feed data from network by url. User for new feeds
	 * @param feedUrl
	 * @return container with fetching data
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
