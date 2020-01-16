package quickfix.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.*;
import quickfix.Session;
import quickfix.ScreenLogFactory;
import quickfix.field.Account;
import quickfix.field.SenderSubID;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.Symbol;
import quickfix.field.Side;
import quickfix.field.OrderQty;
import quickfix.field.Side;
import quickfix.field.OrderQty;
import quickfix.field.OrdType;
import quickfix.field.Price;
import quickfix.field.Text;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;
import quickfix.field.QuoteID;
import quickfix.field.XmlDataLen;
import quickfix.field.XmlData;
import quickfix.fix44.TestRequest;
import quickfix.fix44.NewOrderSingle;

import java.io.FileInputStream;
import java.util.Date;

public class Trading {
	
	private static final Logger log = LoggerFactory.getLogger(Trading.class);
	private boolean initiatorStarted = false;
	private Initiator initiator = null;
	
	public Trading() throws Exception {
		
		String fileName = "./src/main/resources/config/trading.cfg";
		
		SessionSettings settings = new SessionSettings(new FileInputStream(fileName));
		// Manually get some fields that quickfix-j doesn't support
		String password = settings.getString("Password");
		
		Application application = new BaseInitiator(password);
		MessageStoreFactory storeFactory = new FileStoreFactory(settings);
		ScreenLogFactory screenLogFactory = new ScreenLogFactory(settings);
	    MessageFactory messageFactory = new DefaultMessageFactory();
		
	    initiator = new SocketInitiator(application, storeFactory, settings, screenLogFactory, messageFactory);
	    
	}
	
	public void logon() {
		if(!initiatorStarted) {
			try {
				System.out.println("Inside logon");
				initiator.start();
				initiatorStarted = true;
			    try {
			    	Thread.sleep(1000);
			    } catch (Exception e) {
			    	e.getStackTrace();
			    }
			} catch (Exception e) {
				log.error("Logon failed");
			}
		} else {
			for (SessionID sessionID: initiator.getSessions()) {
				Session.lookupSession(sessionID).logon();
			}
		}
	}
	
	public void logout() {
		for (SessionID sessionID: initiator.getSessions()) {
			Session.lookupSession(sessionID).logout("User requested");
		}
	}
	
	public void sendTestRequest() {
		TestRequest testRequest = new TestRequest();
		
		for (SessionID sessionID: initiator.getSessions()) {
			System.out.println("In test Request()");
			Session.lookupSession(sessionID).send(testRequest);
		}
	}
	
	public void sendNewOrderSingle() {
		
	}
	
}