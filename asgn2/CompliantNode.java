import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

	private ArrayList<Boolean> followees;
	private Set<Integer> badFollowees;
	private Set<Transaction> believedTransactions;
	private Set<Transaction> pendingTransactions;
	private ArrayList<Integer> lastCounter;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
		this.believedTransactions = new HashSet<Transaction>();
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
		this.followees = new ArrayList<Boolean>(followees.length);
		this.lastCounter = new ArrayList<>(followees.length);
		for (int i = 0 ; i < followees.length; i++){
			this.followees.add(followees[i]);
			this.lastCounter.add(0);
		}
		this.badFollowees = new HashSet<Integer>();
		
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
		this.pendingTransactions = new HashSet<Transaction>(pendingTransactions);

		this.believedTransactions = new HashSet<Transaction>();
		for (Transaction tx: pendingTransactions) {
			this.believedTransactions.add(tx);
		}
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
		// Based on the current believedTransactions, add one more transaction to it.
		Set<Transaction> transactions = new HashSet<Transaction>();
		for (Transaction tx : this.believedTransactions) {
			transactions.add(tx);
		}

		for (Transaction tx : this.pendingTransactions) {
			transactions.add(tx);
		}
		return transactions;
    }

    public void receiveFromFollowees_68(Set<Candidate> candidates) {
        // IMPLEMENT THIS
		ArrayList<Set<Transaction>> groupedTransactions = new ArrayList<Set<Transaction>>(this.followees.size());
		for (int i = 0; i < this.followees.size() ; i++) {
			groupedTransactions.add(new HashSet<Transaction>());
		}

		for (Candidate candidate: candidates) {
			groupedTransactions.get(candidate.sender).add(candidate.tx);
		}

		for (Set<Transaction> gtxs: groupedTransactions) {
			gtxs.retainAll(this.believedTransactions);
		}

		for (Candidate current_candidate : candidates) {
			if (!groupedTransactions.get(current_candidate.sender).isEmpty()) {
				this.believedTransactions.add(current_candidate.tx);
			  	this.pendingTransactions.remove(current_candidate.tx);
			} else {
				//System.out.println("Find empty");
			}
			//this.believedTransactions.add(current_candidate.tx);
			//this.pendingTransactions.remove(current_candidate.tx);
		}
    }

    public void receiveFromFollowees_90(Set<Candidate> candidates) {
        // IMPLEMENT THIS
		ArrayList<Integer> counter = new ArrayList<>(this.followees.size());
		for (int i = 0 ; i < this.followees.size() ; i++ ){
			counter.add(0);
		}

		for (Candidate current_candidate: candidates) {
			if (this.followees.get(current_candidate.sender)) {
				counter.set(current_candidate.sender, counter.get(current_candidate.sender) + 1);
			}
		}

		for (int i = 0 ; i < counter.size() ; i++) {
			if (counter.get(i) == 0) {
			    this.followees.set(i, false);
			}
		}

		for (Candidate current_candidate : candidates) {
			if (this.followees.get(current_candidate.sender)) {
				// Only process trusted candidate.
				//System.out.println("Found someone trustworthy");
				this.believedTransactions.add(current_candidate.tx);
				this.pendingTransactions.remove(current_candidate.tx);
			}
		}
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
		ArrayList<Integer> counter = new ArrayList<>(this.followees.size());
		for (int i = 0 ; i < this.followees.size() ; i++ ){
			counter.add(0);
		}

		for (Candidate current_candidate: candidates) {
			if (this.followees.get(current_candidate.sender)) {
				counter.set(current_candidate.sender, counter.get(current_candidate.sender) + 1);
			}
		}

		for (int i = 0 ; i < counter.size() ; i++) {
			boolean isLiar = (counter.get(i) == 0 ||			/*The node is sending empty.*/
					counter.get(i) <= this.lastCounter.get(i) 	/*The node suddenly become empty or got fewer transactions.*/
					);
			if (this.followees.get(i) && isLiar) {
			    this.followees.set(i, false);
			}
		}

		for (Candidate current_candidate : candidates) {
			if (this.followees.get(current_candidate.sender)) {
				// Only process trusted candidate.
				//System.out.println("Found someone trustworthy");
				this.believedTransactions.add(current_candidate.tx);
				this.pendingTransactions.remove(current_candidate.tx);
			}
		}

		this.lastCounter = counter;
    }
}
