import entity.CipherText;
import interfaces.ICipherText;
import interfaces.IKey;
import service.VigenereCipherBreaker;
import service.KasiskiAnalyzer;
import service.KasiskiAnalyzer.KeyLengthProbability;
import util.Language;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    private static final VigenereCipherBreaker cipherBreaker = new VigenereCipherBreaker();
    private static final KasiskiAnalyzer kasiskiAnalyzer = new KasiskiAnalyzer();

    public static void main(String[] args) {
        try {
            System.out.println("Vigenère Cipher Kasiski Analizi");
            System.out.println("===============================");
            
            String ciphertext;
            
            if (args.length > 0) {
                // Dosyadan oku
                ciphertext = new String(Files.readAllBytes(Paths.get(args[0]))).trim().toUpperCase();
            } else {
                // Konsoldan oku
                System.out.println("\nLütfen şifreli metni yapıştırın ve iki kere Enter'a basın:");
                StringBuilder input = new StringBuilder();
                Scanner scanner = new Scanner(System.in);
                
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.isEmpty()) {
                        if (input.length() > 0 && input.charAt(input.length() - 1) == '\n') {
                            break;
                        }
                    }
                    input.append(line).append('\n');
                }
                
                ciphertext = input.toString().trim().toUpperCase();
            }
            
            if (ciphertext.isEmpty()) {
                System.out.println("Hata: Boş metin girdiniz!");
                return;
            }

            // CipherText nesnesini oluştur (varsayılan olarak İngilizce)
            ICipherText cipherText = new CipherText(ciphertext, Language.ENGLISH);
            
            System.out.println("\nAnaliz başlıyor...");
            System.out.println("-------------------");
            System.out.println("Analiz edilecek metin: " + ciphertext);

            // Tekrar eden desenleri bul ve göster
            System.out.println("\n1. Tekrar eden desenlerin analizi:");
            for (int length = 3; length <= 5; length++) {
                Map<String, List<Integer>> patterns = cipherText.findRepeatingPatterns(length);
                if (!patterns.isEmpty()) {
                    System.out.println(length + " karakter uzunluğunda tekrar eden desenler:");
                    patterns.forEach((pattern, positions) -> 
                        System.out.println("  " + pattern + " -> Pozisyonlar: " + positions));
                }
            }

            // Olası anahtar uzunluklarını ve olasılıklarını göster
            System.out.println("\n2. Olası anahtar uzunlukları ve olasılıkları:");
            List<KeyLengthProbability> keyLengthProbabilities = kasiskiAnalyzer.findPossibleKeyLengths(cipherText);
            
            for (int i = 0; i < keyLengthProbabilities.size(); i++) {
                KeyLengthProbability prob = keyLengthProbabilities.get(i);
                System.out.printf("%d. %s%n", i + 1, prob);
            }

            // En olası anahtar uzunluğunu kullanarak anahtarı bul
            System.out.println("\n3. Anahtar analizi başlıyor...");
            IKey key = cipherBreaker.analyzeKey(cipherText);
            
            if (key == null) {
                System.out.println("Anahtar bulunamadı!");
                return;
            }

            // Sonuçları göster
            System.out.println("\nBulunan muhtemel anahtar: " + key.getText());
            
            // Metni çöz
            System.out.println("\n4. Metin çözülüyor...");
            String plaintext = cipherBreaker.decrypt(cipherText, key);
            
            System.out.println("\nSonuçlar:");
            System.out.println("---------");
            System.out.println("Şifreli metin: " + ciphertext);
            System.out.println("Anahtar: " + key.getText());
            System.out.println("Çözülmüş metin: " + plaintext);
            
            // Kullanıcıya sonuçların mantıklı olup olmadığını sor
            System.out.println("\nSonuçlar mantıklı görünüyor mu? (E/H)");
            Scanner responseScanner = new Scanner(System.in);
            String response = responseScanner.nextLine().toUpperCase();
            
            if (response.equals("H")) {
                System.out.println("\nFarklı bir anahtar uzunluğu denemek ister misiniz? (E/H)");
                response = responseScanner.nextLine().toUpperCase();
                if (response.equals("E")) {
                    System.out.println("Hangi uzunluğu denemek istersiniz? (1-" + keyLengthProbabilities.size() + "):");
                    int choice = Integer.parseInt(responseScanner.nextLine());
                    if (choice > 0 && choice <= keyLengthProbabilities.size()) {
                        int newLength = keyLengthProbabilities.get(choice - 1).getLength();
                        System.out.println("Bu özellik henüz eklenmedi. Seçilen uzunluk: " + newLength);
                    }
                }
            }
            
            // Farklı dil seçeneği
            System.out.println("\nBaşka bir dilde analiz yapmak ister misiniz? (E/H)");
            response = responseScanner.nextLine().toUpperCase();
            
            if (response.equals("E")) {
                System.out.println("Şu an için sadece İngilizce destekleniyor.");
                System.out.println("Yakında Türkçe desteği de eklenecek!");
            }
            
        } catch (IOException e) {
            System.out.println("Hata oluştu: " + e.getMessage());
        }
    }
}
