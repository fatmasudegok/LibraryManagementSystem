package logic;

import models.ConcreteUser;

public class SessionManager {
    private static SessionManager instance;
    private ConcreteUser currentUser;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null)
            instance = new SessionManager();
        return instance;
    }

    public void login(ConcreteUser user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public ConcreteUser getCurrentUser() {
        return currentUser;
    }
}