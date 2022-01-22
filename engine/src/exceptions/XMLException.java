package exceptions;

public class XMLException extends Exception {
    private String message;

    public XMLException(String newMessage) {
        this.message = newMessage;
    }

    @Override
    public String getMessage() {
        return this.message + "\n XML not loaded!";
    }
}
