package managers;

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

    private final Map<String,String> usernameToType;

    public UserManager() {
        usernameToType = new HashMap<>();
    }

    public synchronized void addUser(String username, String type) {
        usernameToType.put(username,type);
    }

    public synchronized void removeUser(String username) {
        usernameToType.remove(username);
    }

    public synchronized Map<String,String> getUsers() {
        return Collections.unmodifiableMap(usernameToType);
    }

    public boolean isUserExists(String username) {
        return usernameToType.containsKey(username);
    }
}
