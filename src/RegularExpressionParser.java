/*
 * Course: COMP 370-01
 * Author: Will Carhart
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