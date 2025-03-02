import java.io.*;
import java.net.*;

public class ChatClient {
     public static void main(String[] args) {
          try {
               // Step 1: Connect to the server
               Socket socket = new Socket("localhost", 1234);
               System.out.println("Connected to the server!");

               // Step 2: Setup Input and Output
               BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
               PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
               BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

               // Step 3: Send and receive messages
               String message;
               while (true) {
                    System.out.print("You: ");
                    message = userInput.readLine(); // Read message from the user

                    output.println(message); // Send message to server

                    if (message.equalsIgnoreCase("exit")) { // Exit condition
                         System.out.println("Closing connection...");
                         break;
                    }

                    String response = input.readLine(); // Receive server response
                    System.out.println("Server: " + response);
               }

               // Step 4: Close resources
               socket.close();
               input.close();
               output.close();
               userInput.close();

          } catch (IOException e) {
               e.printStackTrace();
          }
     }
}
