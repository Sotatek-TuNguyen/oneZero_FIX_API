package quickfix.test;

import quickfix.SessionID;
import quickfix.field.Symbol;

import java.util.ArrayList;

public class QuoteInitiatorTest {
	public static void main (String[] args) throws Exception {
		QuoteInitiator quoteInitiator = new QuoteInitiator();
		ArrayList<SessionID> sessionIDs = null;
		SessionID firstSessionID = null;
		ArrayList<Symbol> symbols = new ArrayList<Symbol>();
			
		symbols.add(new Symbol("HKD"));
		
		quoteInitiator.logon();
		
		sessionIDs = quoteInitiator.getSessionIDs();
		
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			e.getStackTrace();
		}
		
		if (sessionIDs != null)
			firstSessionID = sessionIDs.get(0);
		
		if (firstSessionID != null) {
			quoteInitiator.sendMarketDataRequest(firstSessionID, symbols);
		
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			e.getStackTrace();
		}
		
		quoteInitiator.sendQuoteCancel(firstSessionID);
		
		quoteInitiator.logout(firstSessionID);
		}
	}
}