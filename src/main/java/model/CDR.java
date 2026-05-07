package model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single Call Detail Record (CDR).
 * Extra/custom fields are stored in a flexible map to support different node types.
 */
public class CDR {

    // ── Core Fields ──────────────────────────────────────────────────────────

    private String cdrId;
    private String nodeId;
    private String nodeType;

    private String callingParty;     // A-number / MSISDN
    private String calledParty;      // B-number / destination
    private String imsi;
    private String imei;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationSeconds;

    private String callType;         // VOICE, SMS, GPRS, etc.
    private String terminationCause;
    private double chargeAmount;
    private String currency;

    private String sourceFile;       // original file this CDR was read from
    private boolean filtered;        // true if this CDR was dropped by a filter rule

    /** Holds any additional / node-specific fields. */
    private Map<String, String> extraFields = new HashMap<>();

    // ── Constructors ─────────────────────────────────────────────────────────

    public CDR() {}

    public CDR(String cdrId, String nodeId) {
        this.cdrId  = cdrId;
        this.nodeId = nodeId;
    }

    // ── Extra Field Helpers ──────────────────────────────────────────────────

    public void addExtraField(String key, String value) {
        extraFields.put(key, value);
    }

    public String getExtraField(String key) {
        return extraFields.getOrDefault(key, null);
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String getCdrId()                        { return cdrId; }
    public void   setCdrId(String cdrId)            { this.cdrId = cdrId; }

    public String getNodeId()                       { return nodeId; }
    public void   setNodeId(String nodeId)          { this.nodeId = nodeId; }

    public String getNodeType()                     { return nodeType; }
    public void   setNodeType(String nodeType)      { this.nodeType = nodeType; }

    public String getCallingParty()                 { return callingParty; }
    public void   setCallingParty(String p)         { this.callingParty = p; }

    public String getCalledParty()                  { return calledParty; }
    public void   setCalledParty(String p)          { this.calledParty = p; }

    public String getImsi()                         { return imsi; }
    public void   setImsi(String imsi)              { this.imsi = imsi; }

    public String getImei()                         { return imei; }
    public void   setImei(String imei)              { this.imei = imei; }

    public LocalDateTime getStartTime()             { return startTime; }
    public void          setStartTime(LocalDateTime t) { this.startTime = t; }

    public LocalDateTime getEndTime()               { return endTime; }
    public void          setEndTime(LocalDateTime t)   { this.endTime = t; }

    public long getDurationSeconds()                { return durationSeconds; }
    public void setDurationSeconds(long d)          { this.durationSeconds = d; }

    public String getCallType()                     { return callType; }
    public void   setCallType(String t)             { this.callType = t; }

    public String getTerminationCause()             { return terminationCause; }
    public void   setTerminationCause(String c)     { this.terminationCause = c; }

    public double getChargeAmount()                 { return chargeAmount; }
    public void   setChargeAmount(double a)         { this.chargeAmount = a; }

    public String getCurrency()                     { return currency; }
    public void   setCurrency(String currency)      { this.currency = currency; }

    public String getSourceFile()                   { return sourceFile; }
    public void   setSourceFile(String f)           { this.sourceFile = f; }

    public boolean isFiltered()                     { return filtered; }
    public void    setFiltered(boolean filtered)    { this.filtered = filtered; }

    public Map<String, String> getExtraFields()     { return extraFields; }
    public void setExtraFields(Map<String, String> m) { this.extraFields = m; }

    @Override
    public String toString() {
        return String.format("CDR{id='%s', node='%s', calling='%s', called='%s', type='%s', duration=%ds}",
                cdrId, nodeId, callingParty, calledParty, callType, durationSeconds);
    }
}