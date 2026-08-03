import java.net.*;

public class LocalDNSServer {

    public static void main(String[] args) throws Exception {

        DatagramSocket socket = new DatagramSocket(5000);

        System.out.println("Local DNS Server Started...");

        while (true) {

            byte[] receiveData = new byte[1024];

            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);

            socket.receive(packet);

            String domain = new String(packet.getData(), 0, packet.getLength());

            System.out.println("Client asked for : " + domain);

            DatagramSocket temp = new DatagramSocket();

            DatagramPacket req = new DatagramPacket(
                    domain.getBytes(),
                    domain.length(),
                    InetAddress.getByName("localhost"),
                    5001);

            temp.send(req);

            byte[] reply = new byte[1024];

            DatagramPacket res = new DatagramPacket(reply, reply.length);

            temp.receive(res);

            String ip = new String(res.getData(), 0, res.getLength());

            DatagramPacket clientReply = new DatagramPacket(
                    ip.getBytes(),
                    ip.length(),
                    packet.getAddress(),
                    packet.getPort());

            socket.send(clientReply);

            temp.close();
        }
    }
}