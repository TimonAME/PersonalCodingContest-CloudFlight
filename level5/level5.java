package level5;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class level5 {
    private static final double HYPERLOOP_SPEED = 250.0;
    private static final double DRIVING_SPEED = 15.0;
    private static final double WAIT_TIME = 200.0;

    private static class Location {
        String name;
        int x, y;

        Location(String name, int x, int y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }
    }

    public void run() {
        System.out.println("Task 5: \n");
        // Process the example input
        //processFile("level5/level5-eg.txt", "level5/out/level5-eg.txt");

        // Process the real levels 1-4

        for (int i = 1; i <= 4; i++) {
            processFile("level5/level5-" + i + ".txt", "level5/out/level5-" + i + ".txt");
        }


    }

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
            locations.put(name, new Location(name, x, y));
        }

        // Parse journey start and end
        String[] journey = content.get(numberOfLocations + 1).split(" ");
        String journeyStart = journey[0];
        String journeyEnd = journey[1];

        // Parse hyperloop route
        String[] routeLine = content.get(numberOfLocations + 2).split(" ");
        int numberOfStops = Integer.parseInt(routeLine[0]);
        List<Location> hyperloopStops = new ArrayList<>();
        for (int i = 0; i < numberOfStops; i++) {
            hyperloopStops.add(locations.get(routeLine[i + 1]));
        }

        // Calculate journey time
        int journeyTime = calculateJourneyTime(
                locations.get(journeyStart),
                locations.get(journeyEnd),
                hyperloopStops
        );

        // Write result
        write(outputFilePath, List.of(String.valueOf(journeyTime)));
    }

    private int calculateJourneyTime(Location start, Location end, List<Location> hyperloopStops) {
        // Find closest hyperloop stops to start and end locations
        Location closestToStart = findClosestStop(start, hyperloopStops);
        Location closestToEnd = findClosestStop(end, hyperloopStops);

        // Calculate driving times to/from hyperloop stops
        double drivingTimeToHyperloop = calculateDrivingTime(start, closestToStart);
        double drivingTimeFromHyperloop = calculateDrivingTime(closestToEnd, end);

        // Calculate hyperloop travel time between the stops
        double hyperloopTime = calculateHyperloopRouteTime(
                closestToStart,
                closestToEnd,
                hyperloopStops
        );

        // Calculate total journey time
        double totalTime = drivingTimeToHyperloop + hyperloopTime + drivingTimeFromHyperloop;

        return (int) Math.round(totalTime);
    }

    private Location findClosestStop(Location location, List<Location> stops) {
        Location closest = stops.get(0);
        double minDistance = calculateDistance(location, closest);

        for (Location stop : stops) {
            double distance = calculateDistance(location, stop);
            if (distance < minDistance) {
                minDistance = distance;
                closest = stop;
            }
        }
        return closest;
    }

    private double calculateHyperloopRouteTime(Location start, Location end, List<Location> stops) {
        // Find indices of start and end stops in the route
        int startIndex = -1;
        int endIndex = -1;
        for (int i = 0; i < stops.size(); i++) {
            if (stops.get(i).name.equals(start.name)) startIndex = i;
            if (stops.get(i).name.equals(end.name)) endIndex = i;
        }

        double totalTime = 0;
        // Calculate time for each segment between the stops
        int step = startIndex < endIndex ? 1 : -1;
        for (int i = startIndex; i != endIndex; i += step) {
            Location current = stops.get(i);
            Location next = stops.get(i + step);
            totalTime += calculateHyperloopTime(current, next);
        }

        return totalTime;
    }

    private double calculateDistance(Location a, Location b) {
        return Math.sqrt(
                Math.pow(b.x - a.x, 2) +
                        Math.pow(b.y - a.y, 2)
        );
    }

    private double calculateHyperloopTime(Location start, Location end) {
        double distance = calculateDistance(start, end);
        return (distance / HYPERLOOP_SPEED) + WAIT_TIME;
    }

    private double calculateDrivingTime(Location start, Location end) {
        double distance = calculateDistance(start, end);
        return distance / DRIVING_SPEED;
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
