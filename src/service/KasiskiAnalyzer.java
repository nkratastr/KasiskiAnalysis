package service;

import interfaces.ICipherText;
import java.util.*;

public class KasiskiAnalyzer {
    private static final int MIN_PATTERN_LENGTH = 3;
    private static final int MAX_PATTERN_LENGTH = 7;
    private static final int MAX_KEY_LENGTH = 19;

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
        Map<Integer, Integer> distanceFrequencies = new HashMap<>();
        Map<Integer, Integer> patternCounts = new HashMap<>();

        // Find repeating patterns and their frequencies
        for (int patternLength = MIN_PATTERN_LENGTH; patternLength <= MAX_PATTERN_LENGTH; patternLength++) {
            Map<String, List<Integer>> patterns = cipherText.findRepeatingPatterns(patternLength);
            
            for (Map.Entry<String, List<Integer>> entry : patterns.entrySet()) {
                List<Integer> positions = entry.getValue();
                for (int i = 0; i < positions.size() - 1; i++) {
                    for (int j = i + 1; j < positions.size(); j++) {
                        int distance = positions.get(j) - positions.get(i);
                        distanceFrequencies.merge(distance, 1, Integer::sum);
                    }
                }
            }
            
            // Her uzunluk için bulunan desen sayısını kaydet
            patterns.forEach((pattern, positions) -> {
                for (int factor : findFactors(positions.get(1) - positions.get(0))) {
                    if (factor <= MAX_KEY_LENGTH) {
                        patternCounts.merge(factor, 1, Integer::sum);
                    }
                }
            });
        }

        // Calculate probabilities for each possible key length
        List<KeyLengthProbability> probabilities = new ArrayList<>();
        Set<Integer> possibleLengths = new HashSet<>();
        
        // Find all possible lengths
        for (int distance : distanceFrequencies.keySet()) {
            possibleLengths.addAll(findFactors(distance));
        }
        possibleLengths.removeIf(length -> length > MAX_KEY_LENGTH);

        // Calculate total frequency for normalization
        int totalFrequency = distanceFrequencies.values().stream().mapToInt(Integer::intValue).sum();
        
        for (int length : possibleLengths) {
            // Calculate pattern frequency score
            int frequencyScore = countFactorFrequency(length, distanceFrequencies);
            
            // Calculate Index of Coincidence for this key length
            double ic = calculateIndexOfCoincidence(cipherText, length);
            
            // Pattern count for this length
            int patterns = patternCounts.getOrDefault(length, 0);
            
            // Calculate probability based on multiple factors
            double probability = calculateProbability(frequencyScore, totalFrequency, ic, patterns, patternCounts.values());
            
            probabilities.add(new KeyLengthProbability(length, probability, patterns, ic));
        }

        // Sort by final score descending
        probabilities.sort(KeyLengthProbability::compareTo);
        
        return probabilities;
    }

    private double calculateIndexOfCoincidence(ICipherText cipherText, int keyLength) {
        List<String> substrings = cipherText.getSubstrings(keyLength);
        double avgIC = 0.0;
        
        for (String substring : substrings) {
            Map<Character, Integer> frequencies = new HashMap<>();
            int n = substring.length();
            
            // Count frequencies
            for (char c : substring.toCharArray()) {
                frequencies.merge(c, 1, Integer::sum);
            }
            
            // Calculate IC for this substring
            double ic = 0.0;
            for (int freq : frequencies.values()) {
                ic += (freq * (freq - 1.0)) / (n * (n - 1.0));
            }
            avgIC += ic;
        }
        
        return avgIC / keyLength;
    }

    private double calculateProbability(int frequencyScore, int totalFrequency, 
                                      double indexOfCoincidence, int patternCount,
                                      Collection<Integer> allPatternCounts) {
        // Normalize frequency score
        double freqProb = (double) frequencyScore / totalFrequency;
        
        // Normalize pattern count
        int maxPatterns = allPatternCounts.stream().mapToInt(Integer::intValue).max().orElse(1);
        double patternProb = (double) patternCount / maxPatterns;
        
        // Weight for Index of Coincidence (English text typically has IC around 0.067)
        double icWeight = Math.exp(-Math.abs(indexOfCoincidence - 0.067) * 10);
        
        // Combine all factors (you can adjust weights)
        double probability = (freqProb * 0.4) + (patternProb * 0.3) + (icWeight * 0.3);
        
        return probability;
    }

    private List<Integer> findFactors(int n) {
        List<Integer> factors = new ArrayList<>();
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) {
                factors.add(i);
                if (i != n/i && n/i <= MAX_KEY_LENGTH) {
                    factors.add(n/i);
                }
            }
        }
        return factors;
    }

    private int countFactorFrequency(int factor, Map<Integer, Integer> distanceFrequencies) {
        int count = 0;
        for (Map.Entry<Integer, Integer> entry : distanceFrequencies.entrySet()) {
            if (entry.getKey() % factor == 0) {
                count += entry.getValue();
            }
        }
        return count;
    }
}
