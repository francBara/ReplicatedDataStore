package it.polimi.ds.View.CLI;

import java.io.BufferedReader;

public class ClientInput {

    public String nextLine(BufferedReader input,String str)
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

    public void clearConsole(){
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

}
