import java.io.*;
import java.net.*;

public class SenderFastRetransmit {

    public static void main(String[] args) throws Exception {

        Socket socket = new Socket("localhost", 5001);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        int totalFrames = 8;

        int duplicateCount = 0;
        int previousAck = -2;

        for (int i = 0; i < totalFrames; i++) {

            System.out.println("Sending Frame " + i);
            out.println(i);

            String response = in.readLine();

            int ack = Integer.parseInt(response.split(":")[1]);

            System.out.println("Received " + response);

            if (ack == previousAck) {

                duplicateCount++;

            } else {

                duplicateCount = 1;
                previousAck = ack;

            }

            while (duplicateCount < 3 && ack == i - 1) {

                response = in.readLine();

                ack = Integer.parseInt(response.split(":")[1]);

                System.out.println("Received " + response);

                duplicateCount++;

            }

            if (duplicateCount == 3 && ack == i - 1) {

                System.out.println("Fast Retransmit Frame " + i);

                out.println(i);

                System.out.println(in.readLine());

            }
        }

        out.println("END");

        socket.close();
    }
}