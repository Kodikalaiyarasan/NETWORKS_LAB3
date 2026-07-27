import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    static String cookie = "";

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        while (true) {

            Socket socket = new Socket("localhost", 5000);

            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));

            PrintWriter out =
                    new PrintWriter(socket.getOutputStream(), true);

            System.out.print("Enter filename (exit to quit): ");

            String file = sc.nextLine();

            if (file.equalsIgnoreCase("exit"))
                break;

            out.println(file + "," + cookie);

            String line;

            System.out.println("\n------Response------");

            while ((line = in.readLine()) != null) {

                System.out.println(line);

                if (line.startsWith("Cookie-ID:")) {

                    cookie = line.substring(10).trim();

                }

            }

            socket.close();

        }

    }

}