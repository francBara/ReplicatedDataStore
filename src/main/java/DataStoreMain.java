import Exceptions.QuorumNumberException;

import java.io.IOException;
import java.util.Scanner;

public class DataStoreMain {

    public static void main(String[] args) {
        //TODO: Improve overall UX

        final Scanner scanner = new Scanner(System.in);

        int port = chooseInt(scanner,"port");
        scanner.nextLine();

        // TODO: writeQuorum and readQuorum are hardcoded. They should be chosen by the user
        // I CREATED TWO FUNCTIONS:  chooseInt to choose port, wQuorum and rQuorum, then chooseRole to initialize or join a datastore

        final DSCommunication dsCommunication = new DSCommunication(port);

        String choice = chooseRole(scanner);

        if (choice.equals("A")) {
            int wQuorum = chooseInt(scanner,"writeQuorum");
            int rQuorum = chooseInt(scanner,"readQuorum");
            try {
                dsCommunication.initiateDataStore(wQuorum, rQuorum);
            } catch(IOException | QuorumNumberException e) {
                System.out.println("ERROR");
            }
        }
        else if (choice.equals("B")) {
            System.out.println("Insert IP:");
            String ip = scanner.nextLine();
            try {
                dsCommunication.joinDataStore(ip, 5000);
            } catch(IOException e) {
                System.out.println(e);
            }
        }
        //scanner.close();
    }

    private static int chooseInt(Scanner scanner, String text){
        System.out.println("Please choose the "+text+"number:");
        while(!scanner.hasNextInt()){
            System.out.println("Invalid input: please enter a valid "+text+" number!");
            scanner.next();
        }
        return scanner.nextInt();
    }
    private static String chooseRole(Scanner scanner) {
        System.out.println("\n\nA - Initialize data store");
        System.out.println("B - Join data store");
        String param = scanner.nextLine();
        while (!param.equals("A") && !param.equals("B")) {
            System.out.println("Invalid input: please enter either 'A' or 'B'");
            param = scanner.nextLine();
        }
        return param;
    }
}