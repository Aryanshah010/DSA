import java.awt.*;

public class Node {
    private int id;
    private String name;
    private Point position;
    private NodeType type;
    public enum NodeType {
        SERVER, CLIENT
    }
    public Node(int id, String name, Point position, NodeType type) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.type = type;
    }
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public Point getPosition() {
        return position;
    }
    
    public void setPosition(Point position) {
        this.position = position;
    }
    
    public NodeType getType() {
        return type;
    }
    
    public void setType(NodeType type) {
        this.type = type;
    }
    @Override
    public String toString() {
        return name;
    }
}