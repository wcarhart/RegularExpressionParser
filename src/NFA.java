/*
 * Object representation of a Non-deterministic Finite Automaton (NFA)
 */
class NFA {
	public int numStates;
	public ArrayList<Character> alphabet;
	public ArrayList<Transition> transitionFunction;
	public int startState;
	public ArrayList<Integer> endStates;

	public NFA() {
		numStates = 0;
		alphabet = null;
		transitionFunction = null;
		startState = 0;
		endStates = null;
	}

	public NFA(int numStates, ArrayList<Character> alphabet,
			ArrayList<Transition> transitionFunction, int startState,
			ArrayList<Integer> endStates) {
		this.numStates = numStates;
		this.alphabet = alphabet;
		this.transitionFunction = transitionFunction;
		this.startState = startState;
		this.endStates = endStates;
	}

	/*
	 * Purpose: builds a new NFA that represents the concatenation of the NFAs
	 * referenced by the left and right children of the tree node
	 * 
	 * @param numStates the number of states in the full NFA
	 * 
	 * @param right the right node of the tree node ('this' is the left node)
	 * 
	 * @return the NFA resulting from the computed concatenation
	 */
	public NFA concatenate(int numStates, NFA right) {
		NFA toReturn = new NFA();
		Toolbox toolbox = new Toolbox();

		toReturn.numStates = this.numStates + right.numStates;
		toReturn.alphabet = toolbox.cloneChar(this.alphabet);
		toReturn.transitionFunction = toolbox
				.cloneTran(this.transitionFunction);
		for (Transition T : right.transitionFunction) {
			toReturn.transitionFunction.add(T);
		}
		for (Integer i : this.endStates) {
			toReturn.transitionFunction.add(new Transition(i.intValue(), 'e',
					right.startState));
		}
		toReturn.endStates = toolbox.cloneInt(right.endStates);
		toReturn.startState = this.startState;

		return toReturn;
	}

	/*
	 * Purpose: builds a new NFA that represents the union of the NFAs
	 * referenced by the left and right children of the tree node
	 * 
	 * @param numStates the number of states in the full NFA
	 * 
	 * @param right the right node of the tree node ('this' is the left node)
	 * 
	 * @return the NFA resulting from the computed union
	 */
	public NFA union(int numStates, NFA right) {
		NFA toReturn = new NFA();
		Toolbox toolbox = new Toolbox();

		toReturn.numStates = this.numStates + right.numStates + 1;
		toReturn.alphabet = toolbox.cloneChar(this.alphabet);
		toReturn.transitionFunction = toolbox
				.cloneTran(this.transitionFunction);
		for (Transition T : right.transitionFunction) {
			toReturn.transitionFunction.add(T);
		}
		toReturn.transitionFunction.add(new Transition(numStates + 1, 'e',
				this.startState));
		toReturn.transitionFunction.add(new Transition(numStates + 1, 'e',
				right.startState));
		toReturn.endStates = toolbox.cloneInt(this.endStates);
		for (Integer i : right.endStates) {
			toReturn.endStates.add(i);
		}
		toReturn.startState = numStates + 1;

		return toReturn;
	}

	/*
	 * Purpose: builds a new NFA that represents the star of a given NFA, which
	 * is referenced by the left child of the tree node
	 * 
	 * @param numStates the number of states in the full NFA
	 * 
	 * @return the NFA representing the computed star
	 */
	public NFA star(int numStates) {
		NFA toReturn = new NFA();
		Toolbox toolbox = new Toolbox();

		toReturn.numStates = this.numStates + 1;
		toReturn.alphabet = toolbox.cloneChar(this.alphabet);
		toReturn.transitionFunction = toolbox
				.cloneTran(this.transitionFunction);
		toReturn.transitionFunction.add(new Transition(numStates + 1, 'e',
				this.startState));
		for (Integer i : this.endStates) {
			toReturn.transitionFunction.add(new Transition(i.intValue(), 'e',
					numStates + 1));
		}
		toReturn.endStates = toolbox.cloneInt(this.endStates);
		toReturn.endStates.add(new Integer(numStates + 1));
		toReturn.startState = numStates + 1;

		return toReturn;
	}
}