import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    System.out.println("Logs from your program will appear here!");


     try(ServerSocket serverSocket = new ServerSocket(4221)) {
       while (true) {

           Socket clientSocket;

           // Since the tester restarts your program quite often, setting SO_REUSEADDR
           // ensures that we don't run into 'Address already in use' errors
           serverSocket.setReuseAddress(true);

           clientSocket = serverSocket.accept();

           new RequestHandler(clientSocket).start();// Wait for connection from client.

       }

     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }
}
