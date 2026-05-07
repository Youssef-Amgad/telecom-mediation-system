package util;

import model.CDR;
import model.Node;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility methods for reading CDR CSV files from disk and writing CDR batches
 * to temporary CSV files before transmission.
 *
 * Expected CSV column order (first-row header ignored):
 * <pre>
 *   cdr_id, calling_party, called_party, imsi, imei,
 *   start_time, end_time, duration_seconds, call_type,
 *   termination_cause, charge_amount, currency
 * </pre>
 */
public final class CsvUtil {

    private static final Logger LOG = Logger.getLogger(CsvUtil.class.getName());
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String CSV_HEADER =
            "cdr_id,node_id,calling_party,called_party,imsi,imei," +
            "start_time,end_time,duration_seconds,call_type," +
            "termination_cause,charge_amount,currency";

    private CsvUtil() {}   // utility class

    // ── Reading ──────────────────────────────────────────────────────────────

    /**
     * Parses all CDR rows in {@code file} and tags each record with the
     * supplying node's ID and type.
     */
    public static List<CDR> parseCdrFile(File file, Node node) throws IOException {
        List<CDR> cdrs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            int lineNum = 0;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (firstLine) {
                    firstLine = false;
                    continue;   // skip header row
                }
                if (line.isBlank()) continue;

                try {
                    CDR cdr = parseLine(line, node);
                    cdr.setSourceFile(file.getName());
                    cdrs.add(cdr);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, String.format(
                            "Skipping malformed line %d in %s: %s",
                            lineNum, file.getName(), e.getMessage()));
                }
            }
        }

        return cdrs;
    }

    // ── Writing ──────────────────────────────────────────────────────────────

    /**
     * Writes the given CDR list to a temporary CSV file in the system temp
     * directory and returns a reference to the file.
     *
     * @param cdrs   records to write
     * @param prefix file name prefix (safe characters only)
     */
    public static File writeCdrsToTempFile(List<CDR> cdrs, String prefix) throws IOException {
        File tmpFile = File.createTempFile(prefix + "_", ".csv",
                new File(System.getProperty("java.io.tmpdir")));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile))) {
            writer.write(CSV_HEADER);
            writer.newLine();

            for (CDR cdr : cdrs) {
                writer.write(toCsvRow(cdr));
                writer.newLine();
            }
        }

        LOG.info("Wrote " + cdrs.size() + " CDRs to temp file: " + tmpFile.getName());
        return tmpFile;
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private static CDR parseLine(String line, Node node) {
        String[] cols = line.split(",", -1);

        CDR cdr = new CDR();
        cdr.setNodeId(node.getNodeId());
        cdr.setNodeType(node.getNodeType());

        cdr.setCdrId(         safeGet(cols, 0, UUID.randomUUID().toString()));
        cdr.setCallingParty(  safeGet(cols, 1, null));
        cdr.setCalledParty(   safeGet(cols, 2, null));
        cdr.setImsi(          safeGet(cols, 3, null));
        cdr.setImei(          safeGet(cols, 4, null));

        String startStr = safeGet(cols, 5, null);
        if (startStr != null && !startStr.isBlank()) {
            cdr.setStartTime(LocalDateTime.parse(startStr.trim(), DT_FMT));
        }

        String endStr = safeGet(cols, 6, null);
        if (endStr != null && !endStr.isBlank()) {
            cdr.setEndTime(LocalDateTime.parse(endStr.trim(), DT_FMT));
        }

        String dur = safeGet(cols, 7, "0");
        cdr.setDurationSeconds(Long.parseLong(dur.trim()));

        cdr.setCallType(          safeGet(cols, 8,  null));
        cdr.setTerminationCause(  safeGet(cols, 9,  null));

        String charge = safeGet(cols, 10, "0.0");
        cdr.setChargeAmount(Double.parseDouble(charge.trim()));

        cdr.setCurrency(safeGet(cols, 11, null));

        return cdr;
    }

    private static String toCsvRow(CDR cdr) {
        return String.join(",",
                escape(cdr.getCdrId()),
                escape(cdr.getNodeId()),
                escape(cdr.getCallingParty()),
                escape(cdr.getCalledParty()),
                escape(cdr.getImsi()),
                escape(cdr.getImei()),
                cdr.getStartTime() != null ? cdr.getStartTime().format(DT_FMT) : "",
                cdr.getEndTime()   != null ? cdr.getEndTime().format(DT_FMT)   : "",
                String.valueOf(cdr.getDurationSeconds()),
                escape(cdr.getCallType()),
                escape(cdr.getTerminationCause()),
                String.valueOf(cdr.getChargeAmount()),
                escape(cdr.getCurrency())
        );
    }

    /** Returns cols[index] trimmed, or defaultValue if out of bounds. */
    private static String safeGet(String[] cols, int index, String defaultValue) {
        if (index >= cols.length) return defaultValue;
        String val = cols[index].trim();
        return val.isEmpty() ? defaultValue : val;
    }

    /** Wraps values that contain commas in double-quotes. */
    private static String escape(String value) {
        if (value == null) return "";
        return value.contains(",") ? "\"" + value + "\"" : value;
    }
}
