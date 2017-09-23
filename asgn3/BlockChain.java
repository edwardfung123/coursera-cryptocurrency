import java.util.ArrayList;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
	private TxHandler txHandler = null;
	private ArrayList<Block> blocks = null;
	private TransactionPool txPool = null;


    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
		UTXOPool pool = new UTXOPool();
		this.txHandler = new TxHandler(pool);

		this.blocks = new ArrayList<Block>();

		this.blocks.add(genesisBlock);
		this.updateUTXOPoolFromBlock(genesisBlock);

		this.txPool = new TransactionPool();
    }


	private void updateUTXOPoolFromBlock(Block block) {
		ArrayList<Transaction> txs = block.getTransactions();
		Transaction[] tx_array = txs.toArray(new Transaction[0]);
		//tx_array[txs.size()] = block.getCoinbase();
		Transaction[] correctTxs = this.txHandler.handleTxs(tx_array);
		// add the coinbase as well
		Transaction cb = block.getCoinbase();
		UTXOPool pool = this.txHandler.getUTXOPool();
		int i = 0;
		for (Transaction.Output o: cb.getOutputs()) {
            UTXO utxo = new UTXO(cb.getHash(), i);
			pool.addUTXO(utxo, o);
			i++;
		}
		System.out.format("Number of correctTxs = %d\n", correctTxs.length);
	}

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
		return this.blocks.get(this.blocks.size() - 1);
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
		return this.txHandler.getUTXOPool();
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
		return this.txPool;
    }

	public boolean isValidBlock(Block block) {
		// A new genesis block won't be mined. If you receive a block which
		// claims to be a genesis block (parent is a null hash) in the
		// addBlock(Block b) function, you can return false.
		if (block.getPrevBlockHash() == null) {
			System.out.println("Null prev hash");
			return false;
		}

		boolean isPrevBlockHashSeenBefore = false;
		for (Block b: this.blocks) {
			if (b.getHash() == block.getPrevBlockHash()) {
				isPrevBlockHashSeenBefore = true;
				break;
			}
		}
		if (!isPrevBlockHashSeenBefore) {
			return false;
		}

		TxHandler handler = new TxHandler(this.txHandler.getUTXOPool());

		// Process the transactions, and check result
		ArrayList<Transaction> txs = block.getTransactions();
		if (txs.size() > 0){
			Transaction[] txsArray = txs.toArray(new Transaction[0]);
			Transaction[] correctTxs = handler.handleTxs(txsArray);
			if (correctTxs.length != txsArray.length)
				  return false;
		}

		return true;
	}

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
		if (!this.isValidBlock(block)) {
			return false;
		}
		if (this.CUT_OFF_AGE <= this.blocks.size()){
			this.blocks.remove(0);
		}
		this.blocks.add(block);

		// update my UTXOs
		this.updateUTXOPoolFromBlock(block);
		return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
		this.txPool.addTransaction(tx);
    }
}
