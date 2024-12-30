package util;

import java.util.HashMap;
import java.util.Map;

public enum Language {
    ENGLISH {
        @Override
        public Map<Character, Double> getLetterFrequencies() {
            Map<Character, Double> freq = new HashMap<>();
            freq.put('A', 0.082);
            freq.put('B', 0.015);
            freq.put('C', 0.028);
            freq.put('D', 0.043);
            freq.put('E', 0.127);
            freq.put('F', 0.022);
            freq.put('G', 0.020);
            freq.put('H', 0.061);
            freq.put('I', 0.070);
            freq.put('J', 0.002);
            freq.put('K', 0.008);
            freq.put('L', 0.040);
            freq.put('M', 0.024);
            freq.put('N', 0.067);
            freq.put('O', 0.075);
            freq.put('P', 0.019);
            freq.put('Q', 0.001);
            freq.put('R', 0.060);
            freq.put('S', 0.063);
            freq.put('T', 0.091);
            freq.put('U', 0.028);
            freq.put('V', 0.010);
            freq.put('W', 0.023);
            freq.put('X', 0.001);
            freq.put('Y', 0.020);
            freq.put('Z', 0.001);
            return freq;
        }
    };

    public abstract Map<Character, Double> getLetterFrequencies();

    public boolean isValidCharacter(char c) {
        return getLetterFrequencies().containsKey(Character.toUpperCase(c));
    }
}
