package level1.level1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class level1 {
    private static final double SPEED = 250.0; // meters per second
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
        System.out.println("Task 1: \n");

        // Process the example input
        //processFile("level1\\level1\\level1-eg.txt", "level1\\level1\\out\\level1-eg.txt");

        // Process the real levels 1-4

        for (int i = 1; i <= 4; i++) {
            processFile("level1\\level1\\level1-" + i + ".txt", "level1\\level1\\out\\level1-" + i + ".txt");
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

        // Parse hyperloop connection
        String[] connection = content.get(numberOfLocations + 1).split(" ");
        String start = connection[0];
        String end = connection[1];

        // Calculate travel time
        int travelTime = calculateTravelTime(locations.get(start), locations.get(end));

        // Write result
        write(outputFilePath, List.of(String.valueOf(travelTime)));
    }

    // Calculate travel time between two locations
    private int calculateTravelTime(Location start, Location end) {
        // Calculate distance using Pythagorean theorem
        double distance = Math.sqrt(
                Math.pow(end.x - start.x, 2) +
                        Math.pow(end.y - start.y, 2)
        );

        // Calculate total time (travel time + wait time)
        double time = (distance / SPEED) + WAIT_TIME;

        // Round to nearest integer
        return (int) Math.round(time);
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
