package nxt;

import nxt.crypto.EncryptedData;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.regex.Pattern;

public interface Attachment extends Appendix {

    TransactionType getTransactionType();

    abstract static class AbstractAttachment extends AbstractAppendix implements Attachment {

        private AbstractAttachment(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
        }

        private AbstractAttachment(JSONObject attachmentData) {
            super(attachmentData);
        }

        private AbstractAttachment(int version) {
            super(version);
        }

        private AbstractAttachment() {}

        @Override
        final void validate(Transaction transaction) throws NxtException.ValidationException {
            getTransactionType().validateAttachment(transaction);
        }

        @Override
        final void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {
            getTransactionType().apply(transaction, senderAccount, recipientAccount);
        }

    }

    abstract static class EmptyAttachment extends AbstractAttachment {

        private EmptyAttachment() {
            super(0);
        }

        @Override
        final int getMySize() {
            return 0;
        }

        @Override
        final void putMyBytes(ByteBuffer buffer) {
        }

        @Override
        final void putMyJSON(JSONObject json) {
        }

        @Override
        final boolean verifyVersion(byte transactionVersion) {
            return true;
        }

    }

    public final static EmptyAttachment ORDINARY_PAYMENT = new EmptyAttachment() {

        @Override
        String getAppendixName() {
            return "OrdinaryPayment";
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Payment.ORDINARY;
        }

    };

    // the message payload is in the Appendix
    public final static EmptyAttachment ARBITRARY_MESSAGE = new EmptyAttachment() {

        @Override
        String getAppendixName() {
            return "ArbitraryMessage";
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Messaging.ARBITRARY_MESSAGE;
        }

    };

    public final static class MessagingPollCreation extends AbstractAttachment {

        private final String pollName;
        private final String pollDescription;
        private final String[] pollOptions;
        private final byte minNumberOfOptions, maxNumberOfOptions;
        private final boolean optionsAreBinary;

        MessagingPollCreation(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
            this.pollName = Convert.readString(buffer, buffer.getShort(), Constants.MAX_POLL_NAME_LENGTH);
            this.pollDescription = Convert.readString(buffer, buffer.getShort(), Constants.MAX_POLL_DESCRIPTION_LENGTH);
            int numberOfOptions = buffer.get();
            if (numberOfOptions > Constants.MAX_POLL_OPTION_COUNT) {
                throw new NxtException.NotValidException("Invalid number of poll options: " + numberOfOptions);
            }
            this.pollOptions = new String[numberOfOptions];
            for (int i = 0; i < numberOfOptions; i++) {
                pollOptions[i] = Convert.readString(buffer, buffer.getShort(), Constants.MAX_POLL_OPTION_LENGTH);
            }
            this.minNumberOfOptions = buffer.get();
            this.maxNumberOfOptions = buffer.get();
            this.optionsAreBinary = buffer.get() != 0;
        }

        MessagingPollCreation(JSONObject attachmentData) {
            super(attachmentData);
            this.pollName = ((String) attachmentData.get("name")).trim();
            this.pollDescription = ((String) attachmentData.get("description")).trim();
            JSONArray options = (JSONArray) attachmentData.get("options");
            this.pollOptions = new String[options.size()];
            for (int i = 0; i < pollOptions.length; i++) {
                pollOptions[i] = ((String) options.get(i)).trim();
            }
            this.minNumberOfOptions = ((Long) attachmentData.get("minNumberOfOptions")).byteValue();
            this.maxNumberOfOptions = ((Long) attachmentData.get("maxNumberOfOptions")).byteValue();
            this.optionsAreBinary = (Boolean) attachmentData.get("optionsAreBinary");
        }

        public MessagingPollCreation(String pollName, String pollDescription, String[] pollOptions, byte minNumberOfOptions,
                                     byte maxNumberOfOptions, boolean optionsAreBinary) {
            this.pollName = pollName;
            this.pollDescription = pollDescription;
            this.pollOptions = pollOptions;
            this.minNumberOfOptions = minNumberOfOptions;
            this.maxNumberOfOptions = maxNumberOfOptions;
            this.optionsAreBinary = optionsAreBinary;
        }

