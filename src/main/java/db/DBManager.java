package db;

import model.MediationRule;
import model.MediationRule.Action;
import model.Node;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides database access for the CDR mediation system.
 *
 * Manages a single JDBC connection (suitable for a single-threaded mediation
 * process). For multi-threaded deployments, replace with a connection pool
 * such as HikariCP.
 *
 * Configuration is read from system properties:
 * <pre>
 *   db.url      – JDBC URL  (default: jdbc:postgresql://localhost:5432/cdrdb)
 *   db.user     – DB user   (default: cdr_app)
 *   db.password – DB password
 * </pre>
 */
public class DBManager {

    private static final Logger LOG = Logger.getLogger(DBManager.class.getName());

    private static final String DB_URL  = "jdbc:postgresql://ep-restless-bread-aqsbn9f0-pooler.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require&channelBinding=require";
    private static final String DB_USER = "neondb_owner";
    private static final String DB_PASS = "npg_8cmBtYE9TynD";

    private Connection connection;

    // ── Lifecycle ────────────────────────────────────────────────────────────

    /** Opens a JDBC connection. Call once at startup. */
    public void connect() throws SQLException {
        LOG.info("Connecting to database: " + DB_URL);
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        connection.setAutoCommit(true);
        LOG.info("Database connection established.");
    }

    /** Closes the JDBC connection. Call on shutdown. */
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                LOG.info("Database connection closed.");
            } catch (SQLException e) {
                LOG.log(Level.WARNING, "Error closing database connection", e);
            }
        }
    }

    //Node Queries

    /**
     * Loads all active nodes from the {@code nodes} table.
     */
    public List<Node> loadNodes() throws SQLException {
        List<Node> nodes = new ArrayList<>();
        String sql = """
                SELECT node_id, node_name, node_type, ip_address,
                       protocol, input_directory, active
                FROM nodes
                WHERE active = TRUE
                ORDER BY node_id
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Node node = new Node(
                        rs.getString("node_id"),
                        rs.getString("node_name"),
                        rs.getString("node_type"),
                        rs.getString("ip_address"),
                        rs.getString("protocol"),
                        rs.getString("input_directory")
                );
                node.setActive(rs.getBoolean("active"));
                nodes.add(node);
            }
        }

        LOG.info("Loaded " + nodes.size() + " active nodes from DB.");
        return nodes;
    }

    // ── MediationRule Queries ─────────────────────────────────────────────────

    /**
     * Loads all active mediation rules, ordered by priority.
     */
    public List<MediationRule> loadRules() throws SQLException {
        List<MediationRule> rules = new ArrayList<>();
        String sql = """
                SELECT rule_id, node_id, call_type, destination,
                       action, priority, transform_expression, active
                FROM mediation_rules
                WHERE active = TRUE
                ORDER BY priority ASC
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                MediationRule rule = new MediationRule(
                        rs.getInt("rule_id"),
                        rs.getString("node_id"),
                        rs.getString("call_type"),
                        rs.getString("destination"),
                        Action.valueOf(rs.getString("action")),
                        rs.getInt("priority")
                );
                rule.setTransformExpression(rs.getString("transform_expression"));
                rule.setActive(rs.getBoolean("active"));
                rules.add(rule);
            }
        }

        LOG.info("Loaded " + rules.size() + " active mediation rules from DB.");
        return rules;
    }

    // ── CDR Audit Write ──────────────────────────────────────────────────────

    /**
     * Inserts a processing summary record for audit/reporting purposes.
     *
     * @param runId       unique identifier for this mediation run
     * @param totalRead   number of CDRs collected
     * @param totalPassed number that passed filtering
     * @param totalRouted number successfully routed
     * @param totalDead   number ending in dead-letter queue
     */
    public void insertRunSummary(String runId, int totalRead, int totalPassed,
                                  int totalRouted, int totalDead) {
        String sql = """
                INSERT INTO mediation_run_log
                    (run_id, run_time, total_read, total_passed, total_routed, total_dead)
                VALUES (?, NOW(), ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, runId);
            ps.setInt(2, totalRead);
            ps.setInt(3, totalPassed);
            ps.setInt(4, totalRouted);
            ps.setInt(5, totalDead);
            ps.executeUpdate();
            LOG.info("Run summary inserted for runId: " + runId);
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Failed to insert run summary: " + e.getMessage(), e);
        }
    }

    // ── Health Check ─────────────────────────────────────────────────────────

    /** Returns true if the connection is open and the DB responds. */
    public boolean isHealthy() {
        try {
            return connection != null && !connection.isClosed()
                    && connection.isValid(3);
        } catch (SQLException e) {
            return false;
        }
    }
}
