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

    public ChatServer(int port) {
        clients = new ArrayList<>();
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
                System.out.println("Client connected: " + socket);
                new Thread(new ClientHandler(socket, this)).start();
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

    public static void main(String[] args) {
        ChatServer server = new ChatServer(9999);
        new Thread(server).start();
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private ChatServer server;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println("Welcome to ChatBox");
            String username = reader.readLine();
            server.broadcastMessage(username + " has joined the chat.", socket);

            String message;
            while ((message = reader.readLine()) != null) {
                server.broadcastMessage(username + ": " + message, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                server.broadcastMessage("A user has left the chat.", socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

