package routing;

import model.CDR;
import model.MediationRule;
import model.Node;
import sender.Sender;
import sender.SenderFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoutingEngine {

    private static final Logger LOG = Logger.getLogger(RoutingEngine.class.getName());

    private final Map<String, MediationRule> ruleMap;
    private final Map<String, Node> nodeMap;
    private final SenderFactory senderFactory;

    private final Queue<CDR> deadLetterQueue = new ConcurrentLinkedQueue<>();

    public RoutingEngine(List<MediationRule> rules,
                         List<Node> nodes,
                         SenderFactory senderFactory) {

        this.senderFactory = senderFactory;

        // index rules by source node (FAST LOOKUP)
        this.ruleMap = new HashMap<>();
        for (MediationRule r : rules) {
            ruleMap.put(String.valueOf(r.getSourceNodeId()), r);
        }

        // node map
        this.nodeMap = new HashMap<>();
        for (Node n : nodes) {
            nodeMap.put(n.getNodeId(), n);
        }
    }

    public void routeAll(List<CDR> cdrs) {

        Map<String, List<CDR>> batches = new HashMap<>();

        for (CDR cdr : cdrs) {

            String sourceId = cdr.getNodeId();

            MediationRule rule = ruleMap.get(sourceId);

            if (rule == null) {
                LOG.warning("No rule for CDR " + cdr.getCdrId()
                        + " sourceNode=" + sourceId);
                deadLetterQueue.add(cdr);
                continue;
            }

            String destKey = String.valueOf(rule.getDestinationNodeId());
            Node destNode = nodeMap.get(destKey);

            if (destNode == null) {
                LOG.warning("Unknown destination node: " + destKey);
                deadLetterQueue.add(cdr);
                continue;
            }

            batches.computeIfAbsent(destKey, k -> new ArrayList<>())
                   .add(cdr);
        }

        // send batches
        for (Map.Entry<String, List<CDR>> entry : batches.entrySet()) {

            Node node = nodeMap.get(entry.getKey());
            sendBatch(node, entry.getValue());
        }

        LOG.info("Routing done. DeadLetter=" + deadLetterQueue.size());
    }

    private void sendBatch(Node node, List<CDR> batch) {

        try {
            Sender sender = senderFactory.getSender(node.getProtocol());

            LOG.info("Sending " + batch.size()
                    + " CDRs → " + node.getNodeName()
                    + " [" + node.getIpAddress() + "]");

            sender.send(batch, node);

        } catch (Exception e) {
            LOG.log(Level.SEVERE,
                    "Send failed to node: " + node.getNodeName(), e);

            deadLetterQueue.addAll(batch);
        }
    }

    public Queue<CDR> getDeadLetterQueue() {
        return deadLetterQueue;
    }
}