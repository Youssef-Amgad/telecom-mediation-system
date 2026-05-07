package sender;

import model.CDR;
import util.CsvUtil;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * Sends CDR files to a remote host via plain FTP.
 *
 * In production use Apache Commons Net {@code FTPClient}. The actual FTP calls
 * are stubbed here; replace the TODO blocks to activate real transfers.
 */
public class FtpSender implements Sender {

    private static final Logger LOG = Logger.getLogger(FtpSender.class.getName());

    private static final String PROTOCOL    = "FTP";
    private static final String REMOTE_USER = System.getProperty("ftp.user", "cdruser");
    private static final String REMOTE_PASS = System.getProperty("ftp.password", "");
    private static final int    REMOTE_PORT = Integer.parseInt(
                                    System.getProperty("ftp.port", "21"));
    private static final String REMOTE_PATH = System.getProperty("ftp.remotePath", "/cdr/incoming/");

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public void send(List<CDR> cdrs, String destination) throws Exception {
        if (cdrs == null || cdrs.isEmpty()) {
            LOG.info("No CDRs to send via FTP to: " + destination);
            return;
        }

        LOG.info(String.format("FTP: sending %d CDRs to %s", cdrs.size(), destination));

        File tempFile = CsvUtil.writeCdrsToTempFile(cdrs, "ftp_" + destination);

        try {
            // TODO: FTPClient ftp = new FTPClient();
            //       ftp.connect(destination, REMOTE_PORT);
            //       ftp.login(REMOTE_USER, REMOTE_PASS);
            //       ftp.setFileType(FTP.BINARY_FILE_TYPE);
            //       ftp.enterLocalPassiveMode();

            LOG.info(String.format("FTP [STUB]: would connect to %s:%d as %s",
                    destination, REMOTE_PORT, REMOTE_USER));

            String remotePath = REMOTE_PATH + tempFile.getName();
            // TODO: try (InputStream in = new FileInputStream(tempFile)) {
            //           ftp.storeFile(remotePath, in);
            //       }
            LOG.info("FTP [STUB]: would store " + tempFile.getName() + " → " + remotePath);

            // TODO: ftp.logout(); ftp.disconnect();

            LOG.info("FTP: transfer completed for destination: " + destination);
        } finally {
            if (tempFile.exists() && !tempFile.delete()) {
                LOG.warning("Could not delete temp file: " + tempFile.getAbsolutePath());
            }
        }
    }
}
