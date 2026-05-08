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
 * SFTP sender (simulation-ready).
 * Replace transfer block with JSch when moving to real SSH.
 */
public class SftpSender implements Sender {

    private static final Logger LOG = Logger.getLogger(SftpSender.class.getName());

    private static final String PROTOCOL = "SFTP";

    // inside container we simulate remote folder
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
        int port = Integer.parseInt(destination.getPort());

        LOG.info("SFTP SEND START → " + destination.getNodeName()
                + " [" + host + ":" + port + "] size=" + cdrs.size());

        File tempFile = CsvUtil.writeCdrsToTempFile(
                cdrs,
                "sftp_" + destination.getNodeId()
        );

        try {

            String remoteFile = REMOTE_DIR + tempFile.getName();

            LOG.info("Connecting SFTP → " + host + ":" + port);

            // -----------------------------------------------------------------
            // 🔥 REAL SFTP WOULD GO HERE (JSch / Apache Mina SSHD)
            // -----------------------------------------------------------------

            simulateUpload(tempFile.toPath(), remoteFile);

            LOG.info("UPLOAD SUCCESS → " + remoteFile);

        } catch (Exception e) {

            LOG.severe("SFTP FAILED → " + destination.getNodeName()
                    + " reason=" + e.getMessage());

            throw e;

        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Simulation layer:
     * Instead of real SSH upload, we copy file into "remote directory".
     *
     * In Docker:
     * /app/cdr/incoming = destination folder
     */
    private void simulateUpload(Path localFile, String remotePath) throws Exception {

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

        LOG.info("SIMULATED SFTP COPY → " + localFile + " → " + target);
    }
}