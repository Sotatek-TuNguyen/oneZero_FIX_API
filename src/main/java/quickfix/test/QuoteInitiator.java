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
import quickfix.fix44.QuoteCancel.NoQuoteEntries;

import java.io.FileInputStream;
import java.util.Date;
import java.util.ArrayList;

public class QuoteInitiator {
	
	private static final Logger log = LoggerFactory.getLogger(QuoteInitiator.class);
	private boolean initiatorStarted = false;
	private Initiator initiator = null;
	private static int nextID = 1;
	
	public QuoteInitiator(String configFile) throws Exception {
		SessionSettings settings = new SessionSettings(new FileInputStream(configFile));
		// Manually get some fields that quickfix-j doesn't support
		String password = settings.getString("Password");
		
		Application application = new BaseFixEngine(password);
		MessageStoreFactory storeFactory = new FileStoreFactory(settings);
		ScreenLogFactory screenLogFactory = new ScreenLogFactory(settings);
	    MessageFactory messageFactory = new DefaultMessageFactory();
		
	    initiator = new SocketInitiator(application, storeFactory, settings, screenLogFactory, messageFactory);
	   
	}
	
	/**
	 * Logon message sent by client to initiate a FIX session
	 */
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
	
	/**
	 * A logout message is sent by oneZero to the client, or the client to oneZero,
	 * to terminate a FIX session
	 * 
	 * @param sessionID
	 */
	public void logout(SessionID sessionID) {
		Session.lookupSession(sessionID).logout("logout requested");
	}
	
	/**
	 * A Test Request message is sent by oneZero to the client, or the client to
	 * oneZero as a means of verifying two-way FIX connectivity
	 * 
	 * @param sessionID
	 */
	public void sendTestRequest(SessionID sessionID) {
		TestRequest testRequest = new TestRequest();
		
		Session.lookupSession(sessionID).send(testRequest);	
	}
	
	/**
	 * subscribe to streaming quote updates from oneZero
	 * 
	 * @param sessionID
	 * @param symbols
	 */
	public void sendMarketDataRequest(SessionID sessionID, ArrayList<Symbol> symbols) {
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
	
	/**
	 * Cancel streaming quote updates all quotes
	 * 
	 * @param sessionID
	 */
	public void sendQuoteCancel(SessionID sessionID) {
			String quoteID = Long.toString(System.currentTimeMillis() + (nextID++));
			
			QuoteCancel quoteCancel = new QuoteCancel(new QuoteID(quoteID),
					new QuoteCancelType(QuoteCancelType.CANCEL_ALL_QUOTES));
			
			Session.lookupSession(sessionID).send(quoteCancel);
	}
	
	/**
	 * Cancel streaming quote updates for one symbol
	 * 
	 * @param sessionID
	 */
	public void sendQuoteCancel(SessionID sessionID, Symbol symbol) {
			String quoteID = Long.toString(System.currentTimeMillis() + (nextID++));
			
			QuoteCancel quoteCancel = new QuoteCancel(new QuoteID(quoteID),
					new QuoteCancelType(QuoteCancelType.CANCEL_FOR_SYMBOL));
			
			NoQuoteEntries noQuoteEntries = new NoQuoteEntries();
			noQuoteEntries.set(symbol);
			
			quoteCancel.addGroup(noQuoteEntries);
			Session.lookupSession(sessionID).send(quoteCancel);
	}
	
	/**
	 * Get all sessionIDs of current Initiator
	 * 
	 * @return ArrayList<SessionID>
	 */
	public ArrayList<SessionID> getSessionIDs() {
		if(initiatorStarted) {
			return initiator.getSessions();	
		} else {
			return null;
		}
	}
	
}