package by.lik.task1.entity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LineFilter {

	private String[] parameterLine;
	private ArrayList<String[]> allLines = new ArrayList<>();

	public void execute() {
		
		readDataFromConsole();

		System.out.println("Вывод:");
		for (String[] line : allLines) {

			if (isLineContainsParameter(line)) {
				printLineInConsole(line);
			}
		}
	}

	private void readDataFromConsole() {

		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {

			System.out.print("Параметр программы: ");
			parameterLine = bufferedReader.readLine().trim().split(" ");

			System.out.println("Строки:");
			String line;
			
			while (!(line = bufferedReader.readLine()).equals("")) {
				line = line.replace(";", "");
				allLines.add(line.trim().split(" "));
			}

		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private boolean isLineContainsParameter(String[] line) {

		for (String word : line) {

			if (isWordEqualsParameter(word)) {
				return true;
			}
		}

		return false;
	}

	private boolean isWordEqualsParameter(String word) {
		
		for (String partParameter : parameterLine) {

			if (isRegularExpression(partParameter)) {

				if (Pattern.matches(partParameter, word)) {
					return true;
				} 
			} 
			
			if (word.equals(partParameter)) {
				return true;
			}
			
		}

		return false;
	}

	private boolean isRegularExpression(String text) {
		try {
			Pattern.compile(text);
			return true;
		} catch (PatternSyntaxException e) {
			return false;
		}

	}

	private void printLineInConsole(String[] line) {

		System.out.print(line[0]);

		for (int i = 1; i < line.length; i++) {
			System.out.print(" " + line[i]);
		}
		System.out.println(";");
	}

}