        @Override
        String getAppendixName() {
            return "PollCreation";
        }

        @Override
        int getMySize() {
            int size = 2 + Convert.toBytes(pollName).length + 2 + Convert.toBytes(pollDescription).length + 1;
            for (String pollOption : pollOptions) {
                size += 2 + Convert.toBytes(pollOption).length;
            }
            size +=  1 + 1 + 1;
            return size;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            byte[] name = Convert.toBytes(this.pollName);
            byte[] description = Convert.toBytes(this.pollDescription);
            byte[][] options = new byte[this.pollOptions.length][];
            for (int i = 0; i < this.pollOptions.length; i++) {
                options[i] = Convert.toBytes(this.pollOptions[i]);
            }
            buffer.putShort((short)name.length);
            buffer.put(name);
            buffer.putShort((short)description.length);
            buffer.put(description);
            buffer.put((byte) options.length);
            for (byte[] option : options) {
                buffer.putShort((short) option.length);
                buffer.put(option);
            }
            buffer.put(this.minNumberOfOptions);
            buffer.put(this.maxNumberOfOptions);
            buffer.put(this.optionsAreBinary ? (byte)1 : (byte)0);
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            attachment.put("name", this.pollName);
            attachment.put("description", this.pollDescription);
            JSONArray options = new JSONArray();
            if (this.pollOptions != null) {
                Collections.addAll(options, this.pollOptions);
            }
            attachment.put("options", options);
            attachment.put("minNumberOfOptions", this.minNumberOfOptions);
            attachment.put("maxNumberOfOptions", this.maxNumberOfOptions);
            attachment.put("optionsAreBinary", this.optionsAreBinary);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Messaging.POLL_CREATION;
        }

        public String getPollName() { return pollName; }

        public String getPollDescription() { return pollDescription; }

        public String[] getPollOptions() { return pollOptions; }

        public byte getMinNumberOfOptions() { return minNumberOfOptions; }

        public byte getMaxNumberOfOptions() { return maxNumberOfOptions; }

        public boolean isOptionsAreBinary() { return optionsAreBinary; }

    }
 
    public final static class WorkIdentifierRefueling extends AbstractAttachment {

        public long getWorkId() {
			return workId;
		}

		private final long workId;

		WorkIdentifierRefueling(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
            this.workId = buffer.getLong();
        }

		WorkIdentifierRefueling(JSONObject attachmentData) {
            super(attachmentData);
            this.workId = Convert.parseUnsignedLong((String)attachmentData.get("id"));
        }

        public WorkIdentifierRefueling(long workId) {
            this.workId = workId;
        }

        @Override
        String getAppendixName() {
            return "WorkIdentifierRefueling";
        }

        @Override
        int getMySize() {
            return 8;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(this.workId);
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            attachment.put("id", Convert.toUnsignedLong(this.workId));
        }

        @Override
        public TransactionType getTransactionType() {
        	return TransactionType.WorkControl.REFUEL_TASK;
        }
    }
    
    public final static class WorkIdentifierCancellation extends AbstractAttachment {

        public long getWorkId() {
			return workId;
		}

		private final long workId;

		WorkIdentifierCancellation(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
            this.workId = buffer.getLong();
        }

		WorkIdentifierCancellation(JSONObject attachmentData) {
            super(attachmentData);
            this.workId = Convert.parseUnsignedLong((String)attachmentData.get("id"));
        }

        public WorkIdentifierCancellation(long workId) {
            this.workId = workId;
        }

        @Override
        String getAppendixName() {
            return "WorkIdentifierCancellation";
        }

        @Override
        int getMySize() {
            return 8;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(this.workId);
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            attachment.put("id", Convert.toUnsignedLong(this.workId));
        }

