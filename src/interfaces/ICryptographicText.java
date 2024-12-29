package interfaces;

import util.Language;

public interface ICryptographicText {
    String getText();
    int getLength();
    boolean isValid();
    void setText(String text);
    Language getLanguage();
    void setLanguage(Language language);
}
