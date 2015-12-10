package xyz.maksimenko.test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import xyz.maksimenko.feedsreader.FeedProcessorBean;
import xyz.maksimenko.feedsreader.ProcessedFeed;
import xyz.maksimenko.feedsreader.feedobject.Feed;
import xyz.maksimenko.feedsreader.parsing.*;
import xyz.maksimenko.util.ParserFactory;

public class FeedProcessingTest extends Assert{
	private final String HABR_TITLE = "Хабрахабр / Все публикации";
	private final String NAG_TITLE = "NAG.RU";
	private final String LENTA_TITLE = "Lenta.ru : Новости";
	private final String LIFENEWS_TITLE = "Первый по срочным новостям — LIFE | NEWS";
	private final String VERGE_TITLE = "The Verge -  All Posts";
	private final List<String> feedUrls = new ArrayList<String>();
	private List<ProcessedFeed> processedFeeds;
	
	public FeedProcessingTest(){
		feedUrls.add("http://habrahabr.ru/rss/all/");
		feedUrls.add("http://nag.ru/rss/articles");
		feedUrls.add("http://lenta.ru/rss");
		feedUrls.add("http://lifenews.ru/xml/feed.xml");
		feedUrls.add("http://www.theverge.com/rss/frontpage");
		FeedProcessorBean processor = new FeedProcessorBean();
		processedFeeds = processor.updateFeeds(feedUrls);
	}
	
	@Test
	public void allFeedsFetched(){
		assertEquals(processedFeeds.size(), 5);
	}
	
	@Test
	public void feedsTitleFetched(){
		boolean habra_title = false, nag_title = false, lenta_title = false, life_title = false, verge_title = false;
		for(ProcessedFeed pf : processedFeeds){
			Feed feed = (Feed) pf.getData();
			System.out.println(feed.getTitle());
			if(feed.getTitle().equals(HABR_TITLE)){
				habra_title = true;
			}
			if(feed.getTitle().equals(NAG_TITLE)){
				nag_title = true;
			}
			if(feed.getTitle().equals(LENTA_TITLE)){
				lenta_title = true;
			}
			if(feed.getTitle().equals(LIFENEWS_TITLE)){
				life_title = true;
			}
			if(feed.getTitle().equals(VERGE_TITLE)){
				verge_title = true;
			}
		}
		assertTrue(habra_title && nag_title && lenta_title && life_title && verge_title);
	}
}
