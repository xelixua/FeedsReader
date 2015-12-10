package xyz.maksimenko.feedsreader.parsing;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import xyz.maksimenko.feedsreader.ProcessedFeed;
import xyz.maksimenko.feedsreader.feedobject.Feed;
import xyz.maksimenko.feedsreader.feedobject.FeedItem;

public class AtomParser implements FeedParser {
	private static AtomParser instance = null;
	private final String DEFAULT_LANGUAGE = "en";
	
	private AtomParser(){}
	
	public static synchronized AtomParser getInstance(){
		if(instance == null){
			instance = new AtomParser();
		}
		
		return instance;
	}
	
	@Override
	public ProcessedFeed parse(ProcessedFeed processedFeed) {
		String s_data = (String) processedFeed.getData();
		processedFeed.setData(parse(s_data));
		return processedFeed;
	}

	@Override
	public Feed parse(String data) {
		System.out.println("Parsing feed");
		Feed feed = null;
		String updated, language, link, logoUrl = null, description = null, generator = null;
		DocumentBuilderFactory docBuildFactory;
		DocumentBuilder docBuild;
		Document feedData;
		Element imageElement;
		NodeList items;
		FeedItem feedItem;
		short i;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
		
		
		docBuildFactory = DocumentBuilderFactory.newInstance();
		try {
			docBuild = docBuildFactory.newDocumentBuilder();
			try {
				try {
					feedData = docBuild.parse(new InputSource(new ByteArrayInputStream(data.replace("\uFEFF", "").getBytes("utf-8"))));
				}
				catch (SAXParseException e) {
					feedData = docBuild.parse(new InputSource(new ByteArrayInputStream(data.getBytes("utf-16BE"))));
				}
				if(feedData.getElementsByTagName("logo").item(0) != null){
					logoUrl = (String) feedData.getElementsByTagName("logo").item(0).getTextContent();
				}
				//TODO get language
				link = feedData.getElementsByTagName("link").item(0).getTextContent(); 
				
				updated = feedData.getElementsByTagName("updated").item(0).getTextContent();
				if(feedData.getElementsByTagName("subtitle").item(0) != null){
					description = feedData.getElementsByTagName("subtitle").item(0).getTextContent(); 
				}
				if(feedData.getElementsByTagName("generator").item(0) != null){
					generator = feedData.getElementsByTagName("generator").item(0).getTextContent();
				}
				
				
				feed = new Feed();
				feed.setDescription(description);
				feed.setImageUrl(logoUrl);
				feed.setLanguage((byte) 0);
				feed.setLink(link);
				feed.setGenerator(generator);
				Long pd;
				try {
					pd = sdf.parse(updated).getTime();
				} catch (ParseException e) {
					sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
					try {
						pd = sdf.parse(updated).getTime();
					} catch (ParseException e1) {
						pd = 0L;
					}
					
				}
				feed.setPubdate(pd);
				feed.setName(feedData.getElementsByTagName("title").item(0).getTextContent());
				feed.setTitle(feedData.getElementsByTagName("title").item(0).getTextContent());
			} catch (SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(); 
				}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return feed;
	}

	@Override
	public FeedItem parseItem(Element itemData) {
		FeedItem feedItem = new FeedItem();
		String title, updated, content = null, link = null, inputString;
		Element documentElement, item, imageElement;
		NodeList categories;
		SimpleDateFormat sdf = new SimpleDateFormat("EEE,dd MMM yyyy HH:mm:ss z", Locale.getDefault());
		File descriptionFile;
		PrintWriter prWriter;
		byte i = 0;
		BufferedReader bufReader = null;
		
		try {
			content = itemData.getElementsByTagName("summary").item(0).getTextContent();
		} catch (NullPointerException e) {
			try {
				content = itemData.getElementsByTagName("content").item(0).getTextContent();
			} catch (NullPointerException e1) {
				content = null;
			}
		}
		feedItem.setDescription(content);
		updated = itemData.getElementsByTagName("updated").item(0).getTextContent();
		Long pd;
		try {
			pd = sdf.parse(updated).getTime();
		} catch (ParseException e1) {
			pd = 0L;
			e1.printStackTrace();
		}
		feedItem.setPubdate(pd);
		feedItem.setGuid(itemData.getElementsByTagName("id").item(0).getTextContent());
		feedItem.setTitle(itemData.getElementsByTagName("title").item(0).getTextContent());
		try {
			feedItem.setAuthor(itemData.getElementsByTagName("author").item(0).getTextContent());
		} catch (NullPointerException e) {
		
		}
		try {
			feedItem.setLink(itemData.getElementsByTagName("link").item(0).getTextContent());
		} catch (NullPointerException e){
		}
		
		return feedItem;
	}

}
