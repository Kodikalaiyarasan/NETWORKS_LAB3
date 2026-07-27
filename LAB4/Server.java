import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {

    static final int PORT = 5000;

    // cookie -> history
    static ConcurrentHashMap<String, List<String>> history =
            new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server Started...");

        while (true) {
            Socket socket = serverSocket.accept();
            new ClientHandler(socket).start();
        }
    }
}

class ClientHandler extends Thread {

    Socket socket;

    ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {

        try {

            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));

            PrintWriter out =
                    new PrintWriter(socket.getOutputStream(), true);

            String request = in.readLine();

            if (request == null)
                return;

            // Request format:
            // filename,cookie
            // cookie can be empty

            String[] parts = request.split(",", 2);

            String filename = parts[0];

            String cookie = "";

            if (parts.length > 1)
                cookie = parts[1];

            if (cookie.equals("") ||
                    !Server.history.containsKey(cookie)) {

                cookie = UUID.randomUUID().toString();

                Server.history.put(cookie,
                        Collections.synchronizedList(new ArrayList<>()));
            }

            Server.history.get(cookie).add(filename);

            File file = new File(filename);

            String status;

            if (file.exists())
                status = "FOUND";
            else
                status = "NOT FOUND";

            StringBuilder response = new StringBuilder();

            response.append("HTTP/1.1 200 OK\n");
            response.append("Cookie-ID: ").append(cookie).append("\n");
            response.append("File Status: ").append(status).append("\n");
            response.append("History:\n");

            for (String s : Server.history.get(cookie)) {
                response.append(s).append("\n");
            }

            out.println(response.toString());

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}