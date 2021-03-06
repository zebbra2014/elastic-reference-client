package nxt.http;

import static nxt.http.JSONResponses.FEATURE_NOT_AVAILABLE;
import static nxt.http.JSONResponses.INCORRECT_ARBITRARY_MESSAGE;
import static nxt.http.JSONResponses.INCORRECT_DEADLINE;
import static nxt.http.JSONResponses.MISSING_DEADLINE;
import static nxt.http.JSONResponses.MISSING_SECRET_PHRASE;
import static nxt.http.JSONResponses.NOT_ENOUGH_FUNDS;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import nxt.Account;
import nxt.Appendix;
import nxt.Attachment;
import nxt.Nxt;
import nxt.NxtException;
import nxt.Transaction;
import nxt.crypto.Crypto;
import nxt.crypto.EncryptedData;
import nxt.util.Convert;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

abstract class CreateTransaction extends APIServlet.APIRequestHandler {

    private static final String[] commonParameters = new String[] {"secretPhrase", "publicKey", "feeNQT",
            "deadline", "referencedTransactionFullHash", "broadcast",
            "message", "messageIsText",
            "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce",
            "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce",
            "recipientPublicKey"};

    private static String[] addCommonParameters(String[] parameters) {
        String[] result = Arrays.copyOf(parameters, parameters.length + commonParameters.length);
        System.arraycopy(commonParameters, 0, result, parameters.length, commonParameters.length);
        return result;
    }

    CreateTransaction(APITag[] apiTags, String... parameters) {
        super(apiTags, addCommonParameters(parameters));
    }

    final JSONStreamAware createTransaction(HttpServletRequest req, Account senderAccount, Attachment attachment)
        throws NxtException {
        return createTransaction(req, senderAccount, 0, 0, attachment);
    }

    final JSONStreamAware createTransaction(HttpServletRequest req, Account senderAccount, long recipientId, long amountNQT)
            throws NxtException {
        return createTransaction(req, senderAccount, recipientId, amountNQT, Attachment.ORDINARY_PAYMENT);
    }

    final JSONStreamAware createTransaction(HttpServletRequest req, Account senderAccount, long recipientId,
                                            long amountNQT, Attachment attachment)
            throws NxtException {
        String deadlineValue = ParameterParser.getParameterMultipart(req, "deadline");
       
        String referencedTransactionFullHash = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "referencedTransactionFullHash"));
        String secretPhrase = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "secretPhrase"));
        String publicKeyValue = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "publicKey"));
        boolean broadcast = !"false".equalsIgnoreCase(ParameterParser.getParameterMultipart(req, "broadcast"));
        Appendix.EncryptedMessage encryptedMessage = null;
        if (attachment.getTransactionType().canHaveRecipient()) {
            EncryptedData encryptedData = ParameterParser.getEncryptedMessage(req, Account.getAccount(recipientId));
            if (encryptedData != null) {
                encryptedMessage = new Appendix.EncryptedMessage(encryptedData, !"false".equalsIgnoreCase(ParameterParser.getParameterMultipart(req, "messageToEncryptIsText")));
            }
        }
        Appendix.EncryptToSelfMessage encryptToSelfMessage = null;
        EncryptedData encryptedToSelfData = ParameterParser.getEncryptToSelfMessage(req);
        if (encryptedToSelfData != null) {
            encryptToSelfMessage = new Appendix.EncryptToSelfMessage(encryptedToSelfData, !"false".equalsIgnoreCase(ParameterParser.getParameterMultipart(req, "messageToEncryptToSelfIsText")));
        }
        Appendix.Message message = null;
        String messageValue = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "message"));
        if (messageValue != null) {
            boolean messageIsText = !"false".equalsIgnoreCase(ParameterParser.getParameterMultipart(req, "messageIsText"));
            try {
                message = messageIsText ? new Appendix.Message(messageValue) : new Appendix.Message(Convert.parseHexString(messageValue));
            } catch (RuntimeException e) {
                throw new ParameterException(INCORRECT_ARBITRARY_MESSAGE);
            }
        }
        Appendix.PublicKeyAnnouncement publicKeyAnnouncement = null;
        String recipientPublicKey = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "recipientPublicKey"));
        if (recipientPublicKey != null) {
            publicKeyAnnouncement = new Appendix.PublicKeyAnnouncement(Convert.parseHexString(recipientPublicKey));
        }

        if (secretPhrase == null && publicKeyValue == null) {
            return MISSING_SECRET_PHRASE;
        } else if (deadlineValue == null) {
            return MISSING_DEADLINE;
        }

        short deadline;
        try {
            deadline = Short.parseShort(deadlineValue);
            if (deadline < 1 || deadline > 1440) {
                return INCORRECT_DEADLINE;
            }
        } catch (NumberFormatException e) {
            return INCORRECT_DEADLINE;
        }

        long feeNQT = ParameterParser.getFeeNQT(req);

        JSONObject response = new JSONObject();

        // shouldn't try to get publicKey from senderAccount as it may have not been set yet
        byte[] publicKey = secretPhrase != null ? Crypto.getPublicKey(secretPhrase) : Convert.parseHexString(publicKeyValue);

        try {
            Transaction.Builder builder = Nxt.newTransactionBuilder(publicKey, amountNQT, feeNQT,
                    deadline, attachment).referencedTransactionFullHash(referencedTransactionFullHash);
            if (attachment.getTransactionType().canHaveRecipient()) {
                builder.recipientId(recipientId);
            }
            if (encryptedMessage != null) {
                builder.encryptedMessage(encryptedMessage);
            }
            if (message != null) {
                builder.message(message);
            }
            if (publicKeyAnnouncement != null) {
                builder.publicKeyAnnouncement(publicKeyAnnouncement);
            }
            if (encryptToSelfMessage != null) {
                builder.encryptToSelfMessage(encryptToSelfMessage);
            }
            Transaction transaction = builder.build();
            try {
                if (Convert.safeAdd(amountNQT, transaction.getFeeNQT()) > senderAccount.getUnconfirmedBalanceNQT()) {
                    return NOT_ENOUGH_FUNDS;
                }
            } catch (ArithmeticException e) {
                return NOT_ENOUGH_FUNDS;
            }
            if (secretPhrase != null) {
                transaction.sign(secretPhrase);
                response.put("transaction", transaction.getStringId());
                response.put("fullHash", transaction.getFullHash());
                response.put("transactionBytes", Convert.toHexString(transaction.getBytes()));
                response.put("signatureHash", Convert.toHexString(Crypto.sha256().digest(transaction.getSignature())));
                if (broadcast) {
                    Nxt.getTransactionProcessor().broadcast(transaction);
                    response.put("broadcasted", true);
                } else {
                    transaction.validate();
                    response.put("broadcasted", false);
                }
            } else {
                transaction.validate();
                response.put("broadcasted", false);
            }
            response.put("unsignedTransactionBytes", Convert.toHexString(transaction.getUnsignedBytes()));
            response.put("transactionJSON", JSONData.unconfirmedTransaction(transaction));

        } catch (NxtException.NotYetEnabledException e) {
            return FEATURE_NOT_AVAILABLE;
        } catch (NxtException.ValidationException e) {
            response.put("error", e.getMessage());
        }
        return response;

    }

    @Override
    final boolean requirePost() {
        return true;
    }

}
