package filter;

import model.CDR;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * Filters a list of CDRs based on configurable predicates.
 *
 * CDRs that fail any registered filter are marked as filtered (rather than
 * removed immediately) so that the outcome can be logged / audited. The
 * caller can then separate passed and rejected CDRs as needed.
 *
 * <pre>
 * CDRFilter filter = new CDRFilter();
 * filter.addRule("RejectTestIMSI", cdr -> !cdr.getImsi().startsWith("00101"));
 * List&lt;CDR&gt; passed = filter.applyFilters(rawCdrs);
 * </pre>
 */
public class CDRFilter {

    private static final Logger LOG = Logger.getLogger(CDRFilter.class.getName());

    /** A named predicate; CDR passes when predicate returns {@code true}. */
    private static class FilterRule {
        final String name;
        final Predicate<CDR> predicate;
        FilterRule(String name, Predicate<CDR> predicate) {
            this.name = name;
            this.predicate = predicate;
        }
    }

    private final List<FilterRule> rules = new ArrayList<>();

    // ── Default Built-in Rules ───────────────────────────────────────────────

    public CDRFilter() {
        // Reject CDRs with no calling party
        addRule("RequireCallingParty",
                cdr -> cdr.getCallingParty() != null && !cdr.getCallingParty().isBlank());

        // Reject CDRs with zero duration for voice calls
        addRule("RejectZeroDurationVoice",
                cdr -> !"VOICE".equalsIgnoreCase(cdr.getCallType())
                        || cdr.getDurationSeconds() > 0);

        // Reject CDRs with no start time
        addRule("RequireStartTime",
                cdr -> cdr.getStartTime() != null);
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Registers a named filter rule.
     *
     * @param name      human-readable rule name (used in logs)
     * @param predicate returns {@code true} to keep the CDR, {@code false} to drop it
     */
    public void addRule(String name, Predicate<CDR> predicate) {
        rules.add(new FilterRule(name, predicate));
        LOG.info("Registered filter rule: " + name);
    }

    /**
     * Applies all rules to the input list.
     *
     * @param cdrs input CDRs (not modified)
     * @return list of CDRs that passed every rule
     */
    public List<CDR> applyFilters(List<CDR> cdrs) {
        List<CDR> passed  = new ArrayList<>();
        int       dropped = 0;

        for (CDR cdr : cdrs) {
            String failedRule = evaluate(cdr);
            if (failedRule == null) {
                passed.add(cdr);
            } else {
                cdr.setFiltered(true);
                dropped++;
                LOG.fine(String.format("CDR %s dropped by rule '%s'", cdr.getCdrId(), failedRule));
            }
        }

        LOG.info(String.format("Filter result: %d passed, %d dropped (total %d)",
                passed.size(), dropped, cdrs.size()));
        return passed;
    }

    /**
     * Returns the name of the first failing rule, or {@code null} if the CDR passes all rules.
     */
    private String evaluate(CDR cdr) {
        for (FilterRule rule : rules) {
            try {
                if (!rule.predicate.test(cdr)) {
                    return rule.name;
                }
            } catch (Exception e) {
                LOG.warning("Exception in filter rule '" + rule.name + "': " + e.getMessage());
                return rule.name;   // treat rule errors as failures
            }
        }
        return null;
    }
}