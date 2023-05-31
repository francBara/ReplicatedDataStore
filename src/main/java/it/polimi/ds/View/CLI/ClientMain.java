package it.polimi.ds.View.CLI;

import it.polimi.ds.Client.Client;
import it.polimi.ds.Client.Exceptions.ReadException;
import it.polimi.ds.DataStore.DataStoreState.DSElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class ClientMain {

    private Client client;
    private BufferedReader input;
    private boolean connected;
    private ClientInput clientInput;
    private InputValidation validation;


    public static void main(String[] args) {
        ClientMain main = new ClientMain();
        main.start();
    }

    private void start(){
        client = new Client();
        input =  new BufferedReader(new InputStreamReader(System.in));
        clientInput = new ClientInput();
        connected = false;
        System.out.println("\nWelcome to the DataStore! Here you can store and retrieve data.\n");

        new Thread(() -> {
            do {
                if (connected){
                    String s = clientInput.nextLine(this.input,"> ");
                    readInput(s);
                }
                else {
                    connect();
                }
            } while (true);
        }).start();
    }


    private void connect(){
        System.out.println("Connection phase");
        validation = new InputValidation();

        boolean isValidIp = false;
        boolean isValidPort = false;
        String dataStoreIp = "";
        String dataStorePort = "";

        while(!(isValidIp && isValidPort)) {
            dataStoreIp = clientInput.nextLine(this.input,"Please provide the IP address of the datastore\n> ");

            isValidIp = validation.validateIp(dataStoreIp);
            if(!isValidIp){
                System.out.println("Please enter a valid ip address.");
                continue;
            }

            dataStorePort =  clientInput.nextLine(input,"Please provide the port number of the datastore\n> ");
            isValidPort = validation.validateInt(dataStorePort);

            if(!isValidPort){
                System.out.println("Please enter a valid integer.");
            }
        }

        client.bind(dataStoreIp, Integer.parseInt(dataStorePort));
        clientInput.clearConsole();
        System.out.println("Connected at "+ dataStoreIp +": "+ Integer.parseInt(dataStorePort)+ "\n");

        System.out.println("\nPerform a read by typing 'read' or a write by typing 'write'.\nType 'help' in any moment for the list of possible actions.\nType 'write many' to perform multiple writes.");
        connected = true;
    }

    private void readInput(String input) {
        input = input.toUpperCase(Locale.ROOT);

        switch (input) {
            case "WRITE" -> {
                String key =  clientInput.nextLine(this.input,"Insert the key: ");
                String value =  clientInput.nextLine(this.input,"Insert the value: ");
                try {
                    if(client.write(key, value)) {
                        System.out.println(Colors.GREEN + "Write executed"+ Colors.RESET);
                    }else{
                        System.out.println(Colors.RED + "Could not perform the operation!" + Colors.RESET);
                    }
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
            case "WRITE MANY" -> {
                String key =  clientInput.nextLine(this.input,"Insert the key: ");
                String value =  clientInput.nextLine(this.input,"Insert the value: ");
                String times;
                do{
                    times = clientInput.nextLine(this.input, "How many writes do you want to perform? ");
                }while(!validation.validateInt(times));
                client.writeMany(Integer.parseInt(times), key, value);
                System.out.println(Colors.GREEN + "Write executed"+ Colors.RESET);
            }
            case "READ" -> {
                String key = clientInput.nextLine(this.input,"Key to read from: ");
                try {
                    DSElement dsElement = client.read(key);
                    if(dsElement.isNull()){
                    System.out.println(Colors.RED + "This element has never been written!"+ Colors.RESET);
                    }else{
                        System.out.println(Colors.GREEN +"READ KEY: "+ key + "\nREAD VALUE: "+dsElement.getValue()+"\nVERSION NUMBER: "+dsElement.getVersionNumber()+ Colors.RESET);
                    }
                } catch (IOException | ReadException e) {
                    System.out.println(e);
                }
            }
            case "HELP" -> {
                clientInput.clearConsole();
                System.out.println(Colors.YELLOW + """
                Here is the list of possible actions:
                'read': allows to read the value of the key you provide
                'write': allows to write the value of the key you provide
                'write many': allows to write multiple values in parallel
                'exit': allows to disconnect from the DataStore
                'clear: clears the console"""+ Colors.RESET);
            }

            case "CLEAR" -> clientInput.clearConsole();
            case "EXIT" -> {
                String key = clientInput.nextLine(this.input,"Are you sure you want to disconnect? (y/n)\n> ");
                if (key.equals("y")) {
                    clientInput.clearConsole();
                    System.out.println("--Disconnected--\n");
                    connected = false;
                } else if (key.equals("n")) {
                    System.out.println("\nPerform a read by typing 'read' or a write by typing 'write'.\nType 'write many' to perform multiple writes.\nType 'help' in any moment for the list of possible actions.");
                } else {
                    System.out.println(Colors.RED+ "Cannot handle this operation!\n"+Colors.RESET);
                }
            }
            case "" -> System.out.print("");
            default -> {
                System.out.println(Colors.RED+ "Cannot handle this operation!\n"+Colors.RESET);
                System.out.println("\nPerform a read by typing 'read' or a write by typing 'write'.\nType 'help' in any moment for the list of possible actions.");
            }
        }
    }
}