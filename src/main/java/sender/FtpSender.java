package sender;

import model.CDR;
import model.Node;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import util.CsvUtil;

import java.io.*;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Sends a batch of CDRs to a downstream node via FTP.
 * Writes CDRs to a temp CSV file then uploads it to the remote node's /app/cdr.
 */
public class FtpSender implements Sender {

    private static final Logger LOG = Logger.getLogger(FtpSender.class.getName());

    private static final String PROTOCOL   = "FTP";
    private static final String REMOTE_DIR = "/";

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

@Override
public void send(List<CDR> cdrs, Node destination) throws Exception {

    if (cdrs == null || cdrs.isEmpty()) return;

    String host = destination.getIpAddress();
    int port = Integer.parseInt(destination.getPort());

    LOG.info("FTP sending " + cdrs.size() + " CDRs → "
            + destination.getNodeName() + " (" + host + ":" + port + ")");

    File tempFile = CsvUtil.writeCdrsToTempFile(
            cdrs, "med_" + destination.getNodeId()
    );

    FTPClient ftp = new FTPClient();

    try {
        // ── TIMEOUTS (IMPORTANT IN DOCKER)
        ftp.setConnectTimeout(10000);
        ftp.setDefaultTimeout(10000);
        ftp.setDataTimeout(10000);

        ftp.connect(host, port);

        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            throw new IOException("FTP refused connection, code: " + reply);
        }

        boolean login = ftp.login(
                destination.getAuthUsername(),
                destination.getAuthPassword()
        );

        if (!login) {
            throw new IOException("FTP login failed for " + destination.getNodeName());
        }

        ftp.enterLocalPassiveMode();
        ftp.setPassiveNatWorkaround(true);
        ftp.setRemoteVerificationEnabled(false);
        ftp.setFileType(FTP.BINARY_FILE_TYPE);

        try (InputStream in = new FileInputStream(tempFile)) {

            String remotePath = REMOTE_DIR + tempFile.getName();

            boolean ok = ftp.storeFile(remotePath, in);

            if (!ok) {
                throw new IOException("FTP upload failed: " + remotePath);
            }

            LOG.info("FTP upload OK → " + remotePath);
        }

    } finally {
        try {
            if (ftp.isConnected()) {
                ftp.logout();
                ftp.disconnect();
            }
        } catch (Exception ignore) {}

        if (tempFile.exists()) tempFile.delete();
    }
}
}