package xyz.maksimenko.util;

import xyz.maksimenko.feedsreader.parsing.*;

public class ParserFactory {
	private final String RSS_SAMPLE = "<rss";
	private final String ATOM_SAMPLE = "xmlns=\"http://www.w3.org/2005/Atom\"";
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
	 * Detects feed type by sample and returns proper parser
	 * @param sample
	 * @return
	 */
	public FeedParser getParser(String sample){
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
				System.out.println("Unknown format. Using RSS parser");
				if(rssParser == null){
					rssParser = RSSParser.getInstance();
				}
				return rssParser;
			}
		}
	}
}
