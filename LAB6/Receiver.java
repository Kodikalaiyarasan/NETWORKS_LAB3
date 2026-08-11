import java.net.*;

public class Receiver {

    public static void main(String[] args) throws Exception {

        DatagramSocket socket = new DatagramSocket(5000);

        int expectedSeq = 0;

        System.out.println("Receiver started...");
        System.out.println("Waiting for packets...\n");

        while (true) {

            byte[] buffer = new byte[1024];

            DatagramPacket packet =
                    new DatagramPacket(buffer, buffer.length);

            socket.receive(packet);

            String message =
                    new String(packet.getData(), 0, packet.getLength());

            // Message format: SEQ:DATA
            String[] parts = message.split(":", 2);

            int seq = Integer.parseInt(parts[0]);
            String data = parts[1];

            System.out.println("Received Packet -> Seq: "
                    + seq + ", Data: " + data);

            if (seq == expectedSeq) {

                System.out.println("Accepted Packet " + seq);

                expectedSeq++;

                // Cumulative ACK
                // ACK means all packets up to this sequence
                // number have been received.
                int ackNumber = expectedSeq - 1;

                String ackMessage = "ACK:" + ackNumber;

                byte[] ackData = ackMessage.getBytes();

                DatagramPacket ackPacket =
                        new DatagramPacket(
                                ackData,
                                ackData.length,
                                packet.getAddress(),
                                packet.getPort()
                        );

                socket.send(ackPacket);

                System.out.println(
                        "Sent Cumulative ACK -> " + ackNumber
                );

            } else {

                System.out.println(
                        "Out-of-order Packet " + seq +
                        " discarded."
                );

                // Send ACK for last correctly received packet
                int lastAck = expectedSeq - 1;

                String ackMessage = "ACK:" + lastAck;

                byte[] ackData = ackMessage.getBytes();

                DatagramPacket ackPacket =
                        new DatagramPacket(
                                ackData,
                                ackData.length,
                                packet.getAddress(),
                                packet.getPort()
                        );

                socket.send(ackPacket);

                System.out.println(
                        "Sent ACK for last correct packet -> "
                                + lastAck
                );
            }

            System.out.println();
        }
    }
}