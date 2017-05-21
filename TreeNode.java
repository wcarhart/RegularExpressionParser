/*
 * Object representation of a node in a generic tree structure
 */
class TreeNode {
	public Character data;
	public TreeNode parent;
	public TreeNode leftChild;
	public TreeNode rightChild;

	public TreeNode(Character data) {
		this.data = data;
		this.leftChild = null;
		this.rightChild = null;
	}

	/*
	 * Purpose: determines if the given node is the root of the tree
	 * 
	 * @return a boolean, true if root of tree, false if not root of tree
	 */
	public boolean isRoot() {
		return (parent == null);
	}

	/*
	 * Purpose: determines if the given node is a leaf node
	 * 
	 * @return a boolean, true if the given node is a leaf node, false if the
	 * given node is not a leaf node
	 */
	public boolean isLeaf() {
		return (leftChild == null && rightChild == null);
	}

	/*
	 * Purpose: computes the depth of a given node
	 * 
	 * @return the depth of a given node
	 */
	public int getDepthOfNode() {
		if (this.isRoot()) {
			return 0;
		} else {
			return parent.getDepthOfNode() + 1;
		}
	}

	/*
	 * Purpose: adds a left child to a given tree node
	 * 
	 * @param child the Character that will be added as the left child to the
	 * given tree node
	 * 
	 * @return the resulting tree node with a new left child
	 */
	public TreeNode addLeftChild(Character child) {
		TreeNode childNode = new TreeNode(child);
		childNode.parent = this;
		this.leftChild = childNode;
		return childNode;
	}

	/*
	 * Purpose: adds a left child to a given tree node
	 * 
	 * @param childNode the tree node to be added as a new left tree node
	 * 
	 * @return the resulting tree node with a new left child
	 */
	public TreeNode addLeftChild(TreeNode childNode) {
		childNode.parent = this;
		this.leftChild = childNode;
		return childNode;
	}

	/*
	 * Purpose: adds a right child to a given tree node
	 * 
	 * @param child the Character that will be added as the right child to the
	 * given tree node
	 * 
	 * @return the resulting tree node with a new right child
	 */
	public TreeNode addRightChild(Character child) {
		TreeNode childNode = new TreeNode(child);
		childNode.parent = this;
		this.rightChild = childNode;
		return childNode;
	}

	/*
	 * Purpose: adds a right child to a given tree node
	 * 
	 * @param childNode the tree node to be added as a new right tree node
	 * 
	 * @return the resulting tree node with a new right child
	 */
	public TreeNode addRightChild(TreeNode childNode) {
		childNode.parent = this;
		this.rightChild = childNode;
		return childNode;
	}

	/*
	 * Purpose: builds a new NFA for a specific input
	 * 
	 * @param numStates the number of states in the full NFA
	 * 
	 * @param alphabet the alphabet for the given Regular Expression
	 * 
	 * @return the computed NFA
	 */
	public NFA buildNFA(int numStates, ArrayList<Character> alphabet) {
		int totalStates = 2;
		int startState = numStates + 1;
		ArrayList<Transition> transitionFunction = new ArrayList<Transition>();
		transitionFunction.add(new Transition(startState,
				this.data.charValue(), startState + 1));
		ArrayList<Integer> endStates = new ArrayList<Integer>();
		endStates.add(new Integer(startState + 1));

		return new NFA(totalStates, alphabet, transitionFunction, startState,
				endStates);
	}

	/*
	 * Purpose: builds a new NFA for an empty string ('e') input
	 * 
	 * @param numStates the number of states in the full NFA
	 * 
	 * @param alphabet the alphabet for the given Regular Expression
	 * 
	 * @return the computed NFA
	 */
	public NFA buildEpsilonNFA(int numStates, ArrayList<Character> alphabet) {
		int totalStates = 1;
		int startState = numStates + 1;
		ArrayList<Transition> transitionFunction = new ArrayList<Transition>();
		ArrayList<Integer> endStates = new ArrayList<Integer>();
		endStates.add(new Integer(numStates + 1));

		return new NFA(totalStates, alphabet, transitionFunction, startState,
				endStates);
	}

}