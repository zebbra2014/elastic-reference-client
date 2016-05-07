package nxt.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import nxt.Account;
import nxt.Attachment;
import nxt.NxtException;
import nxt.WorkLogicManager;

import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import static nxt.http.JSONResponses.MISSING_WORKID;
import static nxt.http.JSONResponses.MISSING_MSLOCATOR;
import static nxt.http.JSONResponses.MISSING_INPUTS;
import static nxt.http.JSONResponses.MISSING_STATE;
import static nxt.http.JSONResponses.INCORRECT_WORKID;
import static nxt.http.JSONResponses.INCORRECT_STATE;
import static nxt.http.JSONResponses.INCORRECT_INPUTS;
import static nxt.http.JSONResponses.INCORRECT_MSLOCATOR;
import static nxt.http.JSONResponses.INCORRECT_BOOLEAN;

public final class ProofOfX extends CreateTransaction {

    static final ProofOfX instance = new ProofOfX();

    private ProofOfX() {
        super(new APITag[] {APITag.POX, APITag.CREATE_TRANSACTION}, "name", "description", "minNumberOfOptions", "maxNumberOfOptions", "optionsAreBinary", "option1", "option2", "option3"); // hardcoded to 3 options for testing
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        String workId = req.getParameter("workId");
        String msLocator = req.getParameter("msLocator");
        String inputs = req.getParameter("inputs");
        String state = req.getParameter("state");
        String ProofOfWork = req.getParameter("pow");
        
        if (workId == null) {
            return MISSING_WORKID;
        } else if (msLocator == null) {
            return MISSING_MSLOCATOR;
        } else if (inputs == null) {
            return MISSING_INPUTS;
        } else if (state == null) {
            return MISSING_STATE;
        }
        
        try{
	        if (WorkLogicManager.haveWork(Integer.parseInt(workId))) {
	            return INCORRECT_WORKID;
	        }
        } catch (NumberFormatException e) {
            return INCORRECT_WORKID;
        } 
        
        boolean proofOfWork = true;
        try {
        	proofOfWork = Boolean.parseBoolean(ProofOfWork);
        } catch (NumberFormatException e) {
            return INCORRECT_BOOLEAN;
        }
      
        short msLocatorShort;
        try {
        	msLocatorShort = Short.parseShort(msLocator);
        } catch (NumberFormatException e) {
            return INCORRECT_MSLOCATOR;
        }
        
        List<Integer> stateRaw = new ArrayList<Integer>();
        try {
        	 Scanner sc = new Scanner(state);
             sc.useDelimiter("(, *)*");
             while(sc.hasNext()){
            	 stateRaw.add(sc.nextInt());
             }
             sc.close();
        } catch (NumberFormatException e) {
            return INCORRECT_STATE;
        }
        int[] stateUltraRaw = new int[stateRaw.size()];
        for (int i=0;i<stateUltraRaw.length;i++){
        	stateUltraRaw[i] = stateRaw.get(i);
        }
        
        List<Integer> inputRaw = new ArrayList<Integer>();
        try {
        	 Scanner sc = new Scanner(inputs);
             sc.useDelimiter("(, *)*");
             while(sc.hasNext()){
            	 inputRaw.add(sc.nextInt());
             }
             sc.close();
        } catch (NumberFormatException e) {
            return INCORRECT_INPUTS;
        }
        int[] inputUltraRaw = new int[inputRaw.size()];
        for (int i=0;i<inputUltraRaw.length;i++){
        	inputUltraRaw[i] = inputRaw.get(i);
        }
        
        
        if(stateRaw.size()<WorkLogicManager.getMinNumberStateInts() || stateRaw.size()>WorkLogicManager.getMaxNumberStateInts()){
        	return INCORRECT_STATE;
        }
        if(inputRaw.size()<WorkLogicManager.getMinNumberInputInts() || inputRaw.size()>WorkLogicManager.getMaxNumberInputInts()){
        	return INCORRECT_INPUTS;
        }

        
        Account account = ParameterParser.getSenderAccount(req);
        
        Attachment attachment = null;
        
        if(proofOfWork)
        	attachment = new Attachment.PiggybackedProofOfWork(Integer.parseInt(workId), msLocatorShort, stateUltraRaw, inputUltraRaw);
        else
        	attachment = new Attachment.PiggybackedProofOfBounty(Integer.parseInt(workId), msLocatorShort, stateUltraRaw, inputUltraRaw);
        
        return createTransaction(req, account, attachment);

    }

}
