import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        final Scanner scanner = new Scanner(System.in);

        //TODO: Now writeQuorum, readQuorum and port are hardcoded. They should be chosen by the user
        final DSCommunication dsCommunication = new DSCommunication(5000);

        System.out.println("REPLICATED DATA STORE");
        System.out.println("A - Initialize data store");
        System.out.println("B - Join data store");

        String choice = scanner.nextLine();

        if (choice.equals("A")) {
            try {
                dsCommunication.initiateDataStore(5, 5);
            } catch(IOException e) {
                System.out.println("ERROR");
            }
        }
        else if (choice.equals("B")) {
            System.out.println("Insert IP:");
            String ip = scanner.nextLine();
            try {
                dsCommunication.joinDataStore(ip, 5000);
            } catch(IOException e) {
                System.out.println("ERROR");
            }
        }
    }
}