        @Override
        public TransactionType getTransactionType() {
        	return TransactionType.WorkControl.CANCEL_TASK;
        }
    }
    
    public final static class PiggybackedProofOfWork extends AbstractAttachment {

        public long getWorkId() {
			return workId;
		}

		private final long workId;
		private final short index_10ms_block;
		

		PiggybackedProofOfWork(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
            this.workId = buffer.getLong();
            this.index_10ms_block = buffer.getShort();
        }

		PiggybackedProofOfWork(JSONObject attachmentData) {
            super(attachmentData);
            this.workId = Convert.parseUnsignedLong((String)attachmentData.get("id"));
            this.index_10ms_block = (short)attachmentData.get("msblock");
        }

        public PiggybackedProofOfWork(long workId, short index_10ms_block) {
            this.workId = workId;
            this.index_10ms_block = index_10ms_block;
        }

        @Override
        String getAppendixName() {
            return "PiggybackedProofOfWork";
        }

        @Override
        int getMySize() {
            return 8 + 4;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(this.workId);
            buffer.putShort(this.index_10ms_block);
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            attachment.put("id", Convert.toUnsignedLong(this.workId));
            attachment.put("msblock", Convert.toUnsignedLong(this.index_10ms_block));
        }

        @Override
        public TransactionType getTransactionType() {
        	return TransactionType.WorkControl.PROOF_OF_WORK;
        }
    }
    
    public final static class PiggybackedProofOfBounty extends AbstractAttachment {

        public long getWorkId() {
			return workId;
		}

		private final long workId;
		private final short index_10ms_block;
		

		PiggybackedProofOfBounty(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
            this.workId = buffer.getLong();
            this.index_10ms_block = buffer.getShort();
        }

		PiggybackedProofOfBounty(JSONObject attachmentData) {
            super(attachmentData);
            this.workId = Convert.parseUnsignedLong((String)attachmentData.get("id"));
            this.index_10ms_block = (short)attachmentData.get("msblock");
        }

        public PiggybackedProofOfBounty(long workId, short index_10ms_block) {
            this.workId = workId;
            this.index_10ms_block = index_10ms_block;
        }

        @Override
        String getAppendixName() {
            return "PiggybackedProofOfWork";
        }

        @Override
        int getMySize() {
            return 8 + 4;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(this.workId);
            buffer.putShort(this.index_10ms_block);
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            attachment.put("id", Convert.toUnsignedLong(this.workId));
            attachment.put("msblock", Convert.toUnsignedLong(this.index_10ms_block));
        }

        @Override
        public TransactionType getTransactionType() {
        	return TransactionType.WorkControl.BOUNTY;
        }
    }

    public final static class WorkCreation extends AbstractAttachment {

        private final String workTitle;
		private final byte workLanguage;
        private final byte[] programmCode;
        private final byte[] bountyHook;
        private final byte numberInputVars, numberOutputVars;
        private final int deadline;

        WorkCreation(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
            this.workTitle = Convert.readString(buffer, buffer.getShort(), Constants.MAX_POLL_NAME_LENGTH);
            this.workLanguage = buffer.get();
            int codeLength = (buffer.get()<<8) | buffer.get();
            if (codeLength > Constants.MAX_WORK_CODE_LENGTH) {
                throw new NxtException.NotValidException("Invalid source code length: " + codeLength);
            }
            this.programmCode = new byte[codeLength];
            buffer.get(this.programmCode, 0, this.programmCode.length);
            
            int bountyHookLength = (buffer.get()<<8) | buffer.get();
            if (bountyHookLength > Constants.MAX_BOUNTY_CODE_LENGTH) {
                throw new NxtException.NotValidException("Invalid bounty hook code length: " + bountyHookLength);
            }
            this.bountyHook = new byte[bountyHookLength];
            buffer.get(this.bountyHook, 0, this.bountyHook.length);
          
            this.numberInputVars = buffer.get();
            this.numberOutputVars = buffer.get();
            
            this.deadline = buffer.getInt();
        }

