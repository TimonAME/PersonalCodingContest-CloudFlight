package level6;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class level6 {
    private static final double HYPERLOOP_SPEED = 250.0;
    private static final double DRIVING_SPEED = 15.0;
    private static final double WAIT_TIME = 200.0;
    private static final int MAX_STOPS = 100;
    private static final int MAX_ATTEMPTS = 100000;

    private static class Location {
        String name;
        int x, y;

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

    private static class Route {
        List<Location> stops;
        double length;
        int benefitCount;

        Route(List<Location> stops) {
            this.stops = stops;
            this.length = calculateRouteLength(stops);
        }
    }

    public void run() {
        System.out.println("Task 6: \n");
        processFile("level6/level6-eg.txt", "level6/out/level6-eg.txt");

        // Process the real levels 1-4

        for (int i = 1; i <= 4; i++) {
            processFile("level6/level6-" + i + ".txt", "level6/out/level6-" + i + ".txt");
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
            locations.put(parts[0], new Location(parts[0],
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])));
        }

        // Parse journeys
        int currentLine = numberOfLocations + 1;
        int numberOfJourneys = Integer.parseInt(content.get(currentLine));
        for (int i = 0; i < numberOfJourneys; i++) {
            String[] parts = content.get(currentLine + 1 + i).split(" ");
            journeys.add(new Journey(parts[0], parts[1],
                    Integer.parseInt(parts[2])));
        }

        // Parse constraints
        int targetBenefits = Integer.parseInt(content.get(currentLine + numberOfJourneys + 1));
        double maxLength = Double.parseDouble(content.get(currentLine + numberOfJourneys + 2));

        // Find optimal route
        Route bestRoute = findOptimalRoute(locations, journeys, targetBenefits, maxLength);

        // Format output
        String output = formatRouteOutput(bestRoute);
        write(outputFilePath, List.of(output));
    }

    private Route findOptimalRoute(Map<String, Location> locations,
                                   List<Journey> journeys,
                                   int targetBenefits,
                                   double maxLength) {
        Route bestRoute = null;
        int maxBenefits = 0;
        Random random = new Random();
        List<Location> locationList = new ArrayList<>(locations.values());

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            // Generate random route
            List<Location> stops = new ArrayList<>();
            Set<String> usedLocations = new HashSet<>();

            // Start with a random location
            Location start = locationList.get(random.nextInt(locationList.size()));
            stops.add(start);
            usedLocations.add(start.name);

            // Add more stops randomly
            while (stops.size() < MAX_STOPS) {
                Location next = locationList.get(random.nextInt(locationList.size()));
                if (!usedLocations.contains(next.name)) {
                    Route currentRoute = new Route(new ArrayList<>(stops));
                    if (currentRoute.length + calculateDistance(stops.get(stops.size()-1), next) > maxLength) {
                        break;
                    }
                    stops.add(next);
                    usedLocations.add(next.name);
                }
            }

            Route route = new Route(stops);
            if (route.length <= maxLength) {
                int benefits = countBenefits(route, journeys, locations);
                if (benefits >= targetBenefits && (bestRoute == null || benefits > maxBenefits)) {
                    bestRoute = route;
                    maxBenefits = benefits;
                    if (benefits == targetBenefits) {
                        break;  // Found a solution that meets the target exactly
                    }
                }
            }
        }

        return bestRoute;
    }

    private int countBenefits(Route route, List<Journey> journeys,
                              Map<String, Location> locations) {
        int count = 0;
        for (Journey journey : journeys) {
            Location start = locations.get(journey.start);
            Location end = locations.get(journey.end);

            int hyperloopTime = calculateJourneyTime(start, end, route.stops);
            if (hyperloopTime < journey.currentTime) {
                count++;
            }
        }
        return count;
    }

    private static double calculateRouteLength(List<Location> stops) {
        double length = 0;
        for (int i = 0; i < stops.size() - 1; i++) {
            length += calculateDistance(stops.get(i), stops.get(i + 1));
        }
        return length;
    }

    private int calculateJourneyTime(Location start, Location end, List<Location> stops) {
        Location closestToStart = findClosestStop(start, stops);
        Location closestToEnd = findClosestStop(end, stops);

        double drivingTimeToHyperloop = calculateDrivingTime(start, closestToStart);
        double drivingTimeFromHyperloop = calculateDrivingTime(closestToEnd, end);
        double hyperloopTime = calculateHyperloopRouteTime(closestToStart, closestToEnd, stops);

        return (int) Math.round(drivingTimeToHyperloop + hyperloopTime + drivingTimeFromHyperloop);
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

    private static double calculateDistance(Location a, Location b) {
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

    private String formatRouteOutput(Route route) {
        StringBuilder sb = new StringBuilder();
        sb.append(route.stops.size());
        for (Location stop : route.stops) {
            sb.append(" ").append(stop.name);
        }
        return sb.toString();
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
