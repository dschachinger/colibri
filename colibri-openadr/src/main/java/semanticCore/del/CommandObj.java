package semanticCore.del;

import java.io.IOException;

/**
 * Created by georg on 28.06.16.
 */
public class CommandObj {

    @Command
    public String msg(String receiver, String...message) throws IOException {
        return "okay";
    }
}
