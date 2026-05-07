package sender;

import model.CDR;
import util.CsvUtil;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * Sends CDR files to a remote host via SFTP.
 *
 * In a production environment this class would use a library such as JSch or
 * Apache Mina SSHD. The actual SSH/SFTP calls are stubbed here and marked with
 * TODO comments so they can be swapped in without changing the surrounding logic.
 */
public class SftpSender implements Sender {

    private static final Logger LOG = Logger.getLogger(SftpSender.class.getName());

    private static final String PROTOCOL    = "SFTP";
    private static final String REMOTE_USER = System.getProperty("sftp.user", "cdruser");
    private static final String REMOTE_PORT = System.getProperty("sftp.port", "22");
    private static final String REMOTE_PATH = System.getProperty("sftp.remotePath", "/cdr/incoming/");
    private static final String KEY_PATH    = System.getProperty("sftp.keyPath", "/etc/cdr/sftp_key");

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public void send(List<CDR> cdrs, String destination) throws Exception {
        if (cdrs == null || cdrs.isEmpty()) {
            LOG.info("No CDRs to send via SFTP to: " + destination);
            return;
        }

        LOG.info(String.format("SFTP: sending %d CDRs to %s", cdrs.size(), destination));

        // Step 1 – write CDRs to a local temp file
        File tempFile = CsvUtil.writeCdrsToTempFile(cdrs, "sftp_" + destination);

        try {
            // Step 2 – open SFTP session
            // TODO: replace stub with real JSch / Mina SSHD session
            //   JSch jsch = new JSch();
            //   jsch.addIdentity(KEY_PATH);
            //   Session session = jsch.getSession(REMOTE_USER, destination,
            //                                     Integer.parseInt(REMOTE_PORT));
            //   session.setConfig("StrictHostKeyChecking", "no");
            //   session.connect(30_000);
            //   ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            //   channel.connect();

            LOG.info(String.format("SFTP [STUB]: would connect to %s:%s as %s using key %s",
                    destination, REMOTE_PORT, REMOTE_USER, KEY_PATH));

            // Step 3 – upload file
            String remotePath = REMOTE_PATH + tempFile.getName();
            // TODO: channel.put(tempFile.getAbsolutePath(), remotePath);
            LOG.info("SFTP [STUB]: would upload " + tempFile.getName() + " → " + remotePath);

            // Step 4 – disconnect
            // TODO: channel.disconnect(); session.disconnect();

            LOG.info("SFTP: transfer completed for destination: " + destination);
        } finally {
            // Always clean up the local temp file
            if (tempFile.exists() && !tempFile.delete()) {
                LOG.warning("Could not delete temp file: " + tempFile.getAbsolutePath());
            }
        }
    }
}
