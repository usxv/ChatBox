import java.io.*;
import java.net.*;

public class ChatServer {
    public static void main(String[] args) {
        try {
            System.out.println("Waiting for client...");
            ServerSocket ss = new ServerSocket(1115);
            Socket soc = ss.accept();
            System.out.println("@Client1 Connected...");

            // BufferedReader to read input from the client
            BufferedReader infrClient = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            // BufferedReader to read input from the server sysyem.in = input from keyboard
            BufferedReader serMsg = new BufferedReader(new InputStreamReader(System.in));
            // PrintWriter to send data to the client
            PrintWriter msgout = new PrintWriter(soc.getOutputStream(), true);

            String msgfrClient;
            while (true) {
                // Read message from client
                msgfrClient = infrClient.readLine();
                if (msgfrClient == null || msgfrClient.equalsIgnoreCase("exit")) {
                    System.out.println("Client disconnected.");
                    break;
                }
                System.out.println("Client: " + msgfrClient);

                // Prompt to send message to the client
                System.out.print("Send Message: ");
                String msg = serMsg.readLine();

                // Check if the server wants to exit
                if ("exit".equalsIgnoreCase(msg)) {
                    System.out.println("Exiting...");
                    msgout.println("Server is shutting down...");
                    break;
                }

                msgout.println(msg); // Send message to the client
            }

            ss.close();
            soc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
