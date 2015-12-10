package xyz.maksimenko.feedsreader;

import java.util.HashSet;
import java.util.Locale.Category;
import java.util.Set;

public class User {
	private Long userId;
	private String login;
	private String name;
	private String passwordHash;
	private String openedCategory;
	private Set categories = new HashSet<Category>();
	
	public User(){};
	
	@Override
	public boolean equals(Object object){
		User user2 = (User) object;
		return (login.equals(user2.getLogin()) &&
				name.equals(user2.getName()) &&
				passwordHash.equals(user2.getPasswordHash()) &&
				((user2.getOpenedCategory() == null && openedCategory == null) || openedCategory.equals(user2.getOpenedCategory())) &&
				categories.equals(user2.getCategories()));
	}
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPasswordHash() {
		return passwordHash;
	}
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	public Set getCategories() {
		return categories;
	}
	public void setCategories(Set categories) {
		this.categories = categories;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}


	public String getOpenedCategory() {
		return openedCategory;
	}


	public void setOpenedCategory(String openedCategory) {
		this.openedCategory = openedCategory;
	}
}
