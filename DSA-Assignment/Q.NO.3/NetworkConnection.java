
import java.util.*;

public class NetworkConnection {
    static class Edge {
        int u, v, cost;
        Edge(int u, int v, int cost) {
            this.u = u;
            this.v = v;
            this.cost = cost;
        }
    }

    static class UnionFind {
        int[] parent, rank;

        UnionFind(int n) {
            parent = new int[n + 1];
            rank = new int[n + 1];
            for (int i = 0; i <= n; i++) parent[i] = i;
        }

        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]); // Path compression
            }
            return parent[x];
        }

        boolean union(int x, int y) {
            int rootX = find(x), rootY = find(y);
            if (rootX == rootY) return false; // Already connected
            if (rank[rootX] > rank[rootY]) parent[rootY] = rootX;
            else if (rank[rootX] < rank[rootY]) parent[rootX] = rootY;
            else {
                parent[rootY] = rootX;
                rank[rootX]++;
            }
            return true;
        }
    }

    public static int minCostToConnect(int n, int[] modules, int[][] connections) {
        List<Edge> edges = new ArrayList<>();

        // Step 1: Add module installation as virtual edges (connecting node `0`)
        for (int i = 0; i < n; i++) {
            edges.add(new Edge(0, i + 1, modules[i])); // Virtual edge from 0 to each node
        }

        // Step 2: Add given connections
        for (int[] conn : connections) {
            edges.add(new Edge(conn[0], conn[1], conn[2]));
        }

        // Step 3: Sort edges by cost (for Kruskal’s Algorithm)
        edges.sort(Comparator.comparingInt(e -> e.cost));

        // Step 4: Apply Kruskal's MST algorithm
        UnionFind uf = new UnionFind(n);
        int totalCost = 0, edgesUsed = 0;

        for (Edge edge : edges) {
            if (uf.union(edge.u, edge.v)) {
                totalCost += edge.cost;
                edgesUsed++;
                if (edgesUsed == n) break; // Stop when we connect all nodes
            }
        }

        return totalCost;
    }

    public static void main(String[] args) {
        int n = 3;
        int[] modules = {1, 2, 2};
        int[][] connections = {{1, 2, 1}, {2, 3, 1}};

        System.out.println(minCostToConnect(n, modules, connections)); 
    }
}
