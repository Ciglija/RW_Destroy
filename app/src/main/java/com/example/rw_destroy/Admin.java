package com.example.rw_destroy;

public class Admin extends User {

    private String adminPassword;

    public Admin(String name, String username, String password, String adminPassword) {
        super(name, username, password);
        this.adminPassword = adminPassword;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    @Override
    public boolean isAdmin() {
        return true;
    }

}
