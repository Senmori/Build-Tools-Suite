package net.senmori.btsuite.gui;

import javafx.scene.control.TextArea;

public class BuildToolsConsole {
    private final TextArea console;

    public BuildToolsConsole(TextArea area) {
        this.console = area;
    }


    public TextArea getConsole() {
        return console;
    }
}
