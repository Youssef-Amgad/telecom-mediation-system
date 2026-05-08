package model;

/**
 * Represents a network node (e.g., MSC, SGSN, GGSN) that generates CDRs.
 */
public class Node {

    private String nodeId;
    private String nodeName;
    private String nodeType; //MSC SMSc ,...
    private String ipAddress;
    private String port;
    private String protocol;
    private String inputDirectory;
    private String authUsername;
    private String authPassword;
    private boolean active;

    public Node() {}

    public Node(String nodeId,
             String nodeName,
             String nodeType,
             String ipAddress,
             String port,
             String protocol,
             String inputDirectory,
             String authUsername,
             String authPassword) {

     this.nodeId = nodeId;
     this.nodeName = nodeName;
     this.nodeType = nodeType;
     this.ipAddress = ipAddress;
     this.port = port;
     this.protocol = protocol;
     this.inputDirectory = inputDirectory;
     this.authUsername = authUsername;
     this.authPassword = authPassword;
 }

    // ── Getters & Setters ─────────────────────

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ip) {
        this.ipAddress = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String dir) {
        this.inputDirectory = dir;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setAuthUsername(String authUsername) {
        this.authUsername = authUsername;
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }
    
    public String getAuthUsername() { return authUsername; }
    public String getAuthPassword() { return authPassword; }

    @Override
    public String toString() {
        return String.format(
                "Node{id='%s', name='%s', type='%s', ip='%s', port='%s', protocol='%s'}",
                nodeId, nodeName, nodeType, ipAddress, port, protocol
        );
    }
}