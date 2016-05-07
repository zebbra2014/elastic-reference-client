package nxt;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import nxt.Attachment.PiggybackedProofOfBounty;
import nxt.Attachment.PiggybackedProofOfWork;
import nxt.Attachment.WorkCreation;
import nxt.Attachment.WorkIdentifierCancellation;
import nxt.Attachment.WorkIdentifierRefueling;
import nxt.Attachment.WorkUpdate;
import nxt.TransactionProcessor.Event;
import nxt.crypto.Crypto;
import nxt.db.DbIterator;
import nxt.db.DbUtils;
import nxt.util.Logger;

public class WorkLogicManager {

	public static boolean checkWorkLanguage(byte w){
		return true;
		//TODO FIXME: ADD REAL CHECKS HERE
	}
	public static boolean checkDeadline(int deadlineInt) {
		//TODO FIXME: ADD REAL CHECKS HERE
		return true;
	}
	public static boolean checkNumberVariables(byte numberInputVarsByte,
			boolean IsInput) {
		//TODO FIXME: ADD REAL CHECKS HERE
		return true;
	}
	
	public static byte[] compileCode(String code, boolean IsBountyHook, byte programLanguage) throws RuntimeException{
		return new byte[]{0,1,2,3,4,5};
	}
	
	public static void cancelWork(WorkIdentifierCancellation attachment) {
		if (!Db.db.isInTransaction()) {
            try {
                Db.db.beginTransaction();
                cancelWork(attachment);
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
                pstmt.setLong(++i, attachment.getWorkId());
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
	}
	
	public static boolean isStillPending(long workId, long senderId) {	
		
		long payoutSoFar = totalPayoutSoFar(workId);
		
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement(
                     "SELECT count(*) FROM work WHERE id = ? and sender_account_id = ? and payback_transaction_id = NULL and last_payment_transaction_id = NULL and amount > ?")) {
        	int i = 0;
            pstmt.setLong(++i, workId);
            pstmt.setLong(++i, senderId);
            pstmt.setLong(++i, payoutSoFar);
            ResultSet check = pstmt.executeQuery();
            if(check.getInt(0) == 0) return false;
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }
	
	public static long totalPayoutSoFar(long workId) {	
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement(
                     "SELECT SUM(payout_amount) FROM proof_of_work WHERE work_id = ?")) {
        	int i = 0;
            pstmt.setLong(++i, workId);
            ResultSet check = pstmt.executeQuery();
            return check.getLong(0); // TODO: Strange case to avoid overflows
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
        
    }

	public static void createNewWork(long workId, long txId, long senderId, long blockId, long amountNQT, WorkCreation attachment) {
		if (!Db.db.isInTransaction()) {
            try {
                Db.db.beginTransaction();
                createNewWork(workId, txId, senderId, blockId, amountNQT, attachment);
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
        	try (Connection con = Db.db.getConnection(); PreparedStatement pstmt = con.prepareStatement("INSERT INTO work (id, work_title, variables_input, variables_output, version_id, language_id, deadline, amount, referenced_transaction_id, block_id, sender_account_id, code, hook) "
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, workId);
                pstmt.setString(++i, attachment.getWorkTitle());
                pstmt.setShort(++i, attachment.getNumberInputVars());
                pstmt.setShort(++i, attachment.getNumberOutputVars());
                pstmt.setShort(++i, attachment.getVersion());
                pstmt.setShort(++i, attachment.getWorkLanguage());
                pstmt.setInt(++i, attachment.getDeadline());
                pstmt.setLong(++i, amountNQT);
                pstmt.setLong(++i, txId);
                pstmt.setLong(++i, blockId);
                pstmt.setLong(++i, senderId);
                pstmt.setBytes(++i, attachment.getProgrammCode());
                pstmt.setBytes(++i, attachment.getBountyHook());
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

	public static void updateWork(WorkUpdate attachment) {
		
	}

	public static void submitBounty(long senderId,
			PiggybackedProofOfBounty attachment) {
		// TODO, We still have to figure out how bounties are submitted
	}

	public static void refuelWork(WorkIdentifierRefueling attachment, long amountNQT) {
		if (!Db.db.isInTransaction()) {
            try {
                Db.db.beginTransaction();
                refuelWork(attachment, amountNQT);
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
            try (Connection con = Db.db.getConnection(); PreparedStatement pstmt = con.prepareStatement("UPDATE work SET amount = amount + ? WHERE id = ?")) {
                int i = 0;
                pstmt.setLong(++i, attachment.getWorkId());
                pstmt.setLong(++i, amountNQT);
               
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
		
	}
	


}
