package User;

public class User {
    private String name;
    private String type;
    private int threadAmount = -1;

    public User(String name, String type, int threadAmount){
        this.name = name;
        this.type = type;
        this.threadAmount = threadAmount;
    }

    public User(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getThreadAmount() {
        return threadAmount;
    }
}
