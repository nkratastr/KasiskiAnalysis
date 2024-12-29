package service;

import entity.Key;
import interfaces.ICipherText;
import interfaces.IKey;
import util.Language;

import java.util.*;

public class VigenereCipherBreaker {
    private final KasiskiAnalyzer kasiskiAnalyzer;
    private final FrequencyAnalyzer frequencyAnalyzer;

    public VigenereCipherBreaker() {
        this.kasiskiAnalyzer = new KasiskiAnalyzer();
        this.frequencyAnalyzer = new FrequencyAnalyzer();
    }

    public IKey analyzeKey(ICipherText cipherText) {
        // Olası anahtar uzunluklarını bul
        List<KasiskiAnalyzer.KeyLengthProbability> keyLengthProbs = kasiskiAnalyzer.findPossibleKeyLengths(cipherText);
        
        if (keyLengthProbs.isEmpty()) {
            return null;
        }

        // En yüksek olasılıklı anahtar uzunluğunu al
        int keyLength = keyLengthProbs.get(0).getLength();
        
        // Metni anahtar uzunluğuna göre alt dizilere böl
        List<String> subTexts = cipherText.getSubstrings(keyLength);
        
        // Her alt dizi için en olası kaydırma miktarını bul
        StringBuilder keyBuilder = new StringBuilder();
        
        for (String subText : subTexts) {
            Map<Character, Double> frequencies = frequencyAnalyzer.analyzeFrequencies(subText);
            char mostLikelyShift = findMostLikelyShift(frequencies, cipherText.getLanguage());
            keyBuilder.append(mostLikelyShift);
        }

        return new Key(keyBuilder.toString());
    }

    public String decrypt(ICipherText cipherText, IKey key) {
        String text = cipherText.getText();
        String keyText = key.getText();
        StringBuilder plaintext = new StringBuilder();
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            char k = keyText.charAt(i % keyText.length());
            
            // Vigenère deşifreleme formülü: Pi = (Ci - Ki + 26) mod 26
            int shift = (c - k + 26) % 26;
            char p = (char) ('A' + shift);
            
            plaintext.append(p);
        }
        
        return plaintext.toString();
    }

    private char findMostLikelyShift(Map<Character, Double> frequencies, Language language) {
        // Dile göre beklenen frekansları al
        Map<Character, Double> expectedFreqs = language.getLetterFrequencies();
        
        // Her kaydırma miktarı için bir skor hesapla
        double bestScore = Double.NEGATIVE_INFINITY;
        char bestShift = 'A';
        
        for (char shift = 'A'; shift <= 'Z'; shift++) {
            double score = 0;
            
            // Her harf için
            for (char c = 'A'; c <= 'Z'; c++) {
                // Kaydırılmış harfi bul
                char shiftedChar = (char) (((c - shift + 26) % 26) + 'A');
                
                // Bu harfin gözlemlenen frekansı
                double observedFreq = frequencies.getOrDefault(c, 0.0);
                
                // Kaydırılmış harfin beklenen frekansı
                double expectedFreq = expectedFreqs.getOrDefault(shiftedChar, 0.0);
                
                // Frekansların benzerliğini skora ekle
                score += observedFreq * expectedFreq;
            }
            
            // En iyi skoru güncelle
            if (score > bestScore) {
                bestScore = score;
                bestShift = shift;
            }
        }
        
        return bestShift;
    }
}
