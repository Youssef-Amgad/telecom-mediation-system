package sender;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Factory that resolves Sender implementation by protocol name.
 */
public class SenderFactory {

    private static final Logger LOG = Logger.getLogger(SenderFactory.class.getName());

    private final Map<String, Sender> registry = new HashMap<>();

    public SenderFactory() {
        register(new FtpSender());
        register(new SftpSender());
        register(new ScpSender());
    }

    public void register(Sender sender) {
        registry.put(sender.getProtocol().toUpperCase(), sender);
        LOG.info("Registered sender: " + sender.getProtocol());
    }

    public Sender getSender(String protocol) {
        if (protocol == null || protocol.isBlank()) {
            throw new IllegalArgumentException("Protocol cannot be null/empty");
        }
        Sender sender = registry.get(protocol.toUpperCase());
        if (sender == null) {
            throw new IllegalArgumentException(
                    "No sender registered for protocol: " + protocol);
        }
        return sender;
    }

    public boolean supports(String protocol) {
        return protocol != null && registry.containsKey(protocol.toUpperCase());
    }
}