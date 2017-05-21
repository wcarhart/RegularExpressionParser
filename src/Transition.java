/*
 * Object representation of a transition between states
 */
class Transition {
	public int start;
	public char input;
	public int end;

	public Transition() {
		start = 0;
		input = 0;
		end = 0;
	}

	public Transition(int start, char input, int end) {
		this.start = start;
		this.input = input;
		this.end = end;
	}

	/*
	 * Purpose: determines the equality of a transition
	 * 
	 * @param transition the transition with which to be compared
	 * 
	 * @return a boolean representing the equality of the to compared
	 * transitions
	 */
	public boolean isEqual(Transition transition) {
		if (start == transition.start && input == transition.input
				&& end == transition.end) {
			return true;
		}
		return false;
	}
}