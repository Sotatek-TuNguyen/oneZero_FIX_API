package quickfix.test;

import quickfix.SessionID;
import quickfix.Session;

import java.util.ArrayList;

public class TradingInitiatorTest {
	public static void main (String[] args) throws Exception {
		TradingInitiator tradingInitiator = new TradingInitiator();
		ArrayList<SessionID> sessionIDs = null;
		SessionID firstSessionID = null;
		
		tradingInitiator.logon();
				
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			e.getStackTrace();
		}
		
		// Test sending order
		sessionIDs = tradingInitiator.getSessionIDs();
		if(sessionIDs != null) {
			firstSessionID = sessionIDs.get(0);

			Order order = new Order();
			order.setSessionID(firstSessionID);
			order.setQuantity(5);
			order.setSymbol("CADJPY");
			order.setOriginalID(order.getID());
			
			tradingInitiator.sendNewOrderSingle(order);
		}
					
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			e.getStackTrace();
		}
		
		tradingInitiator.logout();
	}
}