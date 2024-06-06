import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClient implements ActionListener, Runnable {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField textField;
    private JLabel onlineCountLabel;
    private int onlineCount = 0;

    public ChatClient(String username, String ipAddress) {
        frame = new JFrame("ChatBox @" + username);
        frame.setLayout(new BorderLayout());
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center of the screen

        ImageIcon icon = new ImageIcon("./icons/ChatBox.png");
        frame.setIconImage(icon.getImage());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());

        // Emoji buttons panel
        JPanel emojiPanel = new JPanel(new FlowLayout());
        addEmojiButton(emojiPanel, "\uD83D\uDC4D"); // Thumbs up
        addEmojiButton(emojiPanel, "\uD83D\uDC4E"); // Thumbs down
        addEmojiButton(emojiPanel, "\uD83D\uDE0A"); // Smile
        addEmojiButton(emojiPanel, "\uD83D\uDE22"); // Sad
        addEmojiButton(emojiPanel, "\uD83D\uDE02"); // Laughing
        addEmojiButton(emojiPanel, "\uD83D\uDCA9"); // Poop
        addEmojiButton(emojiPanel, "\uD83D\uDC97"); // Heart

        inputPanel.add(emojiPanel, BorderLayout.NORTH);

        textField = new JTextField();
        inputPanel.add(textField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(this);
        inputPanel.add(sendButton, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Initialize online count label
        onlineCountLabel = new JLabel(" "+"Online: " + onlineCount);
        frame.add(onlineCountLabel, BorderLayout.NORTH);

        frame.setVisible(true);

        try {
            socket = new Socket(ipAddress, 9999);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(username + "\n");
            writer.flush();

            Thread thread = new Thread(this);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addEmojiButton(JPanel panel, String emoji) {
        JButton emojiButton = new JButton(emoji);
        emojiButton.addActionListener(e -> appendEmoji(emoji));
        panel.add(emojiButton);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            String message = textField.getText().trim();
            if (!message.isEmpty()) {
                chatArea.append(" " + "You>> " + message + "\n"); // Display the client's message in the chat area
                writer.write(message + "\n");
                writer.flush();
                textField.setText(""); // Clear the input field after sending the message
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.startsWith("OnlineCount:")) {
                    onlineCount = Integer.parseInt(message.substring(12));
                    updateOnlineCountLabel();
                } else {
                    chatArea.append(message + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateOnlineCountLabel() {
        SwingUtilities.invokeLater(() -> {
            onlineCountLabel.setText(" Online: " + onlineCount);
        });
    }

    private void appendEmoji(String emoji) {
        textField.setText(textField.getText() + emoji);
    }

    public static void main(String[] args) {
        boolean inputValid = false;

        while (!inputValid) {
            // A JFrame for input
            JFrame inputFrame = new JFrame();
            inputFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // A panel for input components
            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

            // Add labels and text field for IP address input
            JLabel ipLabel = new JLabel("Enter IP address:");
            JTextField ipField = new JTextField(20);
            inputPanel.add(ipLabel);
            inputPanel.add(ipField);

            // Add labels and text field for username input
            JLabel userLabel = new JLabel("Set a username:");
            JTextField userField = new JTextField(20);
            inputPanel.add(userLabel);
            inputPanel.add(userField);

            // Show input dialog for IP address and username
            int option = JOptionPane.showConfirmDialog(inputFrame, inputPanel, "Setup: IP & Username",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (option == JOptionPane.OK_OPTION) {
                String ipAddress = ipField.getText().trim();
                String username = userField.getText().trim();
                if (!ipAddress.isEmpty() && !username.isEmpty()) {
                    inputValid = true;
                    new ChatClient(username, ipAddress);
                } else {
                    JOptionPane.showMessageDialog(inputFrame, "IP address and username cannot be empty.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                System.out.println("IP address input cancelled.");
                System.exit(0);
            } // Exit the program if IP address input is cancelled
        }
    }
}
