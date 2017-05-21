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