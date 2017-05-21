/*
 * Course: COMP 370-01
 * Authors: Will Carhart, Alex Alvarez
 * Date: May 8th, 2017
 * Description: Program that takes in a text representation of a Regular
 * 				Expression, converts it to an equivalent NFA, then converts it
 * 				to an equivalent DFA, then parses test input to confirm if
 * 				test strings are in the language of the given Regular
 * 				Expression.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.Stack;

public class RegularExpressionParser {

	/*
	 * only global: used to track total number of states in NFA during build
	 * process - is global because build process of NFA is recursive in form and
	 * requires access to a global shared resource
	 */
	private static int numStates;

	public static void main(String[] args) {
		String inputFilename = null, outputFilename = null;
		numStates = 0;

		// acquire input and output file names
		if (args.length == 0) {
			Scanner in = new Scanner(System.in);
			System.out.println("Please enter the name of the input file: ");
			inputFilename = in.next();
			System.out.println("Please enter the name of the output file: ");
			outputFilename = in.next();
			in.close();
		} else if (args.length == 2) {
			inputFilename = args[0];
			outputFilename = args[1];

			// check proper program usage without any errors
			// if errors occur, enter else, print, and exit program
		} else {
			System.out.println("Incorrect program usage.");
			System.out
					.println("Usage:\n\tjava DFASimulator.java inputFilename outputFilename");
			System.out.println("\t\t\tOR");
			System.out.println("\tjava DFASimulator.java");
			System.exit(0);
		}

		// conversion begins
		RegularExpression re = readInput(inputFilename);
		NFA nfa = RE2NFA(re, outputFilename);
		DFA dfa = NFA2DFA(nfa);
		compute(dfa, inputFilename, outputFilename);
	}

	/*
	 * Purpose: reads in the RE input from a given file
	 * 
	 * @param filename the name of the input file
	 * 
	 * @return a RegularExpression representing the desired Regular Expression
	 */
	public static RegularExpression readInput(String filename) {
		BufferedReader br = null;

		ArrayList<Character> alphabet = new ArrayList<Character>();
		String expression = null;
		String input = null;

		try {
			br = new BufferedReader(new FileReader(filename));
			input = br.readLine();

			for (int i = 0; i < input.length(); i++) {
				alphabet.add(new Character(input.charAt(i)));
			}

			expression = br.readLine();

			br.close();
		} catch (IOException e) {
			System.err.println(filename + " not found");
			System.exit(1);
		}

		System.out.println("\nNow parsing: " + expression);
		return new RegularExpression(alphabet, expression);
	}

	/*
	 * Purpose: determines if the the transition from one character to another
	 * in the string representation of the Regular Expression is a valid implied
	 * concatenation
	 * 
	 * @param current the current character in the transition
	 * 
	 * @param previous the previous character in the transition
	 * 
	 * @return a boolean representing the validity of the implied concatenation
	 */
	public static boolean isValidConcat(char current, char previous,
			RegularExpression re) {
		boolean toReturn = false;
		Character Current = new Character(current);
		Character Previous = new Character(previous);

		// concatenation is valid if for two elements of the input expression,
		// such that a --> b, a and b match one of the following cases:
		// * --> b
		// a --> b
		// ) --> (
		// a --> (
		// ) --> b
		// * --> (

		if (re.alphabet.contains(Current) && previous == '*') {
			toReturn = true;
		} else if (re.alphabet.contains(Current)
				&& re.alphabet.contains(Previous)) {
			toReturn = true;
		} else if (current == '(' && previous == ')') {
			toReturn = true;
		} else if (current == '(' && re.alphabet.contains(Previous)) {
			toReturn = true;
		} else if (re.alphabet.contains(Current) && previous == ')') {
			toReturn = true;
		} else if (current == '(' && previous == '*') {
			toReturn = true;
		}

		return toReturn;
	}

	/*
	 * Purpose: builds an abstract syntax tree based on a given Regular
	 * Expression
	 * 
	 * @param re the Object representation of the Regular Expression
	 * 
	 * @param outputFileName the name of the output results file, in case the
	 * program determines an invalid expression and needs to abort the current
	 * conversion
	 * 
	 * @return TreeNode the root of the abstract syntax tree
	 */
	public static TreeNode buildSyntaxTree(RegularExpression re,
			String outputFilename) {
		Stack<Character> operators = new Stack<Character>();
		Stack<TreeNode> operands = new Stack<TreeNode>();
		char top, current;
		TreeNode a, b, newTreeNode;
		int parensCount = 0;
		boolean validOperator = true;

		String toPrint = re.expression.replace("@", " o ");
		System.out.println("Input converted to: " + toPrint);

		// special characters: ( ) * | @ e
		for (int i = 0; i < re.expression.length(); i++) {
			current = re.expression.charAt(i);

			// switch case to handle each of our 'special' characters
			switch (current) {
			case '(':
				operators.push(new Character(current));
				parensCount++;
				validOperator = false;
				break;
			case ')':
				if (parensCount <= 0) {
					writeResult(false, outputFilename, 0, true);
					System.exit(0);
				}
				parensCount--;
				top = operators.pop();
				while (top != '(' && !operators.isEmpty()) {
					switch (top) {
					case '*':
						a = operands.pop();
						newTreeNode = new TreeNode('*');
						newTreeNode.addLeftChild(a);
						operands.push(newTreeNode);
						break;
					case '@':
						a = operands.pop();
						b = operands.pop();
						newTreeNode = new TreeNode('@');
						newTreeNode.addLeftChild(a);
						newTreeNode.addRightChild(b);
						operands.push(newTreeNode);
						break;
					case '|':
						a = operands.pop();
						b = operands.pop();
						newTreeNode = new TreeNode('|');
						newTreeNode.addLeftChild(a);
						newTreeNode.addRightChild(b);
						operands.push(newTreeNode);
						break;
					default:
						System.out.println("\nFAILURE: Invalid expression");
						writeResult(false, outputFilename, 0, true);
						System.exit(0);
					}
					top = operators.pop();
				}
				break;
			case '*':
				operators.push(new Character(current));
				break;
			case '|':
				if (!validOperator) {
					System.out.println("\nFAILURE: Invalid Expression");
					writeResult(false, outputFilename, 0, true);
					System.exit(0);
				}
				validOperator = false;
				top = operators.peek();
				if (top == '*') {
					top = operators.pop();
					a = operands.pop();
					newTreeNode = new TreeNode('*');
					newTreeNode.addLeftChild(a);
					operands.push(newTreeNode);
					operators.push(new Character(current));
				} else if (top == '@') {
					top = operators.pop();
					a = operands.pop();
					b = operands.pop();
					newTreeNode = new TreeNode('@');
					newTreeNode.addLeftChild(a);
					newTreeNode.addRightChild(b);
					operands.push(newTreeNode);
					operators.push(new Character(current));
				} else {
					operators.push(new Character(current));
				}
				break;
			case '@':
				validOperator = false;
				top = operators.peek();
				if (top == '*') {
					top = operators.pop();
					a = operands.pop();
					newTreeNode = new TreeNode('*');
					newTreeNode.addLeftChild(a);
					operands.push(newTreeNode);
					operators.push(new Character(current));
				} else if (top == '@') {
					a = operands.pop();
					b = operands.pop();
					newTreeNode = new TreeNode('@');
					newTreeNode.addLeftChild(a);
					newTreeNode.addRightChild(b);
					operands.push(newTreeNode);
				} else {
					operators.push(new Character(current));
				}
				break;
			case 'e':
				validOperator = true;
				operands.push(new TreeNode(new Character(current)));
				break;
			case ' ':
				break;
			default:
				validOperator = true;
				operands.push(new TreeNode(new Character(current)));
			}
		}

		if (!operators.isEmpty()) {
			System.out.println("\nFAILURE: Invalid expression");
			writeResult(false, outputFilename, 0, true);
			System.exit(0);
		}

		return (operands.pop());
	}

	/*
	 * Purpose: converts a Regular Expression to a Non-deterministic Finite
	 * Automaton (NFA)
	 * 
	 * @param re the Object representation of the Regular Expression
	 * 
	 * @param outputFilename the name of the output file
	 * 
	 * @return the converted NFA
	 */
	public static NFA RE2NFA(RegularExpression re, String outputFilename) {
		// first step is to redefine expression such that concatenations are
		// explicit, not implicit
		re.expression = re.expression.replace(" ", "");
		String newExpression = "(";
		char current, previous = '\0';
		for (int i = 0; i < re.expression.length(); i++) {
			current = re.expression.charAt(i);
			if (isValidConcat(current, previous, re)) {
				newExpression += '@';
			}
			newExpression += current;
			previous = current;
		}
		newExpression += ")";
		re.expression = newExpression;

		// next step is to build a syntax tree
		TreeNode syntaxTree = buildSyntaxTree(re, outputFilename);

		// final step is to build a DFA from our syntax tree
		// we start with a depth first search, and build up accordingly
		NFA nfa = traverse(syntaxTree, re.alphabet);

		return nfa;
	}

	/*
	 * Purpose: builds an NFA from the abstract syntax tree
	 * 
	 * @param tn the current node of the tree with which the current method call
	 * is working
	 * 
	 * @param alphabet the alphabet of the Regular Expression
	 * 
	 * @return an NFA representing the contents of tn (recursive)
	 */
	public static NFA traverse(TreeNode tn, ArrayList<Character> alphabet) {
		NFA toReturn = null;

		// base case - check if node is leaf
		if (tn.leftChild == null && tn.rightChild == null) {
			if (tn.data == 'e') {
				toReturn = tn.buildEpsilonNFA(numStates, alphabet);
				numStates++;
			} else {
				toReturn = tn.buildNFA(numStates, alphabet);
				numStates += 2;
			}
		}

		// if not, we need to build either concat, union, or star
		NFA left, right;
		switch (tn.data) {
		case '@':
			left = traverse(tn.leftChild, alphabet);
			right = traverse(tn.rightChild, alphabet);
			toReturn = right.concatenate(numStates, left);
			break;
		case '|':
			left = traverse(tn.leftChild, alphabet);
			right = traverse(tn.rightChild, alphabet);
			toReturn = left.union(numStates, right);
			numStates++;
			break;
		case '*':
			left = traverse(tn.leftChild, alphabet);
			toReturn = left.star(numStates);
			numStates++;
			break;
		}

		return toReturn;
	}

	/*
	 * Purpose: builds a DFA from an NFA
	 * 
	 * @param nfa the NFA to be converted into a DFA
	 * 
	 * @return the converted DFA
	 */
	public static DFA NFA2DFA(NFA nfa) {
		Toolbox toolbox = new Toolbox();
		ArrayList<ArrayList<State>> masterList = new ArrayList<ArrayList<State>>();
		ArrayList<DFATransition> finalList = new ArrayList<DFATransition>();

		// setting up 2D ArrayList
		for (int i = 0; i < nfa.alphabet.size(); i++) {
			masterList.add(new ArrayList<State>());
			ArrayList<Integer> temp = new ArrayList<Integer>();
			temp.add(new Integer(nfa.startState));
			masterList.get(i).add(new State(temp));
		}

		while (!masterList.get(0).isEmpty()) {

			// compute mapping subset
			for (int i = 0; i < nfa.alphabet.size(); i++) {
				ArrayList<Integer> currentSubset = new ArrayList<Integer>();
				currentSubset = toolbox.cloneInt(masterList.get(i).get(0).Q);
				currentSubset = buildStartState(currentSubset,
						nfa.transitionFunction);
				masterList.get(i).remove(0);
				ArrayList<Integer> end = toolbox.cloneInt(computeSubset(
						currentSubset, nfa.transitionFunction,
						nfa.alphabet.get(i)));
				DFATransition transition = new DFATransition(currentSubset,
						nfa.alphabet.get(i).charValue(), end);
				finalList.add(transition);

				// add to all other queues
				for (int j = 0; j < nfa.alphabet.size(); j++) {
					masterList.get(j).add(new State(end));
				}
			}

			// check and see if we are done adding states to ArrayList
			boolean done = false;
			while (!done) {
				if (finalList.isEmpty()) {
					done = true;
				}
				for (DFATransition t : finalList) {
					if (!masterList.get(0).isEmpty()) {
						if (t.start.equals(masterList.get(0).get(0).Q)) {
							for (int k = 0; k < nfa.alphabet.size(); k++) {
								masterList.get(k).remove(0);
								done = false;
							}
						} else {
							done = true;
						}
					} else {
						done = true;
					}
				}
			}

		}

		// convert to DFA
		// 1. alphabet
		ArrayList<Character> alphabet = toolbox.cloneChar(nfa.alphabet);

		// 2. number of states
		int numStates = 0;
		if (finalList.size() % nfa.alphabet.size() == 0) {
			System.out
					.println("\nSUCCESS: RE >> NFA >> DFA conversion completed successfully. Please refer to output file for results.");
			numStates = finalList.size() / nfa.alphabet.size();
		} else {
			System.err
					.println("INTERNAL ERROR: Incorrect number of states calculated");
			System.err.println("Please check input file and try again");
			System.exit(1);
		}

		// 3. transition function
		ArrayList<Transition> transitionFunction = new ArrayList<Transition>();
		ArrayList<Mapping> f = new ArrayList<Mapping>();
		for (int k = 0; k < numStates; k++) {
			ArrayList<Integer> temp = finalList.get(k * alphabet.size()).start;
			Mapping newMap = new Mapping(k + 1, temp);
			f.add(newMap);
		}
		for (Mapping m : f) {
			for (Character c : alphabet) {
				int start = m.dfaIndex;
				char input = c.charValue();
				int end = 0;
				for (DFATransition t : finalList) {
					if (t.start.equals(m.nfaIndex)) {
						if (t.input == input) {
							end = searchMapping(f, t.end);
							break;
						}
					}
				}

				if (end == 0) {
					System.err
							.println("INTERNAL ERROR: Could not compute NFA state --> DFA state mapping.");
					System.err.println("Please check input file and try again");
					System.exit(1);
				}
				transitionFunction.add(new Transition(start, input, end));
			}
		}

		// 4. accept states
		ArrayList<Integer> endStates = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> temp2 = new ArrayList<ArrayList<Integer>>();
		for (Integer i : nfa.endStates) {
			int index = i.intValue();
			for (int k = 0; k < numStates; k++) {
				ArrayList<Integer> temp = finalList.get(k * alphabet.size()).start;
				if (temp.contains(index)) {
					temp2.add(temp);
				}
			}
			for (ArrayList<Integer> arr : temp2) {
				Integer toAdd = new Integer(searchMapping(f, arr));
				if (!endStates.contains(toAdd)) {
					endStates.add(toAdd);
				}
			}
		}

		// 5. start state
		int startState = searchMapping(f, finalList.get(0).start);

		return new DFA(numStates, alphabet, transitionFunction, startState,
				endStates);
	}

	/*
	 * Purpose: builds a new start state if the start state in the given NFA has
	 * epsilon transitions exiting itself
	 * 
	 * @param currentSubset the current set of states for which a new start
	 * state will be determined
	 * 
	 * @param transitionFunction the transition function for the NFA
	 * 
	 * @return a list of integer indexes that represent the new start state
	 * (which is a group of states)
	 */
	public static ArrayList<Integer> buildStartState(
			ArrayList<Integer> currentSubset,
			ArrayList<Transition> transitionFunction) {
		ArrayList<Integer> toReturn = new ArrayList<Integer>();
		ArrayList<Integer> temp = new ArrayList<Integer>();

		for (Integer i : currentSubset) {
			temp.add(i);
			toReturn.add(i);
		}

		while (!temp.isEmpty()) {
			for (Transition t : transitionFunction) {
				if (t.input == 'e' && t.start == temp.get(0).intValue()) {
					if (!toReturn.contains(new Integer(t.end))) {
						toReturn.add(new Integer(t.end));
						temp.add(new Integer(t.end));
					}
				}
			}
			temp.remove(0);
		}

		Collections.sort(toReturn);
		return toReturn;
	}

	/*
	 * Purpose: computes the mapping subset of a set of states in an NFA to
	 * another set of states in an NFA
	 * 
	 * @param currentSubset the set of states from which the transition will
	 * begin
	 * 
	 * @param transitionFunction the transition function of the NFA
	 * 
	 * @param alpha the current input
	 * 
	 * @return a list of integer indexes that represent the mapping subset
	 */
	public static ArrayList<Integer> computeSubset(
			ArrayList<Integer> currentSubset,
			ArrayList<Transition> transitionFunction, Character alpha) {
		Toolbox toolbox = new Toolbox();
		ArrayList<Integer> toReturn = new ArrayList<Integer>();
		ArrayList<Integer> temp = new ArrayList<Integer>();

		/*
		 * NFA transitions adhere to the following format: (a e*), where a is
		 * the input alpha and e* represents any finite number of epsilon
		 * transitions, occurring subsequent to alpha
		 */

		currentSubset = toolbox.cloneInt(buildStartState(currentSubset,
				transitionFunction));

		for (Integer i : currentSubset) {
			for (Transition t : transitionFunction) {
				if (t.input == alpha.charValue() && t.start == i.intValue()) {
					if (!toReturn.contains(t.end)) {
						toReturn.add(new Integer(t.end));
						temp.add(new Integer(t.end));
					}

					// trace all epsilon transitions
					while (!temp.isEmpty()) {
						for (Transition t2 : transitionFunction) {
							if (t2.start == temp.get(0).intValue()
									&& t2.input == 'e') {
								if (!temp.contains(t2.end)) {
									toReturn.add(new Integer(t2.end));
									temp.add(new Integer(t2.end));
								}
							}
						}
						temp.remove(0);
					}
				}
			}
		}

		Collections.sort(toReturn);
		return toReturn;
	}

	/*
	 * Purpose: searches a list of Mappings to find the index of a specific
	 * state
	 * 
	 * @param f the list of Mappings
	 * 
	 * @param end the target subset for which this method will find an index
	 * 
	 * @return the index of the given subset
	 */
	public static int searchMapping(ArrayList<Mapping> f, ArrayList<Integer> end) {
		int toReturn = 0;
		for (Mapping m : f) {
			if (m.nfaIndex.equals(end)) {
				toReturn = m.dfaIndex;
				break;
			}
		}

		return toReturn;
	}

	/*
	 * Purpose: computes the result of a given input string based on the
	 * converted DFA
	 * 
	 * @param dfa the DFA on which the computation will run
	 * 
	 * @param inputFilename the name of the input file
	 * 
	 * @param outputFilename the name of the output file
	 */
	public static void compute(DFA dfa, String inputFilename,
			String outputFilename) {
		BufferedReader br;
		String input;
		boolean result;
		int count = 0;

		try {
			br = new BufferedReader(new FileReader(inputFilename));
			br.readLine();
			br.readLine();
			input = br.readLine();

			while (input != null) {
				result = testInput(dfa, input);
				writeResult(result, outputFilename, count, false);
				input = br.readLine();
				count++;
			}

		} catch (IOException e) {
			System.err.println("Error in I/O while attempting to output");
			System.exit(1);
		}
	}

	/*
	 * Purpose: tests the input on a given DFA
	 * 
	 * @param dfa the DFA on which input will be tested
	 * 
	 * @param input the input to be tested
	 * 
	 * @return a boolean which represents if the the string input is in the
	 * language based on dfa
	 */
	public static boolean testInput(DFA dfa, String input) {
		Object currentInput;
		int currentState = dfa.startState;
		int size = input.length(), index = 0, i = 0;

		// large loop to run through each character of the input string
		while (index < size) {
			currentInput = new Character(input.charAt(index));
			i = 0;
			while (i < dfa.transitionFunction.size()) {
				if (dfa.transitionFunction.get(i).start == currentState) {
					if (currentInput.equals((Character) dfa.transitionFunction
							.get(i).input)) {
						currentState = dfa.transitionFunction.get(i).end;
						break;
					}
				}
				i++;
			}
			index++;
		}

		// check and see if the final state is an accepting state
		for (i = 0; i < dfa.endStates.size(); i++) {
			if (currentState == dfa.endStates.get(i)) {
				return true;
			}
		}

		return false;
	}

	/*
	 * Purpose: writes the result of a given computation to an output file
	 * 
	 * @param result the boolean result of the given computation, which is to be
	 * written in the output file
	 * 
	 * @param filename the name of the output file
	 * 
	 * @param count an index that records how many test cases have been written
	 * to the output file
	 * 
	 * @param invalid a boolean flag which represents if the original Regular
	 * Expression is valid or not
	 */
	public static void writeResult(boolean result, String filename, int count,
			boolean invalid) {
		FileWriter fw;
		PrintWriter pw;

		try {
			if (count == 0) {
				// clear file if this is the first time writing to it
				fw = new FileWriter(filename, false);
				pw = new PrintWriter(fw, false);
				pw.flush();
				pw.close();
				fw.close();
			}

			fw = new FileWriter(filename, true);
			pw = new PrintWriter(fw, true);

			if (invalid) {
				pw.write("Invalid Expression\n");
			} else {
				if (result) {
					pw.write("true\n");
				} else {
					pw.write("false\n");
				}
			}

			pw.close();
			fw.close();

		} catch (IOException e) {
			System.err.println("Error in I/O while attempting to output");
			System.exit(1);
		}
	}
}

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

