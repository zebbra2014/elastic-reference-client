package nxt;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import nxt.util.Convert;

import org.json.simple.JSONObject;


public abstract class TransactionType {

    private static final byte TYPE_PAYMENT = 0;
    private static final byte TYPE_MESSAGING = 1;
    private static final byte TYPE_ACCOUNT_CONTROL = 2;
    private static final byte TYPE_WORK_CONTROL = 3;
    

    private static final byte SUBTYPE_PAYMENT_ORDINARY_PAYMENT = 0;

    private static final byte SUBTYPE_MESSAGING_ARBITRARY_MESSAGE = 0;
    private static final byte SUBTYPE_MESSAGING_POLL_CREATION = 2;
    private static final byte SUBTYPE_MESSAGING_VOTE_CASTING = 3;
    private static final byte SUBTYPE_MESSAGING_HUB_ANNOUNCEMENT = 4;
    private static final byte SUBTYPE_MESSAGING_ACCOUNT_INFO = 5;

    private static final byte SUBTYPE_ACCOUNT_CONTROL_EFFECTIVE_BALANCE_LEASING = 0;
    
    private static final byte SUBTYPE_WORK_CONTROL_NEW_TASK = 0;
    private static final byte SUBTYPE_WORK_CONTROL_CANCEL_TASK = 1;
    private static final byte SUBTYPE_WORK_CONTROL_PROOF_OF_WORK = 2;
    private static final byte SUBTYPE_WORK_CONTROL_BOUNTY = 3;

    private static final int BASELINE_FEE_HEIGHT = 1; // At release time must be less than current block - 1440
    private static final Fee BASELINE_FEE = new Fee(Constants.ONE_NXT, 0);
    private static final int NEXT_FEE_HEIGHT = Integer.MAX_VALUE;
    private static final Fee NEXT_FEE = new Fee(Constants.ONE_NXT, 0);

    public static TransactionType findTransactionType(byte type, byte subtype) {
        switch (type) {
            case TYPE_PAYMENT:
                switch (subtype) {
                    case SUBTYPE_PAYMENT_ORDINARY_PAYMENT:
                        return Payment.ORDINARY;
                    default:
                        return null;
                }
            case TYPE_MESSAGING:
                switch (subtype) {
                    case SUBTYPE_MESSAGING_ARBITRARY_MESSAGE:
                        return Messaging.ARBITRARY_MESSAGE;
                    case SUBTYPE_MESSAGING_POLL_CREATION:
                        return Messaging.POLL_CREATION;
                    case SUBTYPE_MESSAGING_VOTE_CASTING:
                        return Messaging.VOTE_CASTING;
                    case SUBTYPE_MESSAGING_HUB_ANNOUNCEMENT:
                        return Messaging.HUB_ANNOUNCEMENT;
                    case SUBTYPE_MESSAGING_ACCOUNT_INFO:
                        return Messaging.ACCOUNT_INFO;
                    default:
                        return null;
                }
            case TYPE_ACCOUNT_CONTROL:
                switch (subtype) {
                    case SUBTYPE_ACCOUNT_CONTROL_EFFECTIVE_BALANCE_LEASING:
                        return AccountControl.EFFECTIVE_BALANCE_LEASING;
                    default:
                        return null;
                }
            case TYPE_WORK_CONTROL:
                switch (subtype) {
                    case SUBTYPE_WORK_CONTROL_NEW_TASK:
                        return WorkControl.NEW_TASK;
                    case SUBTYPE_WORK_CONTROL_CANCEL_TASK:
                        return WorkControl.CANCEL_TASK;
                    case SUBTYPE_WORK_CONTROL_PROOF_OF_WORK:
                        return WorkControl.PROOF_OF_WORK;
                    case SUBTYPE_WORK_CONTROL_BOUNTY:
                        return WorkControl.BOUNTY;
                    default:
                        return null;
                }
            default:
                return null;
        }
    }

    TransactionType() {}

    public abstract byte getType();

