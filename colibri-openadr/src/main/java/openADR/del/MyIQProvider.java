package openADR.del;

import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MyIQProvider extends IQProvider<MyIQ> {

    @Override
    public MyIQ parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException {
        // Define the data we are trying to collect with sane defaults
        int age = -1;
        String user = null;
        String location = null;
        try {
            // Start parsing loop
            outerloop: while(true) {
                int eventType = 0;
                eventType = parser.next();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String elementName = parser.getName();
                        switch (elementName) {
                            case "user":
                                age = ParserUtils.getIntegerAttribute(parser, "age");
                                user = parser.nextText();
                                break;
                            case "location":
                                location = parser.nextText();
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        // Abort condition: if the are on a end tag (closing element) of the same depth
                        if (parser.getDepth() == initialDepth) {
                            break outerloop;
                        }
                        break;
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Construct the IQ instance at the end of parsing, when all data has been collected
        return new MyIQ(user, age, location);
    }
}
