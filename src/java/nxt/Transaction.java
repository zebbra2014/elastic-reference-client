package nxt;

import java.util.List;

import org.json.simple.JSONObject;

public interface Transaction {

    public static interface Builder {

        Builder recipientId(long recipientId);

        Builder referencedTransactionFullHash(String referencedTransactionFullHash);

        Builder message(Appendix.Message message);

        Builder encryptedMessage(Appendix.EncryptedMessage encryptedMessage);

        Builder encryptToSelfMessage(Appendix.EncryptToSelfMessage encryptToSelfMessage);

        Builder publicKeyAnnouncement(Appendix.PublicKeyAnnouncement publicKeyAnnouncement);

        Builder timestamp(int timestamp);

        Builder ecBlockHeight(int height);

        Builder ecBlockId(long blockId);

        Transaction build() throws NxtException.NotValidException;

    }

    long getId();

    String getStringId();

    long getSenderId();

    byte[] getSenderPublicKey();

    long getRecipientId();

    int getHeight();

    long getBlockId();

    Block getBlock();

    short getIndex();

    int getTimestamp();

    int getBlockTimestamp();

    short getDeadline();

    int getExpiration();

    long getAmountNQT();

    long getFeeNQT();

    String getReferencedTransactionFullHash();

    byte[] getSignature();

    String getFullHash();

    TransactionType getType();

    Attachment getAttachment();

    void sign(String secretPhrase);

    boolean verifySignature();

    void validate() throws NxtException.ValidationException;

    byte[] getBytes();

    byte[] getUnsignedBytes();

    JSONObject getJSONObject();

    byte getVersion();

    Appendix.Message getMessage();

    Appendix.EncryptedMessage getEncryptedMessage();

    Appendix.EncryptToSelfMessage getEncryptToSelfMessage();

    List<? extends Appendix> getAppendages();

    /*
    Collection<TransactionType> getPhasingTransactionTypes();

    Collection<TransactionType> getPhasedTransactionTypes();
    */

    int getECBlockHeight();

    long getECBlockId();

}
