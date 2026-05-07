package sender;

import model.CDR;
import util.CsvUtil;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * Sends CDR files to a remote host via SCP (Secure Copy).
 *
 * In production this can be implemented with JSch's exec channel running
 * {@code scp -t <remotePath>}, or via a ProcessBuilder invoking the system
 * {@code scp} binary. The actual SCP calls are stubbed here.
 */
public class ScpSender implements Sender {

    private static final Logger LOG = Logger.getLogger(ScpSender.class.getName());

    private static final String PROTOCOL    = "SCP";
    private static final String REMOTE_USER = System.getProperty("scp.user", "cdruser");
    private static final String REMOTE_PORT = System.getProperty("scp.port", "22");
    private static final String REMOTE_PATH = System.getProperty("scp.remotePath", "/cdr/incoming/");
    private static final String KEY_PATH    = System.getProperty("scp.keyPath", "/etc/cdr/scp_key");

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public void send(List<CDR> cdrs, String destination) throws Exception {
        if (cdrs == null || cdrs.isEmpty()) {
            LOG.info("No CDRs to send via SCP to: " + destination);
            return;
        }

        LOG.info(String.format("SCP: sending %d CDRs to %s", cdrs.size(), destination));

        File tempFile = CsvUtil.writeCdrsToTempFile(cdrs, "scp_" + destination);

        try {
            // Option A – JSch exec channel approach (stubbed)
            // TODO: JSch jsch = new JSch();
            //       jsch.addIdentity(KEY_PATH);
            //       Session session = jsch.getSession(REMOTE_USER, destination,
            //                                         Integer.parseInt(REMOTE_PORT));
            //       session.setConfig("StrictHostKeyChecking", "no");
            //       session.connect(30_000);
            //       // ... SCP protocol handshake via exec channel ...

            // Option B – system scp binary (stubbed)
            // String cmd = String.format("scp -i %s -P %s %s %s@%s:%s",
            //         KEY_PATH, REMOTE_PORT,
            //         tempFile.getAbsolutePath(),
            //         REMOTE_USER, destination, REMOTE_PATH);
            // Process p = Runtime.getRuntime().exec(cmd);
            // p.waitFor();

            LOG.info(String.format("SCP [STUB]: would scp %s → %s@%s:%s (port %s, key %s)",
                    tempFile.getName(), REMOTE_USER, destination,
                    REMOTE_PATH, REMOTE_PORT, KEY_PATH));

            LOG.info("SCP: transfer completed for destination: " + destination);
        } finally {
            if (tempFile.exists() && !tempFile.delete()) {
                LOG.warning("Could not delete temp file: " + tempFile.getAbsolutePath());
            }
        }
    }
}
