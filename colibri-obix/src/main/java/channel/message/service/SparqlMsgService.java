package channel.message.service;

import channel.message.colibriMessage.ColibriMessage;
import channel.message.colibriMessage.ColibriMessageContent;
import channel.message.colibriMessage.ColibriMessageHeader;
import channel.message.messageObj.ContentType;
import channel.message.messageObj.MessageIdentifier;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ListHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SparqlMsgService {

    private static final Logger logger = LoggerFactory.getLogger(SparqlMsgService.class);

    public static void processSparqlResultSetfromColibriMessage(ColibriMessage msg, List<String> expectedVars) {
        String resultSetString = msg.getContent().getContentWithoutBreaksAndWhiteSpace();
        InputStream stream = new ByteArrayInputStream(resultSetString.getBytes(StandardCharsets.UTF_8));
        ResultSet set;
        if(msg.getHeader().getContentType().equals(ContentType.APPLICATION_SPARQL_RESULT_JSON)) {
            set = ResultSetFactory.fromJSON(stream);
        } else if (msg.getHeader().getContentType().equals(ContentType.APPLICATION_SPARQL_RESULT_XML)) {
            set = ResultSetFactory.fromXML(stream);
        } else {
            throw new IllegalArgumentException("QRE messsage content type of mesage with ID " + msg.getHeader().getId() + " not supported");
        }
        if(!ListHelper.listsUnorderedEqual(expectedVars, set.getResultVars())) {
            String message = "The SPARQL result set does not contain the expected result vars. Received Set: " +
                    msg.getContent().getContent();
            logger.info(message);
            throw new IllegalArgumentException(message);
        }
        while(set.hasNext()) {
            QuerySolution sol = set.nextSolution();
            Iterator<String> it = sol.varNames();
            while(it.hasNext()) {
                String name = it.next();
                RDFNode node = sol.get(name);
                if(node.isLiteral()) {
                    Literal l = node.asLiteral();
                    System.out.println(name + ": " + l.getValue());
                }
                if(node.isResource()) {
                    Resource r = node.asResource();
                    System.out.println(name + ": " + r.toString());
                }
            }
        }
    }

    public static String createExampleQuery() {
        return "PREFIX\n" +
                "PREFIX\n" +
                "PREFIX\n" +
                "PREFIX\n" +
                "PREFIX\n" +
                "rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "owl: <http://www.w3.org/2002/07/owl#>\n" +
                "rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "colibri: <https://.../colibri.owl#>\n\n" +
                "SELECT ?service ?identifier\n" +
                "WHERE { ?service rdf:type colibri:DataService.\n" +
                "?service colibri:hasDataConfiguration ?y.\n" +
                "?y colibri:hasParameter ?z.\n" +
                "?z rdf:type colibri:TemperatureParameter.\n" +
                "?service colibri:identifier ?identifier}";
    }

    public static void main (String[] args) {
        String resultSetJson = "{\n" +
                "\"head\": { \"vars\": [ \"service\", \"identifier\" ] },\n" +
                "\"results\": {\n" +
                "\"bindings\": [\n" +
                "{\n" +
                "\"service\" : { \"type\": \"uri\", \"value\": \"http://www.colibri-samples.org/service1\" },\n" +
                "\"identifier\" : { \"type\": \"literal\", \"value\": \"temp_monitoring_17\" }\n" +
                "}\n" +
                "]\n" +
                "}\n" +
                "}";

        System.out.println("-----------------------------------------------\n" +
                "JSON Example Result Set: ");
        ColibriMessageHeader headerJson = new ColibriMessageHeader(ContentType.APPLICATION_SPARQL_RESULT_JSON);
        ColibriMessageContent contentJson = new ColibriMessageContent(resultSetJson);
        ColibriMessage msgJson = new ColibriMessage(MessageIdentifier.QRE, headerJson, contentJson);
        List<String> expectedVars = new ArrayList<>();
        expectedVars.add("service");
        expectedVars.add("identifier");
        processSparqlResultSetfromColibriMessage(msgJson, expectedVars);



        String resultSetXML = "<?xml version=\"1.0\"?>\n" +
                "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\"\n" +
                "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "        xsi:schemaLocation=\"http://www.w3.org/2001/sw/DataAccess/rf1/result2.xsd\">\n" +
                "\n" +
                "  <head>\n" +
                "    <variable name=\"x\"/>\n" +
                "    <variable name=\"hpage\"/>\n" +
                "    <variable name=\"name\"/>\n" +
                "    <variable name=\"mbox\"/>\n" +
                "    <variable name=\"age\"/>\n" +
                "    <variable name=\"blurb\"/>\n" +
                "    <variable name=\"friend\"/>\n" +
                "\n" +
                "    <link href=\"example.rq\" />\n" +
                "  </head>\n" +
                "\n" +
                "  <results>\n" +
                "\n" +
                "    <result>\n" +
                "      <binding name=\"x\"><bnode>r1</bnode></binding>\n" +
                "      <binding name=\"hpage\"><uri>http://work.example.org/alice/</uri></binding>\n" +
                "      <binding name=\"name\"><literal>Alice</literal></binding>\n" +
                "      <binding name=\"mbox\"><literal></literal></binding>\n" +
                "      <binding name=\"friend\"><bnode>r2</bnode></binding>\n" +
                "      <binding name=\"blurb\"><literal datatype=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral\">&lt;p xmlns=\"http://www.w3.org/1999/xhtml\"&gt;My name is &lt;b&gt;alice&lt;/b&gt;&lt;/p&gt;</literal></binding>\n" +
                "    </result>\n" +
                "\n" +
                "    <result> \n" +
                "      <binding name=\"x\"><bnode>r2</bnode></binding>\n" +
                "      <binding name=\"hpage\"><uri>http://work.example.org/bob/</uri></binding>\n" +
                "      <binding name=\"name\"><literal xml:lang=\"en\">Bob</literal></binding>\n" +
                "      <binding name=\"mbox\"><uri>mailto:bob@work.example.org</uri></binding>\n" +
                "      <binding name=\"age\"><literal datatype=\"http://www.w3.org/2001/XMLSchema#integer\">30</literal></binding>\n" +
                "      <binding name=\"friend\"><bnode>r1</bnode></binding>\n" +
                "    </result>\n" +
                "\n" +
                "  </results>\n" +
                "\n" +
                "</sparql>";

        System.out.println("-----------------------------------------------\n" +
                "XML Example Result Set: ");
        ColibriMessageHeader headerXml = new ColibriMessageHeader(ContentType.APPLICATION_SPARQL_RESULT_XML);
        ColibriMessageContent contentXml = new ColibriMessageContent(resultSetXML);
        ColibriMessage msgXml = new ColibriMessage(MessageIdentifier.QRE, headerXml, contentXml);
        List<String> expectedVars2 = new ArrayList<>();
        expectedVars2.add("x");
        expectedVars2.add("hpage");
        expectedVars2.add("name");
        expectedVars2.add("mbox");
        expectedVars2.add("blurb");
        expectedVars2.add("age");
        expectedVars2.add("friend");
        processSparqlResultSetfromColibriMessage(msgXml, expectedVars2);
    }

}
