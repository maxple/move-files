package com.maxple;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Main {
    public static void main(final String[] args) throws IOException {
        final Path inDir = Paths.get("data/in");
        final Path outDir = Paths.get("data/out");
        final Pattern pattern = Pattern.compile("^([0-9]{4})([0-9]{2})");

        try (final Stream<Path> paths = Files.walk(inDir)) {
            paths.filter(Files::isRegularFile).forEach(inFile -> {
                try {
                    final String fileName = inFile.getFileName().toString();

                    final Matcher matcher = pattern.matcher(fileName);
                    if (!matcher.find()) {
                        System.out.printf("File name '%s' does not match regexp, SKIPPED%n", fileName);
                        return;
                    }

                    final String year = matcher.group(1);
                    final String month = matcher.group(2);

                    final Path outYearMonthDir = outDir.resolve(year).resolve(month);

                    Files.createDirectories(outYearMonthDir);

                    final Path outFile = outYearMonthDir.resolve(fileName);

                    if (Files.exists(outFile)) {
                        final long inSize = Files.size(inFile);
                        final long outSize = Files.size(outFile);

                        if (inSize != outSize) {
                            final Path newOutFile = outYearMonthDir.resolve(fileName + "_" + System.currentTimeMillis());
                            Files.move(inFile, newOutFile);
                            System.out.printf("File '%s' with 'size=%,d bytes' exists, file with 'size=%,d bytes' MOVED to '%s'%n", fileName, outSize, inSize, newOutFile);
                        } else {
                            System.out.printf("File '%s' with 'size=%,d bytes' ALREADY EXISTS%n", fileName, inSize);
                        }
                    } else {
                        Files.move(inFile, outFile);
                        System.out.printf("File '%s' MOVED to '%s'%n", fileName, outFile);
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
