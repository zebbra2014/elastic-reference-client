package nxt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;

import nxt.Attachment.PiggybackedProofOfBounty;
import nxt.Attachment.PiggybackedProofOfWork;
import nxt.Attachment.WorkCreation;
import nxt.Attachment.WorkIdentifierCancellation;
import nxt.Attachment.WorkIdentifierRefueling;
import nxt.Attachment.WorkUpdate;
import nxt.TransactionProcessor.Event;
import nxt.db.DbUtils;
import nxt.util.Logger;

public class WorkLogicManager {

	/* apply("CREATE TABLE IF NOT EXISTS work (db_id IDENTITY, id BIGINT NOT NULL, work_title VARCHAR NOT NULL, variables_input INT NOT NULL, variables_output INT NOT NULL, language_id INT NOT NULL"
    + "deadline INT NOT NULL, amount INT NOT NULL, referenced_transaction_id BIGINT, block_id BIGINT NOT NULL, FOREIGN KEY (block_id) REFERENCES block (id) ON DELETE CASCADE, block_height INT NOT NULL, "
    + "sender_account_id BIGINT NOT NULL, code OTHER, hook OTHER, cancelled_at_height INT NOT NULL, payback_transaction_id INT NOT NULL, last_payment_transaction_id INT NOT NULL)"); */

	public static void cancelWork(long senderId, WorkIdentifierCancellation attachment) {
		
	}

	public static void refuelWork(WorkIdentifierRefueling attachment,
			long amountNQT) {
		
	}

	public static void createNewWork(long workId, long txId, long senderId, long blockId, int height, long amountNQT, WorkCreation attachment) {
		if (!Db.db.isInTransaction()) {
            try {
                Db.db.beginTransaction();
                createNewWork(workId, txId, senderId, blockId, height, amountNQT, attachment);
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

	public static void updateWork(WorkUpdate attachment) {
		
	}

	public static void submitProofOfWork(long senderId,
			PiggybackedProofOfWork attachment) {		
	}

	public static void submitBounty(long senderId,
			PiggybackedProofOfBounty attachment) {
		
	}

}
