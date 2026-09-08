import java.net.*;
import java.io.*;

public class UDPClient {

    public static void main(String[] args) throws Exception {

        DatagramSocket socket = new DatagramSocket();
        InetAddress serverIP = InetAddress.getByName("localhost");

        BufferedReader userInput = new BufferedReader(
                new InputStreamReader(System.in));

        byte[] buffer = new byte[2048];

        while (true) {

            System.out.print("You: ");
            String msg = userInput.readLine();

            // Send message to server
            DatagramPacket packet = new DatagramPacket(
                    msg.getBytes(),
                    msg.getBytes().length,
                    serverIP,
                    8080);

            socket.send(packet);

            // Receive server reply
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.receive(reply);

            String response = new String(
                    reply.getData(),
                    0,
                    reply.getLength());

            System.out.println("Server: " + response);

            if (msg.equalsIgnoreCase("exit")) {
                break;
            }
        }

        socket.close();
    }
}