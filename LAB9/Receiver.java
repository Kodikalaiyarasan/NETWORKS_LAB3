import java.io.*;
import java.net.*;

public class ReceiverFastRetransmit {

    public static void main(String[] args) throws Exception {

        ServerSocket server = new ServerSocket(5001);
        System.out.println("Receiver started...");

        Socket socket = server.accept();
        System.out.println("Sender connected.");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        int lostFrame = 2;
        boolean firstLoss = true;
        int lastAck = -1;

        while (true) {

            String frame = in.readLine();

            if (frame == null || frame.equals("END"))
                break;

            int num = Integer.parseInt(frame);

            if (num == lostFrame && firstLoss) {

                System.out.println("Frame " + num + " LOST");

                for (int i = 0; i < 3; i++)
                    out.println("ACK:" + lastAck);

                firstLoss = false;

            } else {

                System.out.println("Received Frame " + num);

                lastAck = num;
                out.println("ACK:" + num);

            }
        }

        socket.close();
        server.close();
    }
}