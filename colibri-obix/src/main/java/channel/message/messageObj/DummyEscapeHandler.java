package channel.message.messageObj;

import com.sun.xml.internal.bind.marshaller.CharacterEscapeHandler;

import java.io.IOException;
import java.io.Writer;

public class DummyEscapeHandler implements CharacterEscapeHandler {
    @Override
    public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
        char[] writeOut = new char[length];
        for (int i = 0; i < length; i++) {
            writeOut[i] = ch[start + i];
        }

        out.write(writeOut);
    }
}
