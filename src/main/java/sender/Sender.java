package sender;

import model.CDR;
import model.Node;

import java.util.List;

/**
 * Contract for all transmission protocols (FTP, SCP, SFTP, etc.)
 */
public interface Sender {

    /**
     * Protocol name used in registry (FTP / SCP / SFTP)
     */
    String getProtocol();

    /**
     * Sends a batch of CDRs to a destination node
     */
    void send(List<CDR> cdrs, Node destination) throws Exception;
}