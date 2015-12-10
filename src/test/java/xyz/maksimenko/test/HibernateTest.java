package xyz.maksimenko.test;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;

import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.junit.Assert;
import org.junit.Test;
import java.util.List;

import xyz.maksimenko.DAO.CategoryDAO;
import xyz.maksimenko.DAO.FeedDAO;
import xyz.maksimenko.DAO.FeedItemDAO;
import xyz.maksimenko.DAO.UserDAO;
import xyz.maksimenko.feedsreader.FeedUrl;
import xyz.maksimenko.feedsreader.User;
import xyz.maksimenko.feedsreader.feedobject.Category;
import xyz.maksimenko.feedsreader.feedobject.Feed;
import xyz.maksimenko.feedsreader.feedobject.FeedItem;
import xyz.maksimenko.util.DAOfactory;
import xyz.maksimenko.util.HibernateUtil;

public class HibernateTest extends Assert{
	
	private final String TEST_LOGIN = "testuser01@mail.ru";
	private final String TEST_CATEGORY = "IT";
	private final String TEST_FEED = "Хабрахабр / Все публикации";
	private final String TEST_ITEM_TITLE = "Test item";
	private final String TEST_LOGIN_GENERATED = "TestGeneratedUser@mail.ru";
	private final String TEST_CATEGORY_GENERATED = "TestCategoryGenerated";
	private final String TEST_FEED_GENERATED = "TestFeedGenerated";
	private final String TEST_ITEM_TITLE_GENERATED = "TestGeneratedItem";
	private final String TEST_GUID = "http://habrahabr.ru/post/271171/";
	private final String TEST_FEED_URL = "http://habrahabr.ru/rss/all/";
	private final String DUMMY = "barabulka";
	private final byte OLD_THRESHOLD = 2; //hours
	
