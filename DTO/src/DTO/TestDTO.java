package DTO;

public class TestDTO {

private UserDTO userDTO;
private Boolean b;

    public TestDTO(UserDTO userDTO, Boolean b) {
        this.userDTO = userDTO;
        this.b = b;
    }

    public UserDTO getUserDTO() {
        return userDTO;
    }

    public Boolean getB() {
        return b;
    }
}
