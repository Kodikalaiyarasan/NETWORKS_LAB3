import java.io.*;
import java.net.*;

class Client
{
        public static void main(String[] args) throws Exception
        {
        if(args.length < 3)
        {
            System.out.println("Error");
            return;
        }

        String proxyHost = args[0];
        int proxyPort = Integer.parseInt(args[1]);
        String path = args[2];

        URL url = new URL(path);

        String host  = url.getHost();
        int  port = url.getPort();

        Socket socket = new Socket(proxyHost , proxyPort);



       PrintWriter out = new PrintWriter(socket.getOutputStream() , true);

                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
                );
            out.println(
                "GET "+ path + " " + "HTTP 1.1\r\n"
                + "Host :" + host + ":" + port + "\r\n"
                + "Connection : Close\r\n"
            );

            String response;

            while((response = in.readLine()) != null)
            {
                System.out.println(response);
            }
        socket.close();

    }
}