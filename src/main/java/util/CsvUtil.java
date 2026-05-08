package util;

import model.CDR;
import model.Node;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CsvUtil {

    private static final Logger LOG = Logger.getLogger(CsvUtil.class.getName());

    private static final DateTimeFormatter TS_FMT
            = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private CsvUtil() {
    }

    // ─────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────
    public static List<CDR> parseCdrFile(File file, Node node) throws IOException {

        List<CDR> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = br.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                try {
                    //calling parsing function the read the file and parse it -> object 
                    CDR cdr = parseLine(line, node);

                    //setting the file name 
                    cdr.setSourceFile(file.getName());

                    //adding the parsed cdr to the list
                    list.add(cdr);

                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Bad CDR line in " + file.getName() + ": " + line);
                }
            }
        }

        return list;
    }

    // ─────────────────────────────────────────────
    // PARSER 
    // ─────────────────────────────────────────────
    private static CDR parseLine(String line, Node node) {

        String[] p = line.split(",");

        /*
         * FORMAT FROM GENERATOR:
         * 0 cdrId
         * 1 msisdn
         * 2 dialB
         * 3 serviceId
         * 4 duration
         * 5 fees
         * 6 timestamp
         */
        CDR cdr = new CDR();

        cdr.setCdrId(p[0]);
        cdr.setNodeId(node.getNodeId());
        cdr.setNodeType(node.getNodeType());

        cdr.setCallingParty(p[1]);
        cdr.setCalledParty(p[2]);

        // serviceId → callType 
        cdr.setCallType(p[3]);

        // duration
        cdr.setDurationSeconds(Long.parseLong(p[4]));

        // charge
        cdr.setChargeAmount(Double.parseDouble(p[5]));

        // timestamp → store in extraFields (simple & safe)
        cdr.addExtraField("timestamp", p[6]);

        return cdr;
    }
    // takes the list of CDRS , Node IP as string named prefix
    public static File writeCdrsToTempFile(List<CDR> cdrs, String prefix) throws IOException {

        File tempFile = File.createTempFile(prefix + "_", ".csv");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {

            // header (optional but recommended)
            bw.write("cdrId,nodeId,nodeType,callingParty,calledParty,callType,duration,charge,timestamp");
            bw.newLine();

            for (CDR cdr : cdrs) {

                String line
                        = cdr.getCdrId() + ","
                        + cdr.getNodeId() + ","
                        + cdr.getNodeType() + ","
                        + cdr.getCallingParty() + ","
                        + cdr.getCalledParty() + ","
                        + cdr.getCallType() + ","
                        + cdr.getDurationSeconds() + ","
                        + cdr.getChargeAmount() + ","
                        + cdr.getExtraField("timestamp");

                bw.write(line);
                bw.newLine();
            }
        }

        LOG.info("Temp file created: " + tempFile.getAbsolutePath());
        return tempFile;
    }
}