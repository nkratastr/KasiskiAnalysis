import entity.CipherText;
import interfaces.ICipherText;
import interfaces.IKey;
import service.VigenereCipherBreaker;
import service.KasiskiAnalyzer;
import service.KasiskiAnalyzer.KeyLengthProbability;
import util.Language;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class App {
    private static final VigenereCipherBreaker cipherBreaker = new VigenereCipherBreaker();
    private static final KasiskiAnalyzer kasiskiAnalyzer = new KasiskiAnalyzer();

    // ANSI renk kodları
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";

    public static void main(String[] args) {
        Scanner scanner = null;
        Scanner responseScanner = null;
        try {
            System.out.println("Vigenère Cipher Kasiski Analysis");
            System.out.println("===============================");
            
            // Read from console
            System.out.println("\nPlease paste the ciphertext and press Enter twice:");
            StringBuilder input = new StringBuilder();
            scanner = new Scanner(System.in);
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.isEmpty()) {
                    if (input.length() > 0 && input.charAt(input.length() - 1) == '\n') {
                        break;
                    }
                }
                input.append(line).append('\n');
            }
            
            String ciphertext = input.toString().trim().toUpperCase();
            
            // Input validation
            if (ciphertext.isEmpty()) {
                System.out.println(ANSI_RED + "Error: Empty text entered!" + ANSI_RESET);
                return;
            }

            // Check for minimum length
            if (ciphertext.length() < 20) {
                System.out.println(ANSI_RED + "Error: Text is too short! Please enter at least 20 characters for meaningful analysis." + ANSI_RESET);
                return;
            }

            // Check for non-letter characters
            if (!ciphertext.matches("[A-Z]+")) {
                System.out.println(ANSI_RED + "Error: Text contains invalid characters! Please use only letters (A-Z)." + ANSI_RESET);
                return;
            }

            // Create CipherText object (default English)
            ICipherText cipherText = new CipherText(ciphertext, Language.ENGLISH);
            
            System.out.println("\nStarting analysis...");
            System.out.println("-------------------");
            System.out.println("Text to analyze: " + ciphertext);

            try {
                // Find and display repeating patterns
                System.out.println("\n1. Analysis of repeating patterns:");
                for (int length = 3; length <= 5; length++) {
                    Map<String, List<Integer>> patterns = cipherText.findRepeatingPatterns(length);
                    if (!patterns.isEmpty()) {
                        System.out.println("Repeating patterns of length " + length + ":");
                        patterns.forEach((pattern, positions) -> 
                            System.out.println("  " + pattern + " -> Positions: " + positions));
                    }
                }

                // Display possible key lengths and probabilities
                System.out.println("\n2. Possible key lengths and probabilities:");
                System.out.println("============================================");
                List<KeyLengthProbability> keyLengthProbabilities = kasiskiAnalyzer.findPossibleKeyLengths(cipherText);
                
                if (keyLengthProbabilities.isEmpty()) {
                    throw new IllegalStateException("No possible key lengths found. The text might be too short or not a Vigenère cipher.");
                }

                System.out.println("\nResults (sorted by final score):");
                System.out.println("-----------------------------------");
                for (int i = 0; i < keyLengthProbabilities.size(); i++) {
                    KeyLengthProbability prob = keyLengthProbabilities.get(i);
                    System.out.printf("%d. %s%n", i + 1, prob);
                }

                System.out.println("\nNote: Final score is calculated based on pattern probability (60%) and coincidence index (40%) weights.");
                
                // Use the highest probability key length first
                int selectedKeyLength = keyLengthProbabilities.get(0).getLength();
                System.out.println("\nAutomatically using the highest probability key length: " + selectedKeyLength);

                boolean tryAgain;
                responseScanner = new Scanner(System.in);
                do {
                    try {
                        // Find key using selected key length
                        System.out.println("\n3. Starting key analysis for length " + selectedKeyLength + "...");
                        cipherText.setExpectedKeyLength(selectedKeyLength);
                        IKey key = cipherBreaker.analyzeKey(cipherText);
                        
                        if (key == null) {
                            throw new IllegalStateException("Failed to find a valid key.");
                        }

                        // Display results
                        System.out.println("\nProbable key found: " + ANSI_RED + key.getText() + ANSI_RESET);
                        
                        // Decrypt text
                        System.out.println("\n4. Decrypting text...");
                        String plaintext = cipherBreaker.decrypt(cipherText, key);
                        
                        System.out.println("\nResults:");
                        System.out.println("---------");
                        System.out.println("Ciphertext: " + ciphertext);
                        System.out.println("Key: " + key.getText());
                        System.out.println("Decrypted text: " + plaintext);

                        // Ask if results are satisfactory
                        System.out.println("\nAre you satisfied with these results? (Y/N)");
                        String response = responseScanner.nextLine().toUpperCase();
                        
                        if (response.equals("N")) {
                            System.out.println("\nAvailable key lengths:");
                            for (int i = 0; i < keyLengthProbabilities.size(); i++) {
                                KeyLengthProbability prob = keyLengthProbabilities.get(i);
                                System.out.printf("%d. Length: %d (Score: %.2f%%)%n", 
                                    i + 1, prob.getLength(), prob.getFinalScore());
                            }
                            
                            System.out.println("\nEnter the number of the key length you want to try (1-" + 
                                keyLengthProbabilities.size() + "):");
                            try {
                                int choice = Integer.parseInt(responseScanner.nextLine());
                                if (choice < 1 || choice > keyLengthProbabilities.size()) {
                                    throw new IllegalArgumentException("Invalid choice. Please select a number between 1 and " + 
                                        keyLengthProbabilities.size());
                                }
                                selectedKeyLength = keyLengthProbabilities.get(choice - 1).getLength();
                                tryAgain = true;
                            } catch (NumberFormatException e) {
                                System.out.println(ANSI_RED + "Invalid input. Please enter a valid number." + ANSI_RESET);
                                tryAgain = true;
                            }
                        } else {
                            tryAgain = false;
                        }
                    } catch (IllegalStateException e) {
                        System.out.println(ANSI_RED + "Error during key analysis: " + e.getMessage() + ANSI_RESET);
                        System.out.println("Would you like to try a different key length? (Y/N)");
                        tryAgain = responseScanner.nextLine().toUpperCase().equals("Y");
                    }
                } while (tryAgain);
            } catch (IllegalStateException e) {
                System.out.println(ANSI_RED + "Analysis error: " + e.getMessage() + ANSI_RESET);
            } catch (Exception e) {
                System.out.println(ANSI_RED + "Unexpected error during analysis: " + e.getMessage() + ANSI_RESET);
            }
        } catch (Exception e) {
            System.out.println(ANSI_RED + "Fatal error: " + e.getMessage() + ANSI_RESET);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
            if (responseScanner != null) {
                responseScanner.close();
            }
        }

        // Language support information and language selection
        try {
            System.out.println("\nWould you like to analyze in a different language? (Y/N)");
            String langResponse = responseScanner.nextLine().toUpperCase();
            
            if (langResponse.equals("Y")) {
                System.out.println("Currently only English is supported.");
                System.out.println("Turkish support will be added soon!");
            }
        } catch (Exception e) {
            System.out.println(ANSI_RED + "Error during language selection: " + e.getMessage() + ANSI_RESET);
        }
    }
}
