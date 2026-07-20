import java.io.*;
import java.net.*;
import java.nio.file.*;

/**
 * OriginServer
 * -------------
 * A simple HTTP "destination web server" that runs entirely on the local
 * machine (127.0.0.1). It serves files that live in the "www" folder
 * next to this class.
 *
 * The Proxy/Cache server (ProxyCacheServer.java) forwards cache-miss
 * requests to this server, exactly like it would forward them to a
 * real remote web server.
 *
 * Run:  java OriginServer 8080
 */
public class OriginServer {

    private static final String WWW_DIR = "www";

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;

        // Make sure the www folder and a couple of sample files exist
        File wwwDir = new File(WWW_DIR);
        if (!wwwDir.exists()) wwwDir.mkdirs();
        createSampleFileIfMissing("index.html",
                "<html><body><h1>Welcome</h1><p>This is index.html served by OriginServer.</p></body></html>");
        createSampleFileIfMissing("about.html",
                "<html><body><h1>About</h1><p>This is about.html served by OriginServer.</p></body></html>");

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("OriginServer listening on port " + port + " (serving files from ./" + WWW_DIR + ")");

        // Handle requests one at a time (sequentially)
        while (true) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            } catch (IOException e) {
                System.out.println("OriginServer error: " + e.getMessage());
            } finally {
                if (clientSocket != null) {
                    try { clientSocket.close(); } catch (IOException ignored) {}
                }
            }
        }
    }

    private static void handleRequest(Socket socket) throws IOException {
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) return;

        System.out.println("OriginServer received: " + requestLine);

        // Consume (and ignore) the remaining header lines
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            // headers ignored for this simple server
        }

        // Parse "GET /path HTTP/1.1"
        String[] parts = requestLine.split(" ");
        if (parts.length < 2 || !parts[0].equalsIgnoreCase("GET")) {
            sendResponse(out, 400, "Bad Request", "text/plain", "Only GET is supported".getBytes());
            return;
        }

        String path = parts[1];
        if (path.equals("/")) path = "/index.html";
        // Strip any leading slash to build a local file path
        String fileName = path.startsWith("/") ? path.substring(1) : path;

        File file = new File(WWW_DIR, fileName);
        if (file.exists() && file.isFile()) {
            byte[] content = Files.readAllBytes(file.toPath());
            String contentType = guessContentType(fileName);
            sendResponse(out, 200, "OK", contentType, content);
        } else {
            byte[] body = ("<html><body><h1>404 Not Found</h1><p>" + fileName + "</p></body></html>").getBytes();
            sendResponse(out, 404, "Not Found", "text/html", body);
        }
    }

    private static void sendResponse(OutputStream out, int code, String status,
                                      String contentType, byte[] body) throws IOException {
        String headers = "HTTP/1.1 " + code + " " + status + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + body.length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        out.write(headers.getBytes());
        out.write(body);
        out.flush();
    }

    private static String guessContentType(String fileName) {
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) return "text/html";
        if (fileName.endsWith(".txt")) return "text/plain";
        if (fileName.endsWith(".css")) return "text/css";
        if (fileName.endsWith(".js")) return "application/javascript";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    private static void createSampleFileIfMissing(String name, String content) throws IOException {
        File f = new File(WWW_DIR, name);
        if (!f.exists()) {
            try (FileWriter fw = new FileWriter(f)) {
                fw.write(content);
            }
        }
    }
}