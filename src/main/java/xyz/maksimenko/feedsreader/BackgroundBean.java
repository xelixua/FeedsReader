/**
 * Singleton bean for various background tasks
 */
package xyz.maksimenko.feedsreader;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
public class BackgroundBean {
	@EJB
	private FeedProcessor feedP;

	/**
	 * Updates all feeds at startup 
	 */
	@PostConstruct
	public void init(){
		System.setProperty("http.agent", "");
		feedP.updateFeeds();
	}	
}
