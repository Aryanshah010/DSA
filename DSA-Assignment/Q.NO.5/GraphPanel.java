import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class GraphPanel extends JPanel {
    private Network network;
    private Node selectedNode;
    private Edge selectedEdge;
    private List<Edge> highlightedPath;
    private Point dragStart;
    private boolean isDragging;
    
    // Constants for drawing
    private static final int NODE_RADIUS = 20;
    private static final Color SERVER_COLOR = new Color(100, 149, 237);
    private static final Color CLIENT_COLOR = new Color(144, 238, 144);
    private static final Color SELECTED_COLOR = new Color(255, 165, 0);
    private static final Color EDGE_COLOR = Color.GRAY;
    private static final Color SELECTED_EDGE_COLOR = new Color(0, 128, 0);
    private static final Color PATH_COLOR = new Color(255, 0, 0);
    
    public GraphPanel(Network network) {
        this.network = network;
        this.highlightedPath = new ArrayList<>();
        
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        // Add mouse listeners for interaction
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e);
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw edges
        for (Edge edge : network.getEdges()) {
            drawEdge(g2d, edge, EDGE_COLOR, 1.0f);
        }
        
        // Draw selected edges
        for (Edge edge : network.getSelectedEdges()) {
            drawEdge(g2d, edge, SELECTED_EDGE_COLOR, 2.0f);
        }
        
        // Draw highlighted path
        for (Edge edge : highlightedPath) {
            drawEdge(g2d, edge, PATH_COLOR, 3.0f);
        }
        
        // Draw nodes
        for (Node node : network.getNodes()) {
            drawNode(g2d, node);
        }
    }
    
    private void drawNode(Graphics2D g2d, Node node) {
        Point pos = node.getPosition();
        
        // Choose color based on node type and selection
        Color color;
        if (node == selectedNode) {
            color = SELECTED_COLOR;
        } else if (node.getType() == Node.NodeType.SERVER) {
            color = SERVER_COLOR;
        } else {
            color = CLIENT_COLOR;
        }
        
        // Draw node circle
        g2d.setColor(color);
        g2d.fillOval(pos.x - NODE_RADIUS, pos.y - NODE_RADIUS, 
                     NODE_RADIUS * 2, NODE_RADIUS * 2);
        
        // Draw node border
        g2d.setColor(Color.BLACK);
        g2d.drawOval(pos.x - NODE_RADIUS, pos.y - NODE_RADIUS, 
                     NODE_RADIUS * 2, NODE_RADIUS * 2);
        
        // Draw node name
        g2d.setColor(Color.BLACK);
        FontMetrics fm = g2d.getFontMetrics();
        String text = node.getName();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        g2d.drawString(text, pos.x - textWidth / 2, pos.y + textHeight / 4);
    }
    
    private void drawEdge(Graphics2D g2d, Edge edge, Color color, float strokeWidth) {
        Point p1 = edge.getSource().getPosition();
        Point p2 = edge.getDestination().getPosition();
        
        // Calculate direction vector
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double length = Math.sqrt(dx * dx + dy * dy);
        
        // Normalize
        if (length > 0) {
            dx /= length;
            dy /= length;
        }
        
        // Adjust start and end points to be on the node boundaries
        Point start = new Point(
            (int) (p1.x + dx * NODE_RADIUS),
            (int) (p1.y + dy * NODE_RADIUS)
        );
        
        Point end = new Point(
            (int) (p2.x - dx * NODE_RADIUS),
            (int) (p2.y - dy * NODE_RADIUS)
        );
        
        // Draw the edge
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.drawLine(start.x, start.y, end.x, end.y);
        
        // Draw edge properties (cost and bandwidth)
        String text = "Cost: " + edge.getCost() + ", BW: " + edge.getBandwidth();
        Point midpoint = new Point((start.x + end.x) / 2, (start.y + end.y) / 2);
        
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        
        // Draw with white background for better readability
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRect(midpoint.x - textWidth / 2 - 2, midpoint.y - fm.getHeight() / 2,
                    textWidth + 4, fm.getHeight());
        
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, midpoint.x - textWidth / 2, midpoint.y + fm.getAscent() / 2);
    }
    
    private void handleMousePressed(MouseEvent e) {
        Point p = e.getPoint();
        
        // Check if a node was clicked
        for (Node node : network.getNodes()) {
            Point nodePos = node.getPosition();
            double distance = Math.sqrt(Math.pow(p.x - nodePos.x, 2) + Math.pow(p.y - nodePos.y, 2));
            
            if (distance <= NODE_RADIUS) {
                selectedNode = node;
                dragStart = p;
                isDragging = true;
                repaint();
                return;
            }
        }
        
        // Check if an edge was clicked
        for (Edge edge : network.getEdges()) {
            if (isPointOnEdge(p, edge)) {
                // Toggle edge selection
                if (network.getSelectedEdges().contains(edge)) {
                    network.deselectEdge(edge);
                } else {
                    network.selectEdge(edge);
                }
                
                repaint();
                return;
            }
        }
        
        // If nothing was clicked, deselect
        selectedNode = null;
        repaint();
    }
    
    private void handleMouseDragged(MouseEvent e) {
        if (isDragging && selectedNode != null) {
            Point p = e.getPoint();
            selectedNode.setPosition(p);
            repaint();
        }
    }
    
    private void handleMouseReleased(MouseEvent e) {
        isDragging = false;
    }
    
    private boolean isPointOnEdge(Point p, Edge edge) {
        Point p1 = edge.getSource().getPosition();
        Point p2 = edge.getDestination().getPosition();
        
        // Calculate distance from point to line
        double lineLength = Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
        double distance = Math.abs((p2.y - p1.y) * p.x - (p2.x - p1.x) * p.y + p2.x * p1.y - p2.y * p1.x) / lineLength;
        
        // Check if point is close to the line and within the line segment
        if (distance < 5) {
            // Check if point is within the bounding box of the line
            int minX = Math.min(p1.x, p2.x) - 5;
            int maxX = Math.max(p1.x, p2.x) + 5;
            int minY = Math.min(p1.y, p2.y) - 5;
            int maxY = Math.max(p1.y, p2.y) + 5;
            
            return p.x >= minX && p.x <= maxX && p.y >= minY && p.y <= maxY;
        }
        
        return false;
    }
    
    public void highlightPath(List<Edge> path) {
        this.highlightedPath = path;
        repaint();
    }
    
    public void clearHighlightedPath() {
        this.highlightedPath.clear();
        repaint();
    }
    
    public Node getSelectedNode() {
        return selectedNode;
    }
}