import java.io.*;
import java.net.*;
import java.util.*;

public class MultiClientServer {
     private static final int PORT = 1234;
     private static Set<PrintWriter> clientOutputs = new HashSet<>();

     public static void main(String[] args) {
          System.out.println("Server started. Waiting for clients...");
          try (ServerSocket serverSocket = new ServerSocket(PORT)) {
               while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected!");
                    new ClientHandler(socket).start(); // Assign a thread for the client
               }
          } catch (IOException e) {
               e.printStackTrace();
          }
     }

     // Thread to handle each client
     private static class ClientHandler extends Thread {
          private Socket socket;
          private PrintWriter output;
          private BufferedReader input;

          public ClientHandler(Socket socket) {
               this.socket = socket;
          }

          public void run() {
               try {
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    output = new PrintWriter(socket.getOutputStream(), true);

                    synchronized (clientOutputs) {
                         clientOutputs.add(output);
                    }

                    String message;
                    while ((message = input.readLine()) != null) {
                         System.out.println("Received: " + message);
                         broadcastMessage(message); // Send message to all clients
                    }
               } catch (IOException e) {
                    System.out.println("Client disconnected.");
               } finally {
                    try {
                         socket.close();
                    } catch (IOException e) {
                         e.printStackTrace();
                    }
                    synchronized (clientOutputs) {
                         clientOutputs.remove(output);
                    }
               }
          }
     }

     // Send the message to all connected clients
     private static void broadcastMessage(String message) {
          synchronized (clientOutputs) {
               for (PrintWriter writer : clientOutputs) {
                    writer.println(message);
               }
          }
     }
}
