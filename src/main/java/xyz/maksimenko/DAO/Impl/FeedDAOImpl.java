package xyz.maksimenko.DAO.Impl;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.exception.ConstraintViolationException;

import xyz.maksimenko.DAO.FeedDAO;
import xyz.maksimenko.DAO.FeedItemDAO;
import xyz.maksimenko.feedsreader.feedobject.Category;
import xyz.maksimenko.feedsreader.feedobject.Feed;
import xyz.maksimenko.feedsreader.feedobject.FeedItem;
import xyz.maksimenko.util.DAOfactory;
import xyz.maksimenko.util.HibernateUtil;

public class FeedDAOImpl implements FeedDAO {

	@Override
	@SuppressWarnings("unchecked")
	public void addFeed(Feed feed) throws SQLException {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.save("xyz.maksimenko.feedsreader.feedobject.Feed", feed);
			session.getTransaction().commit();	
		} catch (Exception e) {
			System.out.println("Cannot add feed " + e);
			e.printStackTrace();
		}
		finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void updateFeed(Feed feed) throws SQLException {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.update(feed);
			session.getTransaction().commit();
			
		/*} catch (ConstraintViolationException e){
			session.close();
			feed = getFeedByUrl(feed.getUrl());
			DAOfactory.getInstance().getFeedItemDAO().deleteAllItemsForFeed(feed);
			updateFeed(feed);*/
		} catch (Exception e) {
			e.printStackTrace(); 
		}
			finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}
	}

	@Override
	public void deleteFeed(Feed feed) throws SQLException {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.delete(feed);
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Cannot delete feed " + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}

	}

	@Override
	public Feed getFeedById(Long feedId) throws SQLException {
		Session session = null;
		Feed feed = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			feed = (Feed) session.load(Feed.class, feedId);
		} catch (Exception e){
			System.out.println("Cannot get feed by id" + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}
		return feed;
	}

	@Override
	public Collection getAllFeeds() throws SQLException {
		Session session = null;
		List feeds = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			feeds = session.createCriteria(Feed.class).list();
		} catch (Exception e){
			System.out.println("Cannot get all feeds " + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}
		return feeds;
	}
	
	/*@Override
	public Collection getAllDistinctFeeds(byte feedType) throws SQLException {
		Session session = null;
		List feeds = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			//feeds = session.createCriteria(Feed.class).setProjection(
				//	Projections.projectionList().
//					add(Projections.distinct(Projections.property("url"))).
					//add(Projections.property("type"), String.valueOf(feedType))).list();
			Map<String, Byte> wheres = new HashMap<String, Byte>();
			wheres.put("type", feedType);
			feeds = session.createCriteria(Feed.class).add(Restrictions.allEq(wheres)).setProjection(Projections.groupProperty("url")).list();
		} catch (Exception e){
			System.out.println("Cannot get all feeds " + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}
		return feeds;
	}*/

	@SuppressWarnings("unchecked")
	@Override
	public Collection getFeedsByCategory(Category category) throws SQLException {
		Session session = null;
		List feeds = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			Long categoryId = category.getCategoryId();
			//Query query = session.createQuery("from Feed where categoryId = :categoryId").setLong("categoryId", categoryId);
			Query query = session.createQuery("select f from Feed f INNER JOIN f.categories category where category.categoryId = :categoryId").setLong("categoryId", categoryId);
			feeds = (List<Feed>) query.list();
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Cannot get feeds by category " + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}
		return feeds;
	}

	@Override
	public Collection getFeedByTitle(String title) throws SQLException {
		Session session = null;
		List feeds = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			Query query = session.createQuery("from Feed where title = :title").setString("title", title);
			feeds = (List<Feed>) query.list();
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Cannot get feeds by title " + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}
		
		return feeds;
	}

	@Override
	public Feed getFeedByUrl(String url) throws SQLException {
		Session session = null;
		Feed feed = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			Query query = session.createQuery("from Feed where url = :url").setString("url", url);
			feed = (Feed) query.uniqueResult();
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Cannot get feeds by url " + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}
		
		return feed;
	}

}
