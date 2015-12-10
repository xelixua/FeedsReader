package xyz.maksimenko.feedsreader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.spi.JsonProvider;
import javax.websocket.Session;

import org.jboss.logging.Logger;

import xyz.maksimenko.DAO.CategoryDAO;
import xyz.maksimenko.DAO.FeedDAO;
import xyz.maksimenko.DAO.UserDAO;
import xyz.maksimenko.feedsreader.feedobject.Category;
import xyz.maksimenko.feedsreader.feedobject.Feed;
import xyz.maksimenko.feedsreader.feedobject.FeedItem;
import xyz.maksimenko.feedsreader.parsing.FeedParser;
import xyz.maksimenko.util.DAOfactory;
import xyz.maksimenko.util.ParserFactory;

@ApplicationScoped
public class WSSessionHandler{
	//private final Set<Session> sessions = new HashSet<Session>();
	private final static Map<String, User> onlineUsers = new HashMap<String, User>();
	private final static Map<Session, User> onlineUserSessions = new HashMap<Session, User>();
	
	@Inject
	private FeedProcessor feedP;
	
	/*public void addSession(Session session){
		sessions.add(session);
	}*/
	
	public WSSessionHandler(){
		System.out.println("WSSessionHandler");
	}
	
	/*public Set<Session> getSessions() {
		return sessions;
	}*/
	