    public abstract byte getSubtype();

    abstract Attachment.AbstractAttachment parseAttachment(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException;

    abstract Attachment.AbstractAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException;

    abstract void validateAttachment(Transaction transaction) throws NxtException.ValidationException;

    // return false iff double spending
    final boolean applyUnconfirmed(Transaction transaction, Account senderAccount) {
    	
        long totalAmountNQT;
        if(!moneyComesFromNowhere()){
        	totalAmountNQT = Convert.safeAdd(transaction.getAmountNQT(), transaction.getFeeNQT());
        }
        else{
        	totalAmountNQT = transaction.getFeeNQT();
        }
        
        if (transaction.getReferencedTransactionFullHash() != null) {
            totalAmountNQT = Convert.safeAdd(totalAmountNQT, Constants.UNCONFIRMED_POOL_DEPOSIT_NQT);
        }
        if (senderAccount.getUnconfirmedBalanceNQT() < totalAmountNQT
                && !(transaction.getTimestamp() == 0 && Arrays.equals(senderAccount.getPublicKey(), Genesis.CREATOR_PUBLIC_KEY))) {
            return false;
        }
        
        senderAccount.addToUnconfirmedBalanceNQT(-totalAmountNQT);
        
        if (!applyAttachmentUnconfirmed(transaction, senderAccount)) {
        	if(!moneyComesFromNowhere())
        		senderAccount.addToUnconfirmedBalanceNQT(totalAmountNQT);
            return false;
        }
        return true;
    }

    abstract boolean applyAttachmentUnconfirmed(Transaction transaction, Account senderAccount);
    
    
    
    

    final void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {
    	if(!moneyComesFromNowhere())
    		senderAccount.addToBalanceNQT(- (Convert.safeAdd(transaction.getAmountNQT(), transaction.getFeeNQT())));
    	else
    		senderAccount.addToBalanceNQT(- transaction.getFeeNQT());
    	
        if (transaction.getReferencedTransactionFullHash() != null) {
            senderAccount.addToUnconfirmedBalanceNQT(Constants.UNCONFIRMED_POOL_DEPOSIT_NQT);
        }
        if (recipientAccount != null) {
            recipientAccount.addToBalanceAndUnconfirmedBalanceNQT(transaction.getAmountNQT());
        }
        applyAttachment(transaction, senderAccount, recipientAccount);
    }
    
    
    

    abstract void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount);

    final void undoUnconfirmed(Transaction transaction, Account senderAccount) {
        undoAttachmentUnconfirmed(transaction, senderAccount);
        if(!moneyComesFromNowhere())
    		senderAccount.addToUnconfirmedBalanceNQT( (Convert.safeAdd(transaction.getAmountNQT(), transaction.getFeeNQT())));
    	else
    		senderAccount.addToUnconfirmedBalanceNQT( transaction.getFeeNQT());
        if (transaction.getReferencedTransactionFullHash() != null) {
            senderAccount.addToUnconfirmedBalanceNQT(Constants.UNCONFIRMED_POOL_DEPOSIT_NQT);
        }
    }

    abstract void undoAttachmentUnconfirmed(Transaction transaction, Account senderAccount);

    boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String,Boolean>> duplicates) {
        return false;
    }

    boolean isUnconfirmedDuplicate(Transaction transaction, Map<TransactionType, Map<String,Boolean>> duplicates) {
        return false;
    }

    static boolean isDuplicate(TransactionType uniqueType, String key, Map<TransactionType, Map<String, Boolean>> duplicates, boolean exclusive) {
        Map<String,Boolean> typeDuplicates = duplicates.get(uniqueType);
        if (typeDuplicates == null) {
            typeDuplicates = new HashMap<>();
            duplicates.put(uniqueType, typeDuplicates);
        }
        Boolean hasExclusive = typeDuplicates.get(key);
        if (hasExclusive == null) {
            typeDuplicates.put(key, exclusive);
            return false;
        }
        return hasExclusive || exclusive;
    }

    public abstract boolean canHaveRecipient();
    
    public boolean specialDepositTX(){
    	return false;
    }
    
    public boolean moneyComesFromNowhere(){
    	return false;
    }

    public boolean mustHaveRecipient() {
        return canHaveRecipient();
    }

    @Override
    public final String toString() {
        return "type: " + getType() + ", subtype: " + getSubtype();
    }

    /*
    Collection<TransactionType> getPhasingTransactionTypes() {
        return Collections.emptyList();
    }

    Collection<TransactionType> getPhasedTransactionTypes() {
        return Collections.emptyList();
    }
    */

    public static abstract class Payment extends TransactionType {

        private Payment() {
        }

        @Override
        public final byte getType() {
            return TransactionType.TYPE_PAYMENT;
        }

        @Override
        final boolean applyAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            return true;
        }

        @Override
        final void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
        }

        @Override
        final void undoAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
        }

        @Override
        final public boolean canHaveRecipient() {
            return true;
        }

        public static final TransactionType ORDINARY = new Payment() {

            @Override
            public final byte getSubtype() {
                return TransactionType.SUBTYPE_PAYMENT_ORDINARY_PAYMENT;
            }

            @Override
            Attachment.EmptyAttachment parseAttachment(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
                return Attachment.ORDINARY_PAYMENT;
            }

            @Override
            Attachment.EmptyAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
                return Attachment.ORDINARY_PAYMENT;
            }

            @Override
            void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
                if (transaction.getAmountNQT() <= 0 || transaction.getAmountNQT() >= Constants.MAX_BALANCE_NQT) {
                    throw new NxtException.NotValidException("Invalid ordinary payment");
                }
            }

        };

    };
    
    public static abstract class WorkControl extends TransactionType {

        private WorkControl() {
        }

        @Override
        public final byte getType() {
            return TransactionType.TYPE_WORK_CONTROL;
        }

        @Override
        final boolean applyAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            return true;
        }

        @Override
        final void undoAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
        }

        public final static TransactionType NEW_TASK = new WorkControl() {

            @Override
            public final byte getSubtype() {
                return TransactionType.SUBTYPE_WORK_CONTROL_NEW_TASK;
            }

            @Override
            Attachment.WorkCreation parseAttachment(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            	return new Attachment.WorkCreation(buffer, transactionVersion);
            }

            @Override
            Attachment.WorkCreation parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
            	return new Attachment.WorkCreation(attachmentData);
            }

            @Override
            void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
            	Attachment.WorkCreation attachment = (Attachment.WorkCreation) transaction.getAttachment();
            	// To calculate the WorkID i just take the TxID and calculate + 1
            	WorkLogicManager.createNewWork(transaction.getId(), transaction.getId(), transaction.getSenderId(), transaction.getBlockId(), transaction.getAmountNQT(), transaction.getFeeNQT(), attachment);
            }

            @Override
            void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
            	// TODO: Verify if all contraints are met!!!
            }

            @Override
            public boolean canHaveRecipient() {
                return false;
            }
            @Override
            public boolean specialDepositTX(){
				return true;

            }
            @Override
            public boolean mustHaveRecipient() {
                return false;
            }

        };
        
        public final static TransactionType CANCEL_TASK = new WorkControl() {

            @Override
            public final byte getSubtype() {
                return TransactionType.SUBTYPE_WORK_CONTROL_CANCEL_TASK;
            }

            @Override
            Attachment.WorkIdentifierCancellation parseAttachment(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            	return new Attachment.WorkIdentifierCancellation(buffer, transactionVersion);
            }

            @Override
            Attachment.WorkIdentifierCancellation parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
            	return new Attachment.WorkIdentifierCancellation(attachmentData);
            }

            @Override
            void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
            	Attachment.WorkIdentifierCancellation attachment = (Attachment.WorkIdentifierCancellation) transaction.getAttachment();
            	WorkLogicManager.cancelWork(transaction, attachment);
            }

            @Override
            void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
            	Attachment.WorkIdentifierCancellation attachment = (Attachment.WorkIdentifierCancellation) transaction.getAttachment();

            	if(WorkLogicManager.isStillPending(attachment.getWorkId(), transaction.getSenderId()) == false){
            		throw new NxtException.NotValidException("Cannot cancel already cancelled or finished work");
            	}
            	if(WorkLogicManager.getRemainingBalance(attachment.getWorkId()) != transaction.getAmountNQT()){
            		throw new NxtException.NotValidException("The cancellation transaction must replay the entire amount that is left");
            	}
            	if(WorkLogicManager.getTransactionInitiator(attachment.getWorkId()) != transaction.getRecipientId()){
            		throw new NxtException.NotValidException("The receipient must be the work initiator");
            	}
            }

            @Override
            public boolean canHaveRecipient() {
                return true;
            }

            @Override
            public boolean mustHaveRecipient() {
                return true;
            }
            
            public boolean moneyComesFromNowhere(){
            	return true;
            }
        };
        
       
        public final static TransactionType PROOF_OF_WORK = new WorkControl() {

            @Override
            public final byte getSubtype() {
                return TransactionType.SUBTYPE_WORK_CONTROL_PROOF_OF_WORK;
            }

            @Override
            Attachment.PiggybackedProofOfWork parseAttachment(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            	return new Attachment.PiggybackedProofOfWork(buffer, transactionVersion);
            }

            @Override
            Attachment.PiggybackedProofOfWork parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
            	return new Attachment.PiggybackedProofOfWork(attachmentData);
            }

            @Override
            void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
            	Attachment.PiggybackedProofOfWork attachment = (Attachment.PiggybackedProofOfWork) transaction.getAttachment();
            	WorkLogicManager.createNewProofOfWork(attachment.getWorkId(), transaction.getId(), transaction.getSenderId(), transaction.getBlockId(), transaction.getAmountNQT(), attachment);
            }

            @Override
            void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
            	// Validate Bounty
            	Attachment.PiggybackedProofOfBounty attachment = (Attachment.PiggybackedProofOfBounty) transaction.getAttachment();
            	WorkLogicManager.validatePOW(transaction.getId(), attachment, transaction.getAmountNQT());
            }

            @Override
            public boolean canHaveRecipient() {
                return true;
            }

            @Override
            public boolean mustHaveRecipient() {
                return true;
            }
            
            public boolean moneyComesFromNowhere(){
            	return true;
            }
            
        };
        public final static TransactionType BOUNTY = new WorkControl() {

            @Override
            public final byte getSubtype() {
                return TransactionType.SUBTYPE_WORK_CONTROL_BOUNTY;
            }

            @Override
            Attachment.PiggybackedProofOfBounty parseAttachment(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            	return new Attachment.PiggybackedProofOfBounty(buffer, transactionVersion);
            }

            @Override
            Attachment.PiggybackedProofOfBounty parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
            	return new Attachment.PiggybackedProofOfBounty(attachmentData);
            }

            @Override
            void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
            	Attachment.PiggybackedProofOfBounty attachment = (Attachment.PiggybackedProofOfBounty) transaction.getAttachment();
            	WorkLogicManager.createNewBounty(attachment.getWorkId(), transaction.getId(), transaction.getSenderId(), transaction.getBlockId(), transaction.getAmountNQT(), attachment);
            }

            @Override
            void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
                // Validate Bounty
            	Attachment.PiggybackedProofOfBounty attachment = (Attachment.PiggybackedProofOfBounty) transaction.getAttachment();
            	WorkLogicManager.validateBounty(transaction.getId(), attachment, transaction.getAmountNQT());
            }

            @Override
            public boolean canHaveRecipient() {
                return true;
            }

            @Override
            public boolean mustHaveRecipient() {
                return true;
            }
            
            public boolean moneyComesFromNowhere(){
            	return true;
            }
        };
    };
    
        
        
        

    public static abstract class Messaging extends TransactionType {

        private Messaging() {
        }

        @Override
        public final byte getType() {
            return TransactionType.TYPE_MESSAGING;
        }

        @Override
        final boolean applyAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            return true;
        }

        @Override
        final void undoAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
        }

        public final static TransactionType ARBITRARY_MESSAGE = new Messaging() {

            @Override
            public final byte getSubtype() {
                return TransactionType.SUBTYPE_MESSAGING_ARBITRARY_MESSAGE;
            }

            @Override
            Attachment.EmptyAttachment parseAttachment(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
                return Attachment.ARBITRARY_MESSAGE;
            }

            @Override
            Attachment.EmptyAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
                return Attachment.ARBITRARY_MESSAGE;
            }

            @Override
            void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
            }

            @Override
            void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
                Attachment attachment = transaction.getAttachment();
                if (transaction.getAmountNQT() != 0) {
                    throw new NxtException.NotValidException("Invalid arbitrary message: " + attachment.getJSONObject());
                }
                if (transaction.getRecipientId() == Genesis.CREATOR_ID) {
                    throw new NxtException.NotCurrentlyValidException("Sending messages to Genesis not allowed.");
                }
            }

            @Override
            public boolean canHaveRecipient() {
                return true;
            }

            @Override
            public boolean mustHaveRecipient() {
                return false;
            }

        };
        public final static TransactionType POLL_CREATION = new Messaging() {
            @Override
            public final byte getSubtype() {
                return TransactionType.SUBTYPE_MESSAGING_POLL_CREATION;
            }

            @Override
            Attachment.MessagingPollCreation parseAttachment(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
                return new Attachment.MessagingPollCreation(buffer, transactionVersion);
            }

            @Override
            Attachment.MessagingPollCreation parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
                return new Attachment.MessagingPollCreation(attachmentData);
            }

            @Override
            void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
                Attachment.MessagingPollCreation attachment = (Attachment.MessagingPollCreation) transaction.getAttachment();
                Poll.addPoll(transaction, attachment);
            }

            @Override
            void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
                Attachment.MessagingPollCreation attachment = (Attachment.MessagingPollCreation) transaction.getAttachment();
                for (int i = 0; i < attachment.getPollOptions().length; i++) {
                    if (attachment.getPollOptions()[i].length() > Constants.MAX_POLL_OPTION_LENGTH) {
                        throw new NxtException.NotValidException("Invalid poll options length: " + attachment.getJSONObject());
                    }
                }
                if (attachment.getPollName().length() > Constants.MAX_POLL_NAME_LENGTH
                        || attachment.getPollDescription().length() > Constants.MAX_POLL_DESCRIPTION_LENGTH
                        || attachment.getPollOptions().length > Constants.MAX_POLL_OPTION_COUNT) {
                    throw new NxtException.NotValidException("Invalid poll attachment: " + attachment.getJSONObject());
                }
            }

            @Override
            public boolean canHaveRecipient() {
                return false;
            }

        };

        public final static TransactionType VOTE_CASTING = new Messaging() {

            @Override
            public final byte getSubtype() {
                return TransactionType.SUBTYPE_MESSAGING_VOTE_CASTING;
            }

            @Override
            Attachment.MessagingVoteCasting parseAttachment(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
                return new Attachment.MessagingVoteCasting(buffer, transactionVersion);
            }

            @Override
            Attachment.MessagingVoteCasting parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
                return new Attachment.MessagingVoteCasting(attachmentData);
            }

            @Override
            void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
                Attachment.MessagingVoteCasting attachment = (Attachment.MessagingVoteCasting) transaction.getAttachment();
                Poll poll = Poll.getPoll(attachment.getPollId());
                if (poll != null) {
                    Vote.addVote(transaction, attachment);
                }
            }

            @Override
            void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
                Attachment.MessagingVoteCasting attachment = (Attachment.MessagingVoteCasting) transaction.getAttachment();
                if (attachment.getPollId() == 0 || attachment.getPollVote() == null
                        || attachment.getPollVote().length > Constants.MAX_POLL_OPTION_COUNT) {
                    throw new NxtException.NotValidException("Invalid vote casting attachment: " + attachment.getJSONObject());
                }
                if (Poll.getPoll(attachment.getPollId()) == null) {
                    throw new NxtException.NotCurrentlyValidException("Invalid poll: " + Convert.toUnsignedLong(attachment.getPollId()));
                }
            }

            @Override
            public boolean canHaveRecipient() {
                return false;
            }

        };

        public static final TransactionType HUB_ANNOUNCEMENT = new Messaging() {

            @Override
            public final byte getSubtype() {
                return TransactionType.SUBTYPE_MESSAGING_HUB_ANNOUNCEMENT;
            }

            @Override
            Attachment.MessagingHubAnnouncement parseAttachment(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
                return new Attachment.MessagingHubAnnouncement(buffer, transactionVersion);
            }

            @Override
            Attachment.MessagingHubAnnouncement parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
                return new Attachment.MessagingHubAnnouncement(attachmentData);
            }

            @Override
            void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
                Attachment.MessagingHubAnnouncement attachment = (Attachment.MessagingHubAnnouncement) transaction.getAttachment();
                Hub.addOrUpdateHub(transaction, attachment);
            }

            @Override
            void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
               
                Attachment.MessagingHubAnnouncement attachment = (Attachment.MessagingHubAnnouncement) transaction.getAttachment();
                if (attachment.getMinFeePerByteNQT() < 0 || attachment.getMinFeePerByteNQT() > Constants.MAX_BALANCE_NQT
                        || attachment.getUris().length > Constants.MAX_HUB_ANNOUNCEMENT_URIS) {
                    // cfb: "0" is allowed to show that another way to determine the min fee should be used
                    throw new NxtException.NotValidException("Invalid hub terminal announcement: " + attachment.getJSONObject());
                }
                for (String uri : attachment.getUris()) {
                    if (uri.length() > Constants.MAX_HUB_ANNOUNCEMENT_URI_LENGTH) {
                        throw new NxtException.NotValidException("Invalid URI length: " + uri.length());
                    }
                    //TODO: also check URI validity here?
                }
            }

            @Override
            public boolean canHaveRecipient() {
                return false;
            }

        };

        public static final Messaging ACCOUNT_INFO = new Messaging() {

            @Override
            public byte getSubtype() {
                return TransactionType.SUBTYPE_MESSAGING_ACCOUNT_INFO;
            }

            @Override
            Attachment.MessagingAccountInfo parseAttachment(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
                return new Attachment.MessagingAccountInfo(buffer, transactionVersion);
            }

            @Override
            Attachment.MessagingAccountInfo parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
                return new Attachment.MessagingAccountInfo(attachmentData);
            }

            @Override
            void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
                Attachment.MessagingAccountInfo attachment = (Attachment.MessagingAccountInfo)transaction.getAttachment();
                //if (attachment.getVersion() >= 2 && Nxt.getBlockchain().getHeight() < Constants.MONETARY_SYSTEM_BLOCK) {
                if (attachment.getMessagePattern() != null) {
                    throw new NxtException.NotYetEnabledException("Message patterns not yet enabled");
                }
                if (attachment.getName().length() > Constants.MAX_ACCOUNT_NAME_LENGTH
                        || attachment.getDescription().length() > Constants.MAX_ACCOUNT_DESCRIPTION_LENGTH
                        || (attachment.getMessagePattern() != null && attachment.getMessagePattern().pattern().length() > Constants.MAX_ACCOUNT_MESSAGE_PATTERN_LENGTH)
                        ) {
                    throw new NxtException.NotValidException("Invalid account info issuance: " + attachment.getJSONObject());
                }
            }

            @Override
            void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
                Attachment.MessagingAccountInfo attachment = (Attachment.MessagingAccountInfo) transaction.getAttachment();
                senderAccount.setAccountInfo(attachment.getName(), attachment.getDescription(), attachment.getMessagePattern());
            }

            @Override
            public boolean canHaveRecipient() {
                return false;
            }

        };

    }

    public static abstract class AccountControl extends TransactionType {

        private AccountControl() {
        }

        @Override
        public final byte getType() {
            return TransactionType.TYPE_ACCOUNT_CONTROL;
        }

        @Override
        final boolean applyAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            return true;
        }

        @Override
        final void undoAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
        }

        public static final TransactionType EFFECTIVE_BALANCE_LEASING = new AccountControl() {

            @Override
            public final byte getSubtype() {
                return TransactionType.SUBTYPE_ACCOUNT_CONTROL_EFFECTIVE_BALANCE_LEASING;
            }

            @Override
            Attachment.AccountControlEffectiveBalanceLeasing parseAttachment(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
                return new Attachment.AccountControlEffectiveBalanceLeasing(buffer, transactionVersion);
            }

            @Override
            Attachment.AccountControlEffectiveBalanceLeasing parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
                return new Attachment.AccountControlEffectiveBalanceLeasing(attachmentData);
            }

            @Override
            void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
                Attachment.AccountControlEffectiveBalanceLeasing attachment = (Attachment.AccountControlEffectiveBalanceLeasing) transaction.getAttachment();
                Account.getAccount(transaction.getSenderId()).leaseEffectiveBalance(transaction.getRecipientId(), attachment.getPeriod());
            }

            @Override
            void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
                Attachment.AccountControlEffectiveBalanceLeasing attachment = (Attachment.AccountControlEffectiveBalanceLeasing)transaction.getAttachment();
                Account recipientAccount = Account.getAccount(transaction.getRecipientId());
                if (transaction.getSenderId() == transaction.getRecipientId()
                        || transaction.getAmountNQT() != 0
                        || attachment.getPeriod() < 1440) {
                    throw new NxtException.NotValidException("Invalid effective balance leasing: "
                            + transaction.getJSONObject() + " transaction " + transaction.getStringId());
                }
                if (recipientAccount == null
                        || (recipientAccount.getPublicKey() == null && ! transaction.getStringId().equals("5081403377391821646"))) {
                    throw new NxtException.NotCurrentlyValidException("Invalid effective balance leasing: "
                            + " recipient account " + transaction.getRecipientId() + " not found or no public key published");
                }
                if (transaction.getRecipientId() == Genesis.CREATOR_ID) {
                    throw new NxtException.NotCurrentlyValidException("Leasing to Genesis account not allowed");
                }
            }

            @Override
            public boolean canHaveRecipient() {
                return true;
            }

        };

    }

    long minimumFeeNQT(TransactionImpl transaction, int height, int appendagesSize) throws NxtException.NotValidException {
        if (height < BASELINE_FEE_HEIGHT) {
            return 0; // No need to validate fees before baseline block
        }
        Fee fee;
        if (height >= NEXT_FEE_HEIGHT) {
            fee = getNextFee(transaction);
        } else {
            fee = getBaselineFee(transaction);
        }
        return Convert.safeAdd(fee.getConstantFee(), Convert.safeMultiply(appendagesSize, fee.getAppendagesFee()));
    }

    protected Fee getBaselineFee(TransactionImpl transaction) throws NxtException.NotValidException {
        return BASELINE_FEE;
    }

    protected Fee getNextFee(TransactionImpl transaction) throws NxtException.NotValidException {
        return NEXT_FEE;
    }

    public static final class Fee {
        private final long constantFee;
        private final long appendagesFee;

        public Fee(long constantFee, long appendagesFee) {
            this.constantFee = constantFee;
            this.appendagesFee = appendagesFee;
        }

        public long getConstantFee() {
            return constantFee;
        }

        public long getAppendagesFee() {
            return appendagesFee;
        }

        @Override
        public String toString() {
            return "Fee{" +
                    "constantFee=" + constantFee +
                    ", appendagesFee=" + appendagesFee +
                    '}';
        }
    }
}
