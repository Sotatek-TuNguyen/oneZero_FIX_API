package quickfix.test;

import quickfix.ApplicationAdapter;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.DoNotSend;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.ExecType;
import quickfix.fix44.ExecutionReport;

public class BaseInitiator extends ApplicationAdapter {
	
	private String password;
	
	public BaseInitiator(String password) {
		super();
		this.password = password;
	}
	
	
	@Override
	public void onLogon(SessionID sessionId) {
		super.onLogon(sessionId);
	}
	
	@Override
	public void onLogout(SessionID sessionID) {
		super.onLogout(sessionID);
	}
	
	@Override
	public void fromAdmin(quickfix.Message message, SessionID sessionId) 
		throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
		super.fromAdmin(message, sessionId);
	}
	
	@Override
	public void toAdmin(quickfix.Message message, SessionID sessionId){
		// Check and process logon message
		try {
			if (message.getHeader().getString(35).equals("A")) {
				this.addPasswordToMessage(message, this.password);
			}	
		} catch (FieldNotFound e) {
			e.printStackTrace();
		}
		super.toAdmin(message, sessionId);
	}
	
	@Override
	public void onCreate(SessionID sessionID) {
		super.onCreate(sessionID);
	}
	
	@Override
	public void fromApp(Message message, SessionID sessionId)
		throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		if (message instanceof ExecutionReport) {
			ExecutionReport executionReport = (ExecutionReport) message;
			try {
				ExecType executionType = (ExecType) executionReport.getExecType();
				System.out.println(executionType);
			} catch (FieldNotFound e) {
				e.printStackTrace();
			}
		}

	}
	
	@Override
	public void toApp(Message message, SessionID sessionId)
		throws DoNotSend {
		super.toApp(message, sessionId);
	}
	
	private void addPasswordToMessage(Message message, String password) {
		message.getHeader().setString(554, password);
	}
	
}