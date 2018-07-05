package net.senmori.btsuite.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import net.senmori.btsuite.util.LogHandler;

import java.lang.management.ManagementFactory;

public class MainController {

    @FXML
    Tab buildTab;
    @FXML
    Tab consoleTab;

    @FXML
    void initialize() {
        LogHandler.info("This program's PID is " + ManagementFactory.getRuntimeMXBean().getName());
    }
}
