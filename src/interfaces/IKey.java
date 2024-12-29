package interfaces;

import java.util.List;

public interface IKey extends ICryptographicText {
    List<Integer> getPossibleLengths();
    void setPossibleLengths(List<Integer> lengths);
    boolean matchesPattern(String pattern);
}
