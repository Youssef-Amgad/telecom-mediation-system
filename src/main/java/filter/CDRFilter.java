package filter;

import model.CDR;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class CDRFilter {

    private static final Logger LOG = Logger.getLogger(CDRFilter.class.getName());

    private static class FilterRule {
        final String name;
        final Predicate<CDR> predicate;

        FilterRule(String name, Predicate<CDR> predicate) {
            this.name = name;
            this.predicate = predicate;
        }
    }

    private final List<FilterRule> rules = new ArrayList<>();

    public CDRFilter() {

        // ─────────────────────────────
        // RULE 1: calling party must exist
        // ─────────────────────────────
        addRule("RequireCallingParty",
                cdr -> cdr.getCallingParty() != null
                        && !cdr.getCallingParty().trim().isEmpty());

        // ─────────────────────────────
        // RULE 2: reject invalid VOICE calls with 0 duration
        // ─────────────────────────────
        addRule("RejectZeroDurationVoice",
                cdr -> !"VOICE".equalsIgnoreCase(cdr.getCallType())
                        || cdr.getDurationSeconds() > 0);

        // ─────────────────────────────
        // RULE 3: OPTIONAL timestamp safety (FIXED)
        // uses extraFields instead of missing startTime
        // ─────────────────────────────
        addRule("RequireTimestamp",
                cdr -> cdr.getExtraField("timestamp") != null);
    }

    // ─────────────────────────────────────────────
    // ADD RULE
    // ─────────────────────────────────────────────
    public void addRule(String name, Predicate<CDR> predicate) {
        rules.add(new FilterRule(name, predicate));
        LOG.info("Registered filter rule: " + name);
    }

    // ─────────────────────────────────────────────
    // APPLY FILTERS
    // ─────────────────────────────────────────────
    public List<CDR> applyFilters(List<CDR> cdrs) {

        List<CDR> passed = new ArrayList<>();
        int dropped = 0;

        for (CDR cdr : cdrs) {

            String failedRule = evaluate(cdr);

            if (failedRule == null) {
                passed.add(cdr);
            } else {
                cdr.setFiltered(true);
                dropped++;

                LOG.fine("CDR " + cdr.getCdrId()
                        + " dropped by rule: " + failedRule);
            }
        }

        LOG.info("Filter summary → passed=" + passed.size()
                + ", dropped=" + dropped
                + ", total=" + cdrs.size());

        return passed;
    }

    // ─────────────────────────────────────────────
    // RULE EVALUATION
    // ─────────────────────────────────────────────
    private String evaluate(CDR cdr) {

        for (FilterRule rule : rules) {
            try {
                if (!rule.predicate.test(cdr)) {
                    return rule.name;
                }
            } catch (Exception e) {
                LOG.warning("Rule error [" + rule.name + "]: " + e.getMessage());
                return rule.name; // treat as failure
            }
        }

        return null;
    }
}