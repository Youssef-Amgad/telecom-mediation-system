package collector;

import model.CDR;
import model.Node;
import util.CsvUtil;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class NodeCollector implements Runnable {

    private static final Logger LOG = Logger.getLogger(NodeCollector.class.getName());

    private static final int BATCH_SIZE = 50;

    private final Node node;
    private final BlockingQueue<List<CDR>> sharedQueue;

    public NodeCollector(Node node, BlockingQueue<List<CDR>> sharedQueue) {
        this.node = node;
        this.sharedQueue = sharedQueue;
    }

    @Override
    public void run() {

        LOG.info("Collector started for node: " + node.getNodeName());

        while (!Thread.currentThread().isInterrupted()) {

            try {

                List<File> remoteFiles = fetchRemoteFiles();

                if (remoteFiles.isEmpty()) {
                    Thread.sleep(3000);
                    continue;
                }

                List<CDR> batch = new ArrayList<>();

                for (File file : remoteFiles) {

                    List<CDR> parsed = CsvUtil.parseCdrFile(file, node);

                    for (CDR cdr : parsed) {

                        batch.add(cdr);

                        if (batch.size() >= BATCH_SIZE) {
                            flush(batch);
                            batch = new ArrayList<>();
                        }
                    }

                    archiveRemote(file);
                }

                if (!batch.isEmpty()) {
                    flush(batch);
                }

            } catch (Exception e) {
                LOG.severe("Collector error: " + node.getNodeName() + " -> " + e.getMessage());

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // ─────────────────────────────────────────────
    // FETCH FILES
    // ─────────────────────────────────────────────

    private List<File> fetchRemoteFiles() throws Exception {

        String host = node.getIpAddress();   // MUST be container name
        int port = 21;                       // ALWAYS internal FTP port

        return fetchViaFTP(host, port, node);
    }

    // ─────────────────────────────────────────────
    // FTP FIXED
    // ─────────────────────────────────────────────

    private List<File> fetchViaFTP(String host, int port, Node node) throws Exception {

        List<File> downloaded = new ArrayList<>();

        FTPClient ftp = new FTPClient();

        ftp.setConnectTimeout(5000);
        ftp.connect(host, port);

        if (!ftp.login(node.getAuthUsername(), node.getAuthPassword())) {
            throw new RuntimeException("FTP login failed: " + host);
        }

        ftp.enterLocalPassiveMode();
        ftp.setFileType(FTP.BINARY_FILE_TYPE);

        if (!ftp.changeWorkingDirectory(node.getInputDirectory())) {
            throw new RuntimeException("Directory not found: " + node.getInputDirectory());
        }

        var files = ftp.listFiles();

        if (files == null || files.length == 0) {
            LOG.info("No files in " + host);
            ftp.logout();
            ftp.disconnect();
            return downloaded;
        }

        for (var remoteFile : files) {

            if (!remoteFile.isFile()) continue;

            File local = File.createTempFile("recv_", ".csv");

            try (OutputStream out = new FileOutputStream(local)) {
                boolean ok = ftp.retrieveFile(remoteFile.getName(), out);

                if (ok) {
                    downloaded.add(local);
                } else {
                    LOG.warning("Failed to download: " + remoteFile.getName());
                }
            }
        }

        ftp.logout();
        ftp.disconnect();

        return downloaded;
    }

    // ─────────────────────────────────────────────

    private void flush(List<CDR> batch) {
        sharedQueue.offer(new ArrayList<>(batch));
        LOG.info("Pushed batch: " + batch.size());
    }

    private void archiveRemote(File file) {
        if (!file.delete()) {
            LOG.warning("Temp file not deleted: " + file.getName());
        }
    }
}