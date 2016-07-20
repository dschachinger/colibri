package semanticCore.MsgObj;

/**
 * Created by georg on 28.06.16.
 */
public enum ContentType {
    TEXT_PLAIN("text/plain"),
    APPLICATION_XTURTLE("application/x-turtle"),
    APPLICATION_RDF_XML("application/rdf+xml"),
    APPLICATION_SPARQLQUERY("application/sparql-query"),
    APPLICATION_SPARQLUPDATE("application/sparql-update"),
    APPLICATION_SPARQLRESULT_RESULTJSON("application/sparql-result+json"),
    APPLICATION_SPARQLRESULT_XML("application/sparql-result+xml")
    ;

    private final String text;

    /**
     * @param text
     */
    private ContentType(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }

    public static ContentType stringToEnum(String value){
        for(ContentType type : ContentType.values()){
            if(type.toString().equals(value)){
                return type;
            }
        }
        return null;
    }
}