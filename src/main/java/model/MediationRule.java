package model;
 
public class MediationRule {
 
    private Integer ruleId;
    private Integer sourceNodeId;
    private Integer destinationNodeId;
 
    // ── Constructors ─────────────────────
 
    public MediationRule() {}
 
    public MediationRule(Integer ruleId,
                         Integer sourceNodeId,
                         Integer destinationNodeId) {
        this.ruleId            = ruleId;
        this.sourceNodeId      = sourceNodeId;
        this.destinationNodeId = destinationNodeId;
    }
 
    // ── Getters & Setters ────────────────
 
    public Integer getRuleId() { return ruleId; }
    public void setRuleId(Integer ruleId) { this.ruleId = ruleId; }
 
    public Integer getSourceNodeId() { return sourceNodeId; }
    public void setSourceNodeId(Integer sourceNodeId) { this.sourceNodeId = sourceNodeId; }
 
    public Integer getDestinationNodeId() { return destinationNodeId; }
    public void setDestinationNodeId(Integer destinationNodeId) { this.destinationNodeId = destinationNodeId; }
 
    /**
     * A CDR matches this rule when its nodeId equals the source node id.
     * Node IDs are stored as Strings in CDR/Node but as Integers in MediationRule,
     * so we compare via String.valueOf().
     */
    public boolean matches(CDR cdr) {
        return cdr.getNodeId() != null
                && cdr.getNodeId().equals(String.valueOf(sourceNodeId));
    }
 
    @Override
    public String toString() {
        return "MediationRule{ruleId=" + ruleId
                + ", sourceNodeId=" + sourceNodeId
                + ", destinationNodeId=" + destinationNodeId + '}';
    }
}