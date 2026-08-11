import java.net.*;
import java.util.*;

public class Sender {

    static final int WINDOW_SIZE = 4;
    static final int TIMEOUT = 3000;

    public static void main(String[] args) throws Exception {

        DatagramSocket socket = new DatagramSocket();

        InetAddress receiverAddress =
                InetAddress.getByName("localhost");

        int receiverPort = 5000;

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter message: ");
        String message = sc.nextLine();

        System.out.print("Enter packet size: ");
        int packetSize = sc.nextInt();

        // --------------------------------------------------
        // Divide message into packets
        // --------------------------------------------------

        ArrayList<String> packets = new ArrayList<>();

        int sequenceNumber = 0;

        for (int i = 0; i < message.length(); i += packetSize) {

            int end = Math.min(i + packetSize, message.length());

            String data = message.substring(i, end);

            packets.add(sequenceNumber + ":" + data);

            sequenceNumber++;
        }

        int totalPackets = packets.size();

        System.out.println("\nTotal packets = "
                + totalPackets);

        System.out.println("Window Size = "
                + WINDOW_SIZE);

        System.out.println("\nPackets:");

        for (String p : packets) {
            System.out.println(p);
        }

        System.out.println();

        // --------------------------------------------------
        // Go-Back-N variables
        // --------------------------------------------------

        int base = 0;       // oldest unacknowledged packet
        int nextSeq = 0;    // next packet to send

        long timerStart = 0;

        Random random = new Random();

        // --------------------------------------------------
        // Main Go-Back-N loop
        // --------------------------------------------------

        while (base < totalPackets) {

            // ----------------------------------------------
            // Send packets while window has space
            // ----------------------------------------------

            while (nextSeq < totalPackets &&
                    nextSeq < base + WINDOW_SIZE) {

                String data = packets.get(nextSeq);

                /*
                 * Simulate packet loss.
                 *
                 * Change LOSS_PROBABILITY to 0
                 * if you don't want packet loss.
                 */

                double LOSS_PROBABILITY = 0.20;

                if (random.nextDouble() < LOSS_PROBABILITY) {

                    System.out.println(
                            "SIMULATED LOSS -> Packet "
                                    + nextSeq
                    );

                } else {

                    sendPacket(
                            socket,
                            receiverAddress,
                            receiverPort,
                            data
                    );

                    System.out.println(
                            "Sent Packet -> "
                                    + nextSeq
                    );
                }

                // Start timer when first packet
                // in the window is sent.
                if (base == nextSeq) {
                    timerStart = System.currentTimeMillis();
                }

                nextSeq++;
            }

            // ----------------------------------------------
            // Wait for ACK
            // ----------------------------------------------

            try {

                socket.setSoTimeout(500);

                byte[] buffer = new byte[1024];

                DatagramPacket ackPacket =
                        new DatagramPacket(
                                buffer,
                                buffer.length
                        );

                socket.receive(ackPacket);

                String ackMessage =
                        new String(
                                ackPacket.getData(),
                                0,
                                ackPacket.getLength()
                        );

                // ACK format: ACK:x
                int ackNumber =
                        Integer.parseInt(
                                ackMessage.substring(4)
                        );

                System.out.println(
                        "Received ACK -> " + ackNumber
                );

                // ------------------------------------------
                // Cumulative ACK
                // ------------------------------------------

                if (ackNumber >= base) {

                    base = ackNumber + 1;

                    System.out.println(
                            "Window slides. New base = "
                                    + base
                    );

                    // Start timer for next oldest packet
                    if (base < nextSeq) {
                        timerStart =
                                System.currentTimeMillis();
                    }
                }

            } catch (SocketTimeoutException e) {

                // ------------------------------------------
                // Timer expired
                // ------------------------------------------

                if (base < nextSeq) {

                    long elapsed =
                            System.currentTimeMillis()
                                    - timerStart;

                    if (elapsed >= TIMEOUT) {

                        System.out.println(
                                "\n*** TIMEOUT ***"
                        );

                        System.out.println(
                                "Retransmitting from Packet "
                                        + base
                        );

                        // ----------------------------------
                        // Go-Back-N retransmission
                        // ----------------------------------

                        for (int i = base; i < nextSeq; i++) {

                            sendPacket(
                                    socket,
                                    receiverAddress,
                                    receiverPort,
                                    packets.get(i)
                            );

                            System.out.println(
                                    "Retransmitted Packet -> "
                                            + i
                            );
                        }

                        timerStart =
                                System.currentTimeMillis();

                        System.out.println();
                    }
                }
            }
        }

        System.out.println(
                "\nAll packets successfully transmitted!"
        );

        socket.close();
    }

    // ------------------------------------------------------
    // Function to send packet
    // ------------------------------------------------------

    static void sendPacket(
            DatagramSocket socket,
            InetAddress address,
            int port,
            String message
    ) throws Exception {

        byte[] data = message.getBytes();

        DatagramPacket packet =
                new DatagramPacket(
                        data,
                        data.length,
                        address,
                        port
                );

        socket.send(packet);
    }
}