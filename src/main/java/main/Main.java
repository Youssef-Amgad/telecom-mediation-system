package main;

import collector.NodeCollector;
import db.DBManager;
import filter.CDRFilter;
import model.CDR;
import model.MediationRule;
import model.Node;
import routing.RoutingEngine;
import sender.SenderFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        LOG.info("=== MEDIATION START ===");

        DBManager db = new DBManager();

        // ✔ FIX: use BlockingQueue (correct producer-consumer model)
        BlockingQueue<List<CDR>> queue = new LinkedBlockingQueue<>();

        List<Thread> collectorThreads = new ArrayList<>();

        try {
            db.connect();

            List<Node> nodes = db.loadNodes();
            List<MediationRule> rules = db.loadRules();

            // ── START COLLECTORS (UPSTREAM ONLY) ─────────────────────────────
            for (Node node : nodes) {

                if (!"UPSTREAM".equalsIgnoreCase(node.getNodeType())) {
                    continue;
                }

                NodeCollector collector = new NodeCollector(node, queue);

                Thread t = new Thread(collector);
                t.setName("Collector-" + node.getNodeName());
                t.start();

                collectorThreads.add(t);

                LOG.info("Started collector for node: " + node.getNodeName());
            }

            if (collectorThreads.isEmpty()) {
                LOG.warning("No upstream nodes found — exiting.");
                return;
            }

            // ── PIPELINE COMPONENTS ──────────────────────────────────────────
            CDRFilter filter = new CDRFilter();
            RoutingEngine router = new RoutingEngine(
                    rules,
                    nodes,
                    new SenderFactory()
            );

            // ── MAIN PROCESSING LOOP ────────────────────────────────────────
            while (true) {

                // ✔ BLOCKING (no CPU waste)
                List<CDR> batch = queue.take();

                LOG.info("Main received batch of size: " + batch.size());

                List<CDR> filtered = filter.applyFilters(batch);

                router.routeAll(filtered);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Fatal error in mediation engine", e);

        } finally {

            db.disconnect();

            // optional graceful shutdown
            for (Thread t : collectorThreads) {
                t.interrupt();
            }

            LOG.info("=== MEDIATION END ===");
        }
    }
}