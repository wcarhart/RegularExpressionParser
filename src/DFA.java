/*
 * Object representation of a Deterministic Finite Automaton (DFA)
 */
class DFA {
	public int numStates;
	public ArrayList<Character> alphabet;
	public ArrayList<Transition> transitionFunction;
	public int startState;
	public ArrayList<Integer> endStates;

	public DFA() {
		numStates = 0;
		alphabet = null;
		transitionFunction = null;
		startState = 0;
		endStates = null;
	}

	public DFA(int numStates, ArrayList<Character> alphabet,
			ArrayList<Transition> transitionFunction, int startState,
			ArrayList<Integer> endStates) {
		this.numStates = numStates;
		this.alphabet = alphabet;
		this.transitionFunction = transitionFunction;
		this.startState = startState;
		this.endStates = endStates;
	}

}