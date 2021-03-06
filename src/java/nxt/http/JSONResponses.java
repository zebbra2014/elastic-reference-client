package nxt.http;

import java.util.Arrays;

import nxt.Constants;
import nxt.util.JSON;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

public final class JSONResponses {

	
	// these are for work creation
	public static final JSONStreamAware MISSING_LANGUAGE = missing("program language");
	public static final JSONStreamAware MISSING_PROGAMCODE = missing("program code");
	public static final JSONStreamAware MISSING_BOUNTYHOOK = missing("bounty hook");

	public static final JSONStreamAware MISSING_NUMBER_INPUTVARS = missing("number of input variables");
	public static final JSONStreamAware MISSING_NUMBER_OUTPUTVARS = missing("number of output variables");
    public static final JSONStreamAware INCORRECT_WORK_NAME_LENGTH = incorrect("work title");
    public static final JSONStreamAware INCORRECT_VARIABLES_NUM = incorrect("number of input or output variables");
    public static final JSONStreamAware INCORRECT_WORK_LANGUAGE = incorrect("work language");
    public static final JSONStreamAware INCORRECT_AMOUNT = incorrect("attached amount");

    public static final JSONStreamAware INCORRECT_PROGRAM = incorrect("program code");
    public static final JSONStreamAware INCORRECT_BOUNTYHOOK = incorrect("bounty hook");
    public static final JSONStreamAware INCORRECT_WORKID = incorrect("workId");
    public static final JSONStreamAware INCORRECT_BOOLEAN = incorrect("boolean");
    
    // THESE ARE FOR Proof-of-X
	public static final JSONStreamAware MISSING_WORKID = missing("workId");
	public static final JSONStreamAware MISSING_MSLOCATOR = missing("10ms block locator");
	public static final JSONStreamAware MISSING_INPUTS = missing("program inputs");
	public static final JSONStreamAware MISSING_STATE = missing("program state");
    public static final JSONStreamAware INCORRECT_STATE = incorrect("workId");
    public static final JSONStreamAware INCORRECT_INPUTS = incorrect("workId");
    public static final JSONStreamAware INCORRECT_MSLOCATOR = incorrect("workId");
    
