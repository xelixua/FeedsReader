package xyz.maksimenko.feedsreader.feedobject;

import java.util.HashSet;
import java.util.Set;

public class Category{
	private Long categoryId;
	private String title;
	private Long userId;
	private Set feeds = new HashSet<Feed>();
	
	@Override
	public boolean equals(Object object){
		Category category2 = (Category) object;
		return(title.equals(category2.getTitle()) &&
				userId.equals(category2.getUserId()));
	}
	
	public Long getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Set getFeeds() {
		return feeds;
	}
	public void setFeeds(Set feeds) {
		this.feeds = feeds;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}	
}
