package channel.message.colibriMessage;

import channel.message.messageObj.ContentType;
import channel.message.messageObj.MessageIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.Configurator;
import service.TimeDurationConverter;

import java.text.ParseException;

/**
 * This class is used to map a message from a String to {@link ColibriMessage}.
 */
public class ColibriMessageMapper {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    static String newLine = Configurator.getInstance().getNewlineString();
    private static final Logger logger = LoggerFactory.getLogger(ColibriMessageMapper.class);

    /******************************************************************
     *                            Methods                             *
     ******************************************************************/

    /**
     * This method returns a {@link ColibriMessage} parsed from the given String.
     *
     * @param msg   The String which is parsed to get the {@link ColibriMessage}.
     *
     * @return                          The parsed {@link ColibriMessage}.
     * @throws IllegalArgumentException Is thrown if the message parsing fails.
     */
    public static ColibriMessage msgToPOJO(String msg) throws IllegalArgumentException{

        try {
            int endCommandPos = msg.indexOf(newLine);
            String cmdName = (endCommandPos >= 0 ? msg.substring(0, endCommandPos) : msg).replaceAll(" ", "");
            int endHeaderPos = msg.indexOf(newLine + newLine);
            if(endCommandPos < 1 || endHeaderPos < 1) {
                throw new IllegalArgumentException("The message is not in the right format and cannot be parsed");
            }
            ColibriMessageHeader header = parseMsgToHeader(msg.substring(endCommandPos + newLine.length(), endHeaderPos));

            return new ColibriMessage(MessageIdentifier.fromString(cmdName), header, new ColibriMessageContent(msg.substring(endHeaderPos)));
        } catch (StringIndexOutOfBoundsException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * This method returns the header of a {@link ColibriMessage} parsed from the given String.
     *
     * @param msg   The String which is parsed to get the {@link ColibriMessageHeader}.
     *
     * @return                          The parsed {@link ColibriMessageHeader}.
     * @throws IllegalArgumentException Is thrown if the message-header parsing fails.
     */
    private static ColibriMessageHeader parseMsgToHeader(String msg) {
        ColibriMessageHeader header = new ColibriMessageHeader(msg);

        String[] splittedMsg = msg.split(newLine);

        for (String s : splittedMsg) {
            String[] parts = s.split(":", 2);
            String fieldType = parts[0].trim();
            String value = parts[1].trim();

            try {
                switch (fieldType) {
                    case "Message-Id":
                        header.setId(value);
                        break;
                    case "Content-Type":
                        header.setContentType(ContentType.fromString(value));
                        break;
                    case "Date":
                        header.setDate(TimeDurationConverter.ical2Date(value));
                        break;
                    case "Expires":
                        header.setExpires(TimeDurationConverter.ical2Date(value));
                        break;
                    case "Reference-Id":
                        header.setRefenceId(value);
                        break;
                }
            } catch (ParseException e) {
                logger.error("Time in Header not parseable");
            }
        }
        return header;
    }
}