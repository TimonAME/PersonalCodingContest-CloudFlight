package level7;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class level7 {
    private static final double HYPERLOOP_SPEED = 250.0;
    private static final double DRIVING_SPEED = 15.0;
    private static final double WAIT_TIME = 200.0;
    private static final double HUB_CHANGE_TIME = 300.0;

    private static class Location {
        String name;
        int x, y;

        Location(String name, int x, int y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }
    }

    private static class Route {
        List<Location> stops;

        Route(List<Location> stops) {
            this.stops = stops;
        }
    }

    public void run() {
        System.out.println("Task 7: \n");
        processFile("level7/level7-eg.txt", "level7/out/level7-eg.txt");

        // Process the real levels 1-4
        for (int i = 1; i <= 4; i++) {
            processFile("level7/level7-" + i + ".txt", "level7/out/level7-" + i + ".txt");
        }
    }

    private void processFile(String inputFilePath, String outputFilePath) {
        List<String> content = read(inputFilePath);
        Map<String, Location> locations = new HashMap<>();
        List<Route> routes = new ArrayList<>();

        // Parse number of locations
        int numberOfLocations = Integer.parseInt(content.get(0));

        // Parse locations
        for (int i = 1; i <= numberOfLocations; i++) {
            String[] parts = content.get(i).split(" ");
            locations.put(parts[0], new Location(parts[0],
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])));
        }

        // Parse journey start and end
        String[] journey = content.get(numberOfLocations + 1).split(" ");
        String journeyStart = journey[0];
        String journeyEnd = journey[1];

        // Parse hub location
        String hubName = content.get(numberOfLocations + 2);
        Location hub = locations.get(hubName);

        // Parse number of routes
        int numberOfRoutes = Integer.parseInt(content.get(numberOfLocations + 3));

        // Parse routes
        for (int i = 0; i < numberOfRoutes; i++) {
            String[] routeLine = content.get(numberOfLocations + 4 + i).split(" ");
            int stopCount = Integer.parseInt(routeLine[0]);
            List<Location> routeStops = new ArrayList<>();
            for (int j = 0; j < stopCount; j++) {
                routeStops.add(locations.get(routeLine[j + 1]));
            }
            routes.add(new Route(routeStops));
        }

        // Calculate journey time
        int journeyTime = calculateJourneyTime(
                locations.get(journeyStart),
                locations.get(journeyEnd),
                hub,
                routes
        );

        // Write result
        write(outputFilePath, List.of(String.valueOf(journeyTime)));
    }

    private int calculateJourneyTime(Location start, Location end,
                                     Location hub, List<Route> routes) {
        // Find closest stops and their routes
        RouteAndStop startRouteAndStop = findClosestRouteAndStop(start, routes);
        RouteAndStop endRouteAndStop = findClosestRouteAndStop(end, routes);

        // Calculate driving times
        double drivingTimeToStart = calculateDrivingTime(start, startRouteAndStop.stop);
        double drivingTimeFromEnd = calculateDrivingTime(endRouteAndStop.stop, end);

        // Calculate hyperloop times
        double hyperloopTime;
        if (startRouteAndStop.route == endRouteAndStop.route) {
            // Same route - no need to change at hub
            hyperloopTime = calculateHyperloopRouteTime(
                    startRouteAndStop.stop,
                    endRouteAndStop.stop,
                    startRouteAndStop.route.stops
            );
        } else {
            // Different routes - need to change at hub
            double timeToHub = calculateHyperloopRouteTime(
                    startRouteAndStop.stop,
                    hub,
                    startRouteAndStop.route.stops
            );
            double timeFromHub = calculateHyperloopRouteTime(
                    hub,
                    endRouteAndStop.stop,
                    endRouteAndStop.route.stops
            );

            // Add hub change time if neither start nor end is the hub
            double changeTime = 0;
            if (!startRouteAndStop.stop.name.equals(hub.name) &&
                    !endRouteAndStop.stop.name.equals(hub.name)) {
                changeTime = HUB_CHANGE_TIME;
            }

            hyperloopTime = timeToHub + changeTime + timeFromHub;
        }

        return (int) Math.round(drivingTimeToStart + hyperloopTime + drivingTimeFromEnd);
    }

    private static class RouteAndStop {
        Route route;
        Location stop;
        double distance;

        RouteAndStop(Route route, Location stop, double distance) {
            this.route = route;
            this.stop = stop;
            this.distance = distance;
        }
    }

    private RouteAndStop findClosestRouteAndStop(Location location, List<Route> routes) {
        RouteAndStop closest = null;
        double minDistance = Double.MAX_VALUE;

        for (Route route : routes) {
            for (Location stop : route.stops) {
                double distance = calculateDistance(location, stop);
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = new RouteAndStop(route, stop, distance);
                }
            }
        }
        return closest;
    }

    private double calculateHyperloopRouteTime(Location start, Location end,
                                               List<Location> stops) {
        int startIndex = -1;
        int endIndex = -1;
        for (int i = 0; i < stops.size(); i++) {
            if (stops.get(i).name.equals(start.name)) startIndex = i;
            if (stops.get(i).name.equals(end.name)) endIndex = i;
        }

        double totalTime = 0;
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
