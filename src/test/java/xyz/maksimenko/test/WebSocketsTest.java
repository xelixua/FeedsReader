package xyz.maksimenko.test;

import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.spi.JsonProvider;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@ClientEndpoint
public class WebSocketsTest extends Assert implements Runnable{
	
	private Tomcat mTomcat;
	private Thread serverThread;
	//private String mWorkingDir = System.getProperty("java.io.tmpdir");
	
	private final String WSURI = "ws://localhost:9999/feeds/";
	private final String CONTEXT_PATH = "/feeds";
	private final String TEST_CATEGORY_NAME = "IT";
	private Session session;
	private String receivedMessage;
	
	@Before
	public void setupAndStartServer() throws Throwable {
		mTomcat = new Tomcat();
		mTomcat.setPort(9999);
		mTomcat.addWebapp(CONTEXT_PATH, new File("src/main/webapp").getAbsolutePath());
		serverThread = new Thread(this);
		serverThread.start();
		connectToServer();
	}
	
	
	@After
	public void stopServer() {
		try {
			mTomcat.stop();
			mTomcat.destroy();
			deleteDirectory(new File("target/tomcat/"));
		} catch (LifecycleException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void deleteDirectory(File path) {
		if(path == null) return;
		if(path.exists()) {
			Arrays.asList(path.listFiles()).stream().forEach(file -> { if(file.isDirectory()) deleteDirectory(file); file.delete(); });
			path.delete();
		}
	}
	
	private void connectToServer(){
		try {
			WebSocketContainer container = ContainerProvider.getWebSocketContainer();
			container.connectToServer(this, new URI(WSURI));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@OnOpen
	private void onOpen(Session session){
		this.session = session;
	}
	
	@OnMessage
	private void onMessage(String message){
		receivedMessage = message;
	}
	
	@OnClose
	private void onClose(Session session){
		
	}

	@Test
	public void login(){
		boolean timeout = false;
		final short timeoutInterval = 32767;
		short counter = 0;
		JsonProvider provider = JsonProvider.provider();
		JsonObject json = provider.createObjectBuilder()
				.add("action", "login")
				.add("login", "testuser01@mail.ru")
				.build();
		session.getAsyncRemote().sendText(json.toString());
		while(receivedMessage == null){
			if(++counter == timeoutInterval) {
				timeout = true; 
				break;
			}
		}
		
		assertFalse(timeout);
		
		try (JsonReader reader = Json.createReader(new StringReader(receivedMessage))){
			JsonObject jsonMessage = reader.readObject();
			String action = jsonMessage.getString("action");
			assertTrue(action.equals("categories"));
			
			JsonArray categoriesJson = jsonMessage.getJsonArray("set");
			JsonObject firstCategory = categoriesJson.getJsonObject(0);
			assertTrue(firstCategory.getString("title").equals(TEST_CATEGORY_NAME));
		}
	}
	
	@Override
	public void run() {
		try {
			mTomcat.start();
		} catch (LifecycleException e){
			throw new RuntimeException(e);
		}
		mTomcat.getServer().await();
	}

}
