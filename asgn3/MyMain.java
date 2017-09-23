import java.lang.Exception;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.util.ArrayList;

//package kareltherobot;

public class Main {
	private static KeyPair getKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException
	{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(512);
		return keyGen.generateKeyPair();
	}

	private static void showAllUTXOInBlockChain(BlockChain bc) {
		UTXOPool unspent = bc.getMaxHeightUTXOPool();
		ArrayList<UTXO> allUTXO = unspent.getAllUTXO();
		if (allUTXO.size() == 0){
			System.out.println("No UTXO?");
		} else {
		}
	}

	public static void main(String [ ] args) throws NoSuchAlgorithmException, NoSuchProviderException
	{
		ArrayList<KeyPair> keyPairs = new ArrayList();
		for (int i = 0 ; i < 10 ; i++) {
			keyPairs.add(getKeyPair());
		}
		PublicKey rootKey = keyPairs.get(0).getPublic();
		Block genesisBlock = new Block(null, rootKey);
		genesisBlock.finalize();
		BlockChain bc = new BlockChain(genesisBlock);
		BlockHandler bh = new BlockHandler(bc);

		boolean ret = false;

		// Start the test!
		Block anotherGenesisBlock = new Block(null, rootKey);

		ret = bh.processBlock(anotherGenesisBlock);
		System.out.println(ret);
		if (ret) {
			System.out.println("Should not able to add genesisBlock");
			return;
		}

		System.out.println("Passed adding another genesisBlock which should return a `false`.");

		showAllUTXOInBlockChain(bc);

		// Start another test
		Block blockWithNoTransaction = new Block(genesisBlock.getHash(), keyPairs.get(1).getPublic());
		blockWithNoTransaction.finalize();
		System.out.println(blockWithNoTransaction);
		ret = bh.processBlock(blockWithNoTransaction);
		if (!ret) {
			System.out.println("[FAIL] Should be able to add block with no transactions.");
			return;
		}
		System.out.println("Passed. Added a block with no transaction.");

		Block blockWithOneTransaction = new Block(blockWithNoTransaction.getHash(), keyPairs.get(1).getPublic());
		Transaction tx = new Transaction();
		tx.addInput(genesisBlock.getCoinbase().getHash(), 0);
		tx.addOutput(25, keyPairs.get(1).getPublic());
		blockWithNoTransaction.addTransaction(tx);
		ret = bh.processBlock(blockWithOneTransaction);
		if (!ret) {
			System.out.println("[FAIL] Should be able to add block with 1 transaction.");
			return;
		}
		System.out.println("Passed. Added a block with 1 transaction.");
	}
}
