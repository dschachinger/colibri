package channel.message.colibriMessage;

import service.TimeDurationConverter;

public class ColibriMessageMapper {

    //TODO: Change to \r\n
    static String newLine = "<br>";

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
            System.out.println("exception");
            return null;
        }
    }

    private static ColibriMessageHeader parseMsgToHeader(String msg) {
        ColibriMessageHeader header = new ColibriMessageHeader(msg);

        String[] splittedMsg = msg.split(newLine);

        for (String s : splittedMsg) {
            System.out.println("header: " + s);
            String[] parts = s.split(":", 2);
            String fieldType = parts[0].trim();
            String value = parts[1].trim();

            System.out.println("field type " + fieldType + " value " + value);

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
        }
        return header;
    }

    public static String POJOToMsg(ColibriMessage msg) {
        return null;
    }
}