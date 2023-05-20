import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class ClientInput {

    private Client client;
    private BufferedReader input;
    private final String welcomeMessage = "Welcome to the DataStore! Here you can store and retrieve data.\n";
    private final String actionMessage = "Perform a read by typing 'read' or a write by typing 'write'.\nType 'help' for the list of possible actions.\n";
    private final String helpMessage = """
                Here is the list of possible actions:
                'read': allows to read the value of the key you provide
                'write': allows to write the value of the key you provide
                'exit': allows to disconnect from the DataStore""";
    private final String errorMessage = "Cannot handle this operation!";
    private final String disconnect = "Are you sure you want to disconnect? (yes/no) \n> ";


    public static void main(String[] args) {
        ClientInput main = new ClientInput();
        main.start();
    }

    private void start(){
        input =  new BufferedReader(new InputStreamReader(System.in));
        client = new Client();

        // TODO: HANDLE THE BINDING OF THE CLIENT WITH OK OR KO MESSAGES
        client.bind("127.0.0.1",5000);


        showMessage(welcomeMessage);
        showMessage(actionMessage);

        Thread mainThread = new Thread(() -> {
                    do {
                        String s = nextLine("> ");
                        readInput(s);
                    } while (true);
                });
        mainThread.start();
    }

    private void showMessage(String str){
        System.out.println(str);
    }

    private String nextLine(String str)
    {
        String s = "";
        System.out.print(str);
        try {
            s = input.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    private void readInput(String input) {
        input = input.toUpperCase(Locale.ROOT);

        switch (input) {
            case "WRITE" -> {

                String key = nextLine("Insert the key: ");
                String value = nextLine("Insert the value: ");
                try {
                    client.write(key, value);
                } catch (IOException e) {
                    System.out.println("FATAL ERROR");
                }
            }
            case "READ" -> {

                String key = nextLine("Key to read from:");
                try {
                    client.read(key);
                } catch (IOException e) {
                    System.out.println("FATAL ERROR");
                }
            }
            case "HELP" -> showMessage(helpMessage);
            case "EXIT" -> {
                String key = nextLine(disconnect);
                if (key.equals("yes")) {
                    System.out.println("disconnecting...");
                } else if (key.equals("no")) {
                    System.out.println(actionMessage);
                } else {
                    System.out.println(errorMessage);
                }
            }
            default -> System.out.println(errorMessage);
        }

    }
}