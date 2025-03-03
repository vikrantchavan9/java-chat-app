import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URI;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ChatClientGUI {
     private String username;
     private Socket socket;
     private PrintWriter out;
     private Scanner in;

     private JFrame frame;
     private JPanel chatPanel;
     private JScrollPane scrollPane;
     private JTextField messageField;
     private JButton sendButton;
     private JButton emojiButton;

     public ChatClientGUI(String serverAddress, int port) {
          try {
               // Connect to the server
               socket = new Socket(serverAddress, port);
               out = new PrintWriter(socket.getOutputStream(), true);
               in = new Scanner(socket.getInputStream());

               // Ask for the username
               username = JOptionPane.showInputDialog("Enter your username:");
               if (username == null || username.trim().isEmpty()) {
                    username = "Anonymous";
               }

               // Send the username to the server
               out.println(username);

               // Setup GUI
               createUI();

               // Start listening for messages from the server
               new Thread(this::listenForMessages).start();

          } catch (Exception e) {
               e.printStackTrace();
          }
     }

     private void createUI() {
          // Frame
          frame = new JFrame("Chat Application - " + username);
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          frame.setSize(400, 500);
          frame.setLayout(new BorderLayout());

          // Chat Area
          chatPanel = new JPanel();
          chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS)); // Stack messages vertically
          scrollPane = new JScrollPane(chatPanel);
          scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
          frame.add(scrollPane, BorderLayout.CENTER);

          // Input and Send Button
          JPanel inputPanel = new JPanel(new BorderLayout());
          messageField = new JTextField();
          messageField.setFont(new Font("Arial", Font.PLAIN, 14));
          sendButton = new JButton("Send");

          // Create an emoji button next to the message field
          emojiButton = new JButton("😀"); // Button with emoji icon
          emojiButton.setFont(new Font("Arial", Font.PLAIN, 18));
          emojiButton.addActionListener(e -> showEmojiPicker()); // Click event

          // Add the emoji button to the message input panel
          inputPanel.add(messageField, BorderLayout.CENTER);
          inputPanel.add(emojiButton, BorderLayout.WEST); // Emoji button to the left
          inputPanel.add(sendButton, BorderLayout.EAST);

          sendButton.addActionListener(e -> sendMessage());
          messageField.addActionListener(e -> sendMessage());

          frame.add(inputPanel, BorderLayout.SOUTH);
          frame.setVisible(true);

          sendButton.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                    sendMessage();
               }
          });

          messageField.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                    sendMessage();
               }
          });

          inputPanel.add(messageField, BorderLayout.CENTER);
          inputPanel.add(sendButton, BorderLayout.EAST);
          frame.add(inputPanel, BorderLayout.SOUTH);

          frame.setVisible(true);
     }

     private void showEmojiPicker() {
          JPopupMenu emojiPopup = new JPopupMenu();

          // Twemoji codes
          String[] emojiCodes = { "1f600", "1f602", "1f60d", "1f60e", "1f622", "1f621", "1f44d", "1f389", "2764",
                    "1f525" };

          for (String code : emojiCodes) {
               try {
                    URL url = new URI("https://cdnjs.cloudflare.com/ajax/libs/twemoji/14.0.2/72x72/" + code + ".png")
                              .toURL();
                    ImageIcon emojiIcon = new ImageIcon(
                              new ImageIcon(url).getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));

                    JMenuItem emojiItem = new JMenuItem(emojiIcon);
                    emojiItem.addActionListener(e -> insertEmoji(":" + code + ":")); // Insert emoji code
                    emojiPopup.add(emojiItem);
               } catch (Exception e) {
                    e.printStackTrace();
               }
          }

          emojiPopup.show(emojiButton, 0, emojiButton.getHeight());
     }

     private void insertEmoji(String emojiCode) {
          // Append emoji code to text field (we will later replace it with an image in
          // chat)
          messageField.setText(messageField.getText() + " " + emojiCode + " ");
     }

     private void sendMessage() {
          String message = messageField.getText().trim();
          if (!message.isEmpty()) {
               // Get timestamp
               String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

               // Replace emoji codes with Twemoji image URLs
               String formattedMessage = message.replaceAll(":1f600:",
                         "<img src='https://cdnjs.cloudflare.com/ajax/libs/twemoji/14.0.2/72x72/1f600.png' width='20'>")
                         .replaceAll(":1f602:",
                                   "<img src='https://cdnjs.cloudflare.com/ajax/libs/twemoji/14.0.2/72x72/1f602.png' width='20'>");

               // Display formatted message in chat area
               displayMessage("[" + timestamp + "] [You]: " + message);

               // Send plain text message to server
               out.println("[" + timestamp + "] " + username + ": " + message);

               // Clear input field
               messageField.setText("");
          }
     }

     private void displayMessage(String text) {
          JPanel messagePanel = new JPanel();
          messagePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2)); // Reduce horizontal and vertical gaps

          String[] words = text.split(" ");
          for (String word : words) {
               if (word.startsWith(":") && word.endsWith(":")) { // Emoji Code Detected
                    String emojiCode = word.substring(1, word.length() - 1);
                    try {
                         URL url = new URI(
                                   "https://cdnjs.cloudflare.com/ajax/libs/twemoji/14.0.2/72x72/" + emojiCode + ".png")
                                   .toURL();
                         ImageIcon emojiIcon = new ImageIcon(
                                   new ImageIcon(url).getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
                         JLabel emojiLabel = new JLabel(emojiIcon);
                         messagePanel.add(emojiLabel);
                    } catch (Exception e) {
                         messagePanel.add(new JLabel(word + " ")); // Fallback to text if error
                    }
               } else {
                    messagePanel.add(new JLabel(word + " "));
               }
          }

          // Ensure minimum spacing
          messagePanel.setMaximumSize(new Dimension(chatPanel.getWidth(), 30)); // Limit message height

          chatPanel.add(messagePanel);
          chatPanel.revalidate();
          chatPanel.repaint();

          // Auto-scroll to the bottom
          SwingUtilities.invokeLater(
                    () -> scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum()));
     }

     private void listenForMessages() {
          while (in.hasNextLine()) {
               String message = in.nextLine();
               SwingUtilities.invokeLater(() -> displayMessage(message));

          }
     }

     public static void main(String[] args) {
          new ChatClientGUI("localhost", 12345);
     }
}
