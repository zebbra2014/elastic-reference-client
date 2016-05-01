package nxt;

import nxt.Attachment.PiggybackedProofOfBounty;
import nxt.Attachment.PiggybackedProofOfWork;
import nxt.Attachment.WorkCreation;
import nxt.Attachment.WorkIdentifierCancellation;
import nxt.Attachment.WorkIdentifierRefueling;
import nxt.Attachment.WorkUpdate;

public class WorkLogicManager {


	public static void cancelWork(long senderId, WorkIdentifierCancellation attachment) {
		
	}

	public static void refuelWork(WorkIdentifierRefueling attachment,
			long amountNQT) {
		
	}

	public static void createNewWork(long amountNQT, WorkCreation attachment) {
		
	}

	public static void updateWork(WorkUpdate attachment) {
		
	}

	public static void submitProofOfWork(long senderId,
			PiggybackedProofOfWork attachment) {
		// TODO Auto-generated method stub
		
	}

	public static void submitBounty(long senderId,
			PiggybackedProofOfBounty attachment) {
		// TODO Auto-generated method stub
		
	}

}
