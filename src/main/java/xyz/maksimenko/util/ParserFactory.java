package xyz.maksimenko.util;

import java.sql.SQLException;

import xyz.maksimenko.feedsreader.ProcessedFeed;
import xyz.maksimenko.feedsreader.feedobject.Feed;
import xyz.maksimenko.feedsreader.parsing.*;

public class ParserFactory {
	private final String RSS_SAMPLE = "<rss";
	private final String ATOM_SAMPLE = "http://www.w3.org/2005/Atom";
	private static ParserFactory instance = null;
	private static FeedParser rssParser = null;
	private static FeedParser atomParser = null;
	
	
	public static synchronized ParserFactory getInstance(){
		if(instance == null){
			instance = new ParserFactory();
		}
		
		return instance;
	}
	
	private FeedParser getAtomParser(){
		//TODO write
		return null;
	}
	
	/**
	 * Get feed type from db if present and returns parser. Uses feed type data
	 * @param sample
	 * @return correct FeedParser
	 */
	public FeedParser getParser(ProcessedFeed data){
		Feed feed = data.getTempFeed();
		if(feed != null){
			if(feed.getType() == (byte) 0) {
				if(rssParser == null){
					rssParser = RSSParser.getInstance();
				}
				
				return rssParser;
			}
			if(feed.getType() == (byte) 1) {
				if(atomParser == null){
					atomParser = AtomParser.getInstance();
				}
				
				return atomParser;
			}
		}
		String sample = (String) data.getData();
		return getParser(sample);
	}
	
	/**
	 * Detects parser from sample and returns it. User for new feeds, of feeds with no parsed data
	 * @param sample
	 * @return correct FeedParser
	 */
	public FeedParser getParser(String sample){
		System.out.println(sample);
		System.out.println(sample.contains(RSS_SAMPLE));
		System.out.println(sample.contains(ATOM_SAMPLE));
		if(sample.contains(RSS_SAMPLE)){
			if(rssParser == null){
				rssParser = RSSParser.getInstance();
			}
			
			return rssParser;
			
		} else {
			if(sample.contains(ATOM_SAMPLE)){
				
				if(atomParser == null){
					atomParser = AtomParser.getInstance();
				}
				
				return atomParser;
			} else {
				//TODO unknown format
				System.out.println("Unknown format. ");
				throw new NullPointerException();
				/*if(rssParser == null){
					rssParser = RSSParser.getInstance();
				}
				return rssParser;*/
			}
		}
	}
}
