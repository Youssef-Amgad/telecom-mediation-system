package NodeCollector;

//import model.CDR;
import model.Node;
import util.CsvUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsible for collecting CDR files from registered nodes.
 *
 * For each active node it scans the configured input directory, reads every
 * CSV file it finds, parses it into {@link CDR} objects, and then moves the
 * processed file to an archive sub-folder so it is not re-processed.
 */
public class NodeCollector {

    private static final Logger LOG = Logger.getLogger(NodeCollector.class.getName());
    private static final String ARCHIVE_DIR = "archive";

    private final List<Node> nodes;

    public NodeCollector(List<Node> nodes) {
        this.nodes = nodes;
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Iterates over all active nodes, collects CDR files, and returns the
     * full list of parsed CDRs.
     */
    public List<CDR> collectAll() {
        List<CDR> allCdrs = new ArrayList<>();

        for (Node node : nodes) {
            if (!node.isActive()) {
                LOG.info("Skipping inactive node: " + node.getNodeId());
                continue;
            }
            try {
                List<CDR> nodeCdrs = collectFromNode(node);
                LOG.info(String.format("Collected %d CDRs from node %s",
                        nodeCdrs.size(), node.getNodeId()));
                allCdrs.addAll(nodeCdrs);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error collecting from node: " + node.getNodeId(), e);
            }
        }

        return allCdrs;
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    /**
     * Scans the node's input directory for CSV files and parses each one.
     */
    private List<CDR> collectFromNode(Node node) throws Exception {
        List<CDR> cdrs = new ArrayList<>();

        File inputDir = new File(node.getInputDirectory());
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            LOG.warning("Input directory not found for node " + node.getNodeId()
                    + ": " + node.getInputDirectory());
            return cdrs;
        }

        File[] csvFiles = inputDir.listFiles(
                (dir, name) -> name.toLowerCase().endsWith(".csv"));

        if (csvFiles == null || csvFiles.length == 0) {
            LOG.info("No CSV files found for node: " + node.getNodeId());
            return cdrs;
        }

        for (File csvFile : csvFiles) {
            try {
                LOG.info("Processing file: " + csvFile.getName());
                List<CDR> parsed = CsvUtil.parseCdrFile(csvFile, node);
                cdrs.addAll(parsed);
                archiveFile(csvFile);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Failed to parse file: " + csvFile.getName(), e);
            }
        }

        return cdrs;
    }

    /**
     * Moves a processed file into an "archive" sub-directory.
     */
    private void archiveFile(File file) {
        File archiveDir = new File(file.getParent(), ARCHIVE_DIR);
        if (!archiveDir.exists()) {
            archiveDir.mkdirs();
        }
        File dest = new File(archiveDir, file.getName());
        if (!file.renameTo(dest)) {
            LOG.warning("Could not archive file: " + file.getName());
        } else {
            LOG.info("Archived: " + file.getName());
        }
    }
}
