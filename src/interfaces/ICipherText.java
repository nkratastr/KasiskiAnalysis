package interfaces;

import util.Language;
import java.util.List;
import java.util.Map;

public interface ICipherText {
    String getText();
    Language getLanguage();
    Map<String, List<Integer>> findRepeatingPatterns(int length);
    List<String> getSubstrings(int keyLength);
    void setExpectedKeyLength(int length);
    int getExpectedKeyLength();
}
