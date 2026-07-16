package org.red.minecraft.uw.core.skill.controller;

public abstract class Controller {
    public Controller(Runnable expire) {

    }

    public abstract void start();

    public abstract void stop();

    protected abstract void tick();
}
