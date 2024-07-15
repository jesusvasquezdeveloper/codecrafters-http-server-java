import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage

     try {
       ServerSocket serverSocket = new ServerSocket(4221);
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

       if (httpRequest[1].equals("/")) {
           clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
       } else {
           clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
       }

     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }
}
