package DTO;

import User.User;

public class UserDTO {
    private String name;
    private String type;
    private int threadAmount;

    public UserDTO(String name, String type, int threadAmount){
        this.name = name;
        this.type = type;
        this.threadAmount = threadAmount;
    }

    public String getName(){return this.name;}

    public String getType(){return this.type;}

    public int getThreadsAmount(){return this.threadAmount;}

}
