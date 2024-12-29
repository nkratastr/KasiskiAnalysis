package entity;

import interfaces.IKey;
import util.Language;
import java.util.ArrayList;
import java.util.List;

public class Key implements IKey {
    private String text;
    private Language language;
    private List<Integer> possibleLengths;

    public Key(String text) {
        this(text, Language.ENGLISH);
    }

    public Key(String text, Language language) {
        setText(text);
        this.language = language;
        this.possibleLengths = new ArrayList<>();
    }

    @Override
    public List<Integer> getPossibleLengths() {
        return new ArrayList<>(possibleLengths);
    }

    @Override
    public void setPossibleLengths(List<Integer> lengths) {
        this.possibleLengths = new ArrayList<>(lengths);
    }

    @Override
    public boolean matchesPattern(String pattern) {
        if (pattern == null || pattern.length() != text.length()) {
            return false;
        }

        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) != '?' && pattern.charAt(i) != text.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public int getLength() {
        return text.length();
    }

    @Override
    public boolean isValid() {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (char c : text.toCharArray()) {
            if (!language.isValidCharacter(c)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setText(String text) {
        this.text = text.toUpperCase();
    }

    @Override
    public Language getLanguage() {
        return language;
    }

    @Override
    public void setLanguage(Language language) {
        this.language = language;
    }
}
