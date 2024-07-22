import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ChatClient implements ActionListener, Runnable {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField textField;

    public ChatClient(String username, String ipAddress) {
        // Setup the GUI
        frame = new JFrame("ChatBox @" + username);
        frame.setLayout(new BorderLayout());
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        textField = new JTextField();
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(this);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        frame.add(inputPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        // Setup the connection to the server
        try {
            socket = new Socket(ipAddress, 9999);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(username + "\n");
            writer.flush();
            new Thread(this).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            String message = textField.getText().trim();
            if (!message.isEmpty()) {
                chatArea.append("You>> " + message + "\n");
                writer.write(message + "\n");
                writer.flush();
                textField.setText("");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                chatArea.append(message + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        JTextField ipField = new JTextField(20);
        JTextField userField = new JTextField(20);

        inputPanel.add(new JLabel("Enter IP address:"));
        inputPanel.add(ipField);
        inputPanel.add(new JLabel("Set a username:"));
        inputPanel.add(userField);

        int option = JOptionPane.showConfirmDialog(null, inputPanel, "Setup: IP & Username",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String ipAddress = ipField.getText().trim();
            String username = userField.getText().trim();
            if (!ipAddress.isEmpty() && !username.isEmpty()) {
                new ChatClient(username, ipAddress);
            } else {
                JOptionPane.showMessageDialog(null, "IP address and username cannot be empty.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.exit(0);
        }
    }
}

