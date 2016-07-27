package semanticCore.WebSocketHandling;

import Utils.TimeDurationConverter;
import semanticCore.MsgObj.ColibriMessage;
import semanticCore.MsgObj.ContentType;
import semanticCore.MsgObj.Header;
import semanticCore.MsgObj.MsgType;

/**
 * Created by georg on 29.06.16.
 * This static class is used to transform a colibri message string into an colibri message object.
 * By the way, POJO stands for "pretty old java object"
 */
public class ColibriMsgMapper {

    static String newLine = "<br>";

    /**
     * This static method transfoms a given colibri message string into an colibri message object.
     * @param msg given colibri message string
     * @return colibri message object
     */
    public static ColibriMessage msgToPOJO(String msg){
        // TODO implement later because dummy server replies also the received messages
        if(msg.contains("\n")){
            return null;
        }

        try{
            int endCommandPos = msg.indexOf(newLine);

            String cmdName = endCommandPos >= 0 ? msg.substring(0, endCommandPos) : msg;

            String content = null;


            int endHeaderPos = msg.indexOf(newLine+newLine);

            // if true message does not contain any content
            if(endHeaderPos==-1){
                endHeaderPos = msg.length();
            } else {
                content = msg.substring(endHeaderPos+(newLine.length()*2)).trim();
            }

            Header header = parseMsgToHeader(msg.substring(endCommandPos+newLine.length(), endHeaderPos));

            ColibriMessage col_msg = new ColibriMessage(MsgType.stringToEnum(cmdName),header, content);

            return col_msg;

        } catch (StringIndexOutOfBoundsException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This static method transfoms a given colibri message header string into an colibri message header object.
     * @param msg given colibri message header string
     * @return colibri message header object
     */
    private static Header parseMsgToHeader(String msg){
        Header header = new Header();

        String[] splitedMsg = msg.split(newLine);

        for(String s : splitedMsg){
            String[] parts = s.split(":",2);
            String fieldType = parts[0].trim();
            String value = parts[1].trim();

            switch (fieldType){
                case "Message-Id":
                    header.setMessageId(value);
                    break;
                case "Content-Type":
                    header.setContentType(ContentType.stringToEnum(value));
                    break;
                case "Date":
                    header.setDate(TimeDurationConverter.ical2Date(value));
                    break;
                case "Expires":
                    header.setExpires(TimeDurationConverter.ical2Date(value));
                    break;
                case "Reference-Id":
                    header.setReferenceId(value);
                    break;
            }
        }

        return header;
    }
}
