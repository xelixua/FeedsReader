/**
 * Singleton bean for various background tasks
 */
package xyz.maksimenko.feedsreader;

import java.sql.SQLException;
import java.util.Calendar;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import xyz.maksimenko.util.DAOfactory;

@Startup
@Singleton
public class BackgroundBean {
	private final byte OLD_THRESHOLD = 5; //hours
	@EJB
	private FeedProcessor feedP;

	/**
	 * Updates all feeds at startup 
	 */
	@PostConstruct
	public void init(){
		System.setProperty("http.agent", "");
		//feedP.updateFeeds();
	}
	
	@Schedule(second="0", minute="*/10", hour="*")
	private void deleteOldItems(){
		System.out.println("Deleting old feedItems");
		Long currentTime = Calendar.getInstance().getTimeInMillis(),
				threshold = currentTime - OLD_THRESHOLD * 3600 * 1000;
		try {
			DAOfactory.getInstance().getFeedItemDAO().deleteOldItems(threshold);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
