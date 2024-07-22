import java.io.*;
import java.net.Socket;

public class RequestHandler extends Thread {

    private Socket socket;

    public RequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();
            System.out.println(line);
            String[] httpRequest = line.split(" ", 0);

            String path = httpRequest[1];

            OutputStream out = socket.getOutputStream();

            if (path.equals("/")) {
                out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
            } else if (path.startsWith("/echo/")) {
                String echoWord = path.split("/echo/")[1];
                out.write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length:" + echoWord.length() + "\r\n\r\n" + echoWord).getBytes());

            } else if (path.equals("/user-agent")) {

                reader.readLine();
                String userAgent = reader.readLine();
                userAgent = userAgent.split(" ")[1];
                userAgent = userAgent.replace("\r\n","");

                out.write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length:" + userAgent.length() + "\r\n\r\n" + userAgent).getBytes());

            } else {
                out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }
            out.flush();
            out.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
