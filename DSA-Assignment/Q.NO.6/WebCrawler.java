import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler {
    // Thread-safe queue for URLs to crawl
    private final ConcurrentLinkedQueue<String> urlQueue;
    // Set to track visited URLs (thread-safe)
    private final Set<String> visitedUrls;
    // Thread pool
    private final ExecutorService executorService;
    // Store crawled data
    private final Map<String, String> crawledData;
    // Maximum URLs to crawl
    private final int maxUrls;
    
    public WebCrawler(int threadPoolSize, int maxUrls) {
        this.urlQueue = new ConcurrentLinkedQueue<>();
        this.visitedUrls = Collections.synchronizedSet(new HashSet<>());
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.crawledData = Collections.synchronizedMap(new HashMap<>());
        this.maxUrls = maxUrls;
    }
    
    // Start crawling from a seed URL
    public void startCrawling(String seedUrl) {
        addUrl(seedUrl);
        crawl();
    }
    
    // Add URL to queue if not visited
    private void addUrl(String url) {
        if (visitedUrls.size() < maxUrls && !visitedUrls.contains(url)) {
            urlQueue.offer(url);
            visitedUrls.add(url);
        }
    }
    
    // Main crawling logic
    private void crawl() {
        while (!urlQueue.isEmpty() && visitedUrls.size() < maxUrls) {
            String url = urlQueue.poll();
            if (url != null) {
                executorService.submit(new CrawlTask(url));
            }
        }
        
        // Shutdown executor when done
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // Task to crawl a single URL
    private class CrawlTask implements Runnable {
        private final String url;
        
        public CrawlTask(String url) {
            this.url = url;
        }
        
        @Override
        public void run() {
            try {
                String content = fetchPage(url);
                processContent(url, content);
                extractLinks(content);
            } catch (IOException e) {
                System.err.println("Error crawling " + url + ": " + e.getMessage());
            }
        }
        
        // Fetch webpage content
        private String fetchPage(String urlString) throws IOException {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "WebCrawler/1.0");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                return content.toString();
            } finally {
                connection.disconnect();
            }
        }
        
        // Process the fetched content
        private void processContent(String url, String content) {
            // Here you can add logic to extract specific data
            // For this example, we'll just store the raw content
            crawledData.put(url, content.substring(0, Math.min(content.length(), 200)));
            System.out.println("Crawled: " + url + " (URLs visited: " + visitedUrls.size() + ")");
        }
        
        // Extract links from content (simple implementation)
        private void extractLinks(String content) {
            // Simple link extraction using regex
            String linkPattern = "href=\"(http[s]?://[^\"]+)\"";
            content.lines()
                .flatMap(line -> Arrays.stream(line.split("\\s+")))
                .filter(word -> word.contains("href="))
                .map(word -> {
                    int start = word.indexOf("http");
                    int end = word.indexOf("\"", start);
                    return end > start ? word.substring(start, end) : null;
                })
                .filter(Objects::nonNull)
                .forEach(WebCrawler.this::addUrl);
        }
    }
    
    // Get crawled data
    public Map<String, String> getCrawledData() {
        return new HashMap<>(crawledData);
    }
    
    // Main method to test the crawler
    public static void main(String[] args) {
        WebCrawler crawler = new WebCrawler(5, 10); // 5 threads, max 10 URLs
        crawler.startCrawling("https://www.w3schools.com/");
        
        // Wait for crawling to complete
        try {
            Thread.sleep(10000); // Wait 10 seconds
            Map<String, String> results = crawler.getCrawledData();
            System.out.println("\nCrawling completed. Results:");
            results.forEach((url, content) -> 
                System.out.println("URL: " + url + "\nContent preview: " + content + "\n"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}