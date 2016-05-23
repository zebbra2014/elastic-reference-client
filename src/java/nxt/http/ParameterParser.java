package nxt.http;

import static nxt.http.JSONResponses.HEIGHT_NOT_AVAILABLE;
import static nxt.http.JSONResponses.INCORRECT_ACCOUNT;
import static nxt.http.JSONResponses.INCORRECT_DGS_ENCRYPTED_GOODS;
import static nxt.http.JSONResponses.INCORRECT_ENCRYPTED_MESSAGE;
import static nxt.http.JSONResponses.INCORRECT_HEIGHT;
import static nxt.http.JSONResponses.INCORRECT_PLAIN_MESSAGE;
import static nxt.http.JSONResponses.INCORRECT_PUBLIC_KEY;
import static nxt.http.JSONResponses.INCORRECT_RECIPIENT;
import static nxt.http.JSONResponses.MISSING_ACCOUNT;
import static nxt.http.JSONResponses.MISSING_RECIPIENT;
import static nxt.http.JSONResponses.MISSING_SECRET_PHRASE;
import static nxt.http.JSONResponses.MISSING_SECRET_PHRASE_OR_PUBLIC_KEY;
import static nxt.http.JSONResponses.MISSING_TRANSACTION_BYTES_OR_JSON;
import static nxt.http.JSONResponses.UNKNOWN_ACCOUNT;
import static nxt.http.JSONResponses.incorrect;
import static nxt.http.JSONResponses.missing;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletRequest;

import nxt.Account;
import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.Transaction;
import nxt.crypto.Crypto;
import nxt.crypto.EncryptedData;
import nxt.util.Convert;
import nxt.util.Logger;

import org.eclipse.jetty.server.Request;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

final public class ParameterParser {
	
