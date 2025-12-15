package logic;

import models.AbstractUser;
import models.ConcreteUser;
import models.Role;

public class UserFactory {

    public static AbstractUser createUser(Role role, String id, String name, String email, String password) {
        if (role == null) {
            return null;
        }

        return new ConcreteUser(id, name, email, password, role);
    }
}