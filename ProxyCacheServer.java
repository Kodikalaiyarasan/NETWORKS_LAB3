import java.io.*;
import java.net.*;

class ProxyServer
{
    public static void main(String[] args) throws Exception
    {
        File cache = new File("cache");

        cache.mkdir();

        ServerSocket server = new ServerSocket(9090);
        System.out.println("Proxy Server started on port 9090");

        while(true)
        {
            Socket client = server.accept();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(client.getInputStream())
            );

            PrintWriter writer = new PrintWriter(
                client.getOutputStream() , true
            );

            String request = reader.readLine();
                                System.out.println("Got the Request : " + request);

            String urlPath = request.split(" ")[1];

            URL url = new URL(urlPath);

            String host = url.getHost();
            int port = (url.getPort());
            String path = url.getPath().substring(1);

            File filePath = new File("cache/" + path);

            if(filePath.exists())
            {
                System.out.println("Cache Hit");

                BufferedReader fileReader = new BufferedReader(new FileReader(filePath));

                String fileContent;

                while((fileContent = fileReader.readLine()) != null)
                {
                    writer.println(fileContent);

                }

            }
            else
            {
                System.out.println("Cache Miss");

                File newFile = new File("cache/" + path);


                Socket Server = new Socket(host,port);

                BufferedReader serverIn = new BufferedReader(
                    new InputStreamReader(Server.getInputStream())
                );


                PrintWriter serverOut = new PrintWriter(
                    Server.getOutputStream() , true
                );

                serverOut.println(
                    "GET "+ "/" + path + " HTTP 1.1\r\n"+
                    "Host :" + host + ":" + port + "\r\n"+
                    "Connection : Close\r\n"
                );

                String response;
                PrintWriter fileWriter = new PrintWriter(newFile);
                while((response = serverIn.readLine()) != null)
                {
                    fileWriter.println(response);
                    writer.println(response);
                }
                fileWriter.close();

            }

            client.close();

        }


    }
}
