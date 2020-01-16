package quickfix.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.*;
import quickfix.Session;
import quickfix.ScreenLogFactory;
import quickfix.field.Symbol;
import quickfix.field.MDReqID;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.MarketDepth;
import quickfix.field.MDUpdateType;
import quickfix.field.MDEntryType;
import quickfix.field.QuoteID;
import quickfix.field.QuoteCancelType;
import quickfix.fix44.TestRequest;
import quickfix.fix44.QuoteCancel;
import quickfix.fix44.MarketDataRequest;
import quickfix.fix44.MarketDataRequest.NoMDEntryTypes;
import quickfix.fix44.MarketDataRequest.NoRelatedSym;

import java.io.FileInputStream;
import java.util.Date;

public class Quote {
	
	private static final Logger log = LoggerFactory.getLogger(Quote.class);
	private boolean initiatorStarted = false;
	private Initiator initiator = null;
	
	public Quote() throws Exception {
		
		String fileName = "./src/main/resources/config/quote.cfg";
		
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
	
	public void sendMarketDataRequest() {
		for (SessionID sessionID: initiator.getSessions()) {
			
			Date date = new Date();
			long time = date.getTime();
			String mdReqID = sessionID.toString() + time;
			
 			MarketDataRequest marketDataRequest = new MarketDataRequest(
					new MDReqID(mdReqID),
					new SubscriptionRequestType('1'),
					new MarketDepth(1));
			
			marketDataRequest.set(new MDUpdateType(0));
			
			NoMDEntryTypes mdEntryTypeGroup = new NoMDEntryTypes();
			mdEntryTypeGroup.set(new MDEntryType('0'));
			marketDataRequest.addGroup(mdEntryTypeGroup);
			mdEntryTypeGroup.set(new MDEntryType('1'));
			marketDataRequest.addGroup(mdEntryTypeGroup);
			
			NoRelatedSym relatedSymGroup = new NoRelatedSym();
			relatedSymGroup.set(new Symbol("GOOG"));
			marketDataRequest.addGroup(relatedSymGroup);
					
			Session.lookupSession(sessionID).send(marketDataRequest);
		}
	}
	
	public void sendQuoteCancel() {
		for (SessionID sessionID: initiator.getSessions()) {
			Date date = new Date();
			long time = date.getTime();
			String cancelQuoteMsg = "Cancel All Quotes at " + time + " session: " + sessionID.toString();
			
			QuoteCancel quoteCancel = new QuoteCancel(new QuoteID(cancelQuoteMsg),
					new QuoteCancelType(4));
			
			Session.lookupSession(sessionID).send(quoteCancel);
		}
	}
	
	
}