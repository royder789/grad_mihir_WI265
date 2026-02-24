package com.example.demo;

public class Service {

    private I i;

    public Service(I i) {
        this.i = i;
    }

    public void perform() {
        i.abc();
        i.abc();
        i.abc();
    }
}