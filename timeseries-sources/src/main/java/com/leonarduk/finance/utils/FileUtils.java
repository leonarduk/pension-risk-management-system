package com.leonarduk.finance.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(FileUtils.class.getName());

    public static void writeFile(final String fileName,
                                 final StringBuilder sb) {
        /**
         * Writing CSV file
         */
        BufferedWriter writer = null;
        try {

            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(sb.toString());
            FileUtils.LOGGER.info("Saved to " + fileName);
        } catch (final IOException ioe) {
            FileUtils.LOGGER.error("Unable to write CSV file", ioe);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (final IOException ioe) {
            }
        }
    }
}
