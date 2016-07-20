package openADR.OADRMsgInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Report{
    /* the amount of time that data can be collected
        (or has been collected thus far for history) */
    private long durationSec;

    // Optional name for a report.
    private String reportName;

    // Identifier for a particular Metadata report specification
    private String reportSpecifierID;

    // Identifier for a particular report request
    private String reportRequestID;

    /* Each datapoint has its own set of oadrReportDescription
        attributes which are used to describe the datapoint.
        This describes the report possibilities for a datapoint. */
    private List<ReportDescription> reportDescriptions;

    // The dateTime the payload was created
    private Date createdDateTime;

    private List<Interval> intervals;

    public Report(){
        reportDescriptions = new ArrayList<>();
        intervals = new ArrayList<>();
    }

    public Report(Report other) {
        this.durationSec = other.durationSec;
        this.reportName = other.reportName;
        this.reportSpecifierID = other.reportSpecifierID;
        this.reportRequestID = other.reportRequestID;
        this.createdDateTime = other.createdDateTime;

        reportDescriptions = new ArrayList<>();
        for(ReportDescription reportDescription : other.reportDescriptions){
            reportDescriptions.add(new ReportDescription(reportDescription));
        }
        intervals = new ArrayList<>();
        for(Interval interval : other.intervals){
            intervals.add(new Interval(interval));
        }
    }

    public List<Interval> getIntervals() {
        return intervals;
    }

    public ReportDescription getNewReportDescription(){
        return new ReportDescription();
    }

    public SamplingRate getNewSamplingRate(){
        return new SamplingRate();
    }

    public PowerReal getNewPowerReal(){
        return new PowerReal();
    }

    public long getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(long durationSec) {
        this.durationSec = durationSec;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportSpecifierID() {
        return reportSpecifierID;
    }

    public void setReportSpecifierID(String reportSpecifierID) {
        this.reportSpecifierID = reportSpecifierID;
    }

    public String getReportRequestID() {
        return reportRequestID;
    }

    public void setReportRequestID(String reportRequestID) {
        this.reportRequestID = reportRequestID;
    }

    public List<ReportDescription> getReportDescriptions() {
        return reportDescriptions;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public class ReportDescription{
        // ReferenceID for this data point
        private String rID;

        /* Sources for data in this report. Examples include meters or
            submeters. For example, if a meter is capable of providing two different types of
            measurements, then each measurement stream would be separately identified. */
        private String reportDataSource;

        // The type of a report such as usage or price
        private String reportType;

        // Metadata about the Readings, such as mean or derived
        private String readingType;

        // A URI identifying a DR Program
        private String marketContext;

        // Sampling rate for telemetry type data
        private SamplingRate samplingRate;

        // Real power measured
        private PowerReal powerReal;

        public ReportDescription() {
        }

        public ReportDescription(ReportDescription other) {
            this.rID = other.rID;
            this.reportDataSource = other.reportDataSource;
            this.reportType = other.reportType;
            this.readingType = other.readingType;
            this.marketContext = other.marketContext;

            this.samplingRate = new SamplingRate(other.samplingRate);
            this.powerReal = new PowerReal(other.powerReal);
        }

        public PowerReal getPowerReal() {
            return powerReal;
        }

        public void setPowerReal(PowerReal powerReal) {
            this.powerReal = powerReal;
        }

        public SamplingRate getSamplingRate() {
            return samplingRate;
        }

        public void setSamplingRate(SamplingRate samplingRate) {
            this.samplingRate = samplingRate;
        }

        public String getrID() {
            return rID;
        }

        public void setrID(String rID) {
            this.rID = rID;
        }

        public String getReportDataSource() {
            return reportDataSource;
        }

        public void setReportDataSource(String reportDataSource) {
            this.reportDataSource = reportDataSource;
        }

        public String getReportType() {
            return reportType;
        }

        public void setReportType(String reportType) {
            this.reportType = reportType;
        }

        public String getReadingType() {
            return readingType;
        }

        public void setReadingType(String readingType) {
            this.readingType = readingType;
        }

        public String getMarketContext() {
            return marketContext;
        }

        public void setMarketContext(String marketContext) {
            this.marketContext = marketContext;
        }
    }

    public class PowerReal{
        // A description of a report unit of measure
        private String itemDescription;

        // The base unit of measure for a report data point
        private String itemUnits;

        // A scaling factor for the base unit of measure for a report
        private String siScaleCode;

        // Hertz of the power supply
        private BigDecimal powerAttributesHertz;

        // Voltage of the power supply
        private BigDecimal powerAttributesVoltage;

        // true...power supply is AC, false...power supply is DC
        private boolean powerAttributesAC;

        public PowerReal() {
        }

        public PowerReal(PowerReal other) {
            this.itemDescription = other.itemDescription;
            this.itemUnits = other.itemUnits;
            this.siScaleCode = other.siScaleCode;
            // BigDecimal: No need for a new object, because it is not possible to change its value
            this.powerAttributesHertz = other.powerAttributesHertz;
            this.powerAttributesVoltage = other.powerAttributesVoltage;
            this.powerAttributesAC = other.powerAttributesAC;
        }

        public String getItemDescription() {
            return itemDescription;
        }

        public void setItemDescription(String itemDescription) {
            this.itemDescription = itemDescription;
        }

        public String getItemUnits() {
            return itemUnits;
        }

        public void setItemUnits(String itemUnits) {
            this.itemUnits = itemUnits;
        }

        public String getSiScaleCode() {
            return siScaleCode;
        }

        public void setSiScaleCode(String siScaleCode) {
            this.siScaleCode = siScaleCode;
        }

        public BigDecimal getPowerAttributesHertz() {
            return powerAttributesHertz;
        }

        public void setPowerAttributesHertz(BigDecimal powerAttributesHertz) {
            this.powerAttributesHertz = powerAttributesHertz;
        }

        public BigDecimal getPowerAttributesVoltage() {
            return powerAttributesVoltage;
        }

        public void setPowerAttributesVoltage(BigDecimal powerAttributesVoltage) {
            this.powerAttributesVoltage = powerAttributesVoltage;
        }

        public boolean isPowerAttributesAC() {
            return powerAttributesAC;
        }

        public void setPowerAttributesAC(boolean powerAttributesAC) {
            this.powerAttributesAC = powerAttributesAC;
        }
    }

    public class SamplingRate{
        // Minimum sampling period unit: seconds
        private long minPeriondSec;

        // Maximum sampling period unit: seconds
        private long maxPeriondSec;

        /* If true then the data will be recorded when it changes, but at no
            greater a frequency than that specified by minPeriod. */
        boolean onChange;

        public SamplingRate() {
        }

        public SamplingRate(SamplingRate other) {
            this.minPeriondSec = other.minPeriondSec;
            this.maxPeriondSec = other.maxPeriondSec;
            this.onChange = other.onChange;
        }

        public long getMinPeriondSec() {
            return minPeriondSec;
        }

        public void setMinPeriondSec(long minPeriondSec) {
            this.minPeriondSec = minPeriondSec;
        }

        public long getMaxPeriondSec() {
            return maxPeriondSec;
        }

        public void setMaxPeriondSec(long maxPeriondSec) {
            this.maxPeriondSec = maxPeriondSec;
        }

        public boolean isOnChange() {
            return onChange;
        }

        public void setOnChange(boolean onChange) {
            this.onChange = onChange;
        }
    }
}

