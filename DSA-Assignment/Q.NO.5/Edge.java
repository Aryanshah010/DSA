public class Edge {
    private Node source;
    private Node destination;
    private double cost;
    private double bandwidth;
    
    public Edge(Node source, Node destination, double cost, double bandwidth) {
        this.source = source;
        this.destination = destination;
        this.cost = cost;
        this.bandwidth = bandwidth;
    }
    
    // Getters and setters
    public Node getSource() {
        return source;
    }
    
    public Node getDestination() {
        return destination;
    }
    
    public double getCost() {
        return cost;
    }
    
    public void setCost(double cost) {
        this.cost = cost;
    }
    
    public double getBandwidth() {
        return bandwidth;
    }
    
    public void setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
    }
    
    @Override
    public String toString() {
        return source.getName() + " -> " + destination.getName();
    }
}