package model;

public class MediationRule {

    private Integer ruleId;

    private Integer sourceNodeId;        // source system
    private Integer destinationNodeId;    // destination system

    // ── Constructors ─────────────────────

    public MediationRule() {}

    public MediationRule(Integer ruleId,
                         Integer sourceNodeId,
                         Integer destinationNodeId) {
        this.ruleId = ruleId;
        this.sourceNodeId = sourceNodeId;
        this.destinationNodeId = destinationNodeId;
    }

    // ── Getters & Setters ────────────────

    public Integer getRuleId() {
        return ruleId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }

    public Integer getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(Integer sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    public Integer getDestinationNodeId() {
        return destinationNodeId;
    }

    public void setDestinationNodeId(Integer destinationNodeId) {
        this.destinationNodeId = destinationNodeId;
    }

    @Override
    public String toString() {
        return "MediationRule{" +
                "ruleId=" + ruleId +
                ", sourceNodeId=" + sourceNodeId +
                ", destinationNodeId=" + destinationNodeId +
                '}';
    }
}