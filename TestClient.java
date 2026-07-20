import java.io.*;
import java.net.*;

/**
 * TestClient
 * ----------
 * A minimal command-line HTTP client used to test ProxyCacheServer
 * without needing to configure a real web browser.
 *
 * It connects to the PROXY (not the origin server directly) and sends
 * a GET request containing the full destination URL, exactly the way a
 * browser does when a proxy is configured.
 *
 * Run:
 *   java TestClient 127.0.0.1 9090 http://localhost:8080/index.html
 *
 *   arg1 = proxy host
 *   arg2 = proxy port
 *   arg3 = full URL of the resource you want (served by OriginServer)
 */
public class TestClient {

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: java TestClient <proxyHost> <proxyPort> <fullUrl>");
            System.out.println("Example: java TestClient 127.0.0.1 9090 http://localhost:8080/index.html");
            return;
        }

        String proxyHost = args[0];
        int proxyPort = Integer.parseInt(args[1]);
        String targetUrl = args[2];

        // Extract host:port for the Host header
        URL url = new URL(targetUrl);
        String hostHeader = url.getHost() + (url.getPort() == -1 ? "" : ":" + url.getPort());

        try (Socket socket = new Socket(proxyHost, proxyPort)) {
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            String request = "GET " + targetUrl + " HTTP/1.1\r\n" +
                    "Host: " + hostHeader + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
            out.write(request.getBytes());
            out.flush();

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }

            System.out.println("----- Response received from proxy -----");
            System.out.println(buffer.toString());
        }
    }
}