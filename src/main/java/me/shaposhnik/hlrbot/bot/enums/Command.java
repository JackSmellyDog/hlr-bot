package me.shaposhnik.hlrbot.bot.enums;

public enum Command {
    STOP("/stop"),
    HLR("/hlr");

    private String command;

    Command(String command) {
        this.command = command;
    }
}
