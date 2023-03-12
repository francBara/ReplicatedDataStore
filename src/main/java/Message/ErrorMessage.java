package Message;

public class ErrorMessage extends Message {
    private String content = "";

    public ErrorMessage() {

    }
    public ErrorMessage(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ERROR " + content;
    }
}
