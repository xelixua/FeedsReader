package xyz.maksimenko.util;

import xyz.maksimenko.DAO.*;
import xyz.maksimenko.DAO.Impl.*;

public class DAOfactory {
	private static FeedItemDAO feedItemDAO = null;
	private static FeedDAO feedDAO = null;
	private static CategoryDAO categoryDAO = null;
	private static UserDAO userDAO = null;
	private static DAOfactory instance = null;
	
	public static synchronized DAOfactory getInstance(){
		if(instance == null){
			instance = new DAOfactory();
		}
		
		return instance;
	}
	
	public FeedItemDAO getFeedItemDAO(){
		if(feedItemDAO == null){
			feedItemDAO = new FeedItemDAOImpl();
		}
		
		return feedItemDAO;
	}
	
	public FeedDAO getFeedDAO(){
		if(feedDAO == null){
			feedDAO = new FeedDAOImpl();
		}
		
		return feedDAO;
	}
	
	public CategoryDAO getCategoryDAO(){
		if(categoryDAO == null){
			categoryDAO = new CategoryDAOImpl();
		}
		
		return categoryDAO;
	}
	
	public UserDAO getUserDAO(){
		if(userDAO == null){
			userDAO = new UserDAOImpl();
		}
		
		return userDAO;
	}
	
	
}
