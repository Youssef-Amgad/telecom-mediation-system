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
            "jdbc:postgresql://ep-restless-bread-aqsbn9f0-pooler.c-8.us-east-1.aws.neon.tech/neondb"
                    + "?sslmode=require&channelBinding=require";

    private static final String DB_USER = "neondb_owner";
    private static final String DB_PASS = "npg_8cmBtYE9TynD";

    private Connection connection;

    public void connect() throws SQLException {
        LOG.info("Connecting to database...");
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        LOG.info("Database connected.");
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOG.info("Database connection closed.");
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Error closing DB", e);
        }
    }

    public List<Node> loadNodes() throws SQLException {

        List<Node> nodes = new ArrayList<>();

        String sql = """
              SELECT node_id,
                     node_name,
                     node_type,
                     ip_address,
                     port,
                     protocol,
                     input_dir,
                     auth_username,
                     auth_password,
                     active
              FROM nodes
              WHERE active = TRUE
              ORDER BY node_id
              """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Node node = new Node();

                node.setNodeId(String.valueOf(rs.getInt("node_id")));
                node.setNodeName(rs.getString("node_name"));
                node.setNodeType(rs.getString("node_type"));
                node.setIpAddress(rs.getString("ip_address"));
                node.setPort(String.valueOf(rs.getInt("port")));
                node.setProtocol(rs.getString("protocol"));
                node.setInputDirectory(rs.getString("input_dir")); // ✅ FIXED
                node.setActive(rs.getBoolean("active"));
                node.setAuthUsername(rs.getString("auth_username"));
                node.setAuthPassword(rs.getString("auth_password"));

                nodes.add(node);
            }
        }

        LOG.info("Loaded " + nodes.size() + " nodes.");
        return nodes;
    }

    public List<MediationRule> loadRules() throws SQLException {

        List<MediationRule> rules = new ArrayList<>();

        String sql = """
                SELECT rule_id,
                       source_node_id,
                       destination_node_id
                FROM mediation_rules
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                rules.add(new MediationRule(
                        rs.getInt("rule_id"),
                        rs.getInt("source_node_id"),
                        rs.getInt("destination_node_id")
                ));
            }
        }

        LOG.info("Loaded " + rules.size() + " rules.");
        return rules;
    }
}