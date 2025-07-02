package filter;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileFilterUtility {
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");
    private static final Pattern FLOAT_PATTERN = Pattern.compile("^-?\\d*\\.\\d+([eE][-+]?\\d+)?$|^-?\\d+([eE][-+]?\\d+)$");

    private static final List<String> integers = new ArrayList<>();
    private static final List<String> floats = new ArrayList<>();
    private static final List<String> strings = new ArrayList<>();

    private static boolean appendMode = false;
    private static boolean shortStats = false;
    private static boolean fullStats = false;
    private static String outputPath = ".";
    private static String prefix = "";

    public static void main(String[] args) {
        List<String> inputFiles = parseArgs(args);

        for (String filePath : inputFiles) {
            processFile(filePath);
        }

        writeOutput("integers.txt", integers);
        writeOutput("floats.txt", floats);
        writeOutput("strings.txt", strings);

        printStats("Integers", integers, true);
        printStats("Floats", floats, true);
        printStats("Strings", strings, false);
    }

    private static List<String> parseArgs(String[] args) {
        List<String> inputFiles = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-a":
                    appendMode = true;
                    break;
                case "-s":
                    shortStats = true;
                    break;
                case "-f":
                    fullStats = true;
                    break;
                case "-o":
                    if (++i < args.length) outputPath = args[i];
                    break;
                case "-p":
                    if (++i < args.length) prefix = args[i];
                    break;
                default:
                    inputFiles.add(args[i]);
                    break;
            }
        }
        return inputFiles;
    }

    private static void processFile(String filePath) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (INTEGER_PATTERN.matcher(line).matches()) {
                    integers.add(line);
                } else if (FLOAT_PATTERN.matcher(line).matches()) {
                    floats.add(line);
                } else {
                    strings.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath + " — " + e.getMessage());
        }
    }

    private static void writeOutput(String type, List<String> data) {
        if (data.isEmpty()) return;
        String filename = outputPath + File.separator + prefix + type;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, appendMode))) {
            for (String entry : data) {
                writer.write(entry);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + filename + " — " + e.getMessage());
        }
    }

    private static void printStats(String label, List<String> data, boolean isNumeric) {
        if (data.isEmpty()) return;
        System.out.println("== " + label + " ==");
        System.out.println("Count: " + data.size());
        if (fullStats && isNumeric) {
            List<Double> numbers = data.stream().map(Double::parseDouble).collect(Collectors.toList());
            double min = Collections.min(numbers);
            double max = Collections.max(numbers);
            double sum = numbers.stream().mapToDouble(Double::doubleValue).sum();
            double avg = sum / numbers.size();
            System.out.printf("Min: %.6f\nMax: %.6f\nSum: %.6f\nAvg: %.6f\n", min, max, sum, avg);
        } else if (fullStats) {
            int minLen = data.stream().mapToInt(String::length).min().orElse(0);
            int maxLen = data.stream().mapToInt(String::length).max().orElse(0);
            System.out.println("Min Length: " + minLen);
            System.out.println("Max Length: " + maxLen);
        }
    }
}

