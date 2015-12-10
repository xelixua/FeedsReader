package xyz.maksimenko.feedsreader.feedobject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class FeedItem{
	private Long itemId;
	private String title;
	private String guid;
	private String link;
	private Long pubdate;
	private String author;
	private Long feedId;
	private byte readstatus;
	private String description;
	
	public FeedItem(){
		
	}

	public Long getItemId() {
		return itemId;
	}


	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public String getGuid() {
		return guid;
	}


	public void setGuid(String guid) {
		this.guid = guid;
	}


	public String getLink() {
		return link;
	}


	public void setLink(String link) {
		this.link = link;
	}


	public Long getPubdate() {
		return pubdate;
	}


	public void setPubdate(Long pubdate) {
		this.pubdate = pubdate;
	}


	public String getAuthor() {
		return author;
	}


	public void setAuthor(String author) {
		this.author = author;
	}


	public Long getFeedId() {
		return feedId;
	}


	public void setFeedId(Long feedId) {
		this.feedId = feedId;
	}


	public byte getReadstatus() {
		return readstatus;
	}


	public void setReadstatus(byte readstatus) {
		this.readstatus = readstatus;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public boolean equals(Object obj){
		FeedItem item = (FeedItem) obj;
		return(title.equals(item.getTitle()) &&
				((item.getGuid() == null && guid == null) || guid.equals(item.getGuid())) &&
				((item.getLink() == null && link == null) || link.toString().equals(item.getLink())) &&
				pubdate.equals(item.getPubdate()) &&
				((item.getAuthor() == null && author == null) || author.equals(item.getAuthor())) &&
				/*feedId.equals(item.getFeedId()) &&*/
				readstatus == item.getReadstatus() &&
				((item.getDescription() == null && description == null) || description.equals(item.getDescription())));
	}
	
	@Override
	public String toString(){
		String items = itemId + " " + title + " " + guid + " " + link + " " + pubdate + " " + author + " " + feedId + " " + readstatus + " " + description;
		return(items);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
}