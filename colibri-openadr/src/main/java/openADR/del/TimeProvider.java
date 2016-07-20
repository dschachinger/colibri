package openADR.del;

import org.jivesoftware.smack.provider.IntrospectionProvider;

public class TimeProvider extends IntrospectionProvider.IQIntrospectionProvider<Time> {

    public TimeProvider() {
        super(Time.class);
    }
}