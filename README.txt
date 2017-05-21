RegularExpressionParser README.txt

Purpose:
Program that takes in a text representation of a Regular Expression, converts it to an equivalent NFA, then converts it to an equivalent DFA, then parses test input to confirm if test strings are in the language of the given Regular Expression.

Usage:
	RegularExpressionParser.java input_filename output_filename
		OR
	RegularExpressionParser.java

Input files:
Each input file must be formatted in the following fashion:
	Alphabet
	Regular expression
	Test case 1
	Test case 2
	.....
	Test case N
Input files can be found in the 'res' directory.

Output file:
The output file will be formatted in the following fashion:
	Result of test case 1
	Result of test case 2
	.....
	Result of test case N
Output file will be generated, named 'results.txt'.

Description:
Program logic will adhere to the following steps:
	1. Read in input alphabet and regular expression from input file to produce a RegularExpression Object.
	2. Convert RegularExpression Object into an NFA Object.
	3. Convert NFA Object into a DFA Object.
	4. Read in each test case from the input file and run each as a computation on the DFA object. Write results to output file.


More in-depth details about RegularExpressionParser can be viewed in the project writeup: "Regular Expression Parser Writeup.docx".