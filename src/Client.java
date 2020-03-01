import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class Client {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(InetAddress.getLocalHost(), 10200);
        System.out.println("Client is created");
        ConsoleScanner consoleScanner = new ConsoleScanner(socket.getOutputStream());
        ServerScanner serverScanner = new ServerScanner(socket);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        Future task1 =  executorService.submit(consoleScanner);
        Future task2 =  executorService.submit(serverScanner);
        SocketHandler socketHandler = new SocketHandler(List.of(task1, task2), socket);
        executorService.submit(socketHandler);
    }
}

class SocketHandler implements Runnable {

    private List<Future> socketTasks;
    private Socket socket;

    SocketHandler(List<Future> socketTasks, Socket socket) {
        this.socketTasks = socketTasks;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if(socketTasks.stream().allMatch(task -> task.isDone())) {
                    socket.getInputStream().close();
                    socket.getOutputStream().close();
                    socket.close();
                } else {
                    Thread.sleep(5_000);
                }
            }
        } catch (IOException e) {
            int a = 0;
        } catch (InterruptedException e){
            int a = 2;
        }

    }
}

class ConsoleScanner implements Callable {

    private OutputStream outputStream;

    public ConsoleScanner(OutputStream outputStream) {
        this.outputStream = outputStream;
    }


    @Override
    public Object call() throws IOException {
        try (Writer writer = new OutputStreamWriter(outputStream);
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

        } catch (Exception e) {
            int a = 2;
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

        } catch (Exception e) {
            int a = 3;
        }
        return null;
    }
}