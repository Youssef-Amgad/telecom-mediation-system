package model;

/**
 * Represents a mediation / routing rule stored in the database.
 *
 * A rule maps an (optional) node + call-type combination to a destination,
 * with a priority to resolve conflicts when multiple rules match.
 */
public class MediationRule {

    public enum Action {
        ROUTE,      // forward CDR to destination
        DROP,       // discard CDR
        TRANSFORM   // apply field transformation then route
    }

    private int    ruleId;
    private String nodeId;       // null means "match any node"
    private String callType;     // null means "match any call type"
    private String destination;  // target system identifier
    private Action action;
    private int    priority;     // lower number = higher priority
    private String transformExpression; // optional; used when action == TRANSFORM
    private boolean active;

    // ── Constructors ─────────────────────────────────────────────────────────

    public MediationRule() {}

    public MediationRule(int ruleId, String nodeId, String callType,
                         String destination, Action action, int priority) {
        this.ruleId      = ruleId;
        this.nodeId      = nodeId;
        this.callType    = callType;
        this.destination = destination;
        this.action      = action;
        this.priority    = priority;
        this.active      = true;
    }

    // ── Match Logic ──────────────────────────────────────────────────────────

    /**
     * Returns true if this rule applies to the given CDR.
     */
    public boolean matches(CDR cdr) {
        if (!active) return false;
        boolean nodeMatch = (nodeId == null || nodeId.equalsIgnoreCase(cdr.getNodeId()));
        boolean typeMatch = (callType == null || callType.equalsIgnoreCase(cdr.getCallType()));
        return nodeMatch && typeMatch;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int    getRuleId()                           { return ruleId; }
    public void   setRuleId(int ruleId)                 { this.ruleId = ruleId; }

    public String getNodeId()                           { return nodeId; }
    public void   setNodeId(String nodeId)              { this.nodeId = nodeId; }

    public String getCallType()                         { return callType; }
    public void   setCallType(String callType)          { this.callType = callType; }

    public String getDestination()                      { return destination; }
    public void   setDestination(String destination)    { this.destination = destination; }

    public Action getAction()                           { return action; }
    public void   setAction(Action action)              { this.action = action; }

    public int    getPriority()                         { return priority; }
    public void   setPriority(int priority)             { this.priority = priority; }

    public String getTransformExpression()              { return transformExpression; }
    public void   setTransformExpression(String expr)   { this.transformExpression = expr; }

    public boolean isActive()                           { return active; }
    public void    setActive(boolean active)            { this.active = active; }

    @Override
    public String toString() {
        return String.format("MediationRule{id=%d, node='%s', type='%s', dest='%s', action=%s, priority=%d}",
                ruleId, nodeId, callType, destination, action, priority);
    }
}
