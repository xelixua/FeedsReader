package xyz.maksimenko.DAO;

import java.sql.SQLException;
import java.util.Collection;

import xyz.maksimenko.feedsreader.User;
import xyz.maksimenko.feedsreader.feedobject.Category;
import xyz.maksimenko.feedsreader.feedobject.Feed;

public interface CategoryDAO {
	public void addCategory(Category category) throws SQLException;
	public void updateCategory(Category category) throws SQLException;
	public void deleteCategory(Category category) throws SQLException;
	public Category getCategoryById(Long categoryId) throws SQLException;
	public Collection getCategoryByTitle(String title) throws SQLException;
	public Collection getCategoriesByUser(User user) throws SQLException;
}
