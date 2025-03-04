import java.util.*;

public class Network {
    private List<Node> nodes;
    private List<Edge> edges;
    private List<Edge> selectedEdges; // Edges in the current topology
    
    public Network() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        selectedEdges = new ArrayList<>();
    }
    
    // Node operations
    public void addNode(Node node) {
        nodes.add(node);
    }
    
    public void removeNode(Node node) {
        // Remove all edges connected to this node
        edges.removeIf(edge -> edge.getSource() == node || edge.getDestination() == node);
        selectedEdges.removeIf(edge -> edge.getSource() == node || edge.getDestination() == node);
        nodes.remove(node);
    }
    
    // Edge operations
    public void addEdge(Edge edge) {
        edges.add(edge);
    }
    
    public void removeEdge(Edge edge) {
        edges.remove(edge);
        selectedEdges.remove(edge);
    }
    
    // Topology operations
    public void selectEdge(Edge edge) {
        if (!selectedEdges.contains(edge)) {
            selectedEdges.add(edge);
        }
    }
    
    public void deselectEdge(Edge edge) {
        selectedEdges.remove(edge);
    }
    
    public void clearSelectedEdges() {
        selectedEdges.clear();
    }
    
    // Getters
    public List<Node> getNodes() {
        return nodes;
    }
    
    public List<Edge> getEdges() {
        return edges;
    }
    
    public List<Edge> getSelectedEdges() {
        return selectedEdges;
    }
    
    // Network metrics
    public double getTotalCost() {
        return selectedEdges.stream().mapToDouble(Edge::getCost).sum();
    }
    
    public double getAverageLatency() {
        if (selectedEdges.isEmpty()) return 0;
        return selectedEdges.stream().mapToDouble(e -> 1.0 / e.getBandwidth()).average().orElse(0);
    }
    
    // Check if the selected topology is connected
    public boolean isConnected() {
        if (nodes.isEmpty()) return true;
        if (selectedEdges.isEmpty()) return nodes.size() <= 1;
        
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        
        // Start BFS from the first node
        queue.add(nodes.get(0));
        visited.add(nodes.get(0));
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            
            // Find all adjacent nodes in the selected topology
            for (Edge edge : selectedEdges) {
                Node next = null;
                if (edge.getSource() == current) {
                    next = edge.getDestination();
                } else if (edge.getDestination() == current) {
                    next = edge.getSource();
                }
                
                if (next != null && !visited.contains(next)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        
        // If all nodes are visited, the graph is connected
        return visited.size() == nodes.size();
    }
    
    // Find minimum spanning tree (Kruskal's algorithm)
    public List<Edge> findMinimumSpanningTree() {
        List<Edge> mst = new ArrayList<>();
        
        if (nodes.isEmpty() || edges.isEmpty()) {
            return mst;
        }
        
        // Sort edges by cost
        List<Edge> sortedEdges = new ArrayList<>(edges);
        sortedEdges.sort(Comparator.comparingDouble(Edge::getCost));
        
        // Initialize disjoint set
        Map<Node, Node> parent = new HashMap<>();
        for (Node node : nodes) {
            parent.put(node, node);
        }
        
        // Kruskal's algorithm
        for (Edge edge : sortedEdges) {
            Node sourceRoot = findSet(parent, edge.getSource());
            Node destRoot = findSet(parent, edge.getDestination());
            
            if (sourceRoot != destRoot) {
                mst.add(edge);
                union(parent, sourceRoot, destRoot);
            }
            
            // If MST is complete, break
            if (mst.size() == nodes.size() - 1) {
                break;
            }
        }
        
        return mst;
    }
    
    // Find the representative of a set
    private Node findSet(Map<Node, Node> parent, Node node) {
        if (parent.get(node) != node) {
            parent.put(node, findSet(parent, parent.get(node)));
        }
        return parent.get(node);
    }
    
    // Union two sets
    private void union(Map<Node, Node> parent, Node x, Node y) {
        parent.put(x, y);
    }
    
    // Find shortest path (Dijkstra's algorithm)
    public List<Edge> findShortestPath(Node source, Node destination) {
        if (source == destination) {
            return new ArrayList<>();
        }
        
        // Initialize distances
        Map<Node, Double> distance = new HashMap<>();
        Map<Node, Node> previous = new HashMap<>();
        Map<Node, Edge> edgeMap = new HashMap<>();
        
        for (Node node : nodes) {
            distance.put(node, Double.MAX_VALUE);
            previous.put(node, null);
        }
        distance.put(source, 0.0);
        
        // Priority queue for Dijkstra's algorithm
        PriorityQueue<Node> queue = new PriorityQueue<>(
            Comparator.comparingDouble(distance::get)
        );
        queue.add(source);
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            
            if (current == destination) {
                break;
            }
            
            if (distance.get(current) == Double.MAX_VALUE) {
                break;
            }
            
            // Find all adjacent nodes in the selected topology
            for (Edge edge : selectedEdges) {
                Node next = null;
                if (edge.getSource() == current) {
                    next = edge.getDestination();
                } else if (edge.getDestination() == current) {
                    next = edge.getSource();
                }
                
                if (next != null) {
                    // Use inverse of bandwidth as weight (higher bandwidth = lower latency)
                    double weight = 1.0 / edge.getBandwidth();
                    double newDistance = distance.get(current) + weight;
                    
                    if (newDistance < distance.get(next)) {
                        distance.put(next, newDistance);
                        previous.put(next, current);
                        edgeMap.put(next, edge);
                        queue.add(next);
                    }
                }
            }
        }
        
        // Reconstruct path
        List<Edge> path = new ArrayList<>();
        Node current = destination;
        
        while (previous.get(current) != null) {
            path.add(0, edgeMap.get(current));
            current = previous.get(current);
        }
        
        return path;
    }
}