import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class RequestHandler extends Thread {

    private Socket socket;
    private String[] args;

    public RequestHandler(Socket socket, String[] args) {
        this.socket = socket;
        this.args = args;
    }

    @Override
    public void run() {
        try {
            HttpRequest request = request(socket);

            String path = request.getPath();

            OutputStream out = socket.getOutputStream();

            if (path.equals("/")) {
                out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
            } else if (path.startsWith("/echo/")) {
                String echoWord = path.split("/echo/")[1];
                out.write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length:" + echoWord.length() + "\r\n\r\n" + echoWord).getBytes());

            } else if (path.equals("/user-agent")) {
                String userAgent = request.getHeader("User-Agent");
                out.write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length:" + userAgent.length() + "\r\n\r\n" + userAgent).getBytes());

            } else if(path.startsWith("/files/")) {

                String directory = args[1];
                String file = path.split("/files/")[1];

                String finalFile = directory + file;

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                try (BufferedReader reader = new BufferedReader(new FileReader(finalFile))) {
                    while((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                        stringBuilder.append("\n");
                    }

                    out.write(("HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length:" + stringBuilder.length() + "\r\n\r\n" + stringBuilder).getBytes());

                } catch (FileNotFoundException e) {
                    out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                }

            } else {
                out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }
            out.flush();
            out.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpRequest request(Socket socket)  {

        try {
            Scanner scanner = new Scanner(socket.getInputStream());

            String[] headerLine = scanner.nextLine().split(" ");

            String path = headerLine[1];

            HttpRequest request = new HttpRequest(path);

            String line;
            while (!(line = scanner.nextLine()).isEmpty()) {
                String[] header = line.split(": ");
                request.addHeader(header[0], header[1]);
            }

            return request;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