        WorkCreation(JSONObject attachmentData) {
            super(attachmentData);
            
            this.workTitle = ((String) attachmentData.get("title")).trim();
            this.workLanguage = ((Long) attachmentData.get("language")).byteValue();
            this.programmCode = Ascii85.decode(((String) attachmentData.get("programCode")).trim());
            this.bountyHook = Ascii85.decode(((String) attachmentData.get("bountyCode")).trim());          
            this.numberInputVars = ((Long) attachmentData.get("numInputs")).byteValue();
            this.numberOutputVars = ((Long) attachmentData.get("numOutputs")).byteValue();
            this.deadline = ((Long) attachmentData.get("deadline")).byteValue();
            
        }

        public WorkCreation(String workTitle, byte workLanguage, byte[] programmCode, byte[] bountyHook,
                                     byte numberInputVars, byte numberOutputVars, int deadline) {
        	this.workTitle = workTitle;
            this.workLanguage = workLanguage;
            this.programmCode = programmCode;
            this.bountyHook = bountyHook;          
            this.numberInputVars = numberInputVars;
            this.numberOutputVars = numberOutputVars;
            this.deadline = deadline;
        }

        @Override
        String getAppendixName() {
            return "WorkCreation";
        }

        @Override
        int getMySize() {
            int size = 2 + Convert.toBytes(workTitle).length + 1 + 2 + this.programmCode.length + 2 + this.bountyHook.length + 1 + 1 + 4;
            return size;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            byte[] name = Convert.toBytes(this.workTitle);
            
            buffer.putShort((short)name.length);
            buffer.put(name);
            
            buffer.put((byte) this.workLanguage);
           
            buffer.put((byte) this.programmCode.length);
            buffer.put(this.programmCode);
            
            buffer.put((byte) this.bountyHook.length);
            buffer.put(this.bountyHook);
            
            buffer.put(this.numberInputVars);
            buffer.put(this.numberOutputVars);
            
            buffer.putInt(this.deadline);
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            attachment.put("title", this.workTitle);
            attachment.put("language", this.workLanguage);
            attachment.put("programCode", Ascii85.encode(this.programmCode));
            attachment.put("bountyCode", Ascii85.encode(this.bountyHook));
            attachment.put("numInputs", this.numberInputVars);
            attachment.put("numOutputs", this.numberOutputVars);
            attachment.put("deadline", this.deadline);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.WorkControl.NEW_TASK;
        }

        public String getWorkTitle() {
			return workTitle;
		}

		public byte getWorkLanguage() {
			return workLanguage;
		}

		public byte[] getProgrammCode() {
			return programmCode;
		}

		public byte[] getBountyHook() {
			return bountyHook;
		}

		public byte getNumberInputVars() {
			return numberInputVars;
		}

		public byte getNumberOutputVars() {
			return numberOutputVars;
		}

		public int getDeadline() {
			return deadline;
		}

    }
    
    public final static class WorkUpdate extends AbstractAttachment {
    	private final long workId;
        private final String workTitle;
		private final byte workLanguage;
        private final byte[] programmCode;
        private final byte[] bountyHook;
        private final byte numberInputVars, numberOutputVars;

        WorkUpdate(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
            this.workId = buffer.getLong();
            this.workTitle = Convert.readString(buffer, buffer.getShort(), Constants.MAX_POLL_NAME_LENGTH);
            this.workLanguage = buffer.get();
            int codeLength = (buffer.get()<<8) | buffer.get();
            if (codeLength > Constants.MAX_WORK_CODE_LENGTH) {
                throw new NxtException.NotValidException("Invalid source code length: " + codeLength);
            }
            this.programmCode = new byte[codeLength];
            buffer.get(this.programmCode, 0, this.programmCode.length);
            
            int bountyHookLength = (buffer.get()<<8) | buffer.get();
            if (bountyHookLength > Constants.MAX_BOUNTY_CODE_LENGTH) {
                throw new NxtException.NotValidException("Invalid bounty hook code length: " + bountyHookLength);
            }
            this.bountyHook = new byte[bountyHookLength];
            buffer.get(this.bountyHook, 0, this.bountyHook.length);
          
            this.numberInputVars = buffer.get();
            this.numberOutputVars = buffer.get();
        }

