import junit.framework.TestCase;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkTest extends TestCase {
    public void testNetwork() {
        new Thread(() -> {
            try {
                final ServerSocket serverSocket = new ServerSocket(5000);
                final ExecutorService executor = Executors.newCachedThreadPool();

                while (true) {
                    //For every open connection, a new thread starts
                    final Socket clientSocket = serverSocket.accept();

                    executor.submit(() -> {
                        System.out.println("OK");
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            fail();
                        }
                    });
                }
            } catch(Exception e) {
                fail();
            }
        }).start();

        final HashSet<Socket> sockets = new HashSet<>();

        for (int i = 0; i < 40; i++) {
            new Thread(() -> {
                try {
                    sockets.add(new Socket("127.0.0.1", 5000));
                } catch (IOException e) {
                    System.out.println(e);
                    fail();
                }
            }).start();
        }

        try {
            Thread.sleep(1000);
        } catch(Exception ignored) {fail();}

        for (Socket socket : sockets) {
            try {
                socket.close();
            } catch(Exception e) {
                fail();
            }
        }
    }
}
