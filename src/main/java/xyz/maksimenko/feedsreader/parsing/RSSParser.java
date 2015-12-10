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

public class RSSParser implements FeedParser {
	private static RSSParser instance = null;
	private final String DEFAULT_LANGUAGE = "en";

	private RSSParser(){}
	
	public static synchronized RSSParser getInstance(){
		if(instance == null){
			instance = new RSSParser();
		}
		
		return instance;
	}
	
	@Override
	public Feed parse(String data) {
		System.out.println("Parsing feed");
		Feed feed = null;
		String pubDate, language, link, generator;
		DocumentBuilderFactory docBuildFactory;
		DocumentBuilder docBuild;
		Document feedData;
		Element imageElement;
		NodeList items;
		FeedItem feedItem;
		short i;
		SimpleDateFormat sdf = new SimpleDateFormat("EEE,dd MMM yyyy HH:mm:ss z", Locale.getDefault());

		docBuildFactory = DocumentBuilderFactory.newInstance();
		try {
			docBuild = docBuildFactory.newDocumentBuilder();
			try {
				try {
					feedData = docBuild.parse(new InputSource(new ByteArrayInputStream(data.replace("\uFEFF", "").getBytes("utf-8"))));
				}
				catch (SAXParseException e) {
					try {
						feedData = docBuild.parse(new InputSource(new ByteArrayInputStream(data.getBytes("utf-16BE"))));
					} catch (SAXParseException e1) {
						feedData = docBuild.parse(new InputSource(new ByteArrayInputStream(data.getBytes("utf-16LE"))));
					}
				}
				imageElement = (Element) feedData.getElementsByTagName("image").item(0);
				try {
					language = feedData.getElementsByTagName("language").item(0).getTextContent();
				} catch (NullPointerException e){
					language = DEFAULT_LANGUAGE;
				}
				link = feedData.getElementsByTagName("link").item(0).getTextContent();
				pubDate = feedData.getElementsByTagName("pubDate").item(0).getTextContent();
				try{
				generator = feedData.getElementsByTagName("generator").item(0).getTextContent();
				} catch (NullPointerException e){
					generator = "";
				}
				
				feed = new Feed();
				feed.setDescription(feedData.getElementsByTagName("description").item(0).getTextContent());
				if(imageElement != null){
					feed.setImageUrl(imageElement.getElementsByTagName("url").item(0).getTextContent());
				}
				byte blang;
				switch(language.toLowerCase()){
				case "ru":
					blang = 0;
					break;
				case "en":
					blang = 1;
					break;
				default: //ru
					blang = 0;
					break;
				}
				feed.setLanguage(blang);
				feed.setLink(link);
				feed.setGenerator(generator);
				Long pd = sdf.parse(pubDate).getTime();
				feed.setPubdate(pd);
				feed.setName(feedData.getElementsByTagName("title").item(0).getTextContent());
				feed.setTitle(feedData.getElementsByTagName("title").item(0).getTextContent());
				items = feedData.getElementsByTagName("item");
				for(i = 0; i < items.getLength(); i++){
					feedItem = parseItem((Element) items.item(i)); //"opens article"	
					feed.addItem(feedItem);
					
				}
				System.out.println("NEW ITEMS: " + i);
				//return rssItems;
				
			} catch (SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
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
	public ProcessedFeed parse(ProcessedFeed processedFeed) {
		String s_data = (String) processedFeed.getData();
		processedFeed.setData(parse(s_data));
		return processedFeed;
	}

	@Override
	public FeedItem parseItem(Element itemData) {
		FeedItem feedItem = new FeedItem();
		String pubDate, description = null;
		NodeList categories;
		SimpleDateFormat sdf = new SimpleDateFormat("EEE,dd MMM yyyy HH:mm:ss z", Locale.getDefault());
		File descriptionFile;
		PrintWriter prWriter;
		byte i = 0;
		BufferedReader bufReader = null;
		
		try{
			description = itemData.getElementsByTagName("description").item(0).getTextContent();
		} catch (NullPointerException e) {
			//System.out.println("No description for item");
		}
		pubDate = itemData.getElementsByTagName("pubDate").item(0).getTextContent();
		Long pd;
		try {
			pd = sdf.parse(pubDate).getTime();
		} catch (ParseException e1) {
			pd = 0L;
			e1.printStackTrace();
		}
		feedItem.setPubdate(pd);
		try{
			feedItem.setGuid(itemData.getElementsByTagName("guid").item(0).getTextContent());
		} catch (NullPointerException e) {
			//System.out.println("No guid for item ");
		}
		feedItem.setTitle(itemData.getElementsByTagName("title").item(0).getTextContent());
		feedItem.setDescription(description);
		try{
			feedItem.setAuthor(itemData.getElementsByTagName("author").item(0).getTextContent());
		} catch (NullPointerException e){
			//System.out.println("No author for item");
		}
		try {
			feedItem.setLink(itemData.getElementsByTagName("link").item(0).getTextContent());
		} catch (NullPointerException e) {
			//System.out.println("No link for item");
		}
		return feedItem;
	}
}
