package channel.message.colibriMessage;

import service.Configurator;

public class ColibriMessageContent {
    private String content;
    private final String newLine = Configurator.getInstance().getNewlineString();

    public ColibriMessageContent() {
        this.content = "";
    }

    public ColibriMessageContent(String content) {
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
        //    content = content.replace("\"", "\\\"");
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

