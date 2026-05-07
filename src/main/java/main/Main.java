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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        LOG.info("=== MEDIATION START ===");

        DBManager db = new DBManager();
        Queue<List<CDR>> queue = new ConcurrentLinkedQueue<>();

        try {
            db.connect();

            List<Node> nodes = db.loadNodes();
            List<MediationRule> rules = db.loadRules();

            // ── START COLLECTOR THREADS (UPSTREAM nodes only) ─────────────────
            List<Thread> threads = new ArrayList<>();

            for (Node node : nodes) {
                if (!"UPSTREAM".equalsIgnoreCase(node.getNodeType())) {
                    continue;
                }

                NodeCollector collector = new NodeCollector(node, queue);
                Thread t = new Thread(collector);
                t.setName("Collector-" + node.getNodeId());
                threads.add(t);
                t.start();
            }

            if (threads.isEmpty()) {
                LOG.warning("No upstream nodes found — nothing to do.");
                return;
            }

            // ── PROCESSING PIPELINE ───────────────────────────────────────────
            CDRFilter filter     = new CDRFilter();
            // pass all nodes so RoutingEngine can resolve destination nodes
            RoutingEngine router = new RoutingEngine(rules, nodes, new SenderFactory());

            while (true) {

                // check if all collector threads are done AND queue is empty
                boolean allDone = threads.stream().noneMatch(Thread::isAlive);

                List<CDR> batch = queue.poll();

                if (batch == null) {
                    if (allDone) {
                        LOG.info("All collectors finished and queue is empty — done.");
                        break;
                    }
                    Thread.sleep(300);
                    continue;
                }

                LOG.info("Main got batch of " + batch.size());

                List<CDR> filtered = filter.applyFilters(batch);
                router.routeAll(filtered);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Fatal error", e);
        } finally {
            db.disconnect();
        }

        LOG.info("=== MEDIATION END ===");
    }
}