        WorkUpdate(JSONObject attachmentData) {
            super(attachmentData);
            this.workId = ((Long) attachmentData.get("id")).byteValue();
            this.workTitle = ((String) attachmentData.get("title")).trim();
            this.workLanguage = ((Long) attachmentData.get("language")).byteValue();
            this.programmCode = Ascii85.decode(((String) attachmentData.get("programCode")).trim());
            this.bountyHook = Ascii85.decode(((String) attachmentData.get("bountyCode")).trim());          
            this.numberInputVars = ((Long) attachmentData.get("numInputs")).byteValue();
            this.numberOutputVars = ((Long) attachmentData.get("numOutputs")).byteValue();
        }

        public WorkUpdate(long workId, String workTitle, byte workLanguage, byte[] programmCode, byte[] bountyHook,
                                     byte numberInputVars, byte numberOutputVars) {
        	this.workId = workId;
        	this.workTitle = workTitle;
            this.workLanguage = workLanguage;
            this.programmCode = programmCode;
            this.bountyHook = bountyHook;          
            this.numberInputVars = numberInputVars;
            this.numberOutputVars = numberOutputVars;
        }

        @Override
        String getAppendixName() {
            return "WorkUpdate";
        }

        @Override
        int getMySize() {
            int size = 8 + 2 + Convert.toBytes(workTitle).length + 1 + 2 + this.programmCode.length + 2 + this.bountyHook.length + 1 + 1;
            return size;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            byte[] name = Convert.toBytes(this.workTitle);
            
            buffer.putLong(this.workId);
            buffer.putShort((short)name.length);
            buffer.put(name);
            
            buffer.put((byte) this.workLanguage);
           
            buffer.put((byte) this.programmCode.length);
            buffer.put(this.programmCode);
            
            buffer.put((byte) this.bountyHook.length);
            buffer.put(this.bountyHook);
            
            buffer.put(this.numberInputVars);
            buffer.put(this.numberOutputVars);
        }

        @Override
        void putMyJSON(JSONObject attachment) {
        	attachment.put("id", this.workId);
            attachment.put("title", this.workTitle);
            attachment.put("language", this.workLanguage);
            attachment.put("programCode", Ascii85.encode(this.programmCode));
            attachment.put("bountyCode", Ascii85.encode(this.bountyHook));
            attachment.put("numInputs", this.numberInputVars);
            attachment.put("numOutputs", this.numberOutputVars);
        }

		@Override
        public TransactionType getTransactionType() {
            return TransactionType.WorkControl.UPDATE_TASK;
        }
		
		public long getWorkId() {
			return workId;
		}

        public String getWorkTitle() {
			return workTitle;
		}

		public byte getWorkLanguage() {
			return workLanguage;
		}

		public byte[] getProgrammCode() {
			return programmCode;
		}

		public byte[] getBountyHook() {
			return bountyHook;
		}

		public byte getNumberInputVars() {
			return numberInputVars;
		}

		public byte getNumberOutputVars() {
			return numberOutputVars;
		}

    }
    
    
    

    public final static class MessagingVoteCasting extends AbstractAttachment {

        private final long pollId;
        private final byte[] pollVote;

        MessagingVoteCasting(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
            this.pollId = buffer.getLong();
            int numberOfOptions = buffer.get();
            if (numberOfOptions > Constants.MAX_POLL_OPTION_COUNT) {
                throw new NxtException.NotValidException("Error parsing vote casting parameters");
            }
            this.pollVote = new byte[numberOfOptions];
            buffer.get(pollVote);
        }

