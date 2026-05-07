package collector;

import model.CDR;
import model.Node;
import util.CsvUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runnable collector for a single Node (MSC / SMSC / PGW).
 */
public class NodeCollector implements Runnable {

    private static final Logger LOG =
            Logger.getLogger(NodeCollector.class.getName());

    private static final String ARCHIVE_DIR = "/app/archive";

    private final Node node;

    public NodeCollector(Node node) {
        this.node = node;
    }

    // ─────────────────────────────────────────────
    // THREAD ENTRY POINT
    // ─────────────────────────────────────────────

    @Override
    public void run() {
        try {
            LOG.info("Starting collector for node: " + node.getNodeId());

            List<CDR> cdrs = collectFromNode(node);

            LOG.info("Node " + node.getNodeId()
                    + " collected " + cdrs.size() + " CDRs");

        } catch (Exception e) {
            LOG.log(Level.SEVERE,
                    "Collector failed for node: " + node.getNodeId(), e);
        }
    }

    // ─────────────────────────────────────────────
    // CORE LOGIC 
    // ─────────────────────────────────────────────

    private List<CDR> collectFromNode(Node node) throws Exception {

        List<CDR> cdrs = new ArrayList<>();

        File inputDir = new File(node.getInputDirectory());

        if (!inputDir.exists() || !inputDir.isDirectory()) {
            LOG.warning("Directory not found: " + node.getInputDirectory());
            return cdrs;
        }

        File[] csvFiles = inputDir.listFiles(
                (dir, name) -> name.toLowerCase().endsWith(".csv")
        );

        if (csvFiles == null || csvFiles.length == 0) {
            LOG.info("No CSV files for node: " + node.getNodeId());
            return cdrs;
        }

        for (File csvFile : csvFiles) {
            try {
                LOG.info("Processing: " + csvFile.getName());
                
                //call the cdr parser ( file.csv -> cdr objects ) 
                List<CDR> parsed = CsvUtil.parseCdrFile(csvFile, node);

                cdrs.addAll(parsed);

                archiveFile(csvFile);

            } catch (Exception e) {
                LOG.log(Level.WARNING,
                        "Failed file: " + csvFile.getName(), e);
            }
        }

        return cdrs;
    }

    private void archiveFile(File file) {

        File archiveDir = new File(file.getParent(), ARCHIVE_DIR);

         //create the archive directory in the mediation container 
        if (!archiveDir.exists()) {
            archiveDir.mkdirs();
        }

        File dest = new File(archiveDir, file.getName());

        if (!file.renameTo(dest)) {
            LOG.warning("Archive failed: " + file.getName());
        } else {
            LOG.info("Archived: " + file.getName());
        }
    }
}