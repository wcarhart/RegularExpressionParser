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