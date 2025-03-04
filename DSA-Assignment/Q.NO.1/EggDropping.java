public class EggDropping {
    public static int minMoves(int k, int n) {
        int[][] dp = new int[k + 1][n + 1];
        int m = 0;
        
        while (dp[k][m] < n) {
            m++;
            for (int i = 1; i <= k; i++) {
                dp[i][m] = dp[i - 1][m - 1] + dp[i][m - 1] + 1;
            }
        }
        return m;
    }
    
    public static void main(String[] args) {
        
        System.out.println(minMoves(1, 2));  
        System.out.println(minMoves(2, 6));  
        System.out.println(minMoves(3, 14)); 
    }
}
