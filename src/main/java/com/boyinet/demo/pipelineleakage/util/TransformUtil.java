package com.boyinet.demo.pipelineleakage.util;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author lengchunyun
 */
public class TransformUtil {

    public static void transform(String src, String desc) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(src)));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(desc)));
        String buffer;
        while ((buffer = bufferedReader.readLine()) != null) {
            String[] split = buffer.split("--->");
            if (split.length == 2) {
                String data = split[1].replaceAll(" ", "");
                String hex = data.substring(47);
                bufferedWriter.write(split[0] +
                        BigDecimal.valueOf(Integer.parseInt(hex, 16))
                                .divide(BigDecimal.valueOf(1000), 2, RoundingMode.DOWN));
            }
        }
        bufferedWriter.flush();
        bufferedWriter.close();
        bufferedReader.close();
    }
}
