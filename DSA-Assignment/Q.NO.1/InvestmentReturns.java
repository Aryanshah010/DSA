import java.util.PriorityQueue;

public class InvestmentReturns {
    
    public static int kthSmallestProduct(int[] returns1, int[] returns2, int k) {
        PriorityQueue<int[]> minHeap = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
        
        for (int i = 0; i < returns1.length; i++) {
            minHeap.offer(new int[]{returns1[i] * returns2[0], i, 0});
        }
        
        while (k-- > 1) {
            int[] current = minHeap.poll();
            int i = current[1], j = current[2];
            
            if (j + 1 < returns2.length) {
                minHeap.offer(new int[]{returns1[i] * returns2[j + 1], i, j + 1});
            }
        }
        
        return minHeap.poll()[0];  
    }
    
    public static void main(String[] args) {
        System.out.println(kthSmallestProduct(new int[]{2, 5}, new int[]{3, 4}, 2));  
        System.out.println(kthSmallestProduct(new int[]{-4, -2, 0, 3}, new int[]{2, 4}, 6));  
    }
}
