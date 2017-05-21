/*
 * Object representation of a function that maps sets of NFA states to a single
 * DFA state
 */
class Mapping {
	public int dfaIndex;
	public ArrayList<Integer> nfaIndex;

	public Mapping(int dfaIndex, ArrayList<Integer> nfaIndex) {
		this.dfaIndex = dfaIndex;
		this.nfaIndex = nfaIndex;
	}

	public Mapping() {
		this.dfaIndex = 0;
		this.nfaIndex = null;
	}
}