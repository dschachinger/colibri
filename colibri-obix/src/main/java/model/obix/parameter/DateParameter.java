package model.obix.parameter;

import service.TimeDurationConverter;

import java.util.Date;

public class DateParameter extends Parameter{

    private Date date;

    private DateParameter(String uri, int paramNumber) {
        super(uri, paramNumber);
    }

    public DateParameter(String uri, int paramNumber, Date date) {
        this(uri, paramNumber);
        this.date = date;

    }

    public DateParameter(String uri, int paramNumber, long dateInMillis) {
        this(uri, paramNumber);
        this.date = new Date(dateInMillis);
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String getValueAsString() {
        return TimeDurationConverter.date2Ical(this.getDate()).toString();
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String getValueType() {
        return "&xsd;dateTime";
    }

    @Override
    public Boolean hasBooleanStates() {
        return false;
    }
}
