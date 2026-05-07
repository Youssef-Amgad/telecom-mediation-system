package com.mycompany.msc;

import java.io.File;
import java.io.FileOutputStream;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class CdrGenerator {

    static Random rand = new Random();

    public static void main(String[] args) throws Exception {
        String dirPath = "/app/cdr/";
        
        String[] prefixPool = {"010", "011", "012", "015"};
        
                    // create /app/cdr if not exists
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs(); 
            }

        while (true) {
            


            int cdr_id = rand.nextInt(100000);
           String msisdn = prefixPool[rand.nextInt(prefixPool.length)]+ (1000000 + rand.nextInt(9000000));
            String dial_b =  prefixPool[rand.nextInt(prefixPool.length)]+ (1000000 + rand.nextInt(9000000));
            
            int service_id = rand.nextInt(3);
            int duration = rand.nextInt(300);
            double fees = Math.round(rand.nextDouble() * 1000) / 100.0;

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            // Temporary: write as text (for testing)
            String cdr
                    = cdr_id + ","
                    + msisdn + ","
                    + dial_b + ","
                    + service_id + ","
                    + duration + ","
                    + fees + ","
                    + timestamp;

            //String fileName = "cdr_" + cdr_id + ".asn1"; // for .ans format
            String fileName = "cdr_" + cdr_id + ".csv";  // for testing 
            //fos.write(cdrSeq.getEncoded());

            try (FileOutputStream fos = new FileOutputStream(dirPath+fileName)) {
                //fos.write(cdrSeq.getEncoded());
                fos.write(cdr.getBytes());
            }

            // System.out.println("Generated: " + fileName);
            try {
               // Thread.sleep(200 + rand.nextInt(800));
               Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
