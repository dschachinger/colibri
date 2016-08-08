package channel.message.messageObj;

public enum ContentType {
    TEXT_PLAIN("text/plain"),
    APPLICATION_RDF_XML("application/rdf+xml"),
    APPLICATION_SPARQL_QUERY("application/sparql-query"),
    APPLICATION_SPARQL_RESULT_XML("application/sparql-result+xml"),
    APPLICATION_SPARQL_UPDATE("application/sparql-update"),
    APPLICATION_SPARQL_RESULT_JSON("application/sparql-result+json");

    private String type;

    ContentType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static ContentType fromString(String text) {
        if (text != null) {
            for (ContentType type : ContentType.values()) {
                if (text.equalsIgnoreCase(type.type)) {
                    return type;
                }
            }
        }
        return TEXT_PLAIN;
    }
}
