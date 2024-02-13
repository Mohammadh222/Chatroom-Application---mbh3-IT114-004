package NumberGuess4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class NumberGuesser4 { 
        public static void main(String[] args) {
                NumberGuesser4 ng = new NumberGuesser4();
                ng.start();
            }

    private int maxLevel = 1;
    private int level = 1;
    private int strikes = 0;
    private int maxStrikes = 5;
    private int number = -1;
    private boolean pickNewRandom = true;
    private Random random = new Random();
    private String fileName = "ng4.txt";
    private String[] fileHeaders = { "Level", "Strikes", "Number", "MaxLevel" }; // used for demo readability

    private void saveState() {
        String[] data = { level + "", strikes + "", number + "", maxLevel + "" };
        String output = String.join(",", data);
        // Note: we don't need a file reference as FileWriter creates the file if it
        // doesn't exist
        try (FileWriter fw = new FileWriter(fileName)) {
            fw.write(String.join(",", fileHeaders));
            fw.write("\n"); // new line
            fw.write(output);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void loadState() {
        File file = new File(fileName);
        if (!file.exists()) {
            // Not providing output here as it's expected for a fresh start
            return;
        }
        try (Scanner reader = new Scanner(file)) {
            int lineNumber = 0;
            while (reader.hasNextLine()) {
                String text = reader.nextLine();
                // System.out.println("Text: " + text);
                if (lineNumber == 1) {
                    String[] data = text.split(",");
                    int temp = strToNum(data[0]);
                    if (temp > -1) {
                        this.level = temp;
                    }
                    temp = strToNum(data[1]);
                    if (temp > -1) {
                        this.strikes = temp;
                    }
                    temp = strToNum(data[2]);
                    if (temp > -1) {
                        this.number = temp;
                        pickNewRandom = false;
                    }
                    temp = strToNum(data[3]);
                    if (temp > -1) {
                        this.maxLevel = temp;
                    }
                }
                lineNumber++;
            }
        } catch (FileNotFoundException e) { // specific exception
            e.printStackTrace();
        } catch (Exception e2) { // any other unhandled exception
            e2.printStackTrace();
        }
        System.out.println("Loaded state");
        if (pickNewRandom) {
            generateNewNumber(level);
        } else {
            int range = 10 + ((level - 1) * 5);
            System.out.println("Welcome back to level " + level);
            System.out.println("Remember, I picked a number between 1-" + range + ", try to guess it.");
        }
    }

    /***
     * Generates a new random number for the current level and displays a welcome
     * message with the new number range.
     * 
     * @param level Current game level to calculate the number range.
     */
    private void generateNewNumber(int level) {
        int range = 10 + ((level - 1) * 5);
        System.out.println("Welcome to level " + level);
        System.out.println("I picked a random number between 1-" + range + ", let's see if you can guess.");
        number = random.nextInt(range) + 1;
    }

    private void win() {
        System.out.println("That's right!");
        level++; // level up!
        strikes = 0;
        pickNewRandom = true;
    }

    private boolean processCommands(String message) {
        boolean processed = false;
        if (message.equalsIgnoreCase("quit")) {
            System.out.println("Tired of playing? No problem, see you next time.");
            processed = true;
        }
        // TODO add other conditions here
        return processed;
    }

    private void lose() {
        System.out.println("Uh oh, looks like you need to get some more practice.");
        System.out.println("The correct number was " + number);
        strikes = 0;
        level--;
        if (level < 1) {
            level = 1;
        }
        pickNewRandom = true;
    }

    private void processGuess(int guess) {
        if (guess < 0) {
            return;
        }
        System.out.println("You guessed " + guess);
        if (guess == number) {
            win();
            pickNewRandom = true;
        } else {
            System.out.println("That's wrong");
        

            // Higher or lower hint
            if (guess > number) {
                System.out.println("Hint: Go lower!");
            } else {
                System.out.println("Hint: Go higher!");
            }

            // Cold, warm, hot indicator
            int difference = Math.abs(guess - number);
            if (difference > 10) {
                System.out.println("Temperature: You're cold.");
            } else if (difference > 5) {
                System.out.println("Temperature: You're warm.");
            } else {
                System.out.println("Temperature: You're hot!");
            }

            strikes++;
            if (strikes >= maxStrikes) {
                lose();
                pickNewRandom = true;
            }
        }
        saveState();
    }

    private int strToNum(String message) {
        try {
            return Integer.parseInt(message.trim());
        } catch (NumberFormatException e) {
            System.out.println("You didn't enter a number, please try again.");
            return -1; // Indicates an error in conversion
        }
    }

    public void start() {
        try (Scanner input = new Scanner(System.in)) {
            System.out.println("Welcome to NumberGuesser4.0");
            System.out.println("To exit, type the word 'quit'.");
            loadState();
            while (true) {
                if (pickNewRandom) {
                    generateNewNumber(level);
                    pickNewRandom = false;
                }
                System.out.println("Type a number and press enter.");
                String message = input.nextLine();
                if (processCommands(message)) {
                    break; // Exit the loop if "quit" command is processed
                }
                int guess = strToNum(message);
                if (guess >= 0) { // Proceed only if a valid number was entered
                    processGuess(guess);
                }
            }
        } catch (Exception e) {
            System.out.println("An unexpected error occurred. Goodbye.");
            e.printStackTrace();
        }
        System.out.println("Thanks for playing!");
}
}

