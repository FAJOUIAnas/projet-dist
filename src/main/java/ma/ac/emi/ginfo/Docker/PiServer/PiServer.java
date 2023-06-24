package ma.ac.emi.ginfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class PiServer {

    public static List<Node> nodes = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length % 2 != 1) {
            System.out.println("Invalid arguments");
            return;
        }

        for (int i = 1; i < args.length; i += 2) {
            nodes.add(new Node(args[i], Integer.parseInt(args[i + 1])));
        }

        System.out.println(nodes);

        try {
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
            System.out.println("Server started. Waiting for client connection...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Handle client request in a separate thread
                Thread clientThread = new Thread(() -> handleClientRequest(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClientRequest(Socket clientSocket) {
        List<Socket> sockets = new ArrayList<>();
        List<ObjectOutputStream> outputNodeStreams = new ArrayList<>();
        List<ObjectInputStream> inputNodeStreams = new ArrayList<>();

        for (Node node : nodes) {
            try {
                Socket socket = new Socket(node.getIpAddress(), node.getPort());
                sockets.add(socket);
                System.out.println("Node on port " + node.getPort() + " is connected");
                ObjectOutputStream outputNodeStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inputNodeStream = new ObjectInputStream(socket.getInputStream());
                outputNodeStreams.add(outputNodeStream);
                inputNodeStreams.add(inputNodeStream);
            } catch (IOException e) {
                System.out.println("Port " + node.getPort() + " is not available");
            }
        }

        System.out.println(sockets);

        try (ObjectOutputStream outputClientStream = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream inputClientStream = new ObjectInputStream(clientSocket.getInputStream())) {

            long iterations = inputClientStream.readLong();
            int numNodes = sockets.size();
            long iterationsPerNode = iterations / numNodes;
            long remainingIterations = iterations % numNodes;

            long[] iteration = new  long[2];
            iteration[0] = 0;


            for (ObjectOutputStream outputStream : outputNodeStreams) {
                long partialIterations = iterationsPerNode;
                if (remainingIterations > 0) {
                    partialIterations++;
                    remainingIterations--;
                }

                iteration[1] = partialIterations;


                outputStream.writeObject(iteration);
                outputStream.flush();

                iteration[0] += partialIterations;

            }

            double sum = 0.0;

            for (ObjectInputStream inputStream : inputNodeStreams) {
                sum += inputStream.readDouble();
            }

            double pi = sum * 4;

            outputClientStream.writeDouble(pi);
            outputClientStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                for (Socket socket : sockets) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class Node {
    private Integer port;
    private String ipAddress;

    public Node(String ipAddress, Integer port) {
        this.port = port;
        this.ipAddress = ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
