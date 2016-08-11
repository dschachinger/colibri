package com.colibri.Header;

/**
 * Created by codelife on 12/8/16.
 */
public enum ContentType {
    TEXT_PLAIN("text/plain"),
    APPLICATION_SPARQL("application/sparql-query"),
    APPLICATION_RDF_XML("application/rdf+xml");

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