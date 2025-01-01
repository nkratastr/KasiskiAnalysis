package service;

import entity.Key;
import interfaces.ICipherText;
import interfaces.IKey;
import util.Language;

import java.util.*;

public class VigenereCipherBreaker {
    private final KasiskiAnalyzer kasiskiAnalyzer;
    private final FrequencyAnalyzer frequencyAnalyzer;
    private static final int MIN_KEY_LENGTH = 2;

    public VigenereCipherBreaker() {
        this.kasiskiAnalyzer = new KasiskiAnalyzer();
        this.frequencyAnalyzer = new FrequencyAnalyzer();
    }

    public IKey analyzeKey(ICipherText cipherText) {
        if (cipherText == null) {
            throw new IllegalArgumentException("CipherText cannot be null");
        }
        
        if (cipherText.getText().isEmpty()) {
            throw new IllegalArgumentException("CipherText cannot be empty");
        }

        int keyLength;
        
        // Use user-specified key length if provided
        if (cipherText.getExpectedKeyLength() > 0) {
            keyLength = cipherText.getExpectedKeyLength();
            if (keyLength < MIN_KEY_LENGTH) {
                throw new IllegalArgumentException("Key length must be at least " + MIN_KEY_LENGTH);
            }
        } else {
            // Find possible key lengths
            List<KasiskiAnalyzer.KeyLengthProbability> keyLengthProbs = kasiskiAnalyzer.findPossibleKeyLengths(cipherText);
            
            if (keyLengthProbs.isEmpty()) {
                throw new IllegalStateException("No possible key lengths found");
            }

            // Get the most probable key length
            keyLength = keyLengthProbs.get(0).getLength();
        }
        
        if (keyLength < MIN_KEY_LENGTH || keyLength > cipherText.getText().length()) {
            throw new IllegalArgumentException("Invalid key length: " + keyLength + 
                ". Must be between " + MIN_KEY_LENGTH + " and " + cipherText.getText().length());
        }

        // Split text into substrings based on key length
        List<String> subTexts = cipherText.getSubstrings(keyLength);
        
        if (subTexts.isEmpty()) {
            throw new IllegalStateException("Failed to split text into substrings");
        }

        // Find most likely shift for each substring
        StringBuilder keyBuilder = new StringBuilder();
        
        for (String subText : subTexts) {
            if (subText.isEmpty()) {
                throw new IllegalStateException("Empty substring encountered during analysis");
            }
            Map<Character, Double> frequencies = frequencyAnalyzer.analyzeFrequencies(subText);
            char mostLikelyShift = findMostLikelyShift(frequencies, cipherText.getLanguage());
            keyBuilder.append(mostLikelyShift);
        }

        String keyText = keyBuilder.toString();
        if (keyText.isEmpty()) {
            throw new IllegalStateException("Failed to generate key");
        }

        return new Key(keyText);
    }

    public String decrypt(ICipherText cipherText, IKey key) {
        if (cipherText == null || key == null) {
            throw new IllegalArgumentException("CipherText and Key cannot be null");
        }

        String text = cipherText.getText();
        String keyText = key.getText();

        if (text.isEmpty() || keyText.isEmpty()) {
            throw new IllegalArgumentException("CipherText and Key cannot be empty");
        }

        StringBuilder plaintext = new StringBuilder();
        
        try {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                char k = keyText.charAt(i % keyText.length());
                
                if (!Character.isLetter(c) || !Character.isLetter(k)) {
                    throw new IllegalArgumentException("Both ciphertext and key must contain only letters");
                }

                // Vigenère decryption formula: Pi = (Ci - Ki + 26) mod 26
                int shift = (c - k + 26) % 26;
                char p = (char) ('A' + shift);
                
                plaintext.append(p);
            }
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalStateException("Error during decryption: " + e.getMessage());
        }
        
        return plaintext.toString();
    }

    private char findMostLikelyShift(Map<Character, Double> frequencies, Language language) {
        if (frequencies == null || language == null) {
            throw new IllegalArgumentException("Frequencies and Language cannot be null");
        }

        // Get expected frequencies for the language
        Map<Character, Double> expectedFreqs = language.getLetterFrequencies();
        
        if (expectedFreqs.isEmpty()) {
            throw new IllegalStateException("No frequency data available for language: " + language);
        }

        // Calculate score for each possible shift
        double bestScore = Double.NEGATIVE_INFINITY;
        char bestShift = 'A';
        
        for (char shift = 'A'; shift <= 'Z'; shift++) {
            double score = 0;
            
            // For each letter
            for (char c = 'A'; c <= 'Z'; c++) {
                // Find shifted letter
                char shiftedChar = (char) (((c - shift + 26) % 26) + 'A');
                
                // Observed frequency for this letter
                double observedFreq = frequencies.getOrDefault(c, 0.0);
                
                // Expected frequency for shifted letter
                double expectedFreq = expectedFreqs.getOrDefault(shiftedChar, 0.0);
                
                // Add frequency similarity to score
                score += observedFreq * expectedFreq;
            }
            
            // Update best score
            if (score > bestScore) {
                bestScore = score;
                bestShift = shift;
            }
        }
        
        return bestShift;
    }
}
