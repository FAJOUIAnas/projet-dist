package ma.ac.emi.ginfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PiNode {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Invalid arguments");
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]))) {
            System.out.println("Node in port " + args[0] + " started. Waiting for task...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Server connected: " + clientSocket.getInetAddress());

                // Handle client request in a separate thread
                Thread clientThread = new Thread(() -> handleServerRequest(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleServerRequest(Socket clientSocket) {
        try (
                ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            // Receive the number of iterations from the client
            long[] iterations = (long[]) inputStream.readObject();

            // Perform pi calculation
            double partialPi = calculatePartialPi(iterations[0], iterations[1]);

            // Send the calculated pi value back to the client
            outputStream.writeDouble(partialPi);
            outputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static double calculatePartialPi(long start, long end) {
        double sum = 0.0;
        double sign = 1.0;

        for (long i = start; i < end; i++) {
            double term = sign / (2 * i + 1);
            sum += term;
            sign *= -1;
        }

        return sum;
    }
}
