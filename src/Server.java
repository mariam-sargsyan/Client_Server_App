import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.*;

public class Server {

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(10200)) {
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            int clientCounter = 0;
            while(clientCounter < 5) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                FutureTask futureTask = new FutureTask(clientHandler);
                executorService.execute(futureTask);
                clientCounter++;
            }
        }
    }
}

class ClientHandler implements Callable {

    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public Object call() throws Exception {
        try (Scanner scanner = new Scanner(clientSocket.getInputStream());
             Writer writer = new OutputStreamWriter(clientSocket.getOutputStream())) {
            while (!clientSocket.isClosed()) {
                if (scanner.hasNext()) {
                    String currentLine = scanner.nextLine();
                    if (currentLine.contains("Stop")) {
                        writer.write("The process is stopped");
                        writer.flush();
                        break;
                    }
                    System.out.println("Message Received: " + currentLine);
                    writer.write("Message Received");
                    writer.flush();
                }
            }
        }
        return null;
    }
}