        MessagingVoteCasting(JSONObject attachmentData) {
            super(attachmentData);
            this.pollId = Convert.parseUnsignedLong((String)attachmentData.get("pollId"));
            JSONArray vote = (JSONArray)attachmentData.get("vote");
            this.pollVote = new byte[vote.size()];
            for (int i = 0; i < pollVote.length; i++) {
                pollVote[i] = ((Long) vote.get(i)).byteValue();
            }
        }

        public MessagingVoteCasting(long pollId, byte[] pollVote) {
            this.pollId = pollId;
            this.pollVote = pollVote;
        }

        @Override
        String getAppendixName() {
            return "VoteCasting";
        }

        @Override
        int getMySize() {
            return 8 + 1 + this.pollVote.length;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(this.pollId);
            buffer.put((byte) this.pollVote.length);
            buffer.put(this.pollVote);
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            attachment.put("pollId", Convert.toUnsignedLong(this.pollId));
            JSONArray vote = new JSONArray();
            if (this.pollVote != null) {
                for (byte aPollVote : this.pollVote) {
                    vote.add(aPollVote);
                }
            }
            attachment.put("vote", vote);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Messaging.VOTE_CASTING;
        }

        public long getPollId() { return pollId; }

        public byte[] getPollVote() { return pollVote; }

    }

    public final static class MessagingHubAnnouncement extends AbstractAttachment {

        private final long minFeePerByteNQT;
        private final String[] uris;

        MessagingHubAnnouncement(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
            this.minFeePerByteNQT = buffer.getLong();
            int numberOfUris = buffer.get();
            if (numberOfUris > Constants.MAX_HUB_ANNOUNCEMENT_URIS) {
                throw new NxtException.NotValidException("Invalid number of URIs: " + numberOfUris);
            }
            this.uris = new String[numberOfUris];
            for (int i = 0; i < uris.length; i++) {
                uris[i] = Convert.readString(buffer, buffer.getShort(), Constants.MAX_HUB_ANNOUNCEMENT_URI_LENGTH);
            }
        }

        MessagingHubAnnouncement(JSONObject attachmentData) throws NxtException.NotValidException {
            super(attachmentData);
            this.minFeePerByteNQT = (Long) attachmentData.get("minFeePerByte");
            try {
                JSONArray urisData = (JSONArray) attachmentData.get("uris");
                this.uris = new String[urisData.size()];
                for (int i = 0; i < uris.length; i++) {
                    uris[i] = (String) urisData.get(i);
                }
            } catch (RuntimeException e) {
                throw new NxtException.NotValidException("Error parsing hub terminal announcement parameters", e);
            }
        }

        public MessagingHubAnnouncement(long minFeePerByteNQT, String[] uris) {
            this.minFeePerByteNQT = minFeePerByteNQT;
            this.uris = uris;
        }

        @Override
        String getAppendixName() {
            return "HubAnnouncement";
        }

        @Override
        int getMySize() {
            int size = 8 + 1;
            for (String uri : uris) {
                size += 2 + Convert.toBytes(uri).length;
            }
            return size;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(minFeePerByteNQT);
            buffer.put((byte) uris.length);
            for (String uri : uris) {
                byte[] uriBytes = Convert.toBytes(uri);
                buffer.putShort((short)uriBytes.length);
                buffer.put(uriBytes);
            }
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            attachment.put("minFeePerByteNQT", minFeePerByteNQT);
            JSONArray uris = new JSONArray();
            Collections.addAll(uris, this.uris);
            attachment.put("uris", uris);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Messaging.HUB_ANNOUNCEMENT;
        }

        public long getMinFeePerByteNQT() {
            return minFeePerByteNQT;
        }

        public String[] getUris() {
            return uris;
        }

    }

    public final static class MessagingAccountInfo extends AbstractAttachment {

        private final String name;
        private final String description;
        private final Pattern messagePattern;

