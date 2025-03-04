import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ControlPanel extends JPanel {
    private Network network;
    @SuppressWarnings("unused")
    private final GraphPanel graphPanel;
    private JComboBox<String> algorithmComboBox;
    private JComboBox<Node> sourceNodeComboBox;
    private JComboBox<Node> destNodeComboBox;
    private JTextField costField;
    private JTextField bandwidthField;
    private JLabel totalCostLabel;
    private JLabel avgLatencyLabel;
    private JLabel connectedLabel;
    private JPanel pathPanel;
    private JComboBox<Node> pathSourceComboBox;
    private JComboBox<Node> pathDestComboBox;
    
    public ControlPanel(Network network, GraphPanel graphPanel) {
        this.network = network;
        this.graphPanel = graphPanel;
        
        setPreferredSize(new Dimension(300, 600));
        setBorder(BorderFactory.createTitledBorder("Controls"));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Node creation panel
        JPanel nodePanel = new JPanel();
        nodePanel.setBorder(BorderFactory.createTitledBorder("Add Node"));
        nodePanel.setLayout(new GridLayout(3, 2, 5, 5));
        
        JTextField nodeNameField = new JTextField(10);
        JComboBox<String> nodeTypeComboBox = new JComboBox<>(new String[]{"Server", "Client"});
        
        nodePanel.add(new JLabel("Name:"));
        nodePanel.add(nodeNameField);
        nodePanel.add(new JLabel("Type:"));
        nodePanel.add(nodeTypeComboBox);
        
        JButton addNodeButton = new JButton("Add Node");
        addNodeButton.addActionListener(e -> {
            String name = nodeNameField.getText();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a node name");
                return;
            }
            
            Node.NodeType type = nodeTypeComboBox.getSelectedIndex() == 0 ? 
                                Node.NodeType.SERVER : Node.NodeType.CLIENT;
            
            // Generate random position
            int x = 50 + (int)(Math.random() * (graphPanel.getWidth() - 100));
            int y = 50 + (int)(Math.random() * (graphPanel.getHeight() - 100));
            
            Node node = new Node(network.getNodes().size(), name, new Point(x, y), type);
            network.addNode(node);
            
            // Update combo boxes
            updateNodeComboBoxes();
            
            graphPanel.repaint();
            nodeNameField.setText("");
        });
        
        nodePanel.add(addNodeButton);
        
        // Edge creation panel
        JPanel edgePanel = new JPanel();
        edgePanel.setBorder(BorderFactory.createTitledBorder("Add Edge"));
        edgePanel.setLayout(new GridLayout(5, 2, 5, 5));
        
        sourceNodeComboBox = new JComboBox<>();
        destNodeComboBox = new JComboBox<>();
        costField = new JTextField("10", 10);
        bandwidthField = new JTextField("100", 10);
        
        edgePanel.add(new JLabel("Source:"));
        edgePanel.add(sourceNodeComboBox);
        edgePanel.add(new JLabel("Destination:"));
        edgePanel.add(destNodeComboBox);
        edgePanel.add(new JLabel("Cost:"));
        edgePanel.add(costField);
        edgePanel.add(new JLabel("Bandwidth:"));
        edgePanel.add(bandwidthField);
        
        JButton addEdgeButton = new JButton("Add Edge");
        addEdgeButton.addActionListener(e -> {
            Node source = (Node) sourceNodeComboBox.getSelectedItem();
            Node dest = (Node) destNodeComboBox.getSelectedItem();
            
            if (source == null || dest == null) {
                JOptionPane.showMessageDialog(this, "Please select both source and destination nodes");
                return;
            }
            
            if (source == dest) {
                JOptionPane.showMessageDialog(this, "Source and destination nodes cannot be the same");
                return;
            }
            
            try {
                double cost = Double.parseDouble(costField.getText());
                double bandwidth = Double.parseDouble(bandwidthField.getText());
                
                if (cost <= 0 || bandwidth <= 0) {
                    JOptionPane.showMessageDialog(this, "Cost and bandwidth must be positive");
                    return;
                }
                
                // Check if edge already exists
                boolean edgeExists = false;
                for (Edge edge : network.getEdges()) {
                    if ((edge.getSource() == source && edge.getDestination() == dest) ||
                        (edge.getSource() == dest && edge.getDestination() == source)) {
                        edgeExists = true;
                        break;
                    }
                }
                
                if (edgeExists) {
                    JOptionPane.showMessageDialog(this, "Edge already exists between these nodes");
                    return;
                }
                
                Edge edge = new Edge(source, dest, cost, bandwidth);
                network.addEdge(edge);
                network.selectEdge(edge); // Automatically select the new edge
                graphPanel.repaint();
                updateNetworkMetrics();
                
                // Clear input fields
                costField.setText("10");
                bandwidthField.setText("100");
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for cost and bandwidth");
            }
        });
        
        edgePanel.add(addEdgeButton);
        
        // Optimization panel
        JPanel optimizationPanel = new JPanel();
        optimizationPanel.setBorder(BorderFactory.createTitledBorder("Optimization"));
        optimizationPanel.setLayout(new GridLayout(3, 1, 5, 5));
        
        algorithmComboBox = new JComboBox<>(new String[]{
            "Minimum Spanning Tree (Cost)",
            "Shortest Path (Latency)"
        });
        
        JButton optimizeButton = new JButton("Optimize Network");
        optimizeButton.addActionListener(e -> {
            int selectedAlgorithm = algorithmComboBox.getSelectedIndex();
            
            if (selectedAlgorithm == 0) {
                // Minimum Spanning Tree
                List<Edge> mst = network.findMinimumSpanningTree();
                network.clearSelectedEdges();
                for (Edge edge : mst) {
                    network.selectEdge(edge);
                }
            } else if (selectedAlgorithm == 1) {
                // Shortest Path between all pairs
                optimizeForLatency();
            }
            
            graphPanel.repaint();
            updateNetworkMetrics();
        });
        
        optimizationPanel.add(new JLabel("Algorithm:"));
        optimizationPanel.add(algorithmComboBox);
        optimizationPanel.add(optimizeButton);
        
        // Path calculation panel
        pathPanel = new JPanel();
        pathPanel.setBorder(BorderFactory.createTitledBorder("Path Calculation"));
        pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS));
        
        // Source panel with proper spacing
        JPanel sourcePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sourcePanel.add(new JLabel("Source:"));
        pathSourceComboBox = new JComboBox<>();
        pathSourceComboBox.setPreferredSize(new Dimension(250, 30));
        sourcePanel.add(pathSourceComboBox);
        
        // Destination panel with proper spacing
        JPanel destPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        destPanel.add(new JLabel("Destination:"));
        pathDestComboBox = new JComboBox<>();
        pathDestComboBox.setPreferredSize(new Dimension(250, 30));
        destPanel.add(pathDestComboBox);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton calculatePathButton = new JButton("Calculate Path");
        calculatePathButton.addActionListener(e -> {
            Node source = (Node) pathSourceComboBox.getSelectedItem();
            Node dest = (Node) pathDestComboBox.getSelectedItem();
            
            if (source == null || dest == null) {
                JOptionPane.showMessageDialog(this, "Please select source and destination nodes");
                return;
            }
            
            List<Edge> path = network.findShortestPath(source, dest);
            graphPanel.highlightPath(path);
            
            if (path.isEmpty() && source != dest) {
                JOptionPane.showMessageDialog(this, "No path found between selected nodes");
            }
        });
        
        JButton clearPathButton = new JButton("Clear Path");
        clearPathButton.addActionListener(e -> {
            graphPanel.clearHighlightedPath();
        });
        
        buttonPanel.add(calculatePathButton);
        buttonPanel.add(clearPathButton);
        
        // Add components with spacing
        pathPanel.add(sourcePanel);
        pathPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        pathPanel.add(destPanel);
        pathPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        pathPanel.add(buttonPanel);
        
        // Update node combo boxes when nodes change
        sourceNodeComboBox.addActionListener(e -> updateNodeComboBoxes());
        destNodeComboBox.addActionListener(e -> updateNodeComboBoxes());
        pathSourceComboBox.addActionListener(e -> updateNodeComboBoxes());
        pathDestComboBox.addActionListener(e -> updateNodeComboBoxes());
        
        // Network metrics panel
        JPanel metricsPanel = new JPanel();
        metricsPanel.setBorder(BorderFactory.createTitledBorder("Network Metrics"));
        metricsPanel.setLayout(new GridLayout(3, 1, 5, 5));
        
        totalCostLabel = new JLabel("Total Cost: 0.0");
        avgLatencyLabel = new JLabel("Average Latency: 0.0");
        connectedLabel = new JLabel("Connected: Yes");
        
        metricsPanel.add(totalCostLabel);
        metricsPanel.add(avgLatencyLabel);
        metricsPanel.add(connectedLabel);
        
        // Add all panels to the control panel
        add(nodePanel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(edgePanel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(optimizationPanel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(pathPanel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(metricsPanel);
        
        // Initialize combo boxes
        updateNodeComboBoxes();
        
        // Set up path calculation combo boxes
        for (Node node : network.getNodes()) {
            pathSourceComboBox.addItem(node);
            pathDestComboBox.addItem(node);
        }
    }
    
    private void updateNodeComboBoxes() {
        // Store current selections
        Node currentSource = (Node) sourceNodeComboBox.getSelectedItem();
        Node currentDest = (Node) destNodeComboBox.getSelectedItem();
        Node currentPathSource = (Node) pathSourceComboBox.getSelectedItem();
        Node currentPathDest = (Node) pathDestComboBox.getSelectedItem();
        
        // Clear and repopulate edge combo boxes
        sourceNodeComboBox.removeAllItems();
        destNodeComboBox.removeAllItems();
        pathSourceComboBox.removeAllItems();
        pathDestComboBox.removeAllItems();
        
        // Add nodes to all combo boxes
        for (Node node : network.getNodes()) {
            sourceNodeComboBox.addItem(node);
            destNodeComboBox.addItem(node);
            pathSourceComboBox.addItem(node);
            pathDestComboBox.addItem(node);
        }
        
        // Restore selections if they still exist
        if (currentSource != null && network.getNodes().contains(currentSource)) {
            sourceNodeComboBox.setSelectedItem(currentSource);
        }
        if (currentDest != null && network.getNodes().contains(currentDest)) {
            destNodeComboBox.setSelectedItem(currentDest);
        }
        if (currentPathSource != null && network.getNodes().contains(currentPathSource)) {
            pathSourceComboBox.setSelectedItem(currentPathSource);
        }
        if (currentPathDest != null && network.getNodes().contains(currentPathDest)) {
            pathDestComboBox.setSelectedItem(currentPathDest);
        }
    }
    
    private void updateNetworkMetrics() {
        double totalCost = network.getTotalCost();
        double avgLatency = network.getAverageLatency();
        boolean isConnected = network.isConnected();
        
        totalCostLabel.setText("Total Cost: " + String.format("%.2f", totalCost));
        avgLatencyLabel.setText("Average Latency: " + String.format("%.2f", avgLatency));
        connectedLabel.setText("Connected: " + (isConnected ? "Yes" : "No"));
        
        // Change color based on connectivity
        connectedLabel.setForeground(isConnected ? new Color(0, 128, 0) : Color.RED);
    }
    
    private void optimizeForLatency() {
        // This is a simplified approach that tries to balance cost and latency
        // Start with MST as a base (for cost optimization)
        List<Edge> mst = network.findMinimumSpanningTree();
        network.clearSelectedEdges();
        
        // Add MST edges to selected edges
        for (Edge edge : mst) {
            network.selectEdge(edge);
        }
        
        // Sort remaining edges by bandwidth (descending)
        List<Edge> remainingEdges = new ArrayList<>(network.getEdges());
        remainingEdges.removeAll(mst);
        remainingEdges.sort(Comparator.comparingDouble(Edge::getBandwidth).reversed());
        
        // Add high-bandwidth edges that improve connectivity
        double initialLatency = network.getAverageLatency();
        double initialCost = network.getTotalCost();
        
        for (Edge edge : remainingEdges) {
            network.selectEdge(edge);
            
            double newLatency = network.getAverageLatency();
            double newCost = network.getTotalCost();
            
            // If adding this edge improves latency significantly without increasing cost too much
            if (newLatency < initialLatency * 0.8 && newCost < initialCost * 1.5) {
                // Keep the edge
                initialLatency = newLatency;
                initialCost = newCost;
            } else {
                // Remove the edge
                network.deselectEdge(edge);
            }
        }
    }
}