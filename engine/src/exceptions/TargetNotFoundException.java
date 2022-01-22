package exceptions;

public class TargetNotFoundException extends Exception {
    private String EXCEPTION_MESSAGE;

    public TargetNotFoundException(String nameOfTarget) {
        this.EXCEPTION_MESSAGE = String.format("Targets.Target '%s' not found", nameOfTarget);
    }

    @Override
    public String getMessage() {
        return EXCEPTION_MESSAGE;
    }
}
