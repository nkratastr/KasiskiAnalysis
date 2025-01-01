package service;

import interfaces.ICipherText;
import java.util.*;

public class KasiskiAnalyzer {
    private static final int MIN_PATTERN_LENGTH = 3;
    private static final int MAX_PATTERN_LENGTH = 7;
    private static final int MAX_KEY_LENGTH = 19;
    private static final int MIN_KEY_LENGTH = 2;  // Added minimum key length
    private final FrequencyAnalyzer frequencyAnalyzer;

    public KasiskiAnalyzer() {
        this.frequencyAnalyzer = new FrequencyAnalyzer();
    }

    public static class KeyLengthProbability implements Comparable<KeyLengthProbability> {
        private final int length;
        private final double probability;
        private final int patternCount;
        private final double indexOfCoincidence;
        private final double finalScore;
        private static final double PATTERN_WEIGHT = 0.6;  // Pattern probability weight
        private static final double IOC_WEIGHT = 0.4;      // Index of Coincidence weight

        public KeyLengthProbability(int length, double probability, int patternCount, double indexOfCoincidence) {
            this.length = length;
            this.probability = probability;
            this.patternCount = patternCount;
            this.indexOfCoincidence = indexOfCoincidence;
            
            // Normalize IoC (typical range 0.06-0.08 for English)
            double normalizedIoC = Math.min((indexOfCoincidence - 0.03) / 0.05, 1.0) * 100;
            // Calculate final score (0-100)
            this.finalScore = (probability * 100 * PATTERN_WEIGHT) + (normalizedIoC * IOC_WEIGHT);
        }

        @Override
        public String toString() {
            return String.format("Length: %d | Pattern Probability: %.2f%% | Pattern Count: %d | Coincidence Index: %.3f | Final Score: %.2f%%",
                    length, probability * 100, patternCount, indexOfCoincidence, finalScore);
        }

        public int getLength() {
            return length;
        }

        public double getProbability() {
            return probability;
        }

        public double getFinalScore() {
            return finalScore;
        }

        @Override
        public int compareTo(KeyLengthProbability other) {
            return Double.compare(other.finalScore, this.finalScore);  // Descending order
        }
    }

    public List<KeyLengthProbability> findPossibleKeyLengths(ICipherText cipherText) {
        if (cipherText == null) {
            throw new IllegalArgumentException("CipherText cannot be null");
        }

        if (cipherText.getText().length() < MIN_PATTERN_LENGTH) {
            throw new IllegalArgumentException("Text length must be at least " + MIN_PATTERN_LENGTH + " characters");
        }

        Map<Integer, Integer> distanceFrequencies = new HashMap<>();
        Map<Integer, Integer> patternCounts = new HashMap<>();

        // Find repeating patterns and their frequencies
        for (int patternLength = MIN_PATTERN_LENGTH; patternLength <= MAX_PATTERN_LENGTH; patternLength++) {
            try {
                Map<String, List<Integer>> patterns = cipherText.findRepeatingPatterns(patternLength);
                
                for (Map.Entry<String, List<Integer>> entry : patterns.entrySet()) {
                    List<Integer> positions = entry.getValue();
                    if (positions.size() < 2) {
                        continue;
                    }
                    
                    for (int i = 0; i < positions.size() - 1; i++) {
                        for (int j = i + 1; j < positions.size(); j++) {
                            int distance = positions.get(j) - positions.get(i);
                            if (distance <= 0) {
                                throw new IllegalStateException("Invalid pattern positions detected");
                            }
                            distanceFrequencies.merge(distance, 1, Integer::sum);
                        }
                    }
                }
                
                // Store pattern count for each length
                patterns.forEach((pattern, positions) -> {
                    if (positions.size() >= 2) {
                        int firstDistance = positions.get(1) - positions.get(0);
                        if (firstDistance > 0) {
                            for (int factor : findFactors(firstDistance)) {
                                if (factor <= MAX_KEY_LENGTH) {
                                    patternCounts.merge(factor, 1, Integer::sum);
                                }
                            }
                        }
                    }
                });
            } catch (Exception e) {
                throw new IllegalStateException("Error analyzing patterns of length " + patternLength + ": " + e.getMessage());
            }
        }

        if (distanceFrequencies.isEmpty()) {
            throw new IllegalStateException("No repeating patterns found in the text");
        }

        // Calculate probabilities for each possible key length
        List<KeyLengthProbability> probabilities = new ArrayList<>();
        Set<Integer> possibleLengths = new HashSet<>();
        
        // Find all possible lengths
        for (int distance : distanceFrequencies.keySet()) {
            possibleLengths.addAll(findFactors(distance));
        }
        possibleLengths.removeIf(length -> length > MAX_KEY_LENGTH || length < MIN_KEY_LENGTH);

        if (possibleLengths.isEmpty()) {
            throw new IllegalStateException("No valid key lengths found (must be between " + MIN_KEY_LENGTH + " and " + MAX_KEY_LENGTH + ")");
        }

        // Calculate total frequency for normalization
        int totalFrequency = distanceFrequencies.values().stream().mapToInt(Integer::intValue).sum();
        
        for (int length : possibleLengths) {
            try {
                // Calculate pattern frequency score
                int frequencyScore = countFactorFrequency(length, distanceFrequencies);
                
                // Calculate Index of Coincidence for this key length
                List<String> subTexts = cipherText.getSubstrings(length);
                double avgIoC = subTexts.stream()
                    .mapToDouble(frequencyAnalyzer::calculateIndexOfCoincidence)
                    .average()
                    .orElseThrow(() -> new IllegalStateException("Failed to calculate IoC for length " + length));
                
                double probability = (double) frequencyScore / totalFrequency;
                int patternCount = patternCounts.getOrDefault(length, 0);
                
                probabilities.add(new KeyLengthProbability(length, probability, patternCount, avgIoC));
            } catch (Exception e) {
                throw new IllegalStateException("Error analyzing key length " + length + ": " + e.getMessage());
            }
        }

        if (probabilities.isEmpty()) {
            throw new IllegalStateException("Failed to calculate probabilities for any key length");
        }

        Collections.sort(probabilities);
        return probabilities;
    }

    private List<Integer> findFactors(int number) {
        if (number <= 0) {
            throw new IllegalArgumentException("Number must be positive");
        }

        List<Integer> factors = new ArrayList<>();
        for (int i = 1; i <= Math.sqrt(number); i++) {
            if (number % i == 0) {
                factors.add(i);
                if (i != number / i) {
                    factors.add(number / i);
                }
            }
        }
        return factors;
    }

    private int countFactorFrequency(int factor, Map<Integer, Integer> frequencies) {
        if (factor <= 0) {
            throw new IllegalArgumentException("Factor must be positive");
        }
        if (frequencies == null || frequencies.isEmpty()) {
            throw new IllegalArgumentException("Frequencies map cannot be null or empty");
        }

        return frequencies.entrySet().stream()
            .filter(e -> e.getKey() % factor == 0)
            .mapToInt(Map.Entry::getValue)
            .sum();
    }
}
