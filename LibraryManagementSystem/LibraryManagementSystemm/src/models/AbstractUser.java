package models;

public abstract class AbstractUser {
    protected String id;
    protected String name;
    protected String email;
    protected String password;
    protected Role role;
    protected double totalPenalty;

    public AbstractUser(String id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.totalPenalty = 0.0;
    }

    public abstract String getDashboardName();


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public double getTotalPenalty() {
        return totalPenalty;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTotalPenalty(double penalty) {
        this.totalPenalty = penalty;
    }
}