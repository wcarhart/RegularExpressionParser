/*
 * Object representation of a transition between sets of states in an NFA
 */
class DFATransition {
	public ArrayList<Integer> start;
	public char input;
	public ArrayList<Integer> end;

	public DFATransition() {
		start = null;
		input = 0;
		end = null;
	}

	public DFATransition(ArrayList<Integer> start, char input,
			ArrayList<Integer> end) {
		this.start = start;
		this.input = input;
		this.end = end;
	}
}