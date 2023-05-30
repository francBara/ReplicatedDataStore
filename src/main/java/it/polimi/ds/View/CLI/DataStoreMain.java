package it.polimi.ds.View.CLI;

import it.polimi.ds.DataStore.DataStoreNetwork;
import it.polimi.ds.DataStore.Exceptions.FullDataStoreException;
import it.polimi.ds.DataStore.Exceptions.QuorumNumberException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DataStoreMain {

    private BufferedReader input;
    private InputValidation inputValidation;
    private ClientInput clientInput;

    public static void main(String[] args) {
        DataStoreMain dataStoreMain = new DataStoreMain();
        dataStoreMain.start();
    }

    public void start(){
        input = new BufferedReader(new InputStreamReader(System.in));
        inputValidation = new InputValidation();
        clientInput = new ClientInput();

        System.out.println("\nDatastore Main! Here you can Initialize or join a datastore.\n");

        String s;
        do{
            s = clientInput.nextLine(input,"Please choose your port number\n> ");
        }while(!inputValidation.validateInt(s));

        int port = Integer.parseInt(s);

        final DataStoreNetwork dsCommunication = new DataStoreNetwork();
        dsCommunication.setPort(port);

        do{
            s = clientInput.nextLine(input,"Please type 'initiate' or 'join'\n> ");
        }while(!s.equals("initiate") &&!s.equals("join"));


        if (s.equals("initiate")) {
            String wQuorum, rQuorum;
            do{
                wQuorum = clientInput.nextLine(input, "Choose the write Quorum number\n> ");
                rQuorum = clientInput.nextLine(input, "Choose the read Quorum number\n> ");
            }while(!(inputValidation.validateInt(wQuorum) && inputValidation.validateInt(rQuorum)));

            try {
                dsCommunication.initiateDataStore(Integer.parseInt(wQuorum), Integer.parseInt(rQuorum));
            } catch(IOException | QuorumNumberException e) {
                System.out.println("ERROR");
            }
        }
        else{
            String ip;
            String dataStorePort;
            do {
                ip = clientInput.nextLine(input, "Please insert the ip address of the datastore\n> ");
            }while(!inputValidation.validateIp(ip));

            do {
                dataStorePort = clientInput.nextLine(input, "Please insert the datastore port number\n> ");
            }while(!inputValidation.validateInt(dataStorePort));

            try {
                dsCommunication.joinDataStore(ip, Integer.parseInt(dataStorePort));
            } catch(IOException  | FullDataStoreException e) {
                System.out.println(e);
           }
        }
    }
}