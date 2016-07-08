package channel.message.colibriMessage;

public class ColibriMessageContent {
    private String content;
    private final String newLine = "<br>";

    public ColibriMessageContent() {
        this.content = "";
    }

    public ColibriMessageContent(String content) {
        this.content = content;
    }

    public String getContentWithoutBreaks() {
        content = content.replaceAll(newLine, "");
        content = content.replaceAll(" ", "");
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
