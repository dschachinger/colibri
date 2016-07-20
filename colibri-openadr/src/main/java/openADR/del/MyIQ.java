package openADR.del;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by georg on 02.06.16.
 */
public class MyIQ extends IQ {
    int age = -1;
    String user = null;
    String location = null;

    public MyIQ(String user, int age, String location) {
        super("myiq", "example:iq:foo");
        this.age = age;
        this.user = user;
        this.location = location;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        System.out.println("custom iq du war da!!");
        xml.setEmptyElement();
        return xml;
    }
}
