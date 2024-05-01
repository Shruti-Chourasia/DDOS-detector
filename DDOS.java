here is the code for DDOS attack detector in java

  import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class DDoSDetector {
    private static final int THRESHOLD = 100; // Maximum allowed requests per IP
    private static final long INTERVAL = 60000; // Time interval for resetting counts (in milliseconds)
    private HashMap<String, Integer> requestCountMap = new HashMap<>();
    private boolean scanning = false;

    public void recordRequest(String ipAddress) {
        int count = requestCountMap.getOrDefault(ipAddress, 0);
        requestCountMap.put(ipAddress, count + 1);

        if (count + 1 > THRESHOLD) {
            System.out.println("Possible DDoS attack detected from IP: " + ipAddress);
            // You can add further actions here, like banning the IP address or logging the event.
        }
    }

    public DDoSDetector() {
        Timer timer = new Timer();
        timer.schedule(new ResetCountsTask(), INTERVAL, INTERVAL);

        try {
            startHttpServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startHttpServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/request", new RequestHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080");
        startScanning();
    }

    private void startScanning() {
        scanning = true;
        System.out.print("Scanning");
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int count = 0;

            @Override
            public void run() {
                count = (count + 1) % 4;
                String[] loaders = {"|", "/", "-", "\\"};
                System.out.print("\b" + loaders[count]);
            }
        }, 0, 250);
    }

    private void stopScanning() {
        scanning = false;
        System.out.println("\nScan complete");
    }

    private class RequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String ipAddress = exchange.getRemoteAddress().getAddress().getHostAddress();
            recordRequest(ipAddress);

            String response = "Request recorded from IP: " + ipAddress;
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

  private class ResetCountsTask extends TimerTask {
    @Override
    public void run() {
        System.out.println("Scanning for DDoS attacks...");
        if (requestCountMap.isEmpty()) {
            System.out.println("No DDoS attacks detected.");
        } else {
            requestCountMap.clear();
            System.out.println("Request counts reset.");
        }
    }
}

    public static void main(String[] args) {
        new DDoSDetector();
    }
}
