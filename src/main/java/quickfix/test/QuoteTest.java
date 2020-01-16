package quickfix.test;

public class QuoteTest {
	public static void main (String[] args) throws Exception {
		Quote quoteInitiator = new Quote();
		
		quoteInitiator.logon();
		
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			e.getStackTrace();
		}
		
		quoteInitiator.sendMarketDataRequest();
		
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			e.getStackTrace();
		}
		
		quoteInitiator.sendQuoteCancel();
		
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			e.getStackTrace();
		}
		
		quoteInitiator.logout();
	}
}