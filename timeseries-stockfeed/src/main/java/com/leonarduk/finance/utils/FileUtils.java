package com.leonarduk.finance.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

@Slf4j
public class FileUtils {

    public static void writeFile(final String fileName,
                                 final StringBuilder sb) {
        /**
         * Writing CSV file
         */
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

            writer.write(sb.toString());
            log.info("Saved to {}", fileName);
        } catch (final IOException ioe) {
            log.error("Unable to write CSV file", ioe);
        }
    }
}
