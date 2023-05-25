package it.polimi.ds.View.CLI;

import it.polimi.ds.Client.Client;
import it.polimi.ds.DataStore.DataStoreState.DSElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class ClientInput {

    private Client client;
    private BufferedReader input;
    private boolean connected;

    public static void main(String[] args) {
        ClientInput main = new ClientInput();
        main.start();
    }

    private void start(){
        client = new Client();
        input =  new BufferedReader(new InputStreamReader(System.in));
        connected = false;
        System.out.println("\nWelcome to the DataStore! Here you can store and retrieve data.\n");

        new Thread(() -> {
                    do {
                        if (connected){
                            String s = nextLine("> ");
                            readInput(s);
                        }
                        else {
                            connect();
                        }
                    } while (true);
        }).start();
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

    private void connect(){
        System.out.println("Connection phase");
        InputValidation validation = new InputValidation();

        boolean isValidIp = false;
        boolean isValidPort = false;
        String dataStoreIp = "";
        String dataStorePort = "";

        while(!(isValidIp && isValidPort)) {
            dataStoreIp = nextLine("Please provide the IP address of the datastore\n> ");

            isValidIp = validation.validateIp(dataStoreIp);
            if(!isValidIp){
                System.out.println("Please enter a valid ip address.");
                continue;
            }

            dataStorePort = nextLine("Please provide the port number of the datastore\n> ");
            isValidPort = validation.validateInt(dataStorePort);

            if(!isValidPort){
                System.out.println("Please enter a valid integer.");
            }
        }

        client.bind(dataStoreIp, Integer.parseInt(dataStorePort));
        System.out.println("Connected at "+ dataStoreIp +": "+ Integer.parseInt(dataStorePort)+ "\n");

        System.out.println("\nPerform a read by typing 'read' or a write by typing 'write'.\nType 'help' in any moment for the list of possible actions.\n");
        connected = true;
    }

    private void readInput(String input) {
        input = input.toUpperCase(Locale.ROOT);

        switch (input) {
            case "WRITE" -> {
                String key = nextLine("Insert the key: ");
                String value = nextLine("Insert the value: ");
                try {
                    client.write(key, value);
                    System.out.println("Write executed");
                } catch (IOException e) {
                    System.out.println(e);
                    System.out.println("Operation failed");
                }
            }
            case "READ" -> {
                String key = nextLine("Key to read from:");
                try {
                    DSElement dsElement = client.read(key);
                    System.out.println(dsElement);
                    System.out.println("Read executed");
                } catch (IOException e) {
                    System.out.println(e);
                    System.out.println("Operation failed");
                }
            }
            case "HELP" -> System.out.println("""
                Here is the list of possible actions:
                'read': allows to read the value of the key you provide
                'write': allows to write the value of the key you provide
                'exit': allows to disconnect from the DataStore""");

            case "EXIT" -> {
                String key = nextLine("Are you sure you want to disconnect? (y/n)\n> ");
                if (key.equals("y")) {
                    System.out.println("disconnecting...\n");
                    connected = false;
                    // TODO: handle the un-binding of the client
                    client.bind("",0);
                } else if (key.equals("n")) {
                    System.out.println("\nPerform a read by typing 'read' or a write by typing 'write'.\nType 'help' in any moment for the list of possible actions.\n");
                } else {
                    System.out.println("Cannot handle this operation!\n");
                }
            }
            default -> {
                System.out.println("Cannot handle this operation!\n");
                System.out.println("\nPerform a read by typing 'read' or a write by typing 'write'.\nType 'help' in any moment for the list of possible actions.\n");
            }
        }
    }
}