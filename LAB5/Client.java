import java.net.*;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws Exception {

        DatagramSocket socket = new DatagramSocket();

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Domain Name: ");
        String domain = sc.nextLine();

        byte[] sendData = domain.getBytes();

        DatagramPacket packet = new DatagramPacket(
                sendData,
                sendData.length,
                InetAddress.getByName("localhost"),
                5000);

        socket.send(packet);

        byte[] receiveData = new byte[1024];

        DatagramPacket response = new DatagramPacket(receiveData, receiveData.length);

        socket.receive(response);

        System.out.println("IP Address : " +
                new String(response.getData(), 0, response.getLength()));

        socket.close();
    }
}