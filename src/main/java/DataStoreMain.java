import Exceptions.QuorumNumberException;

import java.io.IOException;
import java.util.Scanner;

public class DataStoreMain {
    public static void main(String[] args) {
        //TODO: Improve overall UX

        final Scanner scanner = new Scanner(System.in);

        System.out.println("Choose a port:");

        int port = scanner.nextInt();
        scanner.nextLine();

        //TODO: writeQuorum and readQuorum are hardcoded. They should be chosen by the user.
        final DSCommunication dsCommunication = new DSCommunication(port);


        System.out.println("\n\nA - Initialize data store");
        System.out.println("B - Join data store");

        String choice = scanner.nextLine();

        if (choice.equals("A")) {
            try {
                dsCommunication.initiateDataStore(5, 5);
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
    }
}
