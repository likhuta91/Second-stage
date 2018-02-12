package by.lik.task2.entity;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import by.lik.task2.exception.CalculatorException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;

public class ConsoleCalculator {

	public static final String SEVERAL_OPERATORS_IN_ROW_REGEXP = ".*(([-/\\+\\*\\^]{2,})|([-/\\+\\*\\^]+[\\)]+)|([\\(]+[/\\+\\*\\^]+)).*";
	public static final String SPACE_IN_NUMBER_REGEXP = ".*(([0-9]+\\s+[0-9]+)|([,\\.]{1,}\\s+[0-9]+)|([0-9]+\\s+[,\\.]{1,})).*";
	public static final String ALLOWABLE_CHAR_IN_EXPRESSION_REGEXP = "[1234567890,-/\\.\\+\\*\\^\\(\\)\\s]*";

	public static final Map<String, Integer> MATHEMATICAL_OPERATIONS;

	public static final String OPENING_PARENTHESIS = "(";
	public static final String CLOSING_PARENTHESIS = ")";

	static {
		MATHEMATICAL_OPERATIONS = new HashMap<String, Integer>();
		MATHEMATICAL_OPERATIONS.put("^", 1);
		MATHEMATICAL_OPERATIONS.put("*", 2);
		MATHEMATICAL_OPERATIONS.put("/", 2);
		MATHEMATICAL_OPERATIONS.put("+", 3);
		MATHEMATICAL_OPERATIONS.put("-", 3);
	}

	public void exequte() {
		String message = "Результат: ";

		try {
			String expression = readExpressionFromConsole();

			validation(expression);

			expression = conversionExpressionToCanonicalForm(expression);

			String result = calculateExpression(expression);

			message = message + result;

		} catch (CalculatorException exception) {
			message = message + exception.getMessage();

		} catch (NumberFormatException exception) {
			message = message + "Невалидные данные";
		}

		System.out.println(message);
	}

	private String readExpressionFromConsole() throws CalculatorException {

		String expression = "";
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {

			System.out.print("Введите выражение: ");
			expression = bufferedReader.readLine();

		} catch (IOException exception) {
			throw new CalculatorException("Ошибка при вводе выражения");
		}
		return expression;
	}

	private void validation(String expression) throws CalculatorException {

		if (expression.length() == 0) {
			throw new CalculatorException("Исходные данные не введены");
		}

		String cloneLine = conversionExpressionToCanonicalForm(expression);

		if (!Pattern.matches(ALLOWABLE_CHAR_IN_EXPRESSION_REGEXP, expression)
				|| Pattern.matches(SPACE_IN_NUMBER_REGEXP, expression)
				|| Pattern.matches(SEVERAL_OPERATORS_IN_ROW_REGEXP, cloneLine)) {

			throw new CalculatorException("Невалидные данные");
		}
	}

	private String conversionExpressionToCanonicalForm(String expression) {

		expression = expression.replace(" ", "");

		expression = expression.replace(",", ".");

		// Преобразование в выражении отрицательных чисел с унарным минусом, например:
		// "-x" к виду "(0-x)"
		String currentSumbol = "";
		String prevSumbol;

		StringBuffer resultLine = new StringBuffer("");
		String[] sumbolsLine = expression.split("");

		boolean unaryMinusFound = false;

		for (String sumbol : sumbolsLine) {

			prevSumbol = currentSumbol;
			currentSumbol = sumbol;

			if (currentSumbol.equals("-") && Pattern.matches("\\D", prevSumbol)
					&& !prevSumbol.equals(OPENING_PARENTHESIS)) {
				unaryMinusFound = true;
				resultLine.append("(0-");

			} else {

				if (unaryMinusFound == true && Pattern.matches("\\D", currentSumbol)) {
					unaryMinusFound = false;
					resultLine.append(CLOSING_PARENTHESIS);
				}
				resultLine.append(currentSumbol);
			}
		}

		if (unaryMinusFound == true) {
			resultLine.append(CLOSING_PARENTHESIS);
		}

		return resultLine.toString();
	}

