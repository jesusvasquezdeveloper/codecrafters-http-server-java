import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    System.out.println("Logs from your program will appear here!");


     try(ServerSocket serverSocket = new ServerSocket(4221)) {
       Socket clientSocket;

       // Since the tester restarts your program quite often, setting SO_REUSEADDR
       // ensures that we don't run into 'Address already in use' errors
       serverSocket.setReuseAddress(true);

       clientSocket = serverSocket.accept(); // Wait for connection from client.
       System.out.println("accepted new connection");


       InputStream input = clientSocket.getInputStream();
       BufferedReader reader = new BufferedReader(new InputStreamReader(input));
       String line = reader.readLine();
       System.out.println(line);
       String[] httpRequest = line.split(" ", 0);

       String path = httpRequest[1];

         OutputStream out = clientSocket.getOutputStream();

       if (path.equals("/")) {
           out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
       } else if (path.startsWith("/echo/")) {
           String echoWord = path.split("/echo/")[1];
           out.write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length:" + echoWord.length() + "\r\n\r\n" + echoWord).getBytes());
       } else {
           out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
       }
       out.flush();
       out.close();
       clientSocket.close();

     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }
}
