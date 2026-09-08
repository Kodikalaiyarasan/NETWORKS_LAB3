import java.net.*;

class ClientHandler extends Thread {

    private DatagramPacket packet;
    private int clientId;

    ClientHandler(DatagramPacket packet, int clientId) {
        this.packet = packet;
        this.clientId = clientId;
    }

    public void run() {

        try {
            DatagramSocket socket = new DatagramSocket();

            String request = new String(
                    packet.getData(),
                    0,
                    packet.getLength());

            System.out.println("Client " + clientId + ": " + request);

            String response;

            if (request.equalsIgnoreCase("exit")) {
                response = "Connection Closed";
                System.out.println("Client " + clientId + " disconnected");
            } else {
                response = "Hi Client " + clientId;
            }

            DatagramPacket reply = new DatagramPacket(
                    response.getBytes(),
                    response.getBytes().length,
                    packet.getAddress(),
                    packet.getPort());

            socket.send(reply);
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
import java.net.*;

public class MultiServer {

    public static void main(String[] args) throws Exception {

        DatagramSocket socket = new DatagramSocket(8080);
        System.out.println("UDP Server Started...");

        int clientCount = 1;
        byte[] buffer = new byte[2048];

        while (true) {

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            System.out.println("Client " + clientCount + " Connected");

            new ClientHandler(packet, clientCount).start();

            clientCount++;
        }
    }
}