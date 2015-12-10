package xyz.maksimenko.DAO;

import java.sql.SQLException;

import xyz.maksimenko.feedsreader.User;

public interface UserDAO {
	public void addUser(User user) throws SQLException;
	public void updateUser(User user) throws SQLException;
	public void deleteUser(User user) throws SQLException;
	public User getUserByLogin(String login) throws SQLException;
	public User getUserById(Long userId) throws SQLException;
}
