package sender;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Factory that resolves the correct {@link Sender} implementation for a
 * given protocol string.
 *
 * New senders can be registered at runtime, making the factory extensible
 * without changing existing code.
 *
 * <pre>
 * SenderFactory factory = new SenderFactory();
 * Sender sender = factory.getSender("SFTP");
 * sender.send(cdrs, "BILLING_SYSTEM");
 * </pre>
 */
public class SenderFactory {

    private static final Logger LOG = Logger.getLogger(SenderFactory.class.getName());

    private final Map<String, Sender> registry = new HashMap<>();

    public SenderFactory() {
        // Register default implementations
        register(new SftpSender());
        register(new FtpSender());
        register(new ScpSender());
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Registers (or replaces) a sender for its declared protocol.
     */
    public void register(Sender sender) {
        registry.put(sender.getProtocol().toUpperCase(), sender);
        LOG.info("Registered sender for protocol: " + sender.getProtocol());
    }

    /**
     * Returns the sender for the given protocol.
     *
     * @param protocol case-insensitive protocol name, e.g. "SFTP"
     * @return the matching {@link Sender}
     * @throws IllegalArgumentException if no sender is registered for the protocol
     */
    public Sender getSender(String protocol) {
        if (protocol == null || protocol.isBlank()) {
            throw new IllegalArgumentException("Protocol must not be null or blank");
        }
        Sender sender = registry.get(protocol.toUpperCase());
        if (sender == null) {
            throw new IllegalArgumentException("No sender registered for protocol: " + protocol);
        }
        return sender;
    }

    /** Returns true if a sender is registered for the given protocol. */
    public boolean supports(String protocol) {
        return protocol != null && registry.containsKey(protocol.toUpperCase());
    }
}
