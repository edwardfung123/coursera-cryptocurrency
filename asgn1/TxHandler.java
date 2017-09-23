import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

public class TxHandler {

    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
        // Test 5: test isValidTx() with transactions that claim outputs not in the current utxoPool
        int i = 0;
        Set<UTXO> usedInputs = new HashSet<UTXO>();

        for (Transaction.Input input : tx.getInputs()) {
            UTXO ut = new UTXO(input.prevTxHash, input.outputIndex);

            // output is the previous transaction that createdd this input coin!
            Transaction.Output output = this.utxoPool.getTxOutput(ut);
            if (output == null) {
                // Not in the current pool
                return false;
            }

            // (2) the signatures on each input of {@code tx} are valid,
            // Test 2: test isValidTx() with transactions containing signatures of incorrect data
            boolean isValidSignature = Crypto.verifySignature(output.address, tx.getRawDataToSign(i), input.signature);
            if (!isValidSignature) {
                return false;
            }

            i++;

            if (usedInputs.contains(ut)) {
                // double spending inside a tx
                return false;
            } 
            usedInputs.add(ut);
        }

        // (4) all of {@code tx}s output values are non-negative, and
        // Test 7: test isValidTx() with transactions that contain a negative output value
        i = 0;
        for (Transaction.Output output: tx.getOutputs()) {
            if (output.value < 0.0) {
                return false;
            }
        }

        // (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
        //     values; and false otherwise.
        double inputSum = 0.0;
        for (Transaction.Input input : tx.getInputs()) {
            UTXO ut = new UTXO(input.prevTxHash, input.outputIndex);
            // output is the previous transaction that createdd this input coin!
            Transaction.Output output = this.utxoPool.getTxOutput(ut);
            inputSum += output.value;
        }
        double outputSum = 0.0;
        for (Transaction.Output output: tx.getOutputs()) {
            outputSum += output.value;
        }
        if (inputSum < outputSum) {
            return false;
        }
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> acceptedTxs = new ArrayList<Transaction>();
        ArrayList<Transaction> remainingTxs = new ArrayList<Transaction>(Arrays.asList(possibleTxs));
        ArrayList<Transaction> nextRemainingTxs = new ArrayList<Transaction>();
        while (true) {
            nextRemainingTxs = new ArrayList<Transaction>();

            for (Transaction tx : remainingTxs) {
                if (isValidTx(tx)) {
                    acceptedTxs.add(tx);

                    // create utxo from the the curren tx
                    int i = 0;
                    for (Transaction.Output o : tx.getOutputs()) {
                        UTXO utxo = new UTXO(tx.getHash(), i);
                        this.utxoPool.addUTXO(utxo, o);
                        i++;
                    }

                    // remove the used coins.
                    for (Transaction.Input input: tx.getInputs()) {
                        UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                        this.utxoPool.removeUTXO(utxo);
                    }
                } else {
                    // perhaps the next remaining txs depends on some valid transaction.
                    // Take care them in the next cycle.
                    nextRemainingTxs.add(tx);
                }
            }
            if (remainingTxs.size() == nextRemainingTxs.size()){
                // no change in this cycle.
                break;
            }

            remainingTxs = nextRemainingTxs;
        }
        
        return acceptedTxs.toArray(new Transaction[0]);
    }

}
