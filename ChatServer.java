import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
     private static Set<ClientHandler> clients = new HashSet<>();

     public static void main(String[] args) {
          int port = 12345;
          System.out.println("Chat server started on port " + port);

          try (ServerSocket serverSocket = new ServerSocket(port)) {
               while (true) {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socket);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();
               }
          } catch (IOException e) {
               e.printStackTrace();
          }
     }

     public static void broadcast(String message, ClientHandler sender) {
          for (ClientHandler client : clients) {
               if (client != sender) {
                    client.sendMessage(message);
               }
          }
     }

     public static void removeClient(ClientHandler client) {
          clients.remove(client);
     }

     private static class ClientHandler implements Runnable {
          private Socket socket;
          private PrintWriter out;
          private BufferedReader in;
          private String username;

          public ClientHandler(Socket socket) {
               this.socket = socket;
          }

          public void run() {
               try {
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);

                    // Read username from client
                    username = in.readLine();
                    broadcast("📢 " + username + " has joined the chat!", this);

                    String message;
                    while ((message = in.readLine()) != null) {
                         System.out.println("Received: " + message);
                         broadcast("[" + username + "]: " + message, this);
                    }

               } catch (IOException e) {
                    e.printStackTrace();
               } finally {
                    try {
                         socket.close();
                    } catch (IOException e) {
                         e.printStackTrace();
                    }
                    removeClient(this);
                    broadcast("❌ " + username + " has left the chat!", this);
               }
          }

          public void sendMessage(String message) {
               out.println(message);
          }
     }
}
