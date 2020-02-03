package quickfix.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.*;
import quickfix.Session;
import quickfix.ScreenLogFactory;
import quickfix.Message;
import quickfix.field.Account;
import quickfix.field.SenderSubID;
import quickfix.field.ClOrdID;
import quickfix.field.OrigClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.Symbol;
import quickfix.field.Side;
import quickfix.field.OrderQty;
import quickfix.field.Side;
import quickfix.field.OrderQty;
import quickfix.field.LocateReqd;
import quickfix.field.OrdType;
import quickfix.field.Price;
import quickfix.field.Text;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;
import quickfix.field.QuoteID;
import quickfix.field.XmlDataLen;
import quickfix.field.XmlData;
import quickfix.field.StopPx;
import quickfix.fix44.TestRequest;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelRequest;

import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ArrayList;

public class TradingInitiator {
	
	private static final Logger log = LoggerFactory.getLogger(TradingInitiator.class);
	private boolean initiatorStarted = false;
	private Initiator initiator = null;
	static private final TwoWayMap sideMap = new TwoWayMap();
    static private final TwoWayMap typeMap = new TwoWayMap();
    static private final TwoWayMap tifMap = new TwoWayMap();
	
	public TradingInitiator() throws Exception {
		
		String fileName = "./src/main/resources/config/trading.cfg";
		
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
	
	public void logout() {
		for (SessionID sessionID: initiator.getSessions()) {
			Session.lookupSession(sessionID).logout("ogout requested");
		}
	}
	
	public void sendTestRequest() {
		TestRequest testRequest = new TestRequest();
		
		for (SessionID sessionID: initiator.getSessions()) {
			Session.lookupSession(sessionID).send(testRequest);
		}
	}
	
	public void sendNewOrderSingle(Order order) {
		NewOrderSingle newOrderSingle = new NewOrderSingle(
				new ClOrdID(order.getID()),
				sideToFIXSide(order.getSide()),
				new TransactTime(),
				typeToFIXType(order.getType()));
		
		newOrderSingle.set(new OrderQty(order.getQuantity()));
		newOrderSingle.set(new Symbol(order.getSymbol()));
		newOrderSingle.set(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE));
		newOrderSingle.set(tifToFIXTif(order.getTIF()));
		
		Session.lookupSession(order.getSessionID())
			   .send(populateOrder(order, newOrderSingle));
	}
	
	public void sendOrderCancelRequest(Order order) {
		OrderCancelRequest orderCancelRequest = new OrderCancelRequest(
				new OrigClOrdID(order.getOriginalID()),
				new ClOrdID(order.getID()),
				sideToFIXSide(order.getSide()),	
				new TransactTime());
		
		Session.lookupSession(order.getSessionID())
		   .send(orderCancelRequest);
	}
	
	public ArrayList<SessionID> getSessionIDs() {
		if(initiatorStarted) {
			return initiator.getSessions();	
		} else {
			return null;
		}
	}
	
	public Message populateOrder(Order order, Message newOrderSingle) {
		OrderType type = order.getType();
		
		if (type == OrderType.LIMIT) {
			newOrderSingle.setField(new Price(order.getLimit()));			
		} else if (type == OrderType.STOP){
			newOrderSingle.setField(new StopPx(order.getStop()));
		} else if (type == OrderType.STOP_LIMIT) {
			newOrderSingle.setField(new Price(order.getLimit()));			
			newOrderSingle.setField(new StopPx(order.getStop()));
		}
		
		if (order.getSide() == OrderSide.SHORT_SELL
                || order.getSide() == OrderSide.SHORT_SELL_EXEMPT) {
            newOrderSingle.setField(new LocateReqd(false));
        }

        return newOrderSingle;
	}
	public Side sideToFIXSide(OrderSide side) {
        return (Side) sideMap.getFirst(side);
    }

    public OrderSide FIXSideToSide(Side side) {
        return (OrderSide) sideMap.getSecond(side);
    }

    public OrdType typeToFIXType(OrderType type) {
        return (OrdType) typeMap.getFirst(type);
    }

    public OrderType FIXTypeToType(OrdType type) {
        return (OrderType) typeMap.getSecond(type);
    }

    public TimeInForce tifToFIXTif(OrderTIF tif) {
        return (TimeInForce) tifMap.getFirst(tif);
    }

    public OrderTIF FIXTifToTif(TimeInForce tif) {
        return (OrderTIF) typeMap.getSecond(tif);
    }
	
	static {
        sideMap.put(OrderSide.BUY, new Side(Side.BUY));
        sideMap.put(OrderSide.SELL, new Side(Side.SELL));
        sideMap.put(OrderSide.SHORT_SELL, new Side(Side.SELL_SHORT));
        sideMap.put(OrderSide.SHORT_SELL_EXEMPT, new Side(Side.SELL_SHORT_EXEMPT));
        sideMap.put(OrderSide.CROSS, new Side(Side.CROSS));
        sideMap.put(OrderSide.CROSS_SHORT, new Side(Side.CROSS_SHORT));

        typeMap.put(OrderType.MARKET, new OrdType(OrdType.MARKET));
        typeMap.put(OrderType.LIMIT, new OrdType(OrdType.LIMIT));
        typeMap.put(OrderType.STOP, new OrdType(OrdType.STOP));
        typeMap.put(OrderType.STOP_LIMIT, new OrdType(OrdType.STOP_LIMIT));

        tifMap.put(OrderTIF.DAY, new TimeInForce(TimeInForce.DAY));
        tifMap.put(OrderTIF.IOC, new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL));
        tifMap.put(OrderTIF.OPG, new TimeInForce(TimeInForce.AT_THE_OPENING));
        tifMap.put(OrderTIF.GTC, new TimeInForce(TimeInForce.GOOD_TILL_CANCEL));
        tifMap.put(OrderTIF.GTX, new TimeInForce(TimeInForce.GOOD_TILL_CROSSING));
    }
}