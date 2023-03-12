package Message;

public class MessageFactory {
    //TODO: Replace RuntimeException occurrences with proper errors
    public Message buildMessage(String message) {
        final String[] splitMessage = message.split(" ");

        if (splitMessage.length == 0) {
            throw(new RuntimeException());
        }

        if (splitMessage[0].equals("JOIN")) {
            if (splitMessage.length != 3) {
                throw(new RuntimeException());
            }
            return new JoinMessage(Integer.parseInt(splitMessage[1]), Integer.parseInt(splitMessage[2]));
        }
        else if (splitMessage[0].equals("READ")) {
            if (splitMessage.length != 3) {
                throw(new RuntimeException());
            }
            return new ReadMessage(splitMessage[1], Integer.parseInt(splitMessage[2]), false);
        }
        else if (splitMessage[0].equals("READ_Q")) {
            if (splitMessage.length != 3) {
                throw(new RuntimeException());
            }
            return new ReadMessage(splitMessage[1], Integer.parseInt(splitMessage[2]), true);
        }
        else if (splitMessage[0].equals("WRITE")) {
            if (splitMessage.length != 4) {
                throw(new RuntimeException());
            }
            return new WriteMessage(splitMessage[1], splitMessage[2], Integer.parseInt(splitMessage[3]), false);
        }
        else if (splitMessage[0].equals("WRITE_Q")) {
            if (splitMessage.length != 4) {
                throw(new RuntimeException());
            }
            return new WriteMessage(splitMessage[1], splitMessage[2], Integer.parseInt(splitMessage[3]), true);
        }
        else if (splitMessage[0].equals("ERROR")) {
            if (splitMessage.length > 2) {
                throw(new RuntimeException());
            }
            return new ErrorMessage();
        }

        throw(new RuntimeException());
    }
}
