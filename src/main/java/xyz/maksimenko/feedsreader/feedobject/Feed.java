package xyz.maksimenko.feedsreader.feedobject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.json.JsonObject;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

//general abstract class for getting JSON of feed
public class Feed implements Comparable<Feed>{
	
	private Long feedId;
	private String title;
	private byte type = 127;
	private String name;
	private String url;
	private String link;
	private String description;
	private String generator;
	private byte language;
	private Long pubdate;
	private String imageUrl;
	
	private Set categories = new HashSet<FeedItem>();
	private SortedSet feedItems = new TreeSet<FeedItem>();
	
	public Feed(){
		
	}
	
	@Override
	public String toString(){
		return new String(feedId + " " + title + " " + name + " " + url + " " + link + " " + url + " " + description + " " + generator + " " + language + " " + pubdate + " " + imageUrl);
	}
	
	@Override
	public boolean equals(Object object) {
		Feed feed2 = (Feed) object;
		return (title.equals(feed2.getTitle()) &&
				type == feed2.getType() &&
				name.equals(feed2.getName()) &&
				url.equals(feed2.getUrl()) &&
				((feed2.getLink() == null && link == null) || link.equals(feed2.getLink())) &&
				((feed2.getDescription() == null && description == null) || description.equals(feed2.getDescription())) &&
				((feed2.getGenerator() == null && generator == null) || generator.equals(feed2.getGenerator())) &&
				language == feed2.getLanguage() &&
				((feed2.getPubdate() == null && pubdate == null) || pubdate.equals(feed2.getPubdate())) &&
				((feed2.getImageUrl() == null && imageUrl == null) || imageUrl.equals(feed2.getImageUrl())));
	}
	
	public String fetch() throws NullPointerException{
		HttpURLConnection feedConnection;
		String inputString;
		String responseText = "";
		
		try {
			feedConnection = (HttpURLConnection) new URL(url).openConnection();
			feedConnection.setRequestProperty("Content-Type", "text/xml");
			feedConnection.setRequestMethod("GET");
			feedConnection.setDoOutput(true);
			feedConnection.connect();
			
			//reading response
			InputStream inStr = feedConnection.getInputStream();
			InputStreamReader inStrReader = new InputStreamReader(inStr);
			BufferedReader bufReader = new BufferedReader(inStrReader);
			while ((inputString = bufReader.readLine()) != null) {
	            responseText += inputString;
			}
	        bufReader.close();
			inStrReader.close();
			inStr.close();
			feedConnection.disconnect();
			return responseText;
		} catch (IOException e) {
			// TODO retry to fetch feed again
			e.printStackTrace();
			return "";
		}
	}
	
	public Set<String> getGuids(){
		Set<String> result = new HashSet<String>();
		for(Object item : feedItems){
			result.add(((FeedItem) item).getGuid());
		}
		return result;
	}

	private JsonObject feedObject;
	
	public Long getFeedId() {
		return feedId;
	}

	public void setFeedId(Long feedId) {
		this.feedId = feedId;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getGenerator() {
		return generator;
	}

	public void setGenerator(String generator) {
		this.generator = generator;
	}

	@Enumerated(EnumType.ORDINAL)
	public byte getLanguage() {
		return language;
	}

	public void setLanguage(byte language) {
		this.language = language;
	}

	public Long getPubdate() {
		return pubdate;
	}

	public void setPubdate(Long pubdate) {
		this.pubdate = pubdate;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public JsonObject getFeedObject() {
		return feedObject;
	}

	public void setFeedObject(JsonObject feedObject) {
		this.feedObject = feedObject;
	}

	public SortedSet getFeedItems() {
		return feedItems;
	}

	public void setFeedItems(SortedSet feedItems) {
		this.feedItems = feedItems;
	}

	public void addItem(FeedItem feedItem){
		feedItems.add(feedItem);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Set getCategories() {
		return categories;
	}

	public void setCategories(Set categories) {
		this.categories = categories;
	}

	@Override
	public int compareTo(Feed o) {
		return title.compareTo(o.getTitle());
	}
}