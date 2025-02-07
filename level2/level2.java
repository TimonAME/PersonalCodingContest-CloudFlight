package level2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class level2 {
    private static final double HYPERLOOP_SPEED = 250.0; // meters per second
    private static final double DRIVING_SPEED = 15.0; // meters per second
    private static final double WAIT_TIME = 200.0; // seconds

    // Location class to store coordinates
    private static class Location {
        int x, y;

        Location(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // main class to run code
    public void run() {
        System.out.println("Task 2: \n");

        // Process the example input
        //processFile("level2/level2-eg.txt", "level2/out/level2-eg.txt");

        // Process the real levels 1-4

        for (int i = 1; i <= 4; i++) {
            processFile("level2/level2-" + i + ".txt", "level2/out/level2-" + i + ".txt");
        }

    }

    // method to process a single file
    private void processFile(String inputFilePath, String outputFilePath) {
        List<String> content = read(inputFilePath);
        Map<String, Location> locations = new HashMap<>();

        // Parse number of locations
        int numberOfLocations = Integer.parseInt(content.get(0));

        // Parse locations
        for (int i = 1; i <= numberOfLocations; i++) {
            String[] parts = content.get(i).split(" ");
            String name = parts[0];
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            locations.put(name, new Location(x, y));
        }

        // Parse journey start and end
        String[] journey = content.get(numberOfLocations + 1).split(" ");
        String journeyStart = journey[0];
        String journeyEnd = journey[1];

        // Parse hyperloop connection
        String[] hyperloop = content.get(numberOfLocations + 2).split(" ");
        String hyperloopStart = hyperloop[0];
        String hyperloopEnd = hyperloop[1];

        // Calculate total journey time
        int journeyTime = calculateJourneyTime(
                locations.get(journeyStart),
                locations.get(journeyEnd),
                locations.get(hyperloopStart),
                locations.get(hyperloopEnd)
        );

        // Write result
        write(outputFilePath, List.of(String.valueOf(journeyTime)));
    }

    // Calculate distance between two locations
    private double calculateDistance(Location a, Location b) {
        return Math.sqrt(
                Math.pow(b.x - a.x, 2) +
                        Math.pow(b.y - a.y, 2)
        );
    }

    // Calculate hyperloop travel time
    private double calculateHyperloopTime(Location start, Location end) {
        double distance = calculateDistance(start, end);
        return (distance / HYPERLOOP_SPEED) + WAIT_TIME;
    }

    // Calculate driving time
    private double calculateDrivingTime(Location start, Location end) {
        double distance = calculateDistance(start, end);
        return distance / DRIVING_SPEED;
    }

    // Calculate total journey time
    private int calculateJourneyTime(Location journeyStart, Location journeyEnd,
                                     Location hyperloopStart, Location hyperloopEnd) {
        // Calculate distances to both hyperloop stations from journey start
        double distToHyperloopStart = calculateDrivingTime(journeyStart, hyperloopStart);
        double distToHyperloopEnd = calculateDrivingTime(journeyStart, hyperloopEnd);

        // Determine which hyperloop station to start from
        double totalTime;
        if (distToHyperloopStart <= distToHyperloopEnd) {
            // Start -> hyperloopStart -> hyperloopEnd -> End
            totalTime = distToHyperloopStart +
                    calculateHyperloopTime(hyperloopStart, hyperloopEnd) +
                    calculateDrivingTime(hyperloopEnd, journeyEnd);
        } else {
            // Start -> hyperloopEnd -> hyperloopStart -> End
            totalTime = distToHyperloopEnd +
                    calculateHyperloopTime(hyperloopEnd, hyperloopStart) +
                    calculateDrivingTime(hyperloopStart, journeyEnd);
        }

        return (int) Math.round(totalTime);
    }

    // method to read input files
    public List<String> read(String filePath) {
        try {
            // get input file from path
            return Files.readAllLines(Path.of(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // method to print content to a file
    public void write(String filePath, List<String> content) {
        try {
            // write content to file
            Files.write(Paths.get(filePath), content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
