import java.net.*;

public class RootDNSServer {

    public static void main(String[] args) throws Exception {

        DatagramSocket socket = new DatagramSocket(5001);

        System.out.println("Root DNS Started");

        while (true) {

            byte[] receive = new byte[1024];

            DatagramPacket packet = new DatagramPacket(receive, receive.length);

            socket.receive(packet);

            String domain = new String(packet.getData(), 0, packet.getLength());

            System.out.println("Received : " + domain);

            DatagramSocket temp = new DatagramSocket();

            DatagramPacket req = new DatagramPacket(
                    domain.getBytes(),
                    domain.length(),
                    InetAddress.getByName("localhost"),
                    5002);

            temp.send(req);

            byte[] ans = new byte[1024];

            DatagramPacket response = new DatagramPacket(ans, ans.length);

            temp.receive(response);

            String ip = new String(response.getData(), 0, response.getLength());

            DatagramPacket back = new DatagramPacket(
                    ip.getBytes(),
                    ip.length(),
                    packet.getAddress(),
                    packet.getPort());

            socket.send(back);

            temp.close();
        }

    }
}