	@SuppressWarnings("unchecked")
	private void clean(){
		List<FeedItem> oldTestItems;
		List<Feed> oldTestFeeds;
		List<Category> oldTestCategories;
		User oldTestUser;
		try {
			oldTestUser = DAOfactory.getInstance().getUserDAO().getUserByLogin(TEST_LOGIN_GENERATED);
			if(oldTestUser != null) DAOfactory.getInstance().getUserDAO().deleteUser(oldTestUser);
			
			if((oldTestCategories = (List<Category>) DAOfactory.getInstance().getCategoryDAO().getCategoryByTitle(TEST_CATEGORY_GENERATED)) != null) {		
				oldTestCategories.forEach(oldCategory -> { try { DAOfactory.getInstance().getCategoryDAO().deleteCategory(oldCategory); } catch (SQLException e) { e.printStackTrace(); }});
			}
			
			if((oldTestFeeds = (List<Feed>) DAOfactory.getInstance().getFeedDAO().getFeedByTitle(TEST_FEED_GENERATED)) != null){
				oldTestFeeds.forEach(oldFeed -> {try { DAOfactory.getInstance().getFeedDAO().deleteFeed(oldFeed); } catch (SQLException e) { e.printStackTrace(); }});
			}
			
			if((oldTestItems = (List<FeedItem>) DAOfactory.getInstance().getFeedItemDAO().getFeedItemByTitle(TEST_ITEM_TITLE_GENERATED)) != null){
				oldTestItems.forEach(oldItem -> {try { DAOfactory.getInstance().getFeedItemDAO().deleteFeedItem(oldItem); } catch (SQLException e) { e.printStackTrace(); }});
			}
						
			if((oldTestCategories = (List<Category>) DAOfactory.getInstance().getCategoryDAO().getCategoryByTitle(DUMMY)) != null) {		
				oldTestCategories.forEach(oldCategory -> { try { DAOfactory.getInstance().getCategoryDAO().deleteCategory(oldCategory); } catch (SQLException e) { e.printStackTrace(); }});
			}
			
			if((oldTestFeeds = (List<Feed>) DAOfactory.getInstance().getFeedDAO().getFeedByTitle(DUMMY)) != null){
				oldTestFeeds.forEach(oldFeed -> {try { DAOfactory.getInstance().getFeedDAO().deleteFeed(oldFeed); } catch (SQLException e) { e.printStackTrace(); }});
			}
			
			if((oldTestItems = (List<FeedItem>) DAOfactory.getInstance().getFeedItemDAO().getFeedItemByTitle(DUMMY)) != null){
				oldTestItems.forEach(oldItem -> {try { DAOfactory.getInstance().getFeedItemDAO().deleteFeedItem(oldItem); } catch (SQLException e) { e.printStackTrace(); }});
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Test 
	public void getUser(){
		try {
			User user = DAOfactory.getInstance().getUserDAO().getUserByLogin(TEST_LOGIN);
			assertTrue(user.getName().equals("testuser01"));
			assertTrue(user.getPasswordHash().equals("abcdefg"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void addUser(){
		clean();
		User user = new User();
		UserDAO dao = DAOfactory.getInstance().getUserDAO();
		user.setLogin(TEST_LOGIN_GENERATED);
		user.setName("testName");
		user.setPasswordHash("abcdef");
		try {
			dao.addUser(user);
			User user2 = dao.getUserByLogin(TEST_LOGIN_GENERATED);
			dao.deleteUser(user2);
			assertTrue(user.equals(user2));
		} catch (SQLException e){
			e.printStackTrace();
		}		
	}
	
	@Test
	public void updateUser(){
		clean();
		User user = new User();
		UserDAO dao = DAOfactory.getInstance().getUserDAO();
		user.setLogin(TEST_LOGIN_GENERATED);
		user.setName("testName");
		user.setPasswordHash("abcdef");
		try {
			dao.addUser(user);
			user.setName(DUMMY);
			User user2 = dao.getUserByLogin(TEST_LOGIN_GENERATED);
			user2.setName(user.getName());
			dao.updateUser(user2);
			user2 = dao.getUserByLogin(TEST_LOGIN_GENERATED);
			dao.deleteUser(user2);
			assertTrue(user.equals(user2));
		} catch (SQLException e){
			e.printStackTrace();
		}	
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void getCategoriesForUser(){
		try {
			User user = DAOfactory.getInstance().getUserDAO().getUserByLogin(TEST_LOGIN);
			List<Category> categories = (List<Category>) DAOfactory.getInstance().getCategoryDAO().getCategoriesByUser(user);
			assertTrue(categories.size() > 0);
			Category category = categories.get(0);
			
			assertTrue(category.getCategoryId() == 1);
			assertTrue(category.getTitle().equals(TEST_CATEGORY));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void addCategory(){
		clean();
		Category category = new Category();
		CategoryDAO dao = DAOfactory.getInstance().getCategoryDAO();
		try {
			category.setTitle(TEST_CATEGORY_GENERATED);
			category.setUserId(666L);
			dao.addCategory(category);
			
			//get writed to db
			Category category2 = ((List<Category>) dao.getCategoryByTitle(TEST_CATEGORY_GENERATED)).get(0);
			dao.deleteCategory(category2);
			assertTrue(category.equals(category2));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void updateCategory(){
		clean();
		
		Category category = new Category(), category2 = null;
		CategoryDAO dao = DAOfactory.getInstance().getCategoryDAO();
		try{
			category.setTitle(TEST_CATEGORY_GENERATED);
			category.setUserId(666L);
			dao.addCategory(category);
			
			category.setTitle(DUMMY);
			
			//get writed to db
			category2 = ((List<Category>) dao.getCategoryByTitle(TEST_CATEGORY_GENERATED)).get(0);
			category2.setTitle(category.getTitle());
			dao.updateCategory(category2);
			//category2 = dao.getCategoryById(category2.getCategoryId());
			dao.deleteCategory(category2);
			assertTrue(category.equals(category2));
		} catch (SQLException e){
			e.printStackTrace();
		} finally {
			if(category2 != null){
				try {
					dao.deleteCategory(category2);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void getFeedsForCategory(){
		try {
			User user = DAOfactory.getInstance().getUserDAO().getUserByLogin(TEST_LOGIN);
			Category category = ((List<Category>) DAOfactory.getInstance().getCategoryDAO().getCategoriesByUser(user)).get(0);
			List<Feed> feeds = (List<Feed>) DAOfactory.getInstance().getFeedDAO().getFeedsByCategory(category);
			//assertTrue(feeds.size() == 3);
			Feed feed = feeds.get(0);
			assertTrue(feed.getFeedId() == 1);
			assertTrue(feed.getTitle().equals(TEST_FEED));
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void getFeedByUrl(){
		try {
			Feed feed = (Feed) DAOfactory.getInstance().getFeedDAO().getFeedByUrl(TEST_FEED_URL);
			assertTrue(feed.getTitle().equals(TEST_FEED));
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void addFeed(){
		clean();
		Feed feed = new Feed();
		FeedDAO dao = DAOfactory.getInstance().getFeedDAO();
		try {
			feed.setTitle(TEST_FEED_GENERATED);
			feed.setType((byte) 0);
			feed.setUrl("http://habrahahabr.ru/rss");
			feed.setDescription("test");
			feed.setName(TEST_FEED_GENERATED);
			feed.setLanguage((byte) 0);
			//feed.setCategoryId(666L);
			feed.setPubdate(Calendar.getInstance().getTimeInMillis());
			feed.setImageUrl("fsdfsdfsd");
			dao.addFeed(feed);
			
			//get writed to db
			Feed feed2 = ((List<Feed>) dao.getFeedByTitle(TEST_FEED_GENERATED)).get(0);
			assertTrue(feed.equals(feed2));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void updateFeed(){
		clean();
		Feed feed = new Feed();
		FeedDAO dao = DAOfactory.getInstance().getFeedDAO(); 
		try {
			feed.setTitle(TEST_FEED_GENERATED);
			feed.setType((byte) 0);
			feed.setUrl("http://habrahahabr.ru/rss2");
			feed.setDescription("test");
			feed.setName(TEST_FEED_GENERATED);
			feed.setLanguage((byte) 0);
			//feed.setCategoryId(666L);
			dao.addFeed(feed);
			
			//get writed to db
			feed.setDescription(DUMMY);
			Feed feed2 = ((List<Feed>) dao.getFeedByTitle(TEST_FEED_GENERATED)).get(0);
			feed2.setDescription(feed.getDescription());
			dao.updateFeed(feed2);
			feed2 = ((List<Feed>) dao.getFeedByTitle(TEST_FEED_GENERATED)).get(0);
			dao.deleteFeed(feed2);
			assertTrue(feed.equals(feed2));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getDisctinctFeeds(){
		FeedDAO dao = DAOfactory.getInstance().getFeedDAO();
		try {
			List<String> result = (List<String>) dao.getAllDistinctFeeds((byte) 0);
			assertTrue(result.size() > 0);
			result.forEach(feed -> System.out.println(feed));
			result = (List<String>) dao.getAllDistinctFeeds((byte) 1);
			assertEquals(result.size(), 0);
			result.forEach(feed -> System.out.println(feed));
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
		
	/*@Test
	public void getItemByGuid(){
		try {
			FeedItem feedItem = (FeedItem) DAOfactory.getInstance().getFeedItemDAO().getFeedItemByGuid(TEST_GUID);
			assertTrue(feedItem != null);
			assertTrue(feedItem.getTitle().equals(TEST_ITEM_TITLE));
		} catch (SQLException e){
			e.printStackTrace();
		}
	}*/
	
	@SuppressWarnings("unchecked")
	@Test
	public void addRSSItem(){
		clean();
		FeedItem item = new FeedItem();
		FeedItemDAO dao =  DAOfactory.getInstance().getFeedItemDAO();
		try {
			//write to Db
			item.setTitle(TEST_ITEM_TITLE_GENERATED);
			item.setGuid("hop-hey-lalalei");
			item.setLink("http://www.habrahabr.ru");
			item.setPubdate(Calendar.getInstance().getTimeInMillis());
			item.setAuthor("Sergey Maksimenko");
			item.setFeedId(666L);
			item.setReadstatus((byte) 0);
			item.setDescription("hnuh98j984jf89jf894j9k09kf900fn3f309f09kfqffj3qjf09j09jfq3f3");
			dao.addFeedItem(item);
			
			//get writed to db
			FeedItem item2 = ((List<FeedItem>) dao.getFeedItemByTitle(TEST_ITEM_TITLE_GENERATED)).get(0);
			dao.deleteFeedItem(item2);
			
			System.out.println(item);
			System.out.println(item2);
			assertTrue(item.equals(item2));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void updateRSSItem(){
		clean();
		FeedItemDAO dao =  DAOfactory.getInstance().getFeedItemDAO();
		FeedItem item = new FeedItem(), item2 = null;
		try {
			//write to Db
			item.setTitle(TEST_ITEM_TITLE_GENERATED);
			item.setGuid("hop-hey-lalalei");
			item.setLink("http://www.habrahabr.ru");
			item.setPubdate(Calendar.getInstance().getTimeInMillis());
			item.setAuthor("Sergey Maksimenko");
			item.setFeedId(666L);
			item.setReadstatus((byte) 0);
			item.setDescription("hnuh98j984jf89jf894j9k09kf900fn3f309f09kfqffj3qjf09j09jfq3f3");
			dao.addFeedItem(item);
			item.setTitle(DUMMY);
			
			//get writed to db
			item2 =  ((List<FeedItem>) dao.getFeedItemByTitle(TEST_ITEM_TITLE_GENERATED)).get(0);
			item2.setTitle(item.getTitle());
			dao.updateFeedItem(item2);
			//item2 = DAOfactory.getInstance().getFeedItemDAO().getFeedItemById(item2.getFeedId());
			dao.deleteFeedItem(item2);
			
			assertTrue(item.equals(item2));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(item2 != null){
				try {
					dao.deleteFeedItem(item2);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	@Test
	public void deleteOldItems(){
		Long currentTime = Calendar.getInstance().getTimeInMillis(),
				threshold = currentTime - 2 * 3600 * 1000,
				beforeCount = 0L, afterCount = 0L;
		Session session = null;
		try {
			try {
				session = HibernateUtil.getSessionFactory().getCurrentSession();
				beforeCount = (Long) session.createCriteria("FeedItem").setProjection(Projections.rowCount()).uniqueResult();
			} catch (Exception e) {
				System.out.println("Cannot get feedItem by id: " + e);
			}
			DAOfactory.getInstance().getFeedItemDAO().deleteOldItems(threshold);
			try {
				session = HibernateUtil.getSessionFactory().getCurrentSession();
				afterCount = (Long) session.createCriteria("FeedItem").setProjection(Projections.rowCount()).uniqueResult();
				assertTrue(afterCount < beforeCount);
			} catch (Exception e) {
				System.out.println("Cannot get feedItem by id: " + e);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