	static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is);
	    s.useDelimiter("\\A");
	    String res = s.hasNext() ? s.next() : "";
	    s.close();
	    return res;
	}
	
	public static String getParameterMultipart(HttpServletRequest req,String arg0) {
		String result = req.getParameter(arg0);
		if (result == null && req.getMethod()=="POST" && req.getContentType().toLowerCase().contains("multipart")){
			try{
				MultipartConfigElement multipartConfigElement = new MultipartConfigElement((String)null);
				req.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, multipartConfigElement);
				result = convertStreamToString(req.getPart(arg0).getInputStream()); 
			}
			
			catch(Exception e){
				// pass
			}	
		}
		return result;
	}
	
    static byte getByte(HttpServletRequest req, String name, byte min, byte max) throws ParameterException {
        String paramValue = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, name));
        if (paramValue == null) {
            return 0;
        }
        byte value;
        try {
            value = Byte.parseByte(paramValue);
        } catch (RuntimeException e) {
            throw new ParameterException(incorrect(name));
        }
        if (value < min || value > max) {
            throw new ParameterException(incorrect(name));
        }
        return value;
    }

    static int getInt(HttpServletRequest req, String name, int min, int max, boolean isMandatory) throws ParameterException {
        String paramValue = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, name));
        if (paramValue == null) {
            if (isMandatory) {
                throw new ParameterException(missing(name));
            }
            return 0;
        }
        int value;
        try {
            value = Integer.parseInt(paramValue);
        } catch (RuntimeException e) {
            throw new ParameterException(incorrect(name));
        }
        if (value < min || value > max) {
            throw new ParameterException(incorrect(name));
        }
        return value;
    }

    static long getLong(HttpServletRequest req, String name, long min, long max, boolean isMandatory) throws ParameterException {
        String paramValue = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, name));
        if (paramValue == null) {
            if (isMandatory) {
                throw new ParameterException(missing(name));
            }
            return 0;
        }
        long value;
        try {
            value = Long.parseLong(paramValue);
        } catch (RuntimeException e) {
            throw new ParameterException(incorrect(name));
        }
        if (value < min || value > max) {
            throw new ParameterException(incorrect(name));
        }
        return value;
    }

    static long getAmountNQT(HttpServletRequest req) throws ParameterException {
        return getLong(req, "amountNQT", 1L, Constants.MAX_BALANCE_NQT, true);
    }

    static long getFeeNQT(HttpServletRequest req) throws ParameterException {
        return getLong(req, "feeNQT", 0L, Constants.MAX_BALANCE_NQT, true);
    }

    static long getPriceNQT(HttpServletRequest req) throws ParameterException {
        return getLong(req, "priceNQT", 1L, Constants.MAX_BALANCE_NQT, true);
    }


    static long getAmountNQTPerQNT(HttpServletRequest req) throws ParameterException {
        return getLong(req, "amountNQTPerQNT", 1L, Constants.MAX_BALANCE_NQT, true);
    }

    static EncryptedData getEncryptedMessage(HttpServletRequest req, Account recipientAccount) throws ParameterException {
        String data = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "encryptedMessageData"));
        String nonce = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "encryptedMessageNonce"));
        if (data != null && nonce != null) {
            try {
                return new EncryptedData(Convert.parseHexString(data), Convert.parseHexString(nonce));
            } catch (RuntimeException e) {
                throw new ParameterException(INCORRECT_ENCRYPTED_MESSAGE);
            }
        }
        String plainMessage = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "messageToEncrypt"));
        if (plainMessage == null) {
            return null;
        }
        if (recipientAccount == null) {
            throw new ParameterException(INCORRECT_RECIPIENT);
        }
        String secretPhrase = getSecretPhrase(req);
        boolean isText = !"false".equalsIgnoreCase(ParameterParser.getParameterMultipart(req, "messageToEncryptIsText"));
        try {
            byte[] plainMessageBytes = isText ? Convert.toBytes(plainMessage) : Convert.parseHexString(plainMessage);
            return recipientAccount.encryptTo(plainMessageBytes, secretPhrase);
        } catch (RuntimeException e) {
            throw new ParameterException(INCORRECT_PLAIN_MESSAGE);
        }
    }

    static EncryptedData getEncryptToSelfMessage(HttpServletRequest req) throws ParameterException {
        String data = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "encryptToSelfMessageData"));
        String nonce = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "encryptToSelfMessageNonce"));
        if (data != null && nonce != null) {
            try {
                return new EncryptedData(Convert.parseHexString(data), Convert.parseHexString(nonce));
            } catch (RuntimeException e) {
                throw new ParameterException(INCORRECT_ENCRYPTED_MESSAGE);
            }
        }
        String plainMessage = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "messageToEncryptToSelf"));
        if (plainMessage == null) {
            return null;
        }
        String secretPhrase = getSecretPhrase(req);
        Account senderAccount = Account.getAccount(Crypto.getPublicKey(secretPhrase));
        boolean isText = !"false".equalsIgnoreCase(ParameterParser.getParameterMultipart(req, "messageToEncryptToSelfIsText"));
        try {
            byte[] plainMessageBytes = isText ? Convert.toBytes(plainMessage) : Convert.parseHexString(plainMessage);
            return senderAccount.encryptTo(plainMessageBytes, secretPhrase);
        } catch (RuntimeException e) {
            throw new ParameterException(INCORRECT_PLAIN_MESSAGE);
        }
    }

    static EncryptedData getEncryptedGoods(HttpServletRequest req) throws ParameterException {
        String data = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "goodsData"));
        String nonce = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "goodsNonce"));
        if (data != null && nonce != null) {
            try {
                return new EncryptedData(Convert.parseHexString(data), Convert.parseHexString(nonce));
            } catch (RuntimeException e) {
                throw new ParameterException(INCORRECT_DGS_ENCRYPTED_GOODS);
            }
        }
        return null;
    }

    static String getSecretPhrase(HttpServletRequest req) throws ParameterException {
        String secretPhrase = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "secretPhrase"));
        if (secretPhrase == null) {
            throw new ParameterException(MISSING_SECRET_PHRASE);
        }
        return secretPhrase;
    }

    static Account getSenderAccount(HttpServletRequest req) throws ParameterException {
        Account account;
        String secretPhrase = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "secretPhrase"));
        String publicKeyString = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "publicKey"));
        if (secretPhrase != null) {
            account = Account.getAccount(Crypto.getPublicKey(secretPhrase));
        } else if (publicKeyString != null) {
            try {
                account = Account.getAccount(Convert.parseHexString(publicKeyString));
            } catch (RuntimeException e) {
                throw new ParameterException(INCORRECT_PUBLIC_KEY);
            }
        } else {
            throw new ParameterException(MISSING_SECRET_PHRASE_OR_PUBLIC_KEY);
        }
        if (account == null) {
            throw new ParameterException(UNKNOWN_ACCOUNT);
        }
        return account;
    }

    static Account getAccount(HttpServletRequest req) throws ParameterException {
        String accountValue = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "account"));
        if (accountValue == null) {
            throw new ParameterException(MISSING_ACCOUNT);
        }
        try {
            Account account = Account.getAccount(Convert.parseAccountId(accountValue));
            if (account == null) {
                throw new ParameterException(UNKNOWN_ACCOUNT);
            }
            return account;
        } catch (RuntimeException e) {
            throw new ParameterException(INCORRECT_ACCOUNT);
        }
    }

    static List<Account> getAccounts(HttpServletRequest req) throws ParameterException {
        String[] accountValues = req.getParameterValues("account");
        if (accountValues == null || accountValues.length == 0) {
            throw new ParameterException(MISSING_ACCOUNT);
        }
        List<Account> result = new ArrayList<>();
        for (String accountValue : accountValues) {
            if (accountValue == null || accountValue.equals("")) {
                continue;
            }
            try {
                Account account = Account.getAccount(Convert.parseAccountId(accountValue));
                if (account == null) {
                    throw new ParameterException(UNKNOWN_ACCOUNT);
                }
                result.add(account);
            } catch (RuntimeException e) {
                throw new ParameterException(INCORRECT_ACCOUNT);
            }
        }
        return result;
    }

    static int getTimestamp(HttpServletRequest req) throws ParameterException {
        return getInt(req, "timestamp", 0, Integer.MAX_VALUE, false);
    }

    static long getRecipientId(HttpServletRequest req) throws ParameterException {
        String recipientValue = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "recipient"));
        if (recipientValue == null || "0".equals(recipientValue)) {
            throw new ParameterException(MISSING_RECIPIENT);
        }
        long recipientId;
        try {
            recipientId = Convert.parseAccountId(recipientValue);
        } catch (RuntimeException e) {
            throw new ParameterException(INCORRECT_RECIPIENT);
        }
        if (recipientId == 0) {
            throw new ParameterException(INCORRECT_RECIPIENT);
        }
        return recipientId;
    }


    static int getFirstIndex(HttpServletRequest req) {
        int firstIndex;
        try {
            firstIndex = Integer.parseInt(ParameterParser.getParameterMultipart(req, "firstIndex"));
            if (firstIndex < 0) {
                return 0;
            }
        } catch (NumberFormatException e) {
            return 0;
        }
        return firstIndex;
    }

    static int getLastIndex(HttpServletRequest req) {
        int lastIndex;
        try {
            lastIndex = Integer.parseInt(ParameterParser.getParameterMultipart(req, "lastIndex"));
            if (lastIndex < 0) {
                return Integer.MAX_VALUE;
            }
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
        return lastIndex;
    }

    static int getNumberOfConfirmations(HttpServletRequest req) throws ParameterException {
        return getInt(req, "numberOfConfirmations", 0, Nxt.getBlockchain().getHeight(), false);
    }

    static int getHeight(HttpServletRequest req) throws ParameterException {
        String heightValue = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "height"));
        if (heightValue != null) {
            try {
                int height = Integer.parseInt(heightValue);
                if (height < 0 || height > Nxt.getBlockchain().getHeight()) {
                    throw new ParameterException(INCORRECT_HEIGHT);
                }
                if (height < Nxt.getBlockchainProcessor().getMinRollbackHeight()) {
                    throw new ParameterException(HEIGHT_NOT_AVAILABLE);
                }
                return height;
            } catch (NumberFormatException e) {
                throw new ParameterException(INCORRECT_HEIGHT);
            }
        }
        return -1;
    }

    static Transaction parseTransaction(String transactionBytes, String transactionJSON) throws ParameterException {
        if (transactionBytes == null && transactionJSON == null) {
            throw new ParameterException(MISSING_TRANSACTION_BYTES_OR_JSON);
        }
        if (transactionBytes != null) {
            try {
                byte[] bytes = Convert.parseHexString(transactionBytes);
                return Nxt.getTransactionProcessor().parseTransaction(bytes);
            } catch (NxtException.ValidationException|RuntimeException e) {
                Logger.logDebugMessage(e.getMessage(), e);
                JSONObject response = new JSONObject();
                response.put("errorCode", 4);
                response.put("errorDescription", "Incorrect transactionBytes: " + e.toString());
                throw new ParameterException(response);
            }
        } else {
            try {
                JSONObject json = (JSONObject) JSONValue.parseWithException(transactionJSON);
                return Nxt.getTransactionProcessor().parseTransaction(json);
            } catch (NxtException.ValidationException | RuntimeException | ParseException e) {
                Logger.logDebugMessage(e.getMessage(), e);
                JSONObject response = new JSONObject();
                response.put("errorCode", 4);
                response.put("errorDescription", "Incorrect transactionJSON: " + e.toString());
                throw new ParameterException(response);
            }
        }
    }


    private ParameterParser() {} // never

}
