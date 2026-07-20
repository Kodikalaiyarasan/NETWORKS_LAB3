import java.io.*;
import java.net.*;
import java.nio.file.*;

/**
 * ProxyCacheServer
 * ----------------
 * A TCP-socket based HTTP proxy with local web caching.
 *
 * Flow for every client request:
 *   1. Accept an HTTP GET request from a client (browser or test client).
 *   2. Parse the requested URL to work out a cache file name.
 *   3. Check the local "cache" folder:
 *        - Cache Hit  -> read the cached file and send it straight to the
 *                        client. Print "Cache Hit - Serving file from local cache."
 *        - Cache Miss -> open a NEW tcp connection to the destination web
 *                        server, forward the GET request, receive the
 *                        response, store it in the cache, and forward it
 *                        to the client. Print "Cache Miss - Downloading
 *                        file from web server."
 *   4. All sockets/streams are properly closed.
 *   5. Clients are handled ONE AT A TIME (sequentially) via a simple
 *      accept() loop - no threads.
 *
 * Run:  java ProxyCacheServer 9090
 *
 * The client must send an absolute-URI request line, e.g.:
 *   GET http://localhost:8080/index.html HTTP/1.1
 *   Host: localhost:8080
 *
 * (This is exactly what a browser sends when it is configured to use
 *  this program as its HTTP proxy.)
 */
public class ProxyCacheServer {

    private static final String CACHE_DIR = "cache";

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9090;

        File cacheDir = new File(CACHE_DIR);
        if (!cacheDir.exists()) cacheDir.mkdirs();

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("ProxyCacheServer listening on port " + port);
        System.out.println("Configure your browser's HTTP proxy to 127.0.0.1:" + port);
        System.out.println("Cache folder: ./" + CACHE_DIR);
        System.out.println("----------------------------------------------------");

        // Sequential handling: accept -> handle fully -> close -> accept next
        while (true) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            } catch (IOException e) {
                System.out.println("Proxy error: " + e.getMessage());
            } finally {
                if (clientSocket != null) {
                    try { clientSocket.close(); } catch (IOException ignored) {}
                }
            }
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        InputStream clientIn = clientSocket.getInputStream();
        OutputStream clientOut = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(clientIn));
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.trim().isEmpty()) return;

        System.out.println("\nReceived request: " + requestLine);

        // Read and discard remaining request headers (we only need the request line)
        String headerLine;
        String hostHeader = null;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            if (headerLine.toLowerCase().startsWith("host:")) {
                hostHeader = headerLine.substring(5).trim();
            }
        }

        String[] parts = requestLine.split(" ");
        if (parts.length < 2 || !parts[0].equalsIgnoreCase("GET")) {
            sendSimpleResponse(clientOut, 501, "Not Implemented", "Only GET is supported by this proxy.");
            return;
        }

        String requestedUrl = parts[1];

        // Work out target host, target port and path, and a cache file name
        String host;
        int targetPort;
        String path;

        if (requestedUrl.startsWith("http://")) {
            URL url = new URL(requestedUrl);
            host = url.getHost();
            targetPort = (url.getPort() == -1) ? 80 : url.getPort();
            path = url.getFile().isEmpty() ? "/" : url.getFile();
        } else {
            // Relative request line; rely on the Host header
            if (hostHeader == null) {
                sendSimpleResponse(clientOut, 400, "Bad Request", "Missing Host header.");
                return;
            }
            String[] hostParts = hostHeader.split(":");
            host = hostParts[0];
            targetPort = hostParts.length > 1 ? Integer.parseInt(hostParts[1]) : 80;
            path = requestedUrl;
        }

        String cacheFileName = buildCacheFileName(host, targetPort, path);
        File cacheFile = new File(CACHE_DIR, cacheFileName);

        if (cacheFile.exists()) {
            // ---------------- CACHE HIT ----------------
            byte[] cachedResponse = Files.readAllBytes(cacheFile.toPath());
            clientOut.write(cachedResponse);
            clientOut.flush();
            System.out.println("Cache Hit - Serving file from local cache.");
        } else {
            // ---------------- CACHE MISS ----------------
            System.out.println("Cache Miss - Downloading file from web server.");
            byte[] response = fetchFromWebServer(host, targetPort, path);

            // Save to cache
            try (FileOutputStream cacheOut = new FileOutputStream(cacheFile)) {
                cacheOut.write(response);
            }

            // Forward to client
            clientOut.write(response);
            clientOut.flush();
        }
    }

    /**
     * Opens a brand-new TCP connection to the destination web server,
     * forwards the GET request, and reads back the full raw HTTP response
     * (status line + headers + body) as a byte array.
     */
    private static byte[] fetchFromWebServer(String host, int port, String path) throws IOException {
        try (Socket serverSocket = new Socket(host, port)) {
            OutputStream serverOut = serverSocket.getOutputStream();
            InputStream serverIn = serverSocket.getInputStream();

            String request = "GET " + path + " HTTP/1.1\r\n" +
                    "Host: " + host + ":" + port + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
            serverOut.write(request.getBytes());
            serverOut.flush();

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int bytesRead;
            while ((bytesRead = serverIn.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }
            return buffer.toByteArray();
        }
    }

    private static String buildCacheFileName(String host, int port, String path) {
        String p = path.equals("/") ? "/index.html" : path;
        String safe = (host + "_" + port + p).replaceAll("[^a-zA-Z0-9._-]", "_");
        return safe;
    }

    private static void sendSimpleResponse(OutputStream out, int code, String status, String message) throws IOException {
        byte[] body = message.getBytes();
        String headers = "HTTP/1.1 " + code + " " + status + "\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + body.length + "\r\n" +
                "Connection: close\r\n\r\n";
        out.write(headers.getBytes());
        out.write(body);
        out.flush();
    }
}