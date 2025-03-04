public class ClosestPair {
    public static int[] findClosestPair(int[] x_coords, int[] y_coords) {
        int n = x_coords.length;
        int minDist = Integer.MAX_VALUE;
        int minI = -1, minJ = -1;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                // Calculate Manhattan distance
                int dist = Math.abs(x_coords[i] - x_coords[j]) + Math.abs(y_coords[i] - y_coords[j]);

                // Update minimum distance and lexicographically smallest pair
                if (dist < minDist || (dist == minDist && (i < minI || (i == minI && j < minJ)))) {
                    minDist = dist;
                    minI = i;
                    minJ = j;
                }
            }
        }

        return new int[]{minI, minJ};
    }

    public static void main(String[] args) {
        int[] x_coords = {1, 2, 3, 2, 4};
        int[] y_coords = {2, 3, 1, 2, 3};

        int[] result = findClosestPair(x_coords, y_coords);
        System.out.println("[" + result[0] + ", " + result[1] + "]"); 
    }
}
