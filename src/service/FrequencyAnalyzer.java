package service;

import java.util.*;

public class FrequencyAnalyzer {
    
    public Map<Character, Double> analyzeFrequencies(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Input text cannot be null");
        }
        
        Map<Character, Double> frequencies = new HashMap<>();
        int totalChars = 0;
        
        // Harf sayılarını say
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                frequencies.merge(Character.toUpperCase(c), 1.0, Double::sum);
                totalChars++;
            }
        }
        
        // Frekansları hesapla
        if (totalChars == 0) {
            throw new IllegalArgumentException("Input text must contain at least one letter");
        }
        
        for (Map.Entry<Character, Double> entry : frequencies.entrySet()) {
            entry.setValue(entry.getValue() / totalChars);
        }
        
        return frequencies;
    }

    public double calculateChiSquare(Map<Character, Double> observed, Map<Character, Double> expected) {
        if (observed == null || expected == null) {
            throw new IllegalArgumentException("Frequency maps cannot be null");
        }
        
        double chiSquare = 0.0;
        
        for (char c = 'A'; c <= 'Z'; c++) {
            double o = observed.getOrDefault(c, 0.0);
            double e = expected.getOrDefault(c, 0.0);
            
            if (e > 0) {
                chiSquare += Math.pow(o - e, 2) / e;
            }
        }
        
        return chiSquare;
    }

    public double calculateIndexOfCoincidence(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Input text cannot be null");
        }
        
        int n = text.length();
        if (n <= 1) {
            throw new IllegalArgumentException("Text must contain at least 2 characters to calculate IoC");
        }
        
        Map<Character, Integer> frequencies = new HashMap<>();
        for (char c : text.toCharArray()) {
            if (!Character.isLetter(c)) {
                throw new IllegalArgumentException("Text must contain only letters");
            }
            frequencies.merge(Character.toUpperCase(c), 1, Integer::sum);
        }
        
        double sum = 0;
        for (int count : frequencies.values()) {
            sum += count * (count - 1);
        }
        
        return sum / (n * (n - 1));
    }
}
