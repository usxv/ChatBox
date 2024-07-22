import java.io.*;
import java.net.*;

public class ChatClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 1115);
            System.out.println("Connected to server...");

            // BufferedReader to read input from the server
            BufferedReader inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // PrintWriter to send data to the server
            PrintWriter outputToServer = new PrintWriter(socket.getOutputStream(), true);
            // BufferedReader to read input from the client console
            BufferedReader inputFromConsole = new BufferedReader(new InputStreamReader(System.in));

            String msgFromServer, msgToServer;
            while (true) {
                // Check if there is a message from the server
                if (inputFromServer.ready()) {
                    msgFromServer = inputFromServer.readLine();
                    if (msgFromServer.equalsIgnoreCase("Server is shutting down...")) {
                        System.out.println("Server: " + msgFromServer);
                        System.out.println("Connection closed by server.");
                        break;
                    }
                    System.out.println("Server: " + msgFromServer);
                }

                // Check if there is a message from the console
                if (inputFromConsole.ready()) {
                    System.out.print("Send Message: ");
                    msgToServer = inputFromConsole.readLine();

                    // Check if the client wants to exit
                    if ("exit".equalsIgnoreCase(msgToServer)) {
                        System.out.println("Exiting...");
                        outputToServer.println("exit");
                        break;
                    }

                    outputToServer.println(msgToServer); // To send msg to the server
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
