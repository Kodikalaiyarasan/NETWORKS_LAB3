import java.io.*;
import java.net.*;

public class SenderNAK {

    public static void main(String[] args) throws Exception {

        Socket socket = new Socket("localhost", 5000);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        int totalFrames = 8;

        for (int i = 0; i < totalFrames; i++) {

            System.out.println("Sending Frame " + i);
            out.println(i);

            String response = in.readLine();

            if (response.startsWith("NAK")) {

                int frame = Integer.parseInt(response.split(":")[1]);

                System.out.println("Received NAK for Frame " + frame);
                System.out.println("Retransmitting Frame " + frame);

                out.println(frame);

                System.out.println(in.readLine());

            } else {

                System.out.println(response);

            }
        }

        out.println("END");

        socket.close();
    }
}