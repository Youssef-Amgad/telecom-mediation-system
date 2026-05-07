package sender;

import model.CDR;
import model.Node;
import util.CsvUtil;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class FtpSender implements Sender {

    private static final Logger LOG = Logger.getLogger(FtpSender.class.getName());

    private static final String PROTOCOL = "FTP";

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

        LOG.info("FTP sending " + cdrs.size() + " CDRs → " + host + ":" + port);

        File tempFile = CsvUtil.writeCdrsToTempFile(cdrs, "ftp_" + destination.getNodeId());

        try {
            // STUB (replace later with FTPClient)
            LOG.info("FTP CONNECT → " + host + ":" + port);
            LOG.info("UPLOAD FILE → " + tempFile.getName());

            String remotePath = "/cdr/incoming/" + tempFile.getName();
            LOG.info("REMOTE PATH → " + remotePath);

        } finally {
            if (tempFile.exists()) tempFile.delete();
        }
    }
}