/*
 * Object representation of a Regular Expression (RE)
 */
class RegularExpression {
	public ArrayList<Character> alphabet;
	public String expression;

	public RegularExpression() {
		this.alphabet = null;
		this.expression = null;
	}

	public RegularExpression(ArrayList<Character> alphabet, String expression) {
		this.alphabet = alphabet;
		this.expression = expression;
	}
}

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

/*
 * Helper class, used to create deep copies of ArrayLists (rather than shallow
 * copies), works with multiple data types (Character, Integer, and Transition)
 */
class Toolbox {

	/*
	 * Purpose: creates a deep copy of a Character ArrayList
	 * 
	 * @param toClone the ArrayList to be cloned
	 * 
	 * @return the cloned ArrayList
	 */
	public ArrayList<Character> cloneChar(ArrayList<Character> toClone) {
		ArrayList<Character> clone = new ArrayList<Character>(toClone.size());
		for (Character toChar : toClone) {
			clone.add(new Character(toChar));
		}
		return clone;
	}

	/*
	 * Purpose: creates a deep copy of a Integer ArrayList
	 * 
	 * @param toClone the ArrayList to be cloned
	 * 
	 * @return the cloned ArrayList
	 */
	public ArrayList<Integer> cloneInt(ArrayList<Integer> toClone) {
		ArrayList<Integer> clone = new ArrayList<Integer>(toClone.size());
		for (Integer toInt : toClone) {
			clone.add(new Integer(toInt));
		}
		return clone;
	}

	/*
	 * Purpose: creates a deep copy of a Transition ArrayList
	 * 
	 * @param toClone the ArrayList to be cloned
	 * 
	 * @return the cloned ArrayList
	 */
	public ArrayList<Transition> cloneTran(ArrayList<Transition> toClone) {
		ArrayList<Transition> clone = new ArrayList<Transition>(toClone.size());
		for (Transition t : toClone) {
			clone.add(t);
		}
		return clone;
	}
}

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
