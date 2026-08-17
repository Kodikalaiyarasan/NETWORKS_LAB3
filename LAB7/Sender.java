import java.io.*;
import java.net.*;

public class SenderClient {

    static final int PORT = 5000;

    // Sender's maximum sliding window
    static final int SENDER_WINDOW = 4;

    // Total packets to send
    static final int TOTAL_PACKETS = 20;

    // Sender state
    static int base = 0;
    static int nextSeq = 0;

    // Receiver advertised window
    static int rwnd = 0;

    static PrintWriter out;

    public static void main(String[] args) {

        String host = "localhost";

        try {

            Socket socket =
                    new Socket(host, PORT);

            System.out.println(
                    "Connected to receiver.");

            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()));

            out =
                    new PrintWriter(
                            socket.getOutputStream(), true);

            /*
             * Thread continuously receives ACKs
             * from the receiver.
             */
            Thread ackThread = new Thread(() -> {

                try {

                    String message;

                    while ((message = in.readLine()) != null) {

                        String[] parts =
                                message.split(" ");

                        if (parts[0].equals("ACK")) {

                            int ack =
                                    Integer.parseInt(parts[1]);

                            int newRwnd =
                                    Integer.parseInt(parts[2]);

                            /*
                             * ACK tells us that all packets
                             * before 'ack' have been received.
                             */
                            if (ack > base) {
                                base = ack;
                            }

                            rwnd = newRwnd;

                            System.out.println(
                                    "\nACK received:"
                                            + " nextExpected="
                                            + ack
                                            + ", rwnd="
                                            + rwnd);

                            System.out.println(
                                    "Unacknowledged packets = "
                                            + (nextSeq - base));
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            });

            ackThread.start();

            /*
             * Initially assume receiver has some space.
             *
             * In a real TCP implementation the receiver
             * would advertise this through TCP's window.
             *
             * Here we initialize it to 5 because our
             * simulated receiver buffer has capacity 5.
             */
            rwnd = 5;

            while (base < TOTAL_PACKETS) {

                /*
                 * Effective sending window:
                 *
                 * min(sender window, receiver window)
                 */
                int effectiveWindow =
                        Math.min(SENDER_WINDOW, rwnd);

                /*
                 * Number of packets currently
                 * waiting for ACK.
                 */
                int unacknowledged =
                        nextSeq - base;

                /*
                 * Sender is allowed to send only if:
                 *
                 * unacknowledged < effectiveWindow
                 */
                if (nextSeq < TOTAL_PACKETS
                        && unacknowledged < effectiveWindow
                        && rwnd > 0) {

                    System.out.println(
                            "\nSending packet: "
                                    + nextSeq);

                    out.println(
                            "DATA "
                                    + nextSeq
                                    + " Hello");

                    nextSeq++;

                    /*
                     * Small delay just to make
                     * the simulation easy to observe.
                     */
                    Thread.sleep(500);

                } else {

                    /*
                     * rwnd = 0 means receiver buffer
                     * is completely full.
                     */
                    if (rwnd == 0) {

                        System.out.println(
                                "\nReceiver advertised "
                                        + "rwnd = 0");

                        System.out.println(
                                "Sender STOPPED transmitting.");

                    } else {

                        System.out.println(
                                "\nSending window full.");

                        System.out.println(
                                "Waiting for ACK...");
                    }

                    Thread.sleep(500);
                }
            }

            System.out.println(
                    "\nAll packets have been acknowledged.");

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}