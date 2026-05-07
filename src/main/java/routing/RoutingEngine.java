package routing;

import model.CDR;
import model.MediationRule;
import model.Node;
import sender.Sender;
import sender.SenderFactory;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RoutingEngine {

    private static final Logger LOG = Logger.getLogger(RoutingEngine.class.getName());

    private final List<MediationRule> rules;
    private final Map<String, Node> nodeMap;   // key = nodeId (String)
    private final SenderFactory senderFactory;

    private final List<CDR> deadLetterQueue = new ArrayList<>();

    public RoutingEngine(List<MediationRule> rules,
                         List<Node> nodes,
                         SenderFactory senderFactory) {

        this.rules = new ArrayList<>(rules);   // no sorting — no priority field

        this.senderFactory = senderFactory;

        // Node.getNodeId() returns String → key is String
        this.nodeMap = nodes.stream()
                .collect(Collectors.toMap(
                        Node::getNodeId,
                        n -> n
                ));
    }

    public void routeAll(List<CDR> cdrs) {

        Map<Node, List<CDR>> batches = new HashMap<>();

        for (CDR cdr : cdrs) {

            MediationRule rule = findRule(cdr);

            if (rule == null) {
                LOG.warning("No rule for CDR " + cdr.getCdrId()
                        + " (nodeId=" + cdr.getNodeId() + ")");
                deadLetterQueue.add(cdr);
                continue;
            }

            // destination node id is Integer; nodeMap key is String
            String destKey = String.valueOf(rule.getDestinationNodeId());
            Node destNode  = nodeMap.get(destKey);

            if (destNode == null) {
                LOG.warning("Unknown destination node id: " + rule.getDestinationNodeId());
                deadLetterQueue.add(cdr);
                continue;
            }

            batches.computeIfAbsent(destNode, k -> new ArrayList<>()).add(cdr);
        }

        // send per downstream node
        for (Map.Entry<Node, List<CDR>> entry : batches.entrySet()) {
            sendBatch(entry.getKey(), entry.getValue());
        }

        LOG.info("Routing done. DeadLetter=" + deadLetterQueue.size());
    }

    private void sendBatch(Node node, List<CDR> batch) {
        try {
            Sender sender = senderFactory.getSender(node.getProtocol());

            LOG.info("Sending " + batch.size()
                    + " CDRs → " + node.getNodeName()
                    + " [" + node.getIpAddress() + ":" + node.getPort() + "]"
                    + " via " + node.getProtocol());

            sender.send(batch, node);   // pass Node, not String

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed sending to node: " + node.getNodeId(), e);
            deadLetterQueue.addAll(batch);
        }
    }

    private MediationRule findRule(CDR cdr) {
        for (MediationRule rule : rules) {
            if (rule.matches(cdr)) return rule;
        }
        return null;
    }

    public List<CDR> getDeadLetterQueue() {
        return Collections.unmodifiableList(deadLetterQueue);
    }
}