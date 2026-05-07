package db;

import model.MediationRule;
import model.Node;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBManager {

    private static final Logger LOG = Logger.getLogger(DBManager.class.getName());

    private static final String DB_URL =
            "jdbc:postgresql://ep-restless-bread-aqsbn9f0-pooler.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require&channelBinding=require";

    private static final String DB_USER = "neondb_owner";
    private static final String DB_PASS = "YOUR_PASSWORD_HERE"; // move to env later

    private Connection connection;

    // ── Connect / Disconnect ──────────────────────────────────────────────────

    public void connect() throws SQLException {
        LOG.info("Connecting to database...");
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        connection.setAutoCommit(true);
        LOG.info("Database connected.");
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                LOG.info("Database connection closed.");
            } catch (SQLException e) {
                LOG.log(Level.WARNING, "Error closing DB connection", e);
            }
        }
    }

    // ── NODES ─────────────────────────────────────────────────────────────────

    public List<Node> loadNodes() throws SQLException {

        List<Node> nodes = new ArrayList<>();

        // column alias matches what we read below: input_dir aliased as input_directory
        String sql = """
                SELECT node_id, node_name, node_type,
                       ip_address, port, protocol,
                       input_dir AS input_directory, active
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
                        rs.getString("port"),
                        rs.getString("protocol"),
                        rs.getString("input_directory")
                );

                node.setActive(rs.getBoolean("active"));
                nodes.add(node);
            }
        }

        LOG.info("Loaded " + nodes.size() + " nodes.");
        return nodes;
    }

    // ── MEDIATION RULES ───────────────────────────────────────────────────────

    public List<MediationRule> loadRules() throws SQLException {

        List<MediationRule> rules = new ArrayList<>();

        // removed the trailing comma that caused a SQL syntax error
        String sql = """
                SELECT rule_id,
                       source_node_id,
                       destination_node_id
                FROM mediation_rules
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                MediationRule rule = new MediationRule(
                        rs.getInt("rule_id"),
                        rs.getInt("source_node_id"),
                        rs.getInt("destination_node_id")
                );

                rules.add(rule);
            }
        }

        LOG.info("Loaded " + rules.size() + " rules.");
        return rules;
    }

    // ── HEALTH CHECK ──────────────────────────────────────────────────────────

    public boolean isHealthy() {
        try {
            return connection != null
                    && !connection.isClosed()
                    && connection.isValid(3);
        } catch (SQLException e) {
            return false;
        }
    }
}