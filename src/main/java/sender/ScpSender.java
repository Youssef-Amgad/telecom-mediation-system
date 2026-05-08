package sender;

import model.CDR;
import model.Node;
import util.CsvUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Logger;

/**
 * SCP sender (simulated).
 * Replace simulateCopy() with JSch or ProcessBuilder scp command later.
 */
public class ScpSender implements Sender {

    private static final Logger LOG = Logger.getLogger(ScpSender.class.getName());

    private static final String PROTOCOL = "SCP";

    // Simulated remote directory inside container
    private static final String REMOTE_DIR = "/app/cdr/incoming/";

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public void send(List<CDR> cdrs, Node destination) throws Exception {

        if (cdrs == null || cdrs.isEmpty()) {
            return;
        }

        String host = destination.getIpAddress();

        LOG.info("SCP SEND START → " + destination.getNodeName()
                + " [" + host + "] size=" + cdrs.size());

        File tempFile = CsvUtil.writeCdrsToTempFile(
                cdrs,
                "scp_" + destination.getNodeId()
        );

        try {

            String remotePath = REMOTE_DIR + tempFile.getName();

            LOG.info("SCP CONNECT → " + host);
            LOG.info("COPY → " + remotePath);

            simulateScpCopy(tempFile.toPath(), remotePath);

            LOG.info("SCP SUCCESS → " + remotePath);

        } catch (Exception e) {

            LOG.severe("SCP FAILED → " + host + " reason=" + e.getMessage());
            throw e;

        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Simulation layer for SCP.
     * Later replace with:
     * 1) JSch channelExec "scp"
     * OR
     * 2) ProcessBuilder("scp", file, host:dest)
     */
    private void simulateScpCopy(Path localFile, String remotePath) throws Exception {

        File remoteDir = new File(REMOTE_DIR);

        if (!remoteDir.exists()) {
            remoteDir.mkdirs();
        }

        Path target = Path.of(remotePath);

        Files.copy(
                localFile,
                target,
                StandardCopyOption.REPLACE_EXISTING
        );

        LOG.info("SIMULATED SCP COPY → " + localFile + " → " + target);
    }
}