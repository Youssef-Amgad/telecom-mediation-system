package routing;

import model.CDR;
import model.MediationRule;
import model.MediationRule.Action;
import sender.Sender;
import sender.SenderFactory;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Core routing engine.
 *
 * then either drops the CDR, routes it via the appropriate {@link Sender}, or
 * transforms it before routing.
 *
 * CDRs that match no rule are placed in a dead-letter list for inspection.
 */
public class RoutingEngine {

    private static final Logger LOG = Logger.getLogger(RoutingEngine.class.getName());

    private final List<MediationRule> rules;
    private final SenderFactory       senderFactory;
    private final List<CDR>           deadLetterQueue = new ArrayList<>();

    public RoutingEngine(List<MediationRule> rules, SenderFactory senderFactory) {
        // Sort rules by priority ascending (lower number = higher priority)
        this.rules = rules.stream()
                          .sorted(Comparator.comparingInt(MediationRule::getPriority))
                          .collect(Collectors.toList());
        this.senderFactory = senderFactory;
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Routes all CDRs in the supplied list.
     * CDRs are grouped by destination so each sender receives a batch rather
     * than individual records, improving throughput.
     */
    public void routeAll(List<CDR> cdrs) {
        // destination → list of CDRs to send there
        Map<String, List<CDR>> batches = new LinkedHashMap<>();

        for (CDR cdr : cdrs) {
            MediationRule rule = findRule(cdr);

            if (rule == null) {
                LOG.warning("No matching rule for CDR: " + cdr.getCdrId() + " – adding to dead-letter queue");
                deadLetterQueue.add(cdr);
                continue;
            }

            if (rule.getAction() == Action.DROP) {
                LOG.fine("Dropping CDR " + cdr.getCdrId() + " per rule " + rule.getRuleId());
                continue;
            }

            if (rule.getAction() == Action.TRANSFORM) {
                applyTransform(cdr, rule);
            }

            batches.computeIfAbsent(rule.getDestination(), k -> new ArrayList<>()).add(cdr);
        }

        // Dispatch each destination batch
        for (Map.Entry<String, List<CDR>> entry : batches.entrySet()) {
            String     destination = entry.getKey();
            List<CDR>  batch       = entry.getValue();
            dispatchBatch(batch, destination);
        }

        LOG.info(String.format("Routing complete. Routed=%d, DeadLetter=%d",
                cdrs.size() - deadLetterQueue.size(), deadLetterQueue.size()));
    }

    /** Returns unmodifiable view of CDRs that could not be routed. */
    public List<CDR> getDeadLetterQueue() {
        return Collections.unmodifiableList(deadLetterQueue);
    }

    /** Clears the dead-letter queue (e.g., after manual inspection). */
    public void clearDeadLetterQueue() {
        deadLetterQueue.clear();
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    /**
     * Returns the first (highest-priority) rule that matches the CDR, or null.
     */
    private MediationRule findRule(CDR cdr) {
        for (MediationRule rule : rules) {
            if (rule.matches(cdr)) {
                return rule;
            }
        }
        return null;
    }

    /**
     * Applies a simple key=value transformation expression to the CDR.
     * Expression format: "fieldName=newValue" (comma-separated for multiple).
     *
     * Example: "callType=VOICE,currency=USD"
     */
    private void applyTransform(CDR cdr, MediationRule rule) {
        String expr = rule.getTransformExpression();
        if (expr == null || expr.isBlank()) return;

        for (String part : expr.split(",")) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length != 2) continue;
            String field = kv[0].trim();
            String value = kv[1].trim();

            switch (field.toLowerCase()) {
                case "calltype"         -> cdr.setCallType(value);
                case "currency"         -> cdr.setCurrency(value);
                case "terminationcause" -> cdr.setTerminationCause(value);
                default                 -> cdr.addExtraField(field, value);
            }
        }
        LOG.fine("Transformed CDR " + cdr.getCdrId() + " with expression: " + expr);
    }

    /**
     * Resolves the protocol for the destination and sends the batch.
     *
     * Destination format expected: "PROTOCOL://HOST" e.g. "SFTP://billing.corp.net"
     * If no protocol prefix is found, falls back to SFTP.
     */
    private void dispatchBatch(List<CDR> batch, String destination) {
        String protocol = "SFTP";
        String host     = destination;

        if (destination.contains("://")) {
            String[] parts = destination.split("://", 2);
            protocol = parts[0].toUpperCase();
            host     = parts[1];
        }

        try {
            Sender sender = senderFactory.getSender(protocol);
            LOG.info(String.format("Dispatching %d CDRs to %s via %s", batch.size(), host, protocol));
            sender.send(batch, host);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,
                    "Failed to dispatch batch to " + destination + ": " + e.getMessage(), e);
            // Move failed CDRs to dead-letter for retry / inspection
            deadLetterQueue.addAll(batch);
        }
    }
}
