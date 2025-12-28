package com.walhay;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static final String DB = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/";
    private static final String LOGIN = "postgres";
    private static final String PASSWORD = "postgres";

    public static void main(String[] args) throws SQLException {
        Connection connection = DriverManager.getConnection(URL + DB, LOGIN, PASSWORD);
        LibraryManager manager = new LibraryManager(connection);
        TextUserInterface tui = new TextUserInterface(manager);

        tui.run();

        connection.close();
    }
}