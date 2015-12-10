package xyz.maksimenko.DAO.Impl;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import xyz.maksimenko.DAO.FeedItemDAO;
import xyz.maksimenko.feedsreader.feedobject.Feed;
import xyz.maksimenko.feedsreader.feedobject.FeedItem;
import xyz.maksimenko.util.HibernateUtil;

public class FeedItemDAOImpl implements FeedItemDAO{

	@Override
	public void addFeedItem(FeedItem feedItem) throws SQLException {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.save("xyz.maksimenko.feedsreader.feedobject.FeedItem", feedItem);
			session.getTransaction().commit();
		} catch (Exception e) {
			System.out.println("Error while adding feedItem " + e);
		} finally {
			if(session != null && session.isOpen()) session.close();
		}
		
	}

	@Override
	public void updateFeedItem(FeedItem feedItem) throws SQLException {
		Session session = null;
		try{
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			String guid = feedItem.getGuid();
			Long itemId = getFeedItemByGuid(guid).getItemId();
			feedItem.setItemId(itemId);
			session.update("xyz.maksimenko.feedsreader.feedobject.FeedItem", feedItem);
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Error while updating feedItem " + e);
		} finally {
			if(session != null && session.isOpen()) session.close();
		}
		
	}

	@Override
	public FeedItem getFeedItemById(Long itemId) throws SQLException {
		Session session = null;
		FeedItem feedItem = null;
		try{
			session = HibernateUtil.getSessionFactory().openSession();
			feedItem = (FeedItem) session.load(FeedItem.class, itemId);
		} catch (Exception e){
			System.out.println("Error while finding by id " + e);
		} finally {
			if(session != null && session.isOpen()) session.close();
		}
		return feedItem;
	}

	@Override
	public Collection getAllFeedItems() throws SQLException {
		Session session = null;
		List feedItems = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			feedItems = session.createCriteria(FeedItem.class).list();
		} catch (Exception e){
			System.out.println("Error while getting all " + e);
		} finally {
			if(session != null && session.isOpen()) session.close();
		}
		return feedItems;
	}

	@Override
	public void deleteFeedItem(FeedItem feedItem) throws SQLException {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.delete(feedItem);
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Error while addind feedItem " + e);
		} finally {
			if(session != null && session.isOpen()) session.close();
		}
	}

	@Override
	public Collection getFeedItemsByFeed(Feed feed) throws SQLException {
		Session session = null;
		List feedItems = null;
		try{
			session = HibernateUtil.getSessionFactory().getCurrentSession();
			session.beginTransaction();
			Long feedId = feed.getFeedId();
			Query query = session.createQuery("from FeedItem where feedId = :feedId").setLong("feedId", feedId);
			feedItems = (List<FeedItem>) query.list();
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Error while addind feedItem " + e);
		} finally {
			if(session != null && session.isOpen()) session.close();
		}
		return feedItems;
	}

	@Override
	public Collection getFeedItemByTitle(String title) throws SQLException {
		Session session = null;
		Collection feedItems = null;
		try {
			session = HibernateUtil.getSessionFactory().getCurrentSession();
			session.beginTransaction();
			feedItems = (List<FeedItem>) session.createCriteria(FeedItem.class).add(Restrictions.eq("title", title)).list();
			session.getTransaction().commit();
			System.out.println(feedItems.size());
		} catch (Exception e) {
			System.out.println("Cannot get feedItem by id: " + e);
		}
		return feedItems;
	}

	@Override
	public FeedItem getFeedItemByGuid(String guid) throws SQLException {
		Session session = null;
		FeedItem feedItem = null;
		try {
			session = HibernateUtil.getSessionFactory().getCurrentSession();
			session.beginTransaction();
			feedItem = (FeedItem) session.createQuery("from FeedItem where guid = :guid").setString("guid", guid).uniqueResult();
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Cannot get feedItem by guid: " + e);
		}
		return feedItem;
	}

	@Override
	public void deleteOldItems(Long threshold) throws SQLException {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			Query query = session.createQuery("delete FeedItem where pubdate <= :threshold").setLong("threshold", threshold);
			query.executeUpdate();
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Error while addind feedItem " + e);
		} finally {
			if(session != null && session.isOpen()) session.close();
		}
		
	}
	
}
