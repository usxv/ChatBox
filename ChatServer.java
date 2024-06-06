import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer implements Runnable {
    private ServerSocket serverSocket;
    private ArrayList<Socket> clients;
    private int onlineCount; // Variable to store online user count

    public ChatServer(int port) {
        clients = new ArrayList<>();
        onlineCount = 0; // Initialize online user count
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                clients.add(socket);
                onlineCount++; // Increment online user count when a client connects
                System.out.println("Client connected: " + socket + ". Online count: " + onlineCount);
                broadcastOnlineCount(); // Broadcast updated online count to all clients
                Thread thread = new Thread(new ClientHandler(socket, this));
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void broadcastMessage(String message, Socket senderSocket) {
        for (Socket client : clients) {
            if (client != senderSocket) {
                try {
                    PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
                    writer.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Method to decrement online user count when a client disconnects
    public synchronized void decrementOnlineCount() {
        onlineCount--;
        System.out.println("Client disconnected. Online count: " + onlineCount);
        broadcastOnlineCount(); // Broadcast updated online count to all clients
    }

    // Method to broadcast online count to all clients
    public synchronized void broadcastOnlineCount() {
        for (Socket client : clients) {
            try {
                PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
                writer.println("OnlineCount:" + onlineCount);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer(9999);
        Thread serverThread = new Thread(server);
        serverThread.start();
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private ChatServer server;
    private String username;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println(" ChatBox V1.0.RR.SV");
            username = reader.readLine();
            server.broadcastMessage(" " + username + " has joined the chat.", socket);

            String message;
            while ((message = reader.readLine()) != null) {
                server.broadcastMessage(" " + username + "> " + message, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                server.broadcastMessage(" " + username + " has left the chat.", socket);
                server.decrementOnlineCount(); // Decrement online user count when a client disconnects
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