	// generic ones
    public static final JSONStreamAware INCORRECT_SECRET_PHRASE = incorrect("secretPhrase");
    public static final JSONStreamAware MISSING_SECRET_PHRASE = missing("secretPhrase");
    public static final JSONStreamAware INCORRECT_PUBLIC_KEY = incorrect("publicKey");
    public static final JSONStreamAware MISSING_ALIAS_NAME = missing("aliasName");
    public static final JSONStreamAware MISSING_ALIAS_OR_ALIAS_NAME = missing("alias", "aliasName");
    public static final JSONStreamAware MISSING_DEADLINE = missing("deadline");
    public static final JSONStreamAware INCORRECT_DEADLINE = incorrect("deadline");
    public static final JSONStreamAware MISSING_TRANSACTION_BYTES_OR_JSON = missing("transactionBytes", "transactionJSON");
    public static final JSONStreamAware INCORRECT_TRANSACTION_BYTES_OR_JSON = incorrect("transactionBytes or transactionJSON");
    public static final JSONStreamAware MISSING_ORDER = missing("order");
    public static final JSONStreamAware INCORRECT_ORDER = incorrect("order");
    public static final JSONStreamAware UNKNOWN_ORDER = unknown("order");
    public static final JSONStreamAware MISSING_HALLMARK = missing("hallmark");
    public static final JSONStreamAware INCORRECT_HALLMARK = incorrect("hallmark");
    public static final JSONStreamAware MISSING_WEBSITE = missing("website");
    public static final JSONStreamAware INCORRECT_WEBSITE = incorrect("website");
    public static final JSONStreamAware MISSING_TOKEN = missing("token");
    public static final JSONStreamAware INCORRECT_TOKEN = incorrect("token");
    public static final JSONStreamAware MISSING_ACCOUNT = missing("account");
    public static final JSONStreamAware INCORRECT_ACCOUNT = incorrect("account");
    public static final JSONStreamAware INCORRECT_TIMESTAMP = incorrect("timestamp");
    public static final JSONStreamAware UNKNOWN_ACCOUNT = unknown("account");
    public static final JSONStreamAware MISSING_BLOCK = missing("block");
    public static final JSONStreamAware UNKNOWN_BLOCK = unknown("block");
    public static final JSONStreamAware INCORRECT_BLOCK = incorrect("block");
    public static final JSONStreamAware MISSING_PEER = missing("peer");
    public static final JSONStreamAware UNKNOWN_PEER = unknown("peer");
    public static final JSONStreamAware MISSING_TRANSACTION = missing("transaction");
    public static final JSONStreamAware UNKNOWN_TRANSACTION = unknown("transaction");
    public static final JSONStreamAware INCORRECT_TRANSACTION = incorrect("transaction");
    public static final JSONStreamAware MISSING_NAME = missing("name");
    public static final JSONStreamAware INCORRECT_DECIMALS = incorrect("decimals");
    public static final JSONStreamAware MISSING_HOST = missing("host");
    public static final JSONStreamAware MISSING_DATE = missing("date");
    public static final JSONStreamAware MISSING_WEIGHT = missing("weight");
    public static final JSONStreamAware INCORRECT_HOST = incorrect("host", "(the length exceeds 100 chars limit)");
    public static final JSONStreamAware INCORRECT_WEIGHT = incorrect("weight");
    public static final JSONStreamAware INCORRECT_DATE = incorrect("date");
    public static final JSONStreamAware INCORRECT_REFERENCED_TRANSACTION = incorrect("referencedTransactionFullHash");
    public static final JSONStreamAware MISSING_MESSAGE = missing("message");
    public static final JSONStreamAware MISSING_RECIPIENT = missing("recipient");
    public static final JSONStreamAware INCORRECT_RECIPIENT = incorrect("recipient");
    public static final JSONStreamAware INCORRECT_ARBITRARY_MESSAGE = incorrect("message");
    public static final JSONStreamAware MISSING_DESCRIPTION = missing("description");
    public static final JSONStreamAware MISSING_MINNUMBEROFOPTIONS = missing("minNumberOfOptions");
    public static final JSONStreamAware MISSING_MAXNUMBEROFOPTIONS = missing("maxNumberOfOptions");
    public static final JSONStreamAware MISSING_OPTIONSAREBINARY = missing("optionsAreBinary");
    public static final JSONStreamAware MISSING_POLL = missing("poll");
    public static final JSONStreamAware INCORRECT_POLL_NAME_LENGTH = incorrect("name", "(length must be not longer than " + Constants.MAX_POLL_NAME_LENGTH + " characters)");
    public static final JSONStreamAware INCORRECT_POLL_DESCRIPTION_LENGTH = incorrect("description", "(length must be not longer than " + Constants.MAX_POLL_DESCRIPTION_LENGTH + " characters)");
    public static final JSONStreamAware INCORRECT_POLL_OPTION_LENGTH = incorrect("option", "(length must be not longer than " + Constants.MAX_POLL_OPTION_LENGTH + " characters)");
    public static final JSONStreamAware INCORRECT_MINNUMBEROFOPTIONS = incorrect("minNumberOfOptions");
    public static final JSONStreamAware INCORRECT_MAXNUMBEROFOPTIONS = incorrect("maxNumberOfOptions");
    public static final JSONStreamAware INCORRECT_OPTIONSAREBINARY = incorrect("optionsAreBinary");
    public static final JSONStreamAware INCORRECT_POLL = incorrect("poll");
    public static final JSONStreamAware INCORRECT_VOTE = incorrect("vote");
    public static final JSONStreamAware UNKNOWN_POLL = unknown("poll");
    public static final JSONStreamAware INCORRECT_ACCOUNT_NAME_LENGTH = incorrect("name", "(length must be less than " + Constants.MAX_ACCOUNT_NAME_LENGTH + " characters)");
    public static final JSONStreamAware INCORRECT_ACCOUNT_DESCRIPTION_LENGTH = incorrect("description", "(length must be less than " + Constants.MAX_ACCOUNT_DESCRIPTION_LENGTH + " characters)");
    public static final JSONStreamAware INCORRECT_UNSIGNED_BYTES = incorrect("unsignedTransactionBytes");
    public static final JSONStreamAware MISSING_UNSIGNED_BYTES = missing("unsignedTransactionBytes");
    public static final JSONStreamAware MISSING_SIGNATURE_HASH = missing("signatureHash");
    public static final JSONStreamAware INCORRECT_ENCRYPTED_MESSAGE = incorrect("encryptedMessageData");
    public static final JSONStreamAware INCORRECT_DGS_ENCRYPTED_GOODS = incorrect("goodsData");
    public static final JSONStreamAware MISSING_SECRET_PHRASE_OR_PUBLIC_KEY = missing("secretPhrase", "publicKey");
    public static final JSONStreamAware INCORRECT_HEIGHT = incorrect("height");
    public static final JSONStreamAware MISSING_HEIGHT = missing("height");
    public static final JSONStreamAware INCORRECT_PLAIN_MESSAGE = incorrect("messageToEncrypt");
    public static final JSONStreamAware MISSING_CURRENCY = missing("currency");
    public static final JSONStreamAware UNKNOWN_CURRENCY = unknown("currency");
    public static final JSONStreamAware INCORRECT_CURRENCY = incorrect("currency");
    public static final JSONStreamAware MISSING_OFFER = missing("offer");
    public static final JSONStreamAware UNKNOWN_OFFER = unknown("offer");
    public static final JSONStreamAware INCORRECT_OFFER = incorrect("offer");
    public static final JSONStreamAware INCORRECT_MESSAGE_PATTERN_REGEX = incorrect("messagePatternRegex");
    public static final JSONStreamAware INCORRECT_MESSAGE_PATTERN_FLAGS = incorrect("messagePatternFlags");
    public static final JSONStreamAware INCORRECT_ADMIN_PASSWORD = incorrect("adminPassword", "(the specified password does not match nxt.adminPassword)");
    
