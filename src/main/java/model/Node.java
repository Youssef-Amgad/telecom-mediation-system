package model;

/**
 * Represents a network node (e.g., MSC, SGSN, GGSN) that generates CDRs.
 */
public class Node {

    private String nodeId;
    private String nodeName;
    private String nodeType;   // e.g., "MSC", "SGSN", "GGSN", "SMSC"
    private String ipAddress;
    private String protocol;   // e.g., "SFTP", "FTP", "SCP"
    private String inputDirectory;
    private boolean active;

    public Node() {}

    public Node(String nodeId, String nodeName, String nodeType,
                String ipAddress, String protocol, String inputDirectory) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.ipAddress = ipAddress;
        this.protocol = protocol;
        this.inputDirectory = inputDirectory;
        this.active = true;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String getNodeId()                   { return nodeId; }
    public void   setNodeId(String nodeId)      { this.nodeId = nodeId; }

    public String getNodeName()                 { return nodeName; }
    public void   setNodeName(String nodeName)  { this.nodeName = nodeName; }

    public String getNodeType()                 { return nodeType; }
    public void   setNodeType(String nodeType)  { this.nodeType = nodeType; }

    public String getIpAddress()                { return ipAddress; }
    public void   setIpAddress(String ip)       { this.ipAddress = ip; }

    public String getProtocol()                 { return protocol; }
    public void   setProtocol(String protocol)  { this.protocol = protocol; }

    public String getInputDirectory()               { return inputDirectory; }
    public void   setInputDirectory(String dir)     { this.inputDirectory = dir; }

    public boolean isActive()                   { return active; }
    public void    setActive(boolean active)    { this.active = active; }

    @Override
    public String toString() {
        return String.format("Node{id='%s', name='%s', type='%s', ip='%s', protocol='%s'}",
                nodeId, nodeName, nodeType, ipAddress, protocol);
    }
}
