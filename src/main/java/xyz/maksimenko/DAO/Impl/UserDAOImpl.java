package xyz.maksimenko.DAO.Impl;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import xyz.maksimenko.DAO.UserDAO;
import xyz.maksimenko.feedsreader.User;
import xyz.maksimenko.feedsreader.feedobject.Feed;
import xyz.maksimenko.util.HibernateUtil;

public class UserDAOImpl implements UserDAO {

	@Override
	public void addUser(User user) throws SQLException {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.save("xyz.maksimenko.feedsreader.User", user);
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Cannot add user " + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}
	}

	@Override
	public void updateUser(User user) throws SQLException {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.update(user);
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Cannot update user " + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}

	}

	@Override
	public void deleteUser(User user) throws SQLException {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.delete(user);
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Cannot delete user " + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}

	}

	@Override
	public User getUserById(Long userId) throws SQLException {
		Session session = null;
		User user = null;
		
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			user = session.load(User.class, userId);
		} catch (Exception e){
			System.out.println("Cannot get user  by id" + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}
		
		return user;
	}

	@Override
	public User getUserByLogin(String login) throws SQLException {
		Session session = null;
		User user = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			Query query = session.createQuery("from User where login = :login").setString("login", login);
			user = (User) query.uniqueResult();
			session.getTransaction().commit();
		} catch (Exception e){
			System.out.println("Cannot get user by login " + e);
		} finally {
			if(session != null && session.isOpen()){
				session.close();
			}
		}
		return user;
	}

}
