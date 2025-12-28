package com.walhay.lab09;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPooled;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static final String DB = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/";
    private static final String LOGIN = "postgres";
    private static final String PASSWORD = "postgres";

    private static void printItems(Collection<?> collection) {
        collection.stream().map(Object::toString).forEach(System.out::println);
    }

    public static void main(String[] args) throws SQLException, FileNotFoundException, InterruptedException {
        Connection connection = DriverManager.getConnection(URL + DB, LOGIN, PASSWORD);
        Jedis redis = new Jedis("redis://localhost:6379");
        LibraryManager manager = new LibraryManager(connection, redis);

        Scanner scanner = new Scanner(System.in);

        boolean running = true;

        while(running) {
            System.out.println("""
                    0. Выйти;
                    1. Запрос статистической информации;
                    2. Запрос на стороне БД каждые 5 сек;
                    3. Запрос через redis каждые 5 сек;
                    4. Собрать данные;
                    """);
            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 0 -> running = false;
                case 1 -> {
                    System.out.println("Игры по жанрам бд: horror");
                    printItems(manager.getGamesByGenrePostgres("Horror"));
                    System.out.println("Игры по жанрам redis: horror");
                    printItems(manager.getGamesByGenreRedis("Horror"));
                }
                case 2 -> {
                    while(true) {
                        printItems(manager.getGamesByGenrePostgres("Action"));
                        Thread.sleep(Duration.ofSeconds(5));
                    }
                }
                case 3 -> {
                    while(true) {
                        printItems(manager.getGamesByGenreRedis("Action"));
                        Thread.sleep(Duration.ofSeconds(5));
                    }
                }
                case 4 -> {
                    File file = new File("./data/select.txt");
                    PrintWriter pw = new PrintWriter(file);
                    for (int i = 0; i < 10; ++i) {
                        manager.selectQuery(pw);
                        Thread.sleep(Duration.ofSeconds(10));
                    }
                    pw.close();

                    List<String> logins = new ArrayList<>();
                    file = new File("./data/insert.txt");
                    pw = new PrintWriter(file);
                    for (int i = 0; i < 10; ++i) {
                        logins.add(manager.insertQuery(pw));
                        Thread.sleep(Duration.ofSeconds(10));
                    }
                    pw.close();

                    file = new File("./data/update.txt");
                    pw = new PrintWriter(file);
                    for (String login : logins) {
                        manager.updateQuery(login, pw);
                        Thread.sleep(Duration.ofSeconds(10));
                    }
                    pw.close();

                    file = new File("./data/delete.txt");
                    pw = new PrintWriter(file);
                    for (String login : logins) {
                        manager.deleteQuery(login, pw);
                        Thread.sleep(Duration.ofSeconds(10));
                    }
                    pw.close();
                }
            }
        }
        connection.close();
        redis.close();
    }
}