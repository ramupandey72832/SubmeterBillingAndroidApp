package com.application.baselibrary.utils.file;

import java.io.*;
import java.util.*;

public class FileSplitter {

    private static final int BUFFER_SIZE = 8192; // fixed buffer size

    public static List<File> splitFile(File file, long maxPartSize) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }
        if (maxPartSize <= 0) {
            throw new IllegalArgumentException("maxPartSize must be > 0");
        }

        List<File> parts = new ArrayList<>();
        int partNumber = 1;

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                File tempPart = new File(file.getParent(),
                        String.format("%s.part%d.%d", file.getName(), partNumber, System.currentTimeMillis()));

                try (FileOutputStream fos = new FileOutputStream(tempPart)) {
                    long bytesWritten = 0;

                    do {
                        fos.write(buffer, 0, bytesRead);
                        bytesWritten += bytesRead;

                        if (bytesWritten >= maxPartSize) break;
                    } while ((bytesRead = fis.read(buffer)) != -1);

                    parts.add(tempPart);
                    partNumber++;
                }
            }
        }
        return parts;
    }
}
