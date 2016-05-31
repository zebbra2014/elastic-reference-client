package nxt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.json.simple.JSONObject;

import nxt.Attachment.PiggybackedProofOfBounty;
import nxt.Attachment.PiggybackedProofOfWork;
import nxt.Attachment.WorkCreation;
import nxt.Attachment.WorkIdentifierCancellation;
import nxt.crypto.Crypto;
import nxt.db.DbIterator;
import nxt.db.DbUtils;
import nxt.util.Convert;
import nxt.util.Logger;

public class WorkLogicManager {
	
	// Just in case we need it in the future, but i think this can be safely removed
    public static double round(final double value, final int frac) {
        return Math.round(Math.pow(10.0, frac) * value) / Math.pow(10.0, frac);
    }
    
    public static int getCurrentPowReward(){
    	return 10;
    }
    
    public static int getPercentWork(){
    	return 60;
    }
    
    public static int getPercentBounty(){
    	return 40;
    }
    private static String dd(long d){
    	return Convert.toUnsignedLong(d);
   
    }
	private static JSONObject workEntry(byte version, long workId, long referenced_tx, long block_created, long block_closed,
			long cancellation_tx, long last_payment_tx, String title, String account, String language,
			int num_input, int num_output,
			long balance_original, long paid_bounties, long paid_pow, int bounties_connected, int pow_connected, int timeout_at_block, int script_size_bytes, long fee, int block_created_h) {
		JSONObject response = new JSONObject();
		
		response.put("workId", dd(workId));
		response.put("version", version);
		response.put("referenced_tx", dd(referenced_tx));

		response.put("block_created", dd(block_created));
		response.put("block_height_created", block_created_h);
		response.put("block_closed", dd(block_closed));
		response.put("cancellation_tx", dd(cancellation_tx));
		response.put("last_payment_tx", dd(last_payment_tx));
		response.put("title", title);
		response.put("account", account);
		response.put("language", language);
		response.put("num_input", num_input);
		response.put("num_output", num_output);
		response.put("percent_work", getPercentWork());
		response.put("percent_bounties", getPercentBounty());
		response.put("balance_original", dd(balance_original));
		
		//long balance_work = balance_original*getPercentWork()/100-(pow_connected*getCurrentPowReward());
		//long balance_bounties = balance_original*getPercentBounty()/100;
		
		response.put("balance_remained", dd(balance_original-paid_pow-paid_bounties));
		response.put("paid_bounties", dd(paid_bounties));
		response.put("paid_pow", dd(paid_pow));
		
		double done = 100-Math.round(((balance_original-paid_pow-paid_bounties) * 1.0/balance_original)*100.0);
		
		double eff = 0.013;
		response.put("percent_done", done);
		response.put("efficiency", eff);
		response.put("pow_connected", pow_connected);
		response.put("bounties_connected", bounties_connected);
		response.put("timeout_at_block", timeout_at_block);
		response.put("script_size_bytes", script_size_bytes);
		response.put("fee", fee);

		return response;
	}
    public static byte[] compress(String text) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            OutputStream out = new DeflaterOutputStream(baos);
            out.write(text.getBytes("UTF-8"));
            out.close();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return baos.toByteArray();
    }

    public static String decompress(byte[] bytes) {
        InputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[8192];
            int len;
            while((len = in.read(buffer))>0)
                baos.write(buffer, 0, len);
            return new String(baos.toByteArray(), "UTF-8");
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }


	public static byte getLanguageByte(String language){
		if (language.equalsIgnoreCase("LUA")){
			return (byte)0x01;
		}
		return 0;
	}
	public static String getLanguageString(byte language){
		if (language==(byte)0x01){
			return "LUA";
		}
		return "?";
	}
	
	public static boolean checkWorkLanguage(byte w){
		return (w==1); // only allow 1 for now = LUA
	}
	
	public static boolean checkDeadline(int deadlineInt) {
		return (deadlineInt >= 10 && deadlineInt <=250);
	}
	
	public static boolean checkNumberVariables(byte numberInputVarsByte,
			boolean IsInput) {
		return (numberInputVarsByte>=getMinNumberInputInts() && numberInputVarsByte<=getMaxNumberInputInts() && IsInput) || (numberInputVarsByte>=getMinNumberOutputInts() && numberInputVarsByte<=getMaxNumberOutputInts() && !IsInput);
	}
	
	public static int getMaxNumberStateInts(){
		return 16;
	}
	public static int getMinNumberStateInts(){
		return 8;
	}
	public static int getMaxNumberInputInts(){
		return 12;
	}
	public static int getMinNumberInputInts(){
		return 2;
	}
	public static int getMaxNumberOutputInts(){
		return 12;
	}
	public static int getMinNumberOutputInts(){
		return 2;
	}

	public static boolean haveWork(long workId) {
		// TODO, think about caching such things
		 try (Connection con = Db.db.getConnection();
	             PreparedStatement pstmt = con.prepareStatement(
	                     "SELECT COUNT(*) FROM work WHERE id = ?")) {
	        	int i = 0;
	            pstmt.setLong(++i, workId);
	            ResultSet check = pstmt.executeQuery();
	            if (check.next()) {
	            	int result = check.getInt(1);
		            return result>=1;
	            }else{
	            	throw new RuntimeException("Cannot decide if work exists or not");
	            }
	        } catch (SQLException e) {
	            throw new RuntimeException(e.toString(), e);
	        }
	}
	
	public static void cancelWork(Transaction t, WorkIdentifierCancellation attachment) {
		if (!Db.db.isInTransaction()) {
            try {
                Db.db.beginTransaction();
                cancelWork(t,attachment);
                Db.db.commitTransaction();
            } catch (Exception e) {
                Logger.logErrorMessage(e.toString(), e);
                Db.db.rollbackTransaction();
                throw e;
            } finally {
                Db.db.endTransaction();
            }
            return;
        }
        try {
            try (Connection con = Db.db.getConnection(); PreparedStatement pstmt = con.prepareStatement("UPDATE work SET payback_transaction_id = ? WHERE id = ?")) {
                int i = 0;
                pstmt.setLong(++i, t.getId());
                pstmt.setLong(++i, attachment.getWorkId());
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
	}
	
	public static boolean isStillPending(long workId, long senderId) {	
		
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement(
                     "SELECT count(*) FROM work WHERE id = ? and sender_account_id = ? and payback_transaction_id is null and last_payment_transaction_id is null and (paid_amount_bounties+paid_amount_pow) < original_amount")) {
        	int i = 0;
            pstmt.setLong(++i, workId);
            pstmt.setLong(++i, senderId);
            ResultSet check = pstmt.executeQuery();
            if (check.next()) {
            	if(check.getInt(1) == 0) return false;
            }else{
            	throw new RuntimeException("Cannot decide if work is still pending");
            }
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }
	
	public static long totalPayoutSoFar(long workId) {	
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement(
                     "SELECT paid_amount_bounties+paid_amount_pow FROM work WHERE id = ?")) {
        	int i = 0;
            pstmt.setLong(++i, workId);
            ResultSet check = pstmt.executeQuery();
            if (check.next()) {
            	return check.getLong(1); // TODO: Strange case to avoid overflows
            }else{
            	throw new RuntimeException("Cannot get total so-far payouts");
            }
            
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
        
    }

	public static void createNewWork(long workId, long txId, long senderId, long blockId, long amountNQT, long feeNQT, WorkCreation attachment) {
		if (!Db.db.isInTransaction()) {
            try {
                Db.db.beginTransaction();
                createNewWork(workId, txId, senderId, blockId, amountNQT, feeNQT, attachment);
                Db.db.commitTransaction();
            } catch (Exception e) {
                Logger.logErrorMessage(e.toString(), e);
                Db.db.rollbackTransaction();
                throw e;
            } finally {
                Db.db.endTransaction();
            }
            return;
        }
		
        try {
        	try (Connection con = Db.db.getConnection(); PreparedStatement pstmt = con.prepareStatement("INSERT INTO work (id, work_title, variables_input, variables_output, version_id, language_id, deadline, original_amount, paid_amount_bounties, paid_amount_pow, fee, referenced_transaction_id, block_id, sender_account_id, code, num_bounties, num_pow) "
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, workId);
                pstmt.setString(++i, attachment.getWorkTitle());
                pstmt.setShort(++i, attachment.getNumberInputVars());
                pstmt.setShort(++i, attachment.getNumberOutputVars());
                pstmt.setShort(++i, attachment.getVersion());
                pstmt.setShort(++i, attachment.getWorkLanguage());
                pstmt.setInt(++i, attachment.getDeadline());
                pstmt.setLong(++i, amountNQT);
                pstmt.setLong(++i, 0);
                pstmt.setLong(++i, 0);
                pstmt.setLong(++i, feeNQT);
                pstmt.setLong(++i, txId);
                pstmt.setLong(++i, blockId);
                pstmt.setLong(++i, senderId);
                pstmt.setBytes(++i, attachment.getProgrammCode());
                pstmt.setLong(++i, 0);
                pstmt.setLong(++i, 0);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
	}
	
	public static void createNewProofOfWork(long workId, long txId, long senderId, long blockId, long PayOutAmountNQT, PiggybackedProofOfWork attachment) {
		if (!Db.db.isInTransaction()) {
            try {
                Db.db.beginTransaction();
                createNewProofOfWork(workId, txId, senderId, blockId, PayOutAmountNQT, attachment);
                Db.db.commitTransaction();
            } catch (Exception e) {
                Logger.logErrorMessage(e.toString(), e);
                Db.db.rollbackTransaction();
                throw e;
            } finally {
                Db.db.endTransaction();
            }
            return;
        }
        try {
        
        	try (Connection con = Db.db.getConnection(); PreparedStatement pstmt = con.prepareStatement("INSERT INTO work (id, work_id, referenced_transaction_id, block_id, sender_account_id, payout_amount, input, state, ten_ms_locator) "
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, workId);
                pstmt.setLong(++i, txId);
                pstmt.setLong(++i, blockId);
                pstmt.setLong(++i, senderId);
                pstmt.setLong(++i, PayOutAmountNQT);
                
                byte[] input = null;
                byte[] state = null;
                long ten_ms_locator = 0;
                
                // TODO, FILL THOSE FROM ATTACHMENT
                
                pstmt.setBytes(++i, input);
                pstmt.setBytes(++i, state);
                pstmt.setLong(++i, ten_ms_locator);
                
                pstmt.executeUpdate();
            }
        	 
            // at this point it is also required to update any last_payment_transaction_id if necessary
            long payoutSoFar = totalPayoutSoFar(workId);
            
            try (Connection con = Db.db.getConnection(); PreparedStatement pstmt = con.prepareStatement("UPDATE work SET last_payment_transaction_id = ? WHERE id = ? and amount <= ?")) {
                int i = 0;
                pstmt.setLong(++i, txId);
                pstmt.setLong(++i, workId);
                pstmt.setLong(++i, payoutSoFar);
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
	}
	

	public static void createNewBounty(long workId, long txId, long senderId, long blockId, long PayOutAmountNQT, PiggybackedProofOfBounty attachment) {
		if (!Db.db.isInTransaction()) {
            try {
                Db.db.beginTransaction();
                createNewBounty(workId, txId, senderId, blockId, PayOutAmountNQT, attachment);
                Db.db.commitTransaction();
            } catch (Exception e) {
                Logger.logErrorMessage(e.toString(), e);
                Db.db.rollbackTransaction();
                throw e;
            } finally {
                Db.db.endTransaction();
            }
            return;
        }
        try {
        
        	try (Connection con = Db.db.getConnection(); PreparedStatement pstmt = con.prepareStatement("INSERT INTO work (id, work_id, referenced_transaction_id, block_id, sender_account_id, payout_amount, input, state, ten_ms_locator) "
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, workId);
                pstmt.setLong(++i, txId);
                pstmt.setLong(++i, blockId);
                pstmt.setLong(++i, senderId);
                pstmt.setLong(++i, PayOutAmountNQT);
                
                byte[] input = null;
                byte[] state = null;
                long ten_ms_locator = 0;
                
                // TODO, FILL THOSE FROM ATTACHMENT
                
                pstmt.setBytes(++i, input);
                pstmt.setBytes(++i, state);
                pstmt.setLong(++i, ten_ms_locator);
                
                pstmt.executeUpdate();
            }
        	 
            // at this point it is also required to update any last_payment_transaction_id if necessary
            long payoutSoFar = totalPayoutSoFar(workId);
            
            try (Connection con = Db.db.getConnection(); PreparedStatement pstmt = con.prepareStatement("UPDATE work SET last_payment_transaction_id = ? WHERE id = ? and amount <= ?")) {
                int i = 0;
                pstmt.setLong(++i, txId);
                pstmt.setLong(++i, workId);
                pstmt.setLong(++i, payoutSoFar);
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
	}


	public static boolean checkAmount(long amount, String workLanguage,
			String workTitle, String programCode, Byte numberInputVars,
			Byte numberOutputVars, String deadline) {
		return true;
	}
	
    public static DbIterator<JSONObject> getWorkList(Account account, int from, int to, long onlyOneId) {
      
        Connection con = null;
        
        try {
            StringBuilder buf = new StringBuilder();
            buf.append("SELECT * FROM work WHERE sender_account_id = ?");
            
            if(onlyOneId>0){
            	buf.append(" AND id=?");
            }
            
            buf.append(" ORDER BY block_id DESC");
            buf.append(DbUtils.limitsClause(from, to));
            con = Db.db.getConnection();
            PreparedStatement pstmt;
            int i = 0;
            pstmt = con.prepareStatement(buf.toString());
            if(onlyOneId>0){
            	pstmt.setLong(++i, onlyOneId);
            }
            pstmt.setLong(++i, account.getId());
    
            
            return new DbIterator<>(con, pstmt, new DbIterator.ResultSetReader<JSONObject>() {
                @Override
                public JSONObject get(Connection con, ResultSet rs) throws NxtException.ValidationException, SQLException {
                	JSONObject ret = null;
                	
                     long workId = rs.getLong("id");
                     String work_title = rs.getString("work_title");
                     int num_input = rs.getInt("variables_input");
                     int num_output = rs.getInt("variables_output");
                     byte version = rs.getByte("version_id");
                     byte language = rs.getByte("language_id");
                     int deadline = rs.getInt("deadline");
                     long amount = rs.getLong("original_amount");
                     long amount_paid_bounties = rs.getLong("paid_amount_bounties");
                     long amount_paid_pow = rs.getLong("paid_amount_pow");
                     long fee = rs.getLong("fee");
                     long referencedTx = rs.getLong("referenced_transaction_id");
                     long block_id = rs.getLong("block_id");
                     long senderId = rs.getLong("sender_account_id");
                     String languageString = getLanguageString(language);
                     byte[] code = rs.getBytes("code");
                     
                     int num_bounties = rs.getInt("num_bounties");
                     int num_pow = rs.getInt("num_pow");
                     
                     long last_payment = rs.getLong("last_payment_transaction_id");
                     long last_cancel = rs.getLong("payback_transaction_id");
                     
                     int h=BlockchainImpl.getInstance().getBlock(block_id).getHeight();
                     ret = workEntry(version, workId, referencedTx, block_id, last_payment, last_cancel, last_payment, work_title, Crypto.rsEncode(senderId), languageString, num_input, num_output, amount, amount_paid_bounties, amount_paid_pow, num_bounties,
                    		 num_pow, h+deadline, code.length, fee, h);

                     return ret;
                }
            });
        } catch (SQLException e) {
            DbUtils.close(con);
            throw new RuntimeException(e.toString(), e);
        }
    }

	public static long getRemainingBalance(long id) {
		try (Connection con = Db.db.getConnection();
	             PreparedStatement pstmt = con.prepareStatement(
	                     "SELECT (original_amount-(paid_amount_bounties+paid_amount_pow)) as res FROM work WHERE id = ?")) {
	        	int i = 0;
	            pstmt.setLong(++i, id);
	            ResultSet check = pstmt.executeQuery();
	            if (check.next()) {
	            	return check.getLong(1);
	            }else{
	            	throw new RuntimeException("Cannot get remaining balance from DB");
	            }
	            
	        } catch (SQLException e) {
	            throw new RuntimeException(e.toString(), e);
	        }
	}

	public static long getTransactionInitiator(long id) {
		try (Connection con = Db.db.getConnection();
	             PreparedStatement pstmt = con.prepareStatement(
	                     "SELECT sender_account_id FROM work WHERE id = ?")) {
	        	int i = 0;
	            pstmt.setLong(++i, id);
	            ResultSet check = pstmt.executeQuery();
	            if (check.next()) {
	            	return check.getLong(1);
	            }else{
	            	throw new RuntimeException("Cannot get transaction initiator");
	            }
	        } catch (SQLException e) {
	            throw new RuntimeException(e.toString(), e);
	        }
	}

	public static void validatePOW(long id, PiggybackedProofOfBounty attachment, long amount) {

		
	}

	public static void validateBounty(long id,
			PiggybackedProofOfBounty attachment, long amount) {

		
	}

}
