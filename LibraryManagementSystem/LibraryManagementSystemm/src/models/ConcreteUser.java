package models;

public class ConcreteUser extends AbstractUser {
    public ConcreteUser(String id, String name, String email, String password, Role role) {
        super(id, name, email, password, role);
    }

    @Override
    public String getDashboardName() {
        return (role == Role.ADMIN) ? "Personel Paneli" : "Öğrenci Portalı";
    }
}