
package main;

import collector.NodeCollector;
import db.DBManager;
import filter.CDRFilter;
import model.CDR;
import model.MediationRule;
import model.Node;
import routing.RoutingEngine;
import sender.SenderFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        LOG.info("════════════════════════════════════════");
        LOG.info("  CDR Mediation System  –  Starting up  ");
        LOG.info("════════════════════════════════════════");

        String runId = UUID.randomUUID().toString();
        LOG.info("Run ID: " + runId);

        DBManager db = new DBManager();

        try {
            // Step 1: Connect to DB and load configuration 
            db.connect();

            List<Node>           nodes = db.loadNodes();
            List<MediationRule>  rules = db.loadRules();

            if (nodes.isEmpty()) {
                LOG.warning("No active nodes found – nothing to collect. Exiting.");
                return;
            }

            // ── Step 2: Collect CDR files from nodes ──────────────────────────
            NodeCollector collector = new NodeCollector(nodes);
            List<CDR>     rawCdrs  = collector.collectAll();

            LOG.info(String.format("Collection complete: %d raw CDRs", rawCdrs.size()));

            if (rawCdrs.isEmpty()) {
                LOG.info("No CDRs collected – nothing to process. Exiting.");
                db.insertRunSummary(runId, 0, 0, 0, 0);
                return;
            }

            // ── Step 3: Filter CDRs ───────────────────────────────────────────
            CDRFilter  filter     = new CDRFilter();
            List<CDR>  passedCdrs = filter.applyFilters(rawCdrs);

            LOG.info(String.format("Filtering complete: %d / %d CDRs passed",
                    passedCdrs.size(), rawCdrs.size()));

            // ── Step 4: Route CDRs ────────────────────────────────────────────
            SenderFactory  senderFactory = new SenderFactory();
            RoutingEngine  router        = new RoutingEngine(rules, senderFactory);

            router.routeAll(passedCdrs);

            int deadLetterCount = router.getDeadLetterQueue().size();
            int routedCount     = passedCdrs.size() - deadLetterCount;

            if (deadLetterCount > 0) {
                LOG.warning(deadLetterCount + " CDR(s) could not be routed and are in the dead-letter queue.");
            }

            // ── Step 5: Persist run summary ───────────────────────────────────
            db.insertRunSummary(runId,
                    rawCdrs.size(),
                    passedCdrs.size(),
                    routedCount,
                    deadLetterCount);

            LOG.info("════════════════════════════════════════");
            LOG.info(String.format("  Run complete │ Collected: %d │ Passed: %d │ Routed: %d │ DeadLetter: %d",
                    rawCdrs.size(), passedCdrs.size(), routedCount, deadLetterCount));
            LOG.info("════════════════════════════════════════");

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Database error during mediation run", e);
            System.exit(1);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unexpected error during mediation run", e);
            System.exit(2);
        } finally {
            db.disconnect();
        }
    }
}
