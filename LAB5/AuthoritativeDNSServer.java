import java.net.*;
import java.util.HashMap;

public class AuthoritativeDNSServer {

    static HashMap<String, String> dns = new HashMap<>();

    static {

        dns.put("gaia.cs.umass.edu", "128.119.245.12");
        dns.put("mail.cs.umass.edu", "128.119.245.20");
        dns.put("www.google.com", "142.250.183.78");
        dns.put("www.annauniv.edu", "103.27.232.130");

    }

    public static void main(String[] args) throws Exception {

        DatagramSocket socket = new DatagramSocket(5003);

        System.out.println("Authoritative DNS Started");

        while (true) {

            byte[] receive = new byte[1024];

            DatagramPacket packet = new DatagramPacket(receive, receive.length);

            socket.receive(packet);

            String domain = new String(packet.getData(), 0, packet.getLength());

            System.out.println(domain);

            String ip = dns.getOrDefault(domain, "Domain Not Found");

            DatagramPacket response = new DatagramPacket(
                    ip.getBytes(),
                    ip.length(),
                    packet.getAddress(),
                    packet.getPort());

            socket.send(response);

        }

    }

}