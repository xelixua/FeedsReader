package xyz.maksimenko.DAO.Impl;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

import xyz.maksimenko.DAO.CategoryDAO;
import xyz.maksimenko.feedsreader.User;
import xyz.maksimenko.feedsreader.feedobject.Category;
import xyz.maksimenko.feedsreader.feedobject.Feed;
import xyz.maksimenko.util.HibernateUtil;

public class CategoryDAOImpl implements CategoryDAO {

	@Override
	public void addCategory(Category category) throws SQLException {
		Session session = null;
		
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.save("xyz.maksimenko.feedsreader.feedobject.Category", category);
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Cannot add category " + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}
	}

	@Override
	public void updateCategory(Category category) throws SQLException {
		Session session = null;
		
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.update(category);
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Cannot update category " + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}
	}

	@Override
	public void deleteCategory(Category category) throws SQLException {
		Session session = null;
		
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.delete(category);
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Cannot delete category " + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}
	}

	@Override
	public Category getCategoryById(Long categoryId) throws SQLException {
		Session session = null;
		Category category = null;
		
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			category = session.get(Category.class, categoryId); //replace load with get
		} catch (Exception e){
			System.out.println("Cannot get category  by id" + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}
		
		return category;
	}

	@Override
	public List<Category> getCategoriesByUser(User user) throws SQLException {
		Session session = null;
		List categories = null;
		
		try{
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			Long userId = user.getUserId();
			Query query = session.createQuery("from Category where userId = :userId").setLong("userId", userId);
			categories = (List<Collection>) query.list();
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Cannot get categories by user " + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}
		
		return categories;
	}

	@Override
	public Collection getCategoryByTitle(String title) throws SQLException {
		Session session = null;
		List categories = null;
		
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			Query query = session.createQuery("from Category where title = :title").setString("title", title);
			categories = (List<Category>) query.list();
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Cannot get categories by title " + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}
		
		return categories;
	}
}
