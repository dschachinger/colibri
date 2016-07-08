package model.obix.parameter;

import service.TimeDurationConverter;

import java.util.Date;

public class DateParameter extends Parameter{

    private Date date;

    public DateParameter(String parameterUri, Date date) {
        super(parameterUri);
        this.date = date;
        this.parameterUnit = "&colibri;dateTime";
    }

    public DateParameter(String parameterUri, long dateInMillis) {
        super(parameterUri);
        this.date = new Date(dateInMillis);
        this.parameterUnit = "&colibri;dateTime";
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
}
