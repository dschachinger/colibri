package OADRMsgInfo;

/**
 * Created by georg on 18.06.16.
 * Objects from this class represent a time interval
 */
public class Interval{
    // The duration of the state. unit: seconds
    private long durationSec;
    // Signal value
    private float signalValue;

    // Used as an index to identify intervals. Unique Identifier
    private String uid;

    public Interval() {
    }

    public Interval(Interval other) {
        this.durationSec = other.durationSec;
        this.signalValue = other.signalValue;
        this.uid = other.uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(long durationSec) {
        this.durationSec = durationSec;
    }

    public float getSignalValue() {
        return signalValue;
    }

    public void setSignalValue(float signalValue) {
        this.signalValue = signalValue;
    }

    @Override
    public String toString() {
        return "{\"Interval\":{"
                + "                        \"durationSec\":\"" + durationSec + "\"\n"
                + ",                         \"signalValue\":\"" + signalValue + "\"\n"
                + "}}";
    }
}
