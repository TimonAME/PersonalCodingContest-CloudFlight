package level4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class level4 {
    private static final double HYPERLOOP_SPEED = 250.0;
    private static final double DRIVING_SPEED = 15.0;
    private static final double WAIT_TIME = 200.0;

    private static class Location {
        int x, y;
        String name;

        Location(String name, int x, int y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }
    }

    private static class Journey {
        String start, end;
        int currentTime;

        Journey(String start, String end, int currentTime) {
            this.start = start;
            this.end = end;
            this.currentTime = currentTime;
        }
    }

    private static class HyperloopConnection {
        String start, end;
        int benefitCount;

        HyperloopConnection(String start, String end, int benefitCount) {
            this.start = start;
            this.end = end;
            this.benefitCount = benefitCount;
        }
    }

    public void run() {
        System.out.println("Task 4: \n");
        // Process the example input
        //processFile("level4/level4-eg.txt", "level4/out/level4-eg.txt");

        // Process the real levels 1-4

        for (int i = 1; i <= 4; i++) {
            processFile("level4/level4-" + i + ".txt", "level4/out/level4-" + i + ".txt");
        }


    }

    private void processFile(String inputFilePath, String outputFilePath) {
        List<String> content = read(inputFilePath);
        Map<String, Location> locations = new HashMap<>();
        List<Journey> journeys = new ArrayList<>();

        // Parse number of locations
        int numberOfLocations = Integer.parseInt(content.get(0));

        // Parse locations
        for (int i = 1; i <= numberOfLocations; i++) {
            String[] parts = content.get(i).split(" ");
            String name = parts[0];
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            locations.put(name, new Location(name, x, y));
        }

        // Parse number of journeys
        int currentLine = numberOfLocations + 1;
        int numberOfJourneys = Integer.parseInt(content.get(currentLine));

        // Parse journeys
        for (int i = 0; i < numberOfJourneys; i++) {
            String[] parts = content.get(currentLine + 1 + i).split(" ");
            journeys.add(new Journey(
                    parts[0],
                    parts[1],
                    Integer.parseInt(parts[2])
            ));
        }

        // Parse target number of benefiting journeys
        int targetBenefits = Integer.parseInt(content.get(currentLine + numberOfJourneys + 1));

        // Find optimal hyperloop connection
        HyperloopConnection bestConnection = findOptimalConnection(locations, journeys, targetBenefits);

        // Write result
        write(outputFilePath, List.of(bestConnection.start + " " + bestConnection.end));
    }

    private HyperloopConnection findOptimalConnection(Map<String, Location> locations,
                                                      List<Journey> journeys,
                                                      int targetBenefits) {
        HyperloopConnection bestConnection = null;
        int maxBenefits = 0;

        // Try all possible location pairs
        List<String> locationNames = new ArrayList<>(locations.keySet());
        for (int i = 0; i < locationNames.size(); i++) {
            for (int j = i + 1; j < locationNames.size(); j++) {
                String start = locationNames.get(i);
                String end = locationNames.get(j);

                int benefits = countFasterJourneys(journeys, locations,
                        locations.get(start), locations.get(end));

                if (benefits >= targetBenefits && benefits > maxBenefits) {
                    maxBenefits = benefits;
                    bestConnection = new HyperloopConnection(start, end, benefits);

                    // If we found a solution that just meets the target, we can return it
                    if (benefits == targetBenefits) {
                        return bestConnection;
                    }
                }
            }
        }

        return bestConnection;
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
