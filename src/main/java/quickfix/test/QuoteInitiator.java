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
import java.util.ArrayList;

public class QuoteInitiator {
	
	private static final Logger log = LoggerFactory.getLogger(QuoteInitiator.class);
	private boolean initiatorStarted = false;
	private Initiator initiator = null;
	private static int nextID = 1;
	
	public QuoteInitiator() throws Exception {
		
		String fileName = "./src/main/resources/config/quote.cfg";
		
		SessionSettings settings = new SessionSettings(new FileInputStream(fileName));
		// Manually get some fields that quickfix-j doesn't support
		String password = settings.getString("Password");
		
		Application application = new BaseFixEngine(password);
		MessageStoreFactory storeFactory = new FileStoreFactory(settings);
		ScreenLogFactory screenLogFactory = new ScreenLogFactory(settings);
	    MessageFactory messageFactory = new DefaultMessageFactory();
		
	    initiator = new SocketInitiator(application, storeFactory, settings, screenLogFactory, messageFactory);
	    
	}
	
	public void logon() {
		if(!initiatorStarted) {
			try {
				initiator.start();
				initiatorStarted = true;
			} catch (Exception e) {
				log.error("Logon failed");
			}
		} else {
			for (SessionID sessionID: initiator.getSessions()) {
				Session.lookupSession(sessionID).logon();
			}
		}
	}
	
	public void logout(SessionID sessionID) {
		Session.lookupSession(sessionID).logout("User requested");
	}
	
	public void sendTestRequest(SessionID sessionID) {
		TestRequest testRequest = new TestRequest();
		
		Session.lookupSession(sessionID).send(testRequest);	
	}
	
	public void sendMarketDataRequest(SessionID sessionID, ArrayList<Symbol> symbols) {
			Date date = new Date();
			String mdReqID = Long.toString(System.currentTimeMillis() + (nextID++));
			
 			MarketDataRequest marketDataRequest = new MarketDataRequest(
					new MDReqID(mdReqID),
					new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES),
					new MarketDepth(1));
			
			marketDataRequest.set(new MDUpdateType(MDUpdateType.FULL_REFRESH));
			
			NoMDEntryTypes mdEntryTypeGroup = new NoMDEntryTypes();
			mdEntryTypeGroup.set(new MDEntryType(MDEntryType.BID));
			marketDataRequest.addGroup(mdEntryTypeGroup);
			mdEntryTypeGroup.set(new MDEntryType(MDEntryType.OFFER));
			marketDataRequest.addGroup(mdEntryTypeGroup);
			
			NoRelatedSym relatedSymGroup = new NoRelatedSym();
			for(Symbol symbol: symbols) {
				relatedSymGroup.set(symbol);
				marketDataRequest.addGroup(relatedSymGroup);	
			}
			
			Session.lookupSession(sessionID).send(marketDataRequest);
	}
	
	public void sendQuoteCancel(SessionID sessionID) {
			Date date = new Date();
			String cancelQuoteMsg = "Cancel All Quotes at " + date.getTime() + ", session: " + sessionID.toString();
			
			QuoteCancel quoteCancel = new QuoteCancel(new QuoteID(cancelQuoteMsg),
					new QuoteCancelType(4));
			
			Session.lookupSession(sessionID).send(quoteCancel);
	}
	
	public ArrayList<SessionID> getSessionIDs() {
		if(initiatorStarted) {
			return initiator.getSessions();	
		} else {
			return null;
		}
	}
	
}