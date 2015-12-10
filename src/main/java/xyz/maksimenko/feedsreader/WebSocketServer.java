package xyz.maksimenko.feedsreader;

import javax.websocket.server.ServerEndpoint;

import org.jboss.logging.Logger;

import xyz.maksimenko.feedsreader.feedobject.Feed;
import xyz.maksimenko.util.DAOfactory;

import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.OnMessage;
import javax.websocket.OnError;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Level;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.spi.JsonProvider;
import javax.json.JsonArray;

@ApplicationScoped
@ServerEndpoint("/feeds")
public class WebSocketServer {
	
	
	public WebSocketServer() {
		System.out.println("New web socket server");
	}
	
	@Inject
	private WSSessionHandler sessionHandler;// =  new WSSessionHandler();
	
	@OnOpen
	public void open(Session session){
		System.out.println("User connected " + session.getRequestURI() + " " + session.getId());
		//sessionHandler.addSession(session);

	}
	
	@OnMessage
	public void handleMessage(String message, Session session){
		try (JsonReader reader = Json.createReader(new StringReader(message))){
			System.out.println(message);
			JsonObject jsonMessage = reader.readObject();
			
			String action = jsonMessage.getString("action");
			switch(action) {
			case "register":
				System.out.println("register ws received");
				String login = jsonMessage.getString("login");
				String name = jsonMessage.getString("name");
				String passwordHash = jsonMessage.getString("password");
				//TODO validate register input
				sessionHandler.userRegistering(session, login, name, passwordHash);
				break;
			case "login":
				System.out.println("login ws received");
				login = jsonMessage.getString("login");
				passwordHash = jsonMessage.getString("password");
				//TODO validate login input
				try {
					sessionHandler.userLoggedIn(session, login, passwordHash);
				} catch (SecurityException e) {
					//user entered wrong password
					JsonProvider provider = JsonProvider.provider();
					JsonObject json = provider.createObjectBuilder()
							.add("action", "wrongCredentials")
							.build();
					sendToSession(session, json);
				}
				break;
			case "feeds": //get feeds for category
				System.out.println("feeds ws received");
				String categoryName = jsonMessage.getString("category");
				sessionHandler.feedsRequested(session, categoryName);
				break;
			case "items": //get items for feed
				System.out.println("items ws received");
				String feedName = jsonMessage.getString("feed");
				sessionHandler.itemsRequested(session, feedName);
				break;
			case "all_items": //get items for all feeds
				System.out.println("all items ws received");
				sessionHandler.allItemsRequested(session);
				break;
			case "add_feed":
				System.out.println("User adding a feed");
				String feedUrl = jsonMessage.getString("feedurl"),
						categoryToAdd = jsonMessage.getString("categorytoadd");
				sessionHandler.addFeedRequested(session, feedUrl, categoryToAdd);
				break;
			case "remove_feeds":
				System.out.println("User removing feeds");
				JsonArray feedsToRemove = jsonMessage.getJsonArray("feedstoremove");
				sessionHandler.removeFeedsRequested(session, feedsToRemove);
				break;
			case "add_category":
				System.out.println("User adding category");
				categoryName = jsonMessage.getString("categoryName");
				sessionHandler.addCategoryRequested(session, categoryName);
				break;
			case "remove_categories":
				System.out.println("User removing categories");
				JsonArray categoriesToRemove = jsonMessage.getJsonArray("categoriestoremove");
				sessionHandler.removeCategoriesRequested(session, categoriesToRemove);
				break;
			case "sign-out":
				System.out.println("User logging out");
				sessionHandler.userLoggingOut(session);
				break;
			default:
				break;
			}
		} catch (NullPointerException e) {
			//TODO handle user requests with old session
		}
	}
	
	@OnClose
	public void close(Session session){
		//sessionHandler.removeSession(session);
		System.out.println("User disconnected " + session.getRequestURI() + " " + session.getId());
	}
	
	@OnError
	public void onError(Throwable error){
		//Logger.getLogger(DeviceWebSocketServer.class.getName()).log(Level.SEVERE, null, error);
		error.printStackTrace();
	}
	
	public static void sendToSession(Session session, JsonObject message) {
		try {
			session.getBasicRemote().sendText(message.toString());
		} catch (IOException ex){
			
			Logger.getLogger(WSSessionHandler.class.getName()).log(Logger.Level.ERROR, null, ex);
			
		}
	}
	
}
