import javax.swing.*;
import java.awt.*;

public class StatusPanel extends JPanel {
    private Network network;
    private JLabel statusLabel;
    
    public StatusPanel(Network network) {
        this.network = network;
        
        setPreferredSize(new Dimension(800, 30));
        setBorder(BorderFactory.createLoweredBevelBorder());
        setLayout(new BorderLayout());
        
        statusLabel = new JLabel("Ready");
        add(statusLabel, BorderLayout.WEST);
        
        // Add a timer to update status periodically
        Timer timer = new Timer(1000, e -> updateStatus());
        timer.start();
    }
    
    private void updateStatus() {
        int nodeCount = network.getNodes().size();
        int edgeCount = network.getEdges().size();
        int selectedEdgeCount = network.getSelectedEdges().size();
        
        StringBuilder status = new StringBuilder();
        status.append("Nodes: ").append(nodeCount);
        status.append(" | Edges: ").append(edgeCount);
        status.append(" | Selected: ").append(selectedEdgeCount);
        
        if (selectedEdgeCount > 0) {
            status.append(" | Total Cost: ").append(String.format("%.2f", network.getTotalCost()));
            status.append(" | Avg Latency: ").append(String.format("%.2f", network.getAverageLatency()));
            status.append(" | Connected: ").append(network.isConnected() ? "Yes" : "No");
        }
        
        statusLabel.setText(status.toString());
    }
}