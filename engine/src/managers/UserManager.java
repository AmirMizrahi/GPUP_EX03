package managers;

import DTO.UserDTO;
import User.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/*
Adding and retrieving users is synchronized and in that manner - these actions are thread safe
Note that asking if a user exists (isUserExists) does not participate in the synchronization and it is the responsibility
of the user of this class to handle the synchronization of isUserExists with other methods here on it's own
 */
public class UserManager {

    private final Map<String,User> usernameToType;

    public UserManager() {
        usernameToType = new HashMap<>();
    }

    public synchronized void addUser(String username, String type, int threads) {
        usernameToType.put(username,new User(username,type,threads));
    }

    public synchronized void addUser(String username, String type) {
        usernameToType.put(username,new User(username,type));
    }

    public synchronized void removeUser(String username) {
        usernameToType.remove(username);
    }

    public synchronized Map<String, UserDTO> getUsers() {
        Map<String,UserDTO> toReturn = new HashMap<>();
        for (Map.Entry<String, User> entry : usernameToType.entrySet()) {
            toReturn.put(entry.getKey(), new UserDTO(entry.getValue().getName(),entry.getValue().getType(),entry.getValue().getThreadAmount()));
        }
        return Collections.unmodifiableMap(toReturn);
    }

    public boolean isUserExists(String username) {
        return usernameToType.containsKey(username);
    }
}
