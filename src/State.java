/*
 * Object representation of a DFA state (list of NFA states)
 */
class State {
	public ArrayList<Integer> Q;

	public State(ArrayList<Integer> Q) {
		this.Q = Q;
	}

	public State() {
		this.Q = null;
	}
}