	public void userRegistering(Session session, String login, String name, String passwordHash){
		User user = new User();
		user.setLogin(login);
		user.setName(name);
		user.setPasswordHash(passwordHash);
		UserDAO udao = DAOfactory.getInstance().getUserDAO();
		try {
			udao.addUser(user);
			Category defaultCategory = new Category();
			defaultCategory.setTitle("Все фиды");
			user = udao.getUserByLogin(login);
			defaultCategory.setUserId(user.getUserId());
			DAOfactory.getInstance().getCategoryDAO().addCategory(defaultCategory);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void userLoggedIn(Session session, String login, String password) throws SecurityException {
		User user = null;
		System.out.println("in userLogged");
		if(!onlineUsers.containsKey(login)){
			System.out.println("User is not online");
			try {
				//TODO autentificate user
				user = DAOfactory.getInstance().getUserDAO().getUserByLogin(login);
				onlineUsers.put(login, user);
			} catch (SQLException e) {
				System.out.println("Wrong login");
				throw new SecurityException();
			}
		} else {
			user = onlineUsers.get(login);
		}
		if(!user.getPasswordHash().equals(password)) {System.out.println("Wrong password"); throw new SecurityException();}
		onlineUserSessions.put(session, user);
		try {
			JsonProvider provider = JsonProvider.provider();
			JsonObject message = provider.createObjectBuilder()
					.add("action", "auth_success")
					.build();
			WebSocketServer.sendToSession(session, message);
			Set<Category> categories = (Set<Category>) user.getCategories();
			sendListToClient(session, categories, Category.class);
		} catch (NullPointerException e){
			//user not autentificated!
		}
	}
	
	public void userLoggingOut(Session session){
		onlineUsers.remove(session);
		onlineUserSessions.remove(session);
		JsonProvider provider = JsonProvider.provider();
		JsonObject message = provider.createObjectBuilder()
				.add("action", "logged_out")
				.build();
		WebSocketServer.sendToSession(session, message);
	}
	
	public void feedsRequested(Session session, String categoryName){
		User user = onlineUserSessions.get(session);
		user.setOpenedCategory(categoryName);
		Set<Feed> feeds = getFeeds(session, categoryName);
		sendListToClient(session, feeds, Feed.class);
	}
	
	private Set<Feed> getFeeds(Session session, String categoryName){
		User user = onlineUserSessions.get(session);
		user.setOpenedCategory(categoryName);
		Set<Feed> feeds = null;
		try {
			Set<Category> userCategories = (Set<Category>) user.getCategories();
			Category selectedCategory = userCategories.stream().filter(category -> category.getTitle().equals(categoryName)).findFirst().get();
			try {
				if(selectedCategory == null) {
					throw new NullPointerException();
				}
				feeds = new HashSet((List<Feed>) DAOfactory.getInstance().getFeedDAO().getFeedsByCategory(selectedCategory));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				//no category selected. Returl all feeds
				feeds = new HashSet<Feed>();
				for(Category category : userCategories){
					feeds.addAll(category.getFeeds());
				}
			}
		} catch (NullPointerException e){
			//malformed data sent by user
		}
		
		return feeds;
	}
	
	public void itemsRequested(Session session, String feedName){
		User user = onlineUserSessions.get(session);
		String categoryName = user.getOpenedCategory();
		System.out.println("Opened category " + categoryName);
		Set<Feed> feeds = getFeeds(session, categoryName);
		System.out.println(feedName);
		Set<FeedItem> feedItems = getFeedItems(feeds, feedName);
		sendListToClient(session, feedItems, FeedItem.class);
	}
	
	public void allItemsRequested(Session session){
		Set<FeedItem> feedItems = getFeedItems(null, null);
		sendListToClient(session, feedItems, FeedItem.class);
	}
	
	/**
	 * adds existing feed to user category or creates new if not present 
	 * @param session
	 * @param feedUrl
	 * @param categoryToAdd
	 */
	public void addFeedRequested(Session session, String feedUrl, String categoryToAdd){
		//detect if we already have that feed in database
		FeedDAO feedDao = DAOfactory.getInstance().getFeedDAO();
		CategoryDAO catDao = DAOfactory.getInstance().getCategoryDAO();
		try {
			List<Category> categories = (List<Category>) catDao.getCategoriesByUser(onlineUserSessions.get(session));
			Category targetCategory = categories.stream().filter(category -> category.getTitle().equals(categoryToAdd)).findFirst().get();
			try {
				Feed feed = null;
				if((feed = feedDao.getFeedByUrl(feedUrl)) == null){
					//parse feed and add it to database
					feed = feedP.processFeed(feedUrl);
					feed.setUrl(feedUrl);
					//feed.getCategories().add(targetCategory);
					feedDao.addFeed(feed);
				}
				targetCategory.getFeeds().add(feed);
				catDao.updateCategory(targetCategory);
				feedsRequested(session, categoryToAdd);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/**
	 * removes feed from user collections
	 * @param session
	 * @param feedsToRemove
	 */
	@SuppressWarnings("unchecked")
	public void removeFeedsRequested(Session session, JsonArray feedsToRemove){
		User user = onlineUserSessions.get(session);
		Set<Category> userCategories = (Set<Category>) user.getCategories();
		userCategories.forEach(category -> {
			Set<Feed> categoryFeeds = (Set<Feed>) category.getFeeds();
			feedsToRemove.stream()
				.filter(jsonValue -> { //get feed jsons only for this category
					JsonObject json = (JsonObject) jsonValue;
					String categoryName = json.getString("category");
					return categoryName.equals(category.getTitle());
					}).collect(Collectors.toList()).forEach(jsonValue -> { //process each jsons
						JsonObject json = (JsonObject) jsonValue;
						String feedToRemove = json.getString("feed");
						categoryFeeds.stream().filter(feed -> feed.getTitle().equals(feedToRemove)).forEach(feed -> System.out.println(feed.getTitle() + " | "));
						categoryFeeds.remove(categoryFeeds.stream().filter(feed -> feed.getTitle().equals(feedToRemove)).findFirst().get());
						});
					try {
						DAOfactory.getInstance().getCategoryDAO().updateCategory(category);
						feedsRequested(session, user.getOpenedCategory());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
	}
	
	public void addCategoryRequested(Session session, String categoryName){
		User user = onlineUserSessions.get(session);
		Category newCategory = new Category();
		newCategory.setTitle(categoryName);
		newCategory.setUserId(user.getUserId());
		user.getCategories().add(newCategory);
		try {
			DAOfactory.getInstance().getUserDAO().updateUser(user);
			try {
				Set<Category> categories = (Set<Category>) user.getCategories();
				sendListToClient(session, categories, Category.class);
			} catch (NullPointerException e){
				//user not autentificated!
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void removeCategoriesRequested(Session session, JsonArray categoriesToRemove){
		User user = onlineUserSessions.get(session);
		Set<Category> userCategories = (Set<Category>) user.getCategories();
		categoriesToRemove.forEach(jsonValue -> {
			String categoryName = jsonValue.toString();
			userCategories.remove(userCategories.stream().filter(category -> category.getTitle().equals(categoryName)).findFirst().get());
		});
		try {
			DAOfactory.getInstance().getUserDAO().updateUser(user);
			try {
				Set<Category> categories = (Set<Category>) user.getCategories();
				sendListToClient(session, categories, Category.class);
			} catch (NullPointerException e){
				//user not autentificated!
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Set<FeedItem> getFeedItems(Set<Feed> feeds, String feedName) throws NullPointerException{
		Set<FeedItem> feedItems = null;
		Feed selectedFeed = null;
		if(feeds != null){
			selectedFeed = feeds.stream().filter(feed -> feed.getTitle().equals(feedName)).findFirst().orElseGet(null);
		}
		if(selectedFeed == null){
			if(feedName == null){ //user requested all items
				feedItems = new HashSet<FeedItem>();
				for(Feed feed:feeds){
					feedItems.addAll(feed.getFeedItems());
				}
			} else {
				throw new NullPointerException();
			}
		} else {
			feedItems = selectedFeed.getFeedItems();
		}
		return feedItems;
	}
	
	
	public void removeSession(Session session){
		//sessions.remove(session);
		String login = onlineUserSessions.get(session).getLogin();
		onlineUserSessions.remove(session);
		onlineUsers.remove(login);
	}
		
	private void sendToAllConnectedSessions(JsonObject message) {
		
	}
	
	private <T> void sendListToClient(Session session, Set<T> set, Class<T> itemClass){
		System.out.println("In send list to client");
		JsonProvider provider = JsonProvider.provider();
		JsonArrayBuilder setBuilder = provider.createArrayBuilder();
		JsonObjectBuilder setObjectBuilder;
		JsonObject setObject;
		String action = null, title = null, category = null, author = null, description = null;
		short unreadItems = -1;
		for(T item : set){
			if(itemClass.equals(Category.class)){
				title = ((Category) item).getTitle();
			}
			if(itemClass.equals(Feed.class)){
				Feed feed = (Feed) item;
				title = feed.getTitle();
				category = onlineUserSessions.get(session).getOpenedCategory();
			}
			if(itemClass.equals(FeedItem.class)){
				FeedItem feedItem = (FeedItem) item;
				title = feedItem.getTitle();
				author = feedItem.getAuthor();
				description = feedItem.getDescription();
			}
			setObjectBuilder = provider.createObjectBuilder();
			setObjectBuilder.add("title", title);
			if(category != null){
				setObjectBuilder.add("category", category);
			}
			if(unreadItems > -1){
				setObjectBuilder.add("undreadItems", unreadItems);
			}
			if(author != null){
				setObjectBuilder.add("author", author);
			}
			if(description != null){
				setObjectBuilder.add("description", description);
			}
			setObject = setObjectBuilder.build();
			setBuilder.add(setObject);
		}
		JsonArray itemsset = setBuilder.build();
		if(itemClass.equals(Category.class)){
			action = "categories";
		}
		if(itemClass.equals(Feed.class)){
			action = "feeds";
		}
		if(itemClass.equals(FeedItem.class)){
			action = "feed_items";
		}
		JsonArray feedsset = setBuilder.build();
		JsonObject wholeJson = provider.createObjectBuilder()
				.add("action", action)
				.add("set", itemsset)
				.build();
		WebSocketServer.sendToSession(session, wholeJson);	
	}
}
