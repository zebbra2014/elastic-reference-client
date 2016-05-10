package nxt;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nxt.crypto.Crypto;
import nxt.util.Logger;

public final class Genesis {

	public static final long GENESIS_BLOCK_ID = (new BigInteger(
			"-8560601219353650019")).longValue();

	// The Account ID which distributes the Genesis coins.
	public static final long CREATOR_ID = 5177795918244458805L;

	// The public key of this account
	public static final byte[] CREATOR_PUBLIC_KEY = { (byte) 0xf5, (byte) 0x22,
			(byte) 0x3c, (byte) 0xd5, (byte) 0xc1, (byte) 0xb6, (byte) 0xff,
			(byte) 0xa2, (byte) 0x2a, (byte) 0x38, (byte) 0xa0, (byte) 0xb6,
			(byte) 0x83, (byte) 0x59, (byte) 0x13, (byte) 0x2a, (byte) 0x12,
			(byte) 0x20, (byte) 0x44, (byte) 0xda, (byte) 0x80, (byte) 0x0b,
			(byte) 0xc0, (byte) 0xe0, (byte) 0x55, (byte) 0x89, (byte) 0x15,
			(byte) 0xa6, (byte) 0x7d, (byte) 0x6d, (byte) 0xc0, (byte) 0x02 };

	// Here, all the Genesis Coin Receipients are listed
	public static final long[] GENESIS_RECIPIENTS = { (new BigInteger(
			"4273301882745002507")).longValue() };

	// And here the same amounts in the correct order
	public static final int[] GENESIS_AMOUNTS = { 5000000 };

	// All payment signatures from genesis must be signed correctly, namely here
	public static final byte[][] GENESIS_SIGNATURES = { { -37, -71, 114, 16,
			-4, 95, -61, 28, 89, 113, -6, 47, 2, 28, 116, -71, -101, 20, 6,
			-103, 7, -53, 35, 115, 51, 47, -26, 25, -41, 100, 72, 4, -88, -33,
			-125, -42, 115, 83, 3, 54, 48, -15, -52, -21, 73, -126, -110, 33,
			104, 3, 126, 22, 58, 121, -35, 101, -25, 62, 116, -26, -17, -89,
			88, -112 } };

	// And the block itself must be signed
	public static final byte[] GENESIS_BLOCK_SIGNATURE = new byte[] { -95, 87,
			-91, -59, 114, 107, 27, -41, -72, -7, -54, -33, -21, -36, 66, -78,
			-24, -75, -71, 69, -114, -59, 2, -87, 43, -66, 61, 19, -41, 111,
			54, 4, 86, 115, 106, -4, -1, -70, -110, -59, 35, -42, 54, 70, 56,
			53, -55, 7, 11, 125, 41, 59, 94, 49, -67, 12, -126, 83, -115, -1,
			23, -76, -47, -44 };

	private Genesis() {
	} // never

	public static void mineGenesis() {
		String genesisSecretKey = "REMOVED FROM PUBLIC";
		try {
			List<TransactionImpl> transactions = new ArrayList<>();
			String signatures = "{\n";
			for (int i = 0; i < Genesis.GENESIS_RECIPIENTS.length; i++) {
				TransactionImpl transaction = new TransactionImpl.BuilderImpl(
						(byte) 0, Genesis.CREATOR_PUBLIC_KEY,
						Genesis.GENESIS_AMOUNTS[i] * Constants.ONE_NXT, 0,
						(short) 0, Attachment.ORDINARY_PAYMENT).timestamp(0)
						.recipientId(Genesis.GENESIS_RECIPIENTS[i]).height(0)
						.ecBlockHeight(0).ecBlockId(0).build();
				transaction.sign(genesisSecretKey);

				signatures += "{";
				for (int s = 0; s < transaction.getSignature().length; ++s) {
					signatures += String.valueOf((int) transaction
							.getSignature()[s]);
					if (s < transaction.getSignature().length - 1) {
						signatures += " ,";
					}
				}
				signatures += "},\n";
				transactions.add(transaction);

			}
			signatures += "}\n";
			Logger.logMessage("TX SIGNATURES!!!!!!!!!!!!!!!!!!");
			Logger.logMessage(signatures);

			Collections.sort(transactions, new Comparator<TransactionImpl>() {
				@Override
				public int compare(TransactionImpl o1, TransactionImpl o2) {
					return Long.compare(o1.getId(), o2.getId());
				}
			});
			MessageDigest digest = Crypto.sha256();
			for (Transaction transaction : transactions) {
				digest.update(transaction.getBytes());
			}
			BlockImpl genesisBlock = new BlockImpl(-1, 0, 0,
					Constants.MAX_BALANCE_NQT, 0, transactions.size() * 128,
					digest.digest(), Genesis.CREATOR_PUBLIC_KEY, new byte[64],
					null, null, transactions);

			genesisBlock.sign(genesisSecretKey);
			genesisBlock.setPrevious(null);

			signatures = "{\n";
			for (int s = 0; s < genesisBlock.getBlockSignature().length; ++s) {
				signatures += String.valueOf((int) genesisBlock
						.getBlockSignature()[s]);
				if (s < genesisBlock.getBlockSignature().length - 1) {
					signatures += " ,";
				}
			}
			signatures += "}\n";
			Logger.logMessage("Genesisblock will have ID: "
					+ genesisBlock.getStringId());
			Logger.logMessage("BLOCK SIGNATURES!!!!!!!!!!!!!!!!!!");
			Logger.logMessage(signatures);

		} catch (NxtException.ValidationException e) {
			Logger.logMessage(e.getMessage());
			throw new RuntimeException(e.toString(), e);
		}
	}

}
