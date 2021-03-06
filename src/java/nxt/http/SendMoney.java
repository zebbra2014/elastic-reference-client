package nxt.http;

import javax.servlet.http.HttpServletRequest;

import nxt.Account;
import nxt.NxtException;

import org.json.simple.JSONStreamAware;



public final class SendMoney extends CreateTransaction {

    static final SendMoney instance = new SendMoney();

    private SendMoney() {
        super(new APITag[] {APITag.ACCOUNTS, APITag.CREATE_TRANSACTION}, "recipient", "amountNQT");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        try{
	    	long recipient = ParameterParser.getRecipientId(req);
	        long amountNQT = ParameterParser.getAmountNQT(req);
	        Account account = ParameterParser.getSenderAccount(req);
	        return createTransaction(req, account, recipient, amountNQT);
        }catch(NxtException e){
        	e.printStackTrace();
        	throw e;
        }
    }

}
