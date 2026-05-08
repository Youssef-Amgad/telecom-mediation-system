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
    private static final int SLEEP_MS = 3000;

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

            FTPClient ftp = null;

            try {
                ftp = connect();

                List<File> remoteFiles = fetchRemoteFiles(ftp);

                if (remoteFiles.isEmpty()) {
                    Thread.sleep(SLEEP_MS);
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

                    deleteRemoteFile(ftp, file.getName());
                    file.delete();
                }

                if (!batch.isEmpty()) {
                    flush(batch);
                }

            } catch (Exception e) {
                LOG.severe("Collector error [" + node.getNodeName() + "]: " + e.getMessage());

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

            } finally {
                disconnect(ftp);
            }
        }
    }

    // ─────────────────────────────────────────────
    // CONNECT
    // ─────────────────────────────────────────────

    private FTPClient connect() throws Exception {

        FTPClient ftp = new FTPClient();

        ftp.setConnectTimeout(5000);
        ftp.connect(node.getIpAddress(), 21); // ALWAYS internal docker FTP port

        if (!ftp.login(node.getAuthUsername(), node.getAuthPassword())) {
            throw new RuntimeException("FTP login failed for " + node.getNodeName());
        }

        ftp.enterLocalPassiveMode();
        ftp.setFileType(FTP.BINARY_FILE_TYPE);

        if (!ftp.changeWorkingDirectory(node.getInputDirectory())) {
            throw new RuntimeException("Directory not found: " + node.getInputDirectory());
        }

        return ftp;
    }

    // ─────────────────────────────────────────────
    // FETCH FILES
    // ─────────────────────────────────────────────

    private List<File> fetchRemoteFiles(FTPClient ftp) throws Exception {

        List<File> downloaded = new ArrayList<>();

        var files = ftp.listFiles();

        if (files == null || files.length == 0) {
            return downloaded;
        }

        for (var remoteFile : files) {

            if (!remoteFile.isFile()) continue;
            if (remoteFile.getSize() <= 0) continue;

            File local = File.createTempFile("cdr_", ".csv");

            try (OutputStream out = new FileOutputStream(local)) {

                boolean ok = ftp.retrieveFile(remoteFile.getName(), out);

                if (ok) {
                    downloaded.add(local);
                } else {
                    LOG.warning("Failed download: " + remoteFile.getName());
                }
            }
        }

        return downloaded;
    }

    // ─────────────────────────────────────────────
    // DELETE REMOTE FILE
    // ─────────────────────────────────────────────

    private void deleteRemoteFile(FTPClient ftp, String fileName) {
        try {
            boolean ok = ftp.deleteFile(fileName);

            if (!ok) {
                LOG.warning("Could not delete remote file: " + fileName);
            }

        } catch (Exception e) {
            LOG.warning("Error deleting remote file: " + fileName);
        }
    }

    // ─────────────────────────────────────────────
    // PIPELINE
    // ─────────────────────────────────────────────

    private void flush(List<CDR> batch) {
        sharedQueue.offer(new ArrayList<>(batch));
        LOG.info("Pushed batch: " + batch.size());
    }

    // ─────────────────────────────────────────────
    // CLEANUP
    // ─────────────────────────────────────────────

    private void disconnect(FTPClient ftp) {
        try {
            if (ftp != null && ftp.isConnected()) {
                ftp.logout();
                ftp.disconnect();
            }
        } catch (Exception ignored) {}
    }
}