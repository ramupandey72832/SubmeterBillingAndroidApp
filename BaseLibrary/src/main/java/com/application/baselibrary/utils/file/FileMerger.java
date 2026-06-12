package com.application.baselibrary.utils.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * FileMerger
 * ----------
 * A utility class to merge multiple file parts back into a single file.
 */
public class FileMerger {

    private static final int BUFFER_SIZE = 8192; // 8KB buffer

    /**
     * Merges a list of file parts into a single output file.
     *
     * @param parts       The list of file parts (in correct order).
     * @param outputFile  The file to write the merged content into.
     * @throws IOException If an I/O error occurs.
     */
    public static void mergeFiles(List<File> parts, File outputFile) throws IOException {
        if (parts == null || parts.isEmpty()) {
            throw new IllegalArgumentException("Parts list cannot be null or empty");
        }
        if (outputFile == null) {
            throw new IllegalArgumentException("Output file cannot be null");
        }

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[BUFFER_SIZE];

            for (File part : parts) {
                if (!part.exists()) {
                    throw new IOException("Missing part file: " + part.getAbsolutePath());
                }

                try (FileInputStream fis = new FileInputStream(part)) {
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
    }
}
// none used Class