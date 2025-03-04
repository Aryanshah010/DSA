import javax.swing.*;
import java.awt.*;


public class NetworkOptimizer extends JFrame {
    private GraphPanel graphPanel;
    private ControlPanel controlPanel;
    private StatusPanel statusPanel;
    private Network network;
    
    public NetworkOptimizer() {
        super("Network Topology Optimizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLayout(new BorderLayout());
        
        // Initialize network model
        network = new Network();
        
        // Create panels
        graphPanel = new GraphPanel(network);
        controlPanel = new ControlPanel(network, graphPanel);
        statusPanel = new StatusPanel(network);
        
        // Add panels to frame
        add(graphPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NetworkOptimizer());
    }
}