        MessagingAccountInfo(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
            this.name = Convert.readString(buffer, buffer.get(), Constants.MAX_ACCOUNT_NAME_LENGTH);
            this.description = Convert.readString(buffer, buffer.getShort(), Constants.MAX_ACCOUNT_DESCRIPTION_LENGTH);
            if (getVersion() < 2) {
                this.messagePattern = null;
            } else {
                String regex = Convert.readString(buffer, buffer.getShort(), Constants.MAX_ACCOUNT_MESSAGE_PATTERN_LENGTH);
                if (regex.length() > 0) {
                    int flags = buffer.getInt();
                    this.messagePattern = Pattern.compile(regex, flags);
                } else {
                    this.messagePattern = null;
                }
            }
        }

        MessagingAccountInfo(JSONObject attachmentData) {
            super(attachmentData);
            this.name = Convert.nullToEmpty((String) attachmentData.get("name"));
            this.description = Convert.nullToEmpty((String) attachmentData.get("description"));
            if (getVersion() < 2) {
                this.messagePattern = null;
            } else {
                String regex = Convert.emptyToNull((String)attachmentData.get("messagePatternRegex"));
                if (regex != null) {
                    int flags = ((Long) attachmentData.get("messagePatternFlags")).intValue();
                    this.messagePattern = Pattern.compile(regex, flags);
                } else {
                    this.messagePattern = null;
                }
            }
        }

        public MessagingAccountInfo(String name, String description) {
            super(1);
            this.name = name;
            this.description = description;
            this.messagePattern = null;
        }

        /*
        public MessagingAccountInfo(String name, String description, Pattern messagePattern) {
            super(messagePattern == null ? 1 : 2);
            this.name = name;
            this.description = description;
            this.messagePattern = messagePattern;
        }
        */

        @Override
        String getAppendixName() {
            return "AccountInfo";
        }

        @Override
        int getMySize() {
            return 1 + Convert.toBytes(name).length + 2 + Convert.toBytes(description).length +
                    (getVersion() < 2 ? 0 : 2 + (messagePattern == null ? 0 : Convert.toBytes(messagePattern.pattern()).length + 4));
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            byte[] name = Convert.toBytes(this.name);
            byte[] description = Convert.toBytes(this.description);
            buffer.put((byte)name.length);
            buffer.put(name);
            buffer.putShort((short) description.length);
            buffer.put(description);
            if (getVersion() >=2 ) {
                if (messagePattern == null) {
                    buffer.putShort((short)0);
                } else {
                    byte[] regexBytes = Convert.toBytes(messagePattern.pattern());
                    buffer.putShort((short) regexBytes.length);
                    buffer.put(regexBytes);
                    buffer.putInt(messagePattern.flags());
                }
            }
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            attachment.put("name", name);
            attachment.put("description", description);
            if (messagePattern != null) {
                attachment.put("messagePatternRegex", messagePattern.pattern());
                attachment.put("messagePatternFlags", messagePattern.flags());
            }
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Messaging.ACCOUNT_INFO;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Pattern getMessagePattern() {
            return messagePattern;
        }

    }

    public final static class AccountControlEffectiveBalanceLeasing extends AbstractAttachment {

        private final short period;

        AccountControlEffectiveBalanceLeasing(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.period = buffer.getShort();
        }

        AccountControlEffectiveBalanceLeasing(JSONObject attachmentData) {
            super(attachmentData);
            this.period = ((Long) attachmentData.get("period")).shortValue();
        }

        public AccountControlEffectiveBalanceLeasing(short period) {
            this.period = period;
        }

        @Override
        String getAppendixName() {
            return "EffectiveBalanceLeasing";
        }

        @Override
        int getMySize() {
            return 2;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.putShort(period);
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            attachment.put("period", period);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.AccountControl.EFFECTIVE_BALANCE_LEASING;
        }

        public short getPeriod() {
            return period;
        }
    }

}
