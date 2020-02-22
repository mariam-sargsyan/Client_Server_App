import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.*;

public class Client {

    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket(InetAddress.getLocalHost(), 10200)) {
            System.out.println("Client is created");
            ConsoleScanner consoleScanner = new ConsoleScanner(socket);
            ServerScanner serverScanner = new ServerScanner(socket);
            FutureTask consoleFutureTask = new FutureTask(consoleScanner);
            FutureTask serverFutureTask = new FutureTask(serverScanner);
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            executorService.execute(consoleFutureTask);
            executorService.execute(serverFutureTask);
        }

    }
}

class ConsoleScanner implements Callable {

    private Socket socket;

    public ConsoleScanner(Socket socket) {
        this.socket = socket;
    }


    @Override
    public Object call() throws IOException {
        try (Writer writer = new OutputStreamWriter(socket.getOutputStream());
             Scanner consoleScanner = new Scanner(System.in)) {
            while (true) {
                if(consoleScanner.hasNext()) {
                    String currentLine = consoleScanner.nextLine();
                    if (currentLine.startsWith("Stop")) {
                        break;
                    } else {
                        writer.write(currentLine);
                        writer.flush();
                    }
                }
            }

        }
        return null;
    }
}

class ServerScanner implements Callable {

    private Socket socket;

    public ServerScanner (Socket socket) {
        this.socket = socket;
    }


    @Override
    public Object call() throws IOException {
        try (Scanner serverScanner = new Scanner(socket.getInputStream())) {
            while (!socket.isClosed() && serverScanner.hasNext()){
                String currentLine = serverScanner.nextLine();
                if (currentLine.startsWith("Stop")) {
                    break;
                } else {
                    System.out.println(currentLine);
                }
            }

        }
        return null;
    }
}