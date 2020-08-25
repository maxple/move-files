package com.maxple;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;

public class Main {
    private static final boolean MOVE = true;

    private static final Path IN_DIR = Paths.get("d:/_PHOTO_VIDEO_/Camera");
    private static final Path OUT_DIR = Paths.get("d:/_PHOTO_VIDEO_");

    private static final String ACTION_WORD = MOVE ? "MOVED" : "COPIED";
    private static final String SKIPPED_WORD = "SKIPPED";
    private static final Pattern PATTERN = Pattern.compile("^([0-9]{4})([0-9]{2})");

    private static int skipped = 0;
    private static int processed = 0;

    public static void main(final String[] args) throws IOException {

        try (final Stream<Path> paths = Files.walk(IN_DIR)) {
            paths.filter(Files::isRegularFile).forEach(inFile -> {
                try {
                    final String fileName = inFile.getFileName().toString();

                    final int dotIndex = fileName.lastIndexOf(".");
                    if (dotIndex < 0) {
                        System.out.printf("File name '%s' has not dot, %s%n", fileName, SKIPPED_WORD);
                        skipped++;
                        return;
                    }

                    final String baseName = fileName.substring(0, dotIndex);
                    final String extension = fileName.substring(dotIndex + 1);

                    final Matcher matcher = PATTERN.matcher(fileName);
                    if (!matcher.find()) {
                        System.out.printf("File name '%s' does not match regexp, %s%n", fileName, SKIPPED_WORD);
                        skipped++;
                        return;
                    }

                    final String year = matcher.group(1);
                    final String month = matcher.group(2);

                    final Path yearMonthOutDir = OUT_DIR.resolve(year).resolve(year + "_" + month);

                    final Path targetDir = "mp4".equals(extension) ? yearMonthOutDir.resolve("video") : yearMonthOutDir;

                    Files.createDirectories(targetDir);

                    final Path outFile = targetDir.resolve(fileName);

                    if (Files.exists(outFile)) {
                        final long inSize = Files.size(inFile);
                        final long outSize = Files.size(outFile);

                        if (inSize == outSize) {
                            System.out.printf("File '%s' with same size ALREADY EXISTS in %s, %s%n", fileName, targetDir, SKIPPED_WORD);
                            skipped++;
                        } else {
                            final Path newOutFile = getNewOutFile(targetDir, baseName, extension);
                            moveOrCopy(inFile, newOutFile);
                            System.out.printf("File '%s' with 'size=%,d bytes' exists, file with 'size=%,d bytes' %s to '%s'%n", fileName, outSize, inSize, ACTION_WORD, newOutFile);
                            processed++;
                        }
                    } else {
                        moveOrCopy(inFile, outFile);
                        System.out.printf("File '%s' %s to '%s'%n", fileName, ACTION_WORD, outFile);
                        processed++;
                    }
                } catch (final Exception e) {
                    System.out.printf("Error: %s, %s%n", e.getMessage(), SKIPPED_WORD);
                    skipped++;
                }
            });
        }

        System.out.printf("%s %d files%n", SKIPPED_WORD, skipped);
        System.out.printf("%s %d files%n", ACTION_WORD, processed);
    }

    private static void moveOrCopy(final Path inFile, final Path newOutFile) throws IOException {
        if (MOVE) {
            Files.move(inFile, newOutFile);
        } else {
            Files.copy(inFile, newOutFile);
        }
    }

    private static Path getNewOutFile(final Path dir, final String baseName, final String extension) {
        Path path = dir.resolve(format("%s.%s", baseName, extension));

        if (!Files.exists(path)) {
            return path;
        }

        for (int i = 2; i < Integer.MAX_VALUE; i++) {
            path = dir.resolve(format("%s (%d).%s", baseName, i, extension));
            if (!Files.exists(path)) {
                return path;
            }
        }

        throw new IllegalStateException();
    }
}