	private String calculateExpression(String expression) throws CalculatorException {

		String reversePolishNotation = createReversePolishNotation(expression);
		StringTokenizer tokenizer = new StringTokenizer(reversePolishNotation, " ");
		Stack<BigDecimal> stack = new Stack<BigDecimal>();

		BigDecimal operand1;
		BigDecimal operand2;

		while (tokenizer.hasMoreTokens()) {

			String token = tokenizer.nextToken();

			if (!MATHEMATICAL_OPERATIONS.keySet().contains(token)) {
				stack.push(new BigDecimal(token));

			} else {

				operand2 = stack.pop();

				if (stack.empty()) {
					operand1 = BigDecimal.ZERO;
				} else {
					operand1 = stack.pop();
				}

				if (token.equals("^")) {
					
					stack.push(operand1.pow(operand2.intValue()));
				
				} else if (token.equals("*")) {
					stack.push(operand1.multiply(operand2));

				} else if (token.equals("/")) {

					try {
						stack.push(operand1.divide(operand2));
					} catch (ArithmeticException exception) {
						throw new CalculatorException("Деление на 0");
					}

				} else if (token.equals("+")) {
					stack.push(operand1.add(operand2));

				} else if (token.equals("-")) {
					stack.push(operand1.subtract(operand2));
				}
			}
		}

		if (stack.size() != 1) {
			throw new CalculatorException("В выражении синтаксическая ошибка");
		}

		return stack.pop().stripTrailingZeros().toPlainString();
	}

	private String createReversePolishNotation(String expression) throws CalculatorException {

		ArrayList<String> outLine = new ArrayList<String>();
		Stack<String> stack = new Stack<String>();

		Set<String> operatorsAndBrackets = new HashSet<String>(MATHEMATICAL_OPERATIONS.keySet());
		operatorsAndBrackets.add(OPENING_PARENTHESIS);
		operatorsAndBrackets.add(CLOSING_PARENTHESIS);

		// Индекс строки, до которого проведен парсинг выражения
		int currentIndex = 0;
		String nextOperatorOrBracket = "";

		// Парсинг выражения в стэк и выходную строку
		while (true) {

			int nextOperatorOrBracketIndex = expression.length();
			nextOperatorOrBracket = "";

			for (String operatorOrBracket : operatorsAndBrackets) {

				int i = expression.indexOf(operatorOrBracket, currentIndex);
				if (i >= 0 && i < nextOperatorOrBracketIndex) {
					nextOperatorOrBracket = operatorOrBracket;
					nextOperatorOrBracketIndex = i;
				}
			}

			if (nextOperatorOrBracketIndex == expression.length()) {
				break;
			} else {

				if (currentIndex != nextOperatorOrBracketIndex) {
					outLine.add(expression.substring(currentIndex, nextOperatorOrBracketIndex));
				}

				if (nextOperatorOrBracket.equals(OPENING_PARENTHESIS)) {
					stack.push(nextOperatorOrBracket);

				} else if (nextOperatorOrBracket.equals(CLOSING_PARENTHESIS)) {

					while (!stack.peek().equals(OPENING_PARENTHESIS)) {
						outLine.add(stack.pop());
						if (stack.empty()) {
							throw new CalculatorException("Невалидные данные");
						}
					}
					stack.pop();

				} else {

					while (!stack.empty() && !stack.peek().equals(OPENING_PARENTHESIS) && (MATHEMATICAL_OPERATIONS
							.get(nextOperatorOrBracket) >= MATHEMATICAL_OPERATIONS.get(stack.peek()))) {
						outLine.add(stack.pop());
					}
					stack.push(nextOperatorOrBracket);
				}
				currentIndex = nextOperatorOrBracketIndex + nextOperatorOrBracket.length();
			}
		}

		if (currentIndex != expression.length()) {
			outLine.add(expression.substring(currentIndex));
		}

		while (!stack.empty()) {
			outLine.add(stack.pop());
		}

		StringBuffer result = new StringBuffer();
		result.append(outLine.remove(0));

		while (!outLine.isEmpty()) {
			result.append(" ").append(outLine.remove(0));
		}

		return result.toString();
	}

}
