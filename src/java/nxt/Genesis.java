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
			"-5156238019438419925")).longValue();

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
	public static final byte[][] GENESIS_SIGNATURES = {
		{105 ,39 ,-121 ,90 ,-53 ,-47 ,92 ,29 ,-115 ,87 ,60 ,36 ,101 ,-95 ,-11 ,-95 ,-124 ,119 ,-109 ,77 ,104 ,-32 ,-81 ,-66 ,-19 ,-46 ,-76 ,122 ,61 ,80 ,43 ,15 ,-105 ,-13 ,75 ,36 ,-9 ,-31 ,-63 ,-125 ,-83 ,103 ,-95 ,13 ,-91 ,-100 ,112 ,-85 ,77 ,106 ,112 ,104 ,127 ,119 ,122 ,42 ,-94 ,-66 ,44 ,49 ,-85 ,112 ,-39 ,-89},
	};

	// And the block itself must be signed
	public static final byte[] GENESIS_BLOCK_SIGNATURE = new byte[]{
		75 ,-123 ,-74 ,-106 ,20 ,117 ,-112 ,114 ,95 ,-62 ,-36 ,-88 ,34 ,93 ,25 ,-98 ,-98 ,42 ,-26 ,124 ,-37 ,-25 ,-109 ,-110 ,105 ,118 ,103 ,13 ,-34 ,83 ,16 ,8 ,-35 ,8 ,82 ,93 ,120 ,44 ,-7 ,19 ,-93 ,-33 ,57 ,-16 ,-39 ,-122 ,-33 ,-102 ,114 ,19 ,68 ,-95 ,48 ,33 ,43 ,-74 ,-75 ,111 ,-48 ,62 ,-96 ,84 ,-3 ,65};

	private Genesis() {
	} // never

	public static void mineGenesis() {
		String genesisSecretKey = "**ANONYMIZED***";
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
					digest.digest(), Genesis.CREATOR_PUBLIC_KEY, new byte[32],
					null, new byte[32], transactions);

			genesisBlock.sign(genesisSecretKey);

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
					+ String.valueOf(genesisBlock.getId()));
			Logger.logMessage("BLOCK SIGNATURES!!!!!!!!!!!!!!!!!!");
			Logger.logMessage(signatures);
			Logger.logMessage("GENESISBLOCK VERIFYBLOCKSIGNATURE: " + String.valueOf(genesisBlock.verifyBlockSignature()));

		} catch (NxtException.ValidationException e) {
			Logger.logMessage(e.getMessage());
			throw new RuntimeException(e.toString(), e);
		}
	}

}
