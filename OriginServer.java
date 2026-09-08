import java.net.*;
import java.io.*;

class Server
{
    public static void main(String[] args) throws Exception
    {
        File folder = new File("www");
        folder.mkdir();

        File file1 = new File("www/index.html");
        File file2 = new File("www/index.css");

        if(!file1.exists())
        {
            PrintWriter w = new PrintWriter(file1);

            w.println("<h1> HI HELLO </h1>");
            w.close();
        }

        if(!file2.exists())
        {
            PrintWriter w1 = new PrintWriter(file2);
            w1.println(".id{ border : 'none' }");
            w1.close();
        }

        ServerSocket server = new ServerSocket(8080);

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
            String path = request.split(" ")[1];

            File filePath = new File("www" + path);

            if(filePath.exists())
            {
                BufferedReader fileReader = new BufferedReader(new FileReader(filePath));

                String line;

                writer.println(
                    "HTTP 1.1 200 OK \r\n"+
                    "Content-Type : html\r\n"+
                    "Connection : Closed\r\n"
                );
                while((line = fileReader.readLine()) != null)
                {
                    writer.println(line);
                }

                fileReader.close();
            }
            else
            {
                writer.println("HTTP 1.1 404 Not Found \r\n"+
                            "Content-Type : text/html\r\n"+
                            "Connection : Closed\r\n");


            }

            client.close();


        }
    }
}
kodikalaiyarasan@LAP