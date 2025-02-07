package level3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class level3 {
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

    // Journey class to store journey details
    private static class Journey {
        String start, end;
        int currentTime;

        Journey(String start, String end, int currentTime) {
            this.start = start;
            this.end = end;
            this.currentTime = currentTime;
        }
    }

    // main class to run code
    public void run() {
        System.out.println("Task 3: \n");

        // Process the example input
        //processFile("level3/level3-eg.txt", "level3/out/level3-eg.txt");

        // Process the real levels 1-4

        for (int i = 1; i <= 4; i++) {
            processFile("level3/level3-" + i + ".txt", "level3/out/level3-" + i + ".txt");
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

        // Parse number of journeys
        int currentLine = numberOfLocations + 1;
        int numberOfJourneys = Integer.parseInt(content.get(currentLine));

        // Parse journeys
        List<Journey> journeys = new ArrayList<>();
        for (int i = 0; i < numberOfJourneys; i++) {
            String[] parts = content.get(currentLine + 1 + i).split(" ");
            journeys.add(new Journey(
                    parts[0],
                    parts[1],
                    Integer.parseInt(parts[2])
            ));
        }

        // Parse hyperloop connection
        String[] hyperloop = content.get(currentLine + numberOfJourneys + 1).split(" ");
        String hyperloopStart = hyperloop[0];
        String hyperloopEnd = hyperloop[1];

        // Count faster journeys
        int fasterJourneys = countFasterJourneys(
                journeys,
                locations,
                locations.get(hyperloopStart),
                locations.get(hyperloopEnd)
        );

        // Write result
        write(outputFilePath, List.of(String.valueOf(fasterJourneys)));
    }

    private int countFasterJourneys(List<Journey> journeys,
                                    Map<String, Location> locations,
                                    Location hyperloopStart,
                                    Location hyperloopEnd) {
        int count = 0;
        for (Journey journey : journeys) {
            Location start = locations.get(journey.start);
            Location end = locations.get(journey.end);

            int hyperloopTime = calculateJourneyTime(start, end, hyperloopStart, hyperloopEnd);

            if (hyperloopTime < journey.currentTime) {
                count++;
            }
        }
        return count;
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
            totalTime = distToHyperloopStart +
                    calculateHyperloopTime(hyperloopStart, hyperloopEnd) +
                    calculateDrivingTime(hyperloopEnd, journeyEnd);
        } else {
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
