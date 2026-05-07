package sender;

import model.CDR;
import model.Node;
import util.CsvUtil;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * Sends CDR files to a remote host via SFTP.
 * The actual SSH/SFTP calls are stubbed — replace with JSch or Apache Mina SSHD when ready.
 */
public class SftpSender implements Sender {

    private static final Logger LOG = Logger.getLogger(SftpSender.class.getName());

    private static final String PROTOCOL    = "SFTP";
    private static final String REMOTE_PATH = System.getProperty("sftp.remotePath", "/cdr/incoming/");

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
        int    port = Integer.parseInt(destination.getPort());

        LOG.info("SFTP sending " + cdrs.size() + " CDRs to "
                + destination.getNodeName() + " (" + host + ":" + port + ")");

        File tempFile = CsvUtil.writeCdrsToTempFile(cdrs, "sftp_" + destination.getNodeId());

        try {
            String remotePath = REMOTE_PATH + tempFile.getName();
            LOG.info("SFTP CONNECT → " + host + ":" + port);
            LOG.info("UPLOAD → " + remotePath);
            // TODO: replace stub with real SFTP implementation (JSch / Apache Mina)

        } finally {
            tempFile.delete();
        }
    }
}