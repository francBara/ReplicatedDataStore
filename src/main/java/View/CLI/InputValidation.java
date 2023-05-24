package View.CLI;

public class InputValidation {

    public boolean validateIp(String ipAddress){
        String zeroTo255 = "(\\d{1,2}|(0|1)\\d{2}|2[0-4]\\d|25[0-5])";
        String ipRegex = zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255;
        return ipAddress.matches(ipRegex);
    }

    public boolean validateInt(String value){
        String intRegex = "-?\\d+(\\.\\d+)?";
        return value.matches(intRegex);
    }
}