package sender;

import model.CDR;
import model.Node;
import util.CsvUtil;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * Sends CDR files to a remote host via SCP (Secure Copy).
 * The actual SCP transfer is stubbed — replace with JSch or ProcessBuilder when ready.
 */
public class ScpSender implements Sender {

    private static final Logger LOG = Logger.getLogger(ScpSender.class.getName());

    private static final String PROTOCOL    = "SCP";
    private static final String REMOTE_PATH = System.getProperty("scp.remotePath", "/cdr/incoming/");

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

        LOG.info("SCP sending " + cdrs.size() + " CDRs to " + host);

        File tempFile = CsvUtil.writeCdrsToTempFile(cdrs, "scp_" + destination.getNodeId());

        try {
            String remotePath = REMOTE_PATH + tempFile.getName();
            LOG.info("SCP COPY → " + host + ":" + remotePath);
            // TODO: replace stub with real SCP implementation (JSch / ProcessBuilder)

        } finally {
            tempFile.delete();
        }
    }
}