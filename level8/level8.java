package level8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class level8 {
    private static final double HYPERLOOP_SPEED = 250.0;
    private static final double DRIVING_SPEED = 15.0;
    private static final double WAIT_TIME = 200.0;
    private static final double HUB_CHANGE_TIME = 300.0;
    private static final int MAX_STOPS = 100;
    private static int MAX_ATTEMPTS;
    private static final int MAX_ROUTES = 80;

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

        Route(List<Location> stops) {
            this.stops = stops;
        }

        double getLength() {
            double length = 0;
            for (int i = 0; i < stops.size() - 1; i++) {
                length += calculateDistance(stops.get(i), stops.get(i + 1));
            }
            return length;
        }
    }

    private static class HyperloopSystem {
        List<Route> routes;
        double totalLength;
        int benefitCount;

        HyperloopSystem(List<Route> routes) {
            this.routes = routes;
            this.totalLength = calculateTotalLength();
        }

        private double calculateTotalLength() {
            return routes.stream()
                    .mapToDouble(Route::getLength)
                    .sum();
        }
    }

    public void run() {
        System.out.println("Task 8: \n");
        processFile("level8/level8-2.txt", "level8/out/level8-2.txt");

        // Process the real levels 1-4
        /*
        for (int i = 1; i <= 4; i++) {
            processFile("level8/level8-" + i + ".txt", "level8/out/level8-" + i + ".txt");
        }

         */
    }

    private void processFile(String inputFilePath, String outputFilePath) {
        List<String> content = read(inputFilePath);
        Map<String, Location> locations = new HashMap<>();
        List<Journey> journeys = new ArrayList<>();

        // Parse input similarly to Level 7
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

        // Parse hub, target benefits, and max length
        String hubName = content.get(currentLine + numberOfJourneys + 1);
        Location hub = locations.get(hubName);
        int targetBenefits = Integer.parseInt(content.get(currentLine + numberOfJourneys + 2));
        double maxLength = Double.parseDouble(content.get(currentLine + numberOfJourneys + 3));

        // Set MAX_ATTEMPTS based on problem size
        MAX_ATTEMPTS = Math.max(10000, numberOfLocations * numberOfJourneys * 100);

        // Find optimal system
        HyperloopSystem system = findOptimalSystem(locations, journeys, hub,
                targetBenefits, maxLength);

        // Handle case where no valid solution is found
        if (system == null) {
            System.out.println("No valid system found - generating fallback system");
            system = generateFallbackSystem(locations, hub, maxLength);
        }

        // Write result
        write(outputFilePath, List.of(formatSystemOutput(system)));
    }

    private HyperloopSystem generateFallbackSystem(Map<String, Location> locations,
                                                   Location hub,
                                                   double maxLength) {
        // Create a simple two-route system as fallback
        List<Route> routes = new ArrayList<>();
        List<Location> locationList = new ArrayList<>(locations.values());

        // First route: hub + closest location
        List<Location> route1Stops = new ArrayList<>();
        route1Stops.add(hub);
        Location closest = findClosestLocation(hub, locationList);
        if (closest != null) {
            route1Stops.add(closest);
        }
        routes.add(new Route(route1Stops));

        // Second route: hub + second closest location
        List<Location> route2Stops = new ArrayList<>();
        route2Stops.add(hub);
        Location secondClosest = findSecondClosestLocation(hub, locationList, closest);
        if (secondClosest != null) {
            route2Stops.add(secondClosest);
        }
        routes.add(new Route(route2Stops));

        return new HyperloopSystem(routes);
    }

    private Location findClosestLocation(Location hub, List<Location> locations) {
        Location closest = null;
        double minDistance = Double.MAX_VALUE;

        for (Location loc : locations) {
            if (!loc.name.equals(hub.name)) {
                double distance = calculateDistance(hub, loc);
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = loc;
                }
            }
        }
        return closest;
    }

    private Location findSecondClosestLocation(Location hub,
                                               List<Location> locations,
                                               Location closest) {
        Location secondClosest = null;
        double minDistance = Double.MAX_VALUE;

        for (Location loc : locations) {
            if (!loc.name.equals(hub.name) &&
                    (closest == null || !loc.name.equals(closest.name))) {
                double distance = calculateDistance(hub, loc);
                if (distance < minDistance) {
                    minDistance = distance;
                    secondClosest = loc;
                }
            }
        }
        return secondClosest;
    }

    private HyperloopSystem findOptimalSystem(Map<String, Location> locations,
                                              List<Journey> journeys,
                                              Location hub,
                                              int targetBenefits,
                                              double maxLength) {
        // OPTIMIZATION TRACKING
        System.out.println("Starting optimization with " + MAX_ATTEMPTS + " attempts");
        int progressInterval = 10000;  // Report progress every 10%

        HyperloopSystem bestSystem = null;
        int maxBenefits = 0;
        Random random = new Random();

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            // Progress tracking
            if (attempt % progressInterval == 0) {
                System.out.println("Optimization progress: " +
                        (attempt * 100 / MAX_ATTEMPTS) + "%");
            }

            List<Route> routes = new ArrayList<>();
            double totalLength = 0;

            // Always try to create at least two routes
            for (int r = 0; r < 2; r++) {
                Route route = generateRoute(locations, hub, maxLength - totalLength);
                if (route != null) {
                    totalLength += route.getLength();
                    routes.add(route);
                }
            }

            // Optionally add more routes if there's room
            while (routes.size() < MAX_ROUTES && random.nextDouble() < 0.5) {
                Route route = generateRoute(locations, hub, maxLength - totalLength);
                if (route != null) {
                    double newLength = totalLength + route.getLength();
                    if (newLength <= maxLength) {
                        totalLength = newLength;
                        routes.add(route);
                    }
                }
            }

            if (!routes.isEmpty()) {
                HyperloopSystem system = new HyperloopSystem(routes);
                if (system.totalLength <= maxLength) {
                    // Calculate benefits for this system
                    int benefits = countBenefits(system, journeys, locations, hub);

                     if (benefits >= targetBenefits) {
                         System.out.println("Found solution with exact or more benefits!");
                         break;  // Exit the loop
                     }

                    // Track if this is the best system so far
                    /*
                    if (benefits >= targetBenefits && benefits > maxBenefits) {
                        System.out.println("New best system found!");
                        System.out.println("Benefits: " + benefits +
                                ", Total length: " + system.totalLength);
                        bestSystem = system;
                        maxBenefits = benefits;

                        // Break condition
                        if (benefits > targetBenefits) {
                            System.out.println("Found solution with exact target benefits!");
                            break;  // Exit the loop
                        }
                    }
                    */
                }
            }
        }

        return bestSystem;
    }

    private Route generateRoute(Map<String, Location> locations,
                                Location hub,
                                double maxLength) {
        List<Location> stops = new ArrayList<>();
        Set<String> usedLocations = new HashSet<>();
        Random random = new Random();
        List<Location> locationList = new ArrayList<>(locations.values());

        // Add 1-3 stops before hub
        int stopsBeforeHub = 1 + random.nextInt(3);
        for (int i = 0; i < stopsBeforeHub; i++) {
            Location loc = getRandomUnusedLocation(locationList, usedLocations, hub);
            if (loc != null) {
                stops.add(loc);
                usedLocations.add(loc.name);
            }
        }

        // Add hub
        stops.add(hub);

        // Add 1-3 stops after hub
        int stopsAfterHub = 1 + random.nextInt(3);
        for (int i = 0; i < stopsAfterHub; i++) {
            Location loc = getRandomUnusedLocation(locationList, usedLocations, hub);
            if (loc != null) {
                stops.add(loc);
                usedLocations.add(loc.name);
            }
        }

        Route route = new Route(stops);
        return route.getLength() <= maxLength ? route : null;
    }

    private Location getRandomUnusedLocation(List<Location> locations,
                                             Set<String> usedLocations,
                                             Location hub) {
        List<Location> available = new ArrayList<>();
        for (Location loc : locations) {
            if (!usedLocations.contains(loc.name) && !loc.name.equals(hub.name)) {
                available.add(loc);
            }
        }
        if (available.isEmpty()) return null;
        return available.get(new Random().nextInt(available.size()));
    }

    private int countBenefits(HyperloopSystem system,
                              List<Journey> journeys,
                              Map<String, Location> locations,
                              Location hub) {
        int count = 0;
        for (Journey journey : journeys) {
            Location start = locations.get(journey.start);
            Location end = locations.get(journey.end);

            int hyperloopTime = calculateJourneyTime(start, end, hub, system.routes);
            if (hyperloopTime < journey.currentTime) {
                count++;
            }
        }
        return count;
    }

    private String formatSystemOutput(HyperloopSystem system) {
        StringBuilder sb = new StringBuilder();
        sb.append(system.routes.size());

        for (Route route : system.routes) {
            sb.append(" ").append(route.stops.size());
            for (Location stop : route.stops) {
                sb.append(" ").append(stop.name);
            }
        }

        return sb.toString();
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
