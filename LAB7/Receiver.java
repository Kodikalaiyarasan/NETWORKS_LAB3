import java.io.*;
import java.net.*;
import java.util.*;

public class ReceiverServer {

    // Simulated receiver buffer
    static final int BUFFER_CAPACITY = 5;

    // Stores received packets
    static Queue<Integer> buffer = new LinkedList<>();

    // Next packet expected by receiver
    static int nextExpected = 0;

    static PrintWriter out;

    public static void main(String[] args) {

        int port = 5000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Receiver started...");
            System.out.println("Waiting for sender...");

            Socket socket = serverSocket.accept();

            System.out.println("Sender connected!");

            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));

            out =
                    new PrintWriter(
                            socket.getOutputStream(), true);

            // Thread that simulates application consuming data
            Thread consumerThread = new Thread(() -> {

                while (true) {

                    try {
                        Thread.sleep(3000);

                        synchronized (buffer) {

                            if (!buffer.isEmpty()) {

                                int packet = buffer.poll();

                                System.out.println(
                                        "\nApplication consumed packet: "
                                                + packet);

                                int rwnd =
                                        BUFFER_CAPACITY - buffer.size();

                                System.out.println(
                                        "Buffer size: "
                                                + buffer.size());

                                System.out.println(
                                        "Advertised window (rwnd): "
                                                + rwnd);

                                // Send window update
                                out.println(
                                        "ACK "
                                                + nextExpected
                                                + " "
                                                + rwnd);
                            }
                        }

                    } catch (InterruptedException e) {
                        break;
                    }
                }

            });

            consumerThread.start();

            String message;

            while ((message = in.readLine()) != null) {

                if (!message.startsWith("DATA")) {
                    continue;
                }

                String[] parts = message.split(" ");

                int sequenceNumber =
                        Integer.parseInt(parts[1]);

                synchronized (buffer) {

                    System.out.println(
                            "\nReceived packet: "
                                    + sequenceNumber);

                    /*
                     * Accept only the packet that we are expecting.
                     */
                    if (sequenceNumber == nextExpected) {

                        if (buffer.size() < BUFFER_CAPACITY) {

                            buffer.add(sequenceNumber);

                            nextExpected++;

                            System.out.println(
                                    "Packet "
                                            + sequenceNumber
                                            + " stored in buffer.");

                        } else {

                            System.out.println(
                                    "BUFFER FULL!");
                        }

                    } else {

                        System.out.println(
                                "Unexpected packet: "
                                        + sequenceNumber);
                    }

                    int rwnd =
                            BUFFER_CAPACITY - buffer.size();

                    System.out.println(
                            "Current buffer size: "
                                    + buffer.size());

                    System.out.println(
                            "Sending ACK: nextExpected="
                                    + nextExpected
                                    + ", rwnd="
                                    + rwnd);

                    /*
                     * ACK contains:
                     * 1. Next expected sequence number
                     * 2. Receiver advertised window
                     */
                    out.println(
                            "ACK "
                                    + nextExpected
                                    + " "
                                    + rwnd);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}