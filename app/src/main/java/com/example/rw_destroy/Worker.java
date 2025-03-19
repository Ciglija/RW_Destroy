package com.example.rw_destroy;

public class Worker extends User{
    public Worker(String name, String username, String password) {
        super(name, username, password);
    }

    @Override
    public boolean isAdmin() {
        return false;
    }

}
