package collector;

import model.CDR;
import model.Node;
import util.CsvUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NodeCollector implements Runnable {

    private static final Logger LOG =
            Logger.getLogger(NodeCollector.class.getName());

    // relative folder name — placed next to the input directory
    private static final String ARCHIVE_DIR = "archive";
    private static final int BATCH_SIZE = 50;

    private final Node node;
    private final Queue<List<CDR>> sharedQueue;

    public NodeCollector(Node node, Queue<List<CDR>> sharedQueue) {
        this.node = node;
        this.sharedQueue = sharedQueue;
    }

    @Override
    public void run() {
        try {
            LOG.info("Collector started: " + node.getNodeId());

            List<CDR> buffer = new ArrayList<>();

            File inputDir = new File(node.getInputDirectory());

            File[] files = inputDir.listFiles(
                    (d, name) -> name.endsWith(".txt") || name.endsWith(".csv")
            );

            if (files == null || files.length == 0) {
                LOG.info("No files found for node: " + node.getNodeId());
                return;
            }

            for (File file : files) {

                List<CDR> parsed = CsvUtil.parseCdrFile(file, node);

                for (CDR cdr : parsed) {
                    buffer.add(cdr);

                    // when we hit BATCH_SIZE → push batch
                    if (buffer.size() == BATCH_SIZE) {
                        flush(buffer);
                        buffer = new ArrayList<>();
                    }
                }

                archiveFile(file);
            }

            // flush any remaining CDRs that didn't fill a full batch
            if (!buffer.isEmpty()) {
                flush(buffer);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Collector failed: " + node.getNodeId(), e);
        }
    }

    private void flush(List<CDR> batch) {
        sharedQueue.add(new ArrayList<>(batch));   // defensive copy
        LOG.info("Node " + node.getNodeId() + " pushed batch of " + batch.size());
    }

    private void archiveFile(File file) {
        // archive/ is a sibling folder of the input directory
        File archiveDir = new File(file.getParentFile(), ARCHIVE_DIR);

        if (!archiveDir.exists()) {
            archiveDir.mkdirs();
        }

        File dest = new File(archiveDir, file.getName());

        if (!file.renameTo(dest)) {
            LOG.warning("Archive failed: " + file.getName());
        }
    }
}