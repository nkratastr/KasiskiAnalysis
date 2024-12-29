package interfaces;

public interface ICipherBreaker {
    IKey analyzeKey(ICipherText cipherText);
    String decrypt(ICipherText cipherText, IKey key);
}
