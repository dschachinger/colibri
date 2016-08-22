package com.colibri.Header;
// This class is used to clean the message
/**
 * Created by codelife on 12/8/16.
 */
public class Content {
    private String content;
    private final String newLine = "<br>";

    public Content() {
        this.content = "";
    }

    public Content(String content) {
        this.content = content;
    }

    public String getContentWithoutBreaksAndWhiteSpace() {
        content = content.replaceAll(newLine, "");
        content = content.replaceAll(" ", "");
        return content;
    }

    public String getContentWithoutBreaks() {
        content = content.replaceAll(newLine, "");
        while(content.contains(" <")) {
            content = content.replaceAll(" <", "<");
        }
        return content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return getContent();
    }
}
