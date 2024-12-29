package entity;

import interfaces.ICipherText;
import util.Language;

import java.util.*;

public class CipherText implements ICipherText {
    private final String text;
    private final Language language;

    public CipherText(String text, Language language) {
        this.text = text;
        this.language = language;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Language getLanguage() {
        return language;
    }

    @Override
    public Map<String, List<Integer>> findRepeatingPatterns(int length) {
        Map<String, List<Integer>> patterns = new HashMap<>();
        
        for (int i = 0; i <= text.length() - length; i++) {
            String pattern = text.substring(i, i + length);
            patterns.computeIfAbsent(pattern, k -> new ArrayList<>()).add(i);
        }
        
        // Remove patterns that don't repeat
        patterns.entrySet().removeIf(entry -> entry.getValue().size() < 2);
        
        return patterns;
    }

    @Override
    public List<String> getSubstrings(int keyLength) {
        List<String> substrings = new ArrayList<>();
        
        // Her alt dizi için StringBuilder oluştur
        StringBuilder[] builders = new StringBuilder[keyLength];
        for (int i = 0; i < keyLength; i++) {
            builders[i] = new StringBuilder();
        }
        
        // Metni alt dizilere böl
        for (int i = 0; i < text.length(); i++) {
            builders[i % keyLength].append(text.charAt(i));
        }
        
        // StringBuilder'ları String'e çevir
        for (StringBuilder builder : builders) {
            substrings.add(builder.toString());
        }
        
        return substrings;
    }
}
