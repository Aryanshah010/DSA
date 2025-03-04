import java.util.*;

public class PackageDelivery {
    public static void main(String[] args) {
        // Test case 1
        int[] packages1 = {1, 0, 0, 0, 0, 1};
        int[][] roads1 = {{0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}};
        System.out.println("Test case 1 - Minimum roads to traverse: " + 
                          minRoadsToCollectPackages(packages1, roads1));
        
        // Test case 2
        int[] packages2 = {0, 0, 0, 1, 1, 0, 0, 1};
        int[][] roads2 = {{0, 1}, {0, 2}, {1, 3}, {1, 4}, {2, 5}, {5, 6}, {5, 7}};
        System.out.println("Test case 2 - Minimum roads to traverse: " + 
                          minRoadsToCollectPackages(packages2, roads2));
    }
    
    public static int minRoadsToCollectPackages(int[] packages, int[][] roads) {
        int n = packages.length;
        
        // Build adjacency list representation of the graph
        List<List<Integer>> graph = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }
        for (int[] road : roads) {
            graph.get(road[0]).add(road[1]);
            graph.get(road[1]).add(road[0]);
        }
        
        // Find all package locations
        List<Integer> packageLocations = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (packages[i] == 1) {
                packageLocations.add(i);
            }
        }
        
        // Calculate distances between all pairs of nodes
        int[][] distances = new int[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(distances[i], Integer.MAX_VALUE);
            calculateDistances(i, graph, distances);
        }
        
        // Try each location as starting point
        int minRoads = Integer.MAX_VALUE;
        
        for (int start = 0; start < n; start++) {
            int roadsNeeded = findMinRoads(start, packageLocations, graph, distances);
            minRoads = Math.min(minRoads, roadsNeeded);
        }
        
        return minRoads == Integer.MAX_VALUE ? -1 : minRoads;
    }
    
    private static void calculateDistances(int start, List<List<Integer>> graph, int[][] distances) {
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(start);
        distances[start][start] = 0;
        
        while (!queue.isEmpty()) {
            int current = queue.poll();
            
            for (int next : graph.get(current)) {
                if (distances[start][next] == Integer.MAX_VALUE) {
                    distances[start][next] = distances[start][current] + 1;
                    queue.offer(next);
                }
            }
        }
    }
    
    private static int findMinRoads(int start, List<Integer> packageLocations, 
                                  List<List<Integer>> graph, int[][] distances) {
        class State {
            int pos;
            Set<Integer> collected;
            int moves;
            
            State(int pos, Set<Integer> collected, int moves) {
                this.pos = pos;
                this.collected = new HashSet<>(collected);
                this.moves = moves;
            }
            
            String getKey() {
                List<Integer> sortedCollected = new ArrayList<>(collected);
                Collections.sort(sortedCollected);
                return pos + ":" + sortedCollected.toString();
            }
        }
        
        // Calculate initial packages that can be collected from start
        Set<Integer> initialCollected = new HashSet<>();
        for (int packageLoc : packageLocations) {
            if (distances[start][packageLoc] <= 2) {
                initialCollected.add(packageLoc);
            }
        }
        
        // If all packages can be collected from start, no need to move
        if (initialCollected.size() == packageLocations.size()) {
            return 0;
        }
        
        // BFS to find minimum path
        Queue<State> queue = new LinkedList<>();
        queue.offer(new State(start, initialCollected, 0));
        Set<String> visited = new HashSet<>();
        
        while (!queue.isEmpty()) {
            State current = queue.poll();
            String stateKey = current.getKey();
            
            if (visited.contains(stateKey)) {
                continue;
            }
            visited.add(stateKey);
            
            // If all packages are collected, return to start
            if (current.collected.size() == packageLocations.size()) {
                return current.moves + distances[current.pos][start];
            }
            
            // Try moving to each adjacent location
            for (int next : graph.get(current.pos)) {
                Set<Integer> newCollected = new HashSet<>(current.collected);
                
                // Check if we can collect any new packages from this position
                for (int packageLoc : packageLocations) {
                    if (!newCollected.contains(packageLoc) && 
                        distances[next][packageLoc] <= 2) {
                        newCollected.add(packageLoc);
                    }
                }
                
                queue.offer(new State(next, newCollected, current.moves + 1));
            }
        }
        
        return Integer.MAX_VALUE;
    }
}