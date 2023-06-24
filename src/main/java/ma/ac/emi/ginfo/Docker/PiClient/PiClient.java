package ma.ac.emi.ginfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class PiClient {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter the number of iterations: ");
            long iterations = scanner.nextLong();

            Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
            System.out.println("Connected to server.");

            try (
                    ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())
            ) {
                // Send the number of iterations to the server
                outputStream.writeLong(iterations);
                outputStream.flush();

                // Receive the calculated pi value from the server
                double pi = inputStream.readDouble();
                System.out.println("Calculated value of pi: " + pi);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}