    public static final JSONStreamAware NOT_ENOUGH_FUNDS;
    static {
        JSONObject response = new JSONObject();
        response.put("errorCode", 6);
        response.put("errorDescription", "Not enough funds");
        NOT_ENOUGH_FUNDS = JSON.prepare(response);
    }


    public static final JSONStreamAware ERROR_NOT_ALLOWED;
    static {
        JSONObject response = new JSONObject();
        response.put("errorCode", 7);
        response.put("errorDescription", "Not allowed");
        ERROR_NOT_ALLOWED = JSON.prepare(response);
    }

    public static final JSONStreamAware ERROR_INCORRECT_REQUEST;
    static {
        JSONObject response  = new JSONObject();
        response.put("errorCode", 1);
        response.put("errorDescription", "Incorrect request");
        ERROR_INCORRECT_REQUEST = JSON.prepare(response);
    }

    public static final JSONStreamAware NOT_FORGING;
    static {
        JSONObject response = new JSONObject();
        response.put("errorCode", 5);
        response.put("errorDescription", "Account is not forging");
        NOT_FORGING = JSON.prepare(response);
    }

    public static final JSONStreamAware POST_REQUIRED;
    static {
        JSONObject response = new JSONObject();
        response.put("errorCode", 1);
        response.put("errorDescription", "This request is only accepted using POST!");
        POST_REQUIRED = JSON.prepare(response);
    }

    public static final JSONStreamAware FEATURE_NOT_AVAILABLE;
    static {
        JSONObject response = new JSONObject();
        response.put("errorCode", 9);
        response.put("errorDescription", "Feature not available");
        FEATURE_NOT_AVAILABLE = JSON.prepare(response);
    }

    public static final JSONStreamAware DECRYPTION_FAILED;
    static {
        JSONObject response = new JSONObject();
        response.put("errorCode", 8);
        response.put("errorDescription", "Decryption failed");
        DECRYPTION_FAILED = JSON.prepare(response);
    }


    public static final JSONStreamAware NO_MESSAGE;
    static {
        JSONObject response = new JSONObject();
        response.put("errorCode", 8);
        response.put("errorDescription", "No attached message found");
        NO_MESSAGE = JSON.prepare(response);
    }

    public static final JSONStreamAware HEIGHT_NOT_AVAILABLE;
    static {
        JSONObject response = new JSONObject();
        response.put("errorCode", 8);
        response.put("errorDescription", "Requested height not available");
        HEIGHT_NOT_AVAILABLE = JSON.prepare(response);
    }


    public static final JSONStreamAware NO_PASSWORD_IN_CONFIG;
    static {
        JSONObject response = new JSONObject();
        response.put("errorCode", 8);
        response.put("errorDescription", "Administrator's password is not configured. Please set nxt.adminPassword");
        NO_PASSWORD_IN_CONFIG = JSON.prepare(response);
    }

    static JSONStreamAware missing(String... paramNames) {
        JSONObject response = new JSONObject();
        response.put("errorCode", 3);
        if (paramNames.length == 1) {
            response.put("errorDescription", "\"" + paramNames[0] + "\"" + " not specified");
        } else {
            response.put("errorDescription", "At least one of " + Arrays.toString(paramNames) + " must be specified");
        }
        return JSON.prepare(response);
    }

    static JSONStreamAware incorrect(String paramName) {
        return incorrect(paramName, null);
    }

    private static JSONStreamAware incorrect(String paramName, String details) {
        JSONObject response = new JSONObject();
        response.put("errorCode", 4);
        response.put("errorDescription", "Incorrect \"" + paramName + (details != null ? "\" " + details : "\""));
        return JSON.prepare(response);
    }

    private static JSONStreamAware unknown(String objectName) {
        JSONObject response = new JSONObject();
        response.put("errorCode", 5);
        response.put("errorDescription", "Unknown " + objectName);
        return JSON.prepare(response);
    }

    private JSONResponses() {} // never

}
