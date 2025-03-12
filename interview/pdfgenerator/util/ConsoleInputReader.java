package com.interview.pdfgenerator.util;

import org.springframework.stereotype.Component;
import java.util.Scanner;

/**
 * Utility class for reading user input from the console.
 */
@Component
public class ConsoleInputReader {
    private final Scanner scanner;

    public ConsoleInputReader() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Read a line of input from the console with a prompt.
     * 
     * @param prompt The prompt to display to the user
     * @return The user's input as a string
     */
    public String readInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /**
     * Read an integer input from the console with a prompt.
     * 
     * @param prompt The prompt to display to the user
     * @param defaultValue The default value to use if input is invalid
     * @return The user's input as an integer, or the default value if input is invalid
     */
    public int readIntInput(String prompt, int defaultValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Using default value: " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * Read a yes/no input from the console with a prompt.
     * 
     * @param prompt The prompt to display to the user
     * @param defaultValue The default value to use if input is invalid
     * @return true for yes, false for no
     */
    public boolean readYesNoInput(String prompt, boolean defaultValue) {
        String defaultStr = defaultValue ? "Y" : "N";
        System.out.print(prompt + " (Y/N) [" + defaultStr + "]: ");
        String input = scanner.nextLine().trim().toUpperCase();
        
        if (input.isEmpty()) {
            return defaultValue;
        }
        
        return input.startsWith("Y");
    }
} 