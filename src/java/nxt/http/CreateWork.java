package nxt.http;

import nxt.Account;
import nxt.Attachment;
import nxt.Constants;
import nxt.NxtException;
import nxt.WorkLogicManager;
import nxt.crypto.Crypto;
import nxt.util.Convert;

import org.json.simple.JSONStreamAware;





import static nxt.http.JSONResponses.INCORRECT_WORK_NAME_LENGTH;
import static nxt.http.JSONResponses.INCORRECT_VARIABLES_NUM;
import static nxt.http.JSONResponses.INCORRECT_WORK_LANGUAGE;
import static nxt.http.JSONResponses.INCORRECT_DEADLINE;
import static nxt.http.JSONResponses.INCORRECT_PROGRAM;
import static nxt.http.JSONResponses.INCORRECT_BOUNTYHOOK;
import static nxt.http.JSONResponses.MISSING_NUMBER_INPUTVARS;
import static nxt.http.JSONResponses.MISSING_LANGUAGE;
import static nxt.http.JSONResponses.MISSING_DEADLINE;
import static nxt.http.JSONResponses.MISSING_SECRET_PHRASE;
import static nxt.http.JSONResponses.INCORRECT_SECRET_PHRASE;
import static nxt.http.JSONResponses.MISSING_PROGAMCODE;
import static nxt.http.JSONResponses.MISSING_BOUNTYHOOK;
import static nxt.http.JSONResponses.MISSING_NUMBER_OUTPUTVARS;
import static nxt.http.JSONResponses.MISSING_NAME;


public final class CreateWork extends CreateTransaction {

    static final CreateWork instance = new CreateWork();

    private CreateWork() {
        super(new APITag[] {APITag.WC, APITag.CREATE_TRANSACTION}, "name", "description", "minNumberOfOptions", "maxNumberOfOptions", "optionsAreBinary", "option1", "option2", "option3"); // hardcoded to 3 options for testing
    }

    @Override
    JSONStreamAware processRequest(FakeServletRequest req) throws NxtException {

        String workTitle = req.getParameter("work_title");
        String workLanguage = "LUA"; // FIXME, HACKME, TODO: Hardcoded language, fix that if you wanna support more
        String programCode = req.getParameter("source_code");
        String deadline = req.getParameter("work_deadline");
        String amount_spent = req.getParameter("amountNQT");
        
        
        
        Account account = ParameterParser.getSenderAccount(req);
               
        Byte numberInputVars = 1;
        Byte numberOutputVars = 1;
        // TODO, FIXME, HACKME: At this point, we have to parse the source file and fill this here correctly
        
        
        
        if (workTitle == null) {
            return MISSING_NAME;
        } else if (workLanguage == null) {
            return MISSING_LANGUAGE;
        } else if (programCode == null) {
            return MISSING_PROGAMCODE;
        }  else if (numberInputVars == null) {
            return MISSING_NUMBER_INPUTVARS;
        } else if (numberOutputVars == null) {
            return MISSING_NUMBER_OUTPUTVARS;
        } else if (deadline == null) {
            return MISSING_DEADLINE;
        }

        if (workTitle.length() > Constants.MAX_POLL_NAME_LENGTH || workTitle.length() < 1) {
            return INCORRECT_WORK_NAME_LENGTH;
        }
        
        // TODO FIXME: DO SOME ADDITIONAL CODE CHECKS, THIS IS REALLY IMPORTANT
        // RIGHT NOW WE ARE JUST PASSING THROUGH
        
           
        byte workLanguageByte;
        try {
        	workLanguageByte = WorkLogicManager.getLanguageByte(workLanguage);
        	if(WorkLogicManager.checkWorkLanguage(workLanguageByte) == false){
        		return INCORRECT_WORK_LANGUAGE;
        	}
        } catch (NumberFormatException e) {
            return INCORRECT_WORK_LANGUAGE;
        }
        
        int deadlineInt;
        try {
        	deadlineInt = Integer.parseInt(deadline);
        	if(WorkLogicManager.checkDeadline(deadlineInt) == false){
        		return INCORRECT_DEADLINE;
        	}
        } catch (NumberFormatException e) {
            return INCORRECT_DEADLINE;
        }
        
        Attachment attachment = new Attachment.WorkCreation(workTitle, workLanguageByte, WorkLogicManager.compress(programCode), numberInputVars, numberOutputVars, deadlineInt);
        return createTransaction(req, account, attachment);

    }

}
