package com.walhay;

import java.sql.SQLException;
import java.util.Scanner;

public class TextUserInterface {
    private final LibraryManager manager;
    private boolean running = false;
    private static final Scanner scanner = new Scanner(System.in);

    public TextUserInterface(LibraryManager manager) {
        this.manager = manager;
    }

    public void run() {
        running = true;
        while(running) {
            printOptions();
            try {
                executeOption(nextOption());
            } catch (SQLException e) {
                System.err.printf("Во время запроса произошла ошибка: %s\n", e.getMessage());
            }
        }
    }

    private void printOptions() {
        String options = """
                1. Выполнить скалярный запрос;
                2. Выполнить запрос с несколькими соединениями (JOIN);
                3. Выполнить запрос с ОТВ(CTE) и оконными функциями;
                4. Выполнить запрос к метаданным;
                5. Вызвать скалярную функцию (написанную в третьей лабораторной работе);
                6. Вызвать многооператорную или табличную функцию (написанную в третьей
                лабораторной работе);
                7. Вызвать хранимую процедуру (написанную в третьей лабораторной работе);
                8. Вызвать системную функцию или процедуру;
                9. Создать таблицу в базе данных, соответствующую тематике БД;
                10. Выполнить вставку данных в созданную таблицу с использованием
                инструкции INSERT или COPY.
                11. Удалить базу данных;
                12. Создать базу данных;
                0. Выйти;
                """;

        System.out.println(options);
    }

    private int nextOption() {
        System.out.println("\nВведите опцию [0, 12]:");
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    private void executeOption(int option) throws SQLException {
        switch (option) {
            case 0 -> running = false;
            case 1 -> {
                System.out.println("Введите логин пользователя для которого хотите получить ник:");
                String login = scanner.nextLine();
                String nickname = manager.getNameByLogin(login);
                System.out.println("Ник игрока: " + nickname);
            }
            case 2 -> {
                System.out.println("Введите логин пользователя игры которого хотите просмотреть:");
                String login = scanner.nextLine();
                var games = manager.getUserGames(login);
                System.out.println("Игры пользователя:");
                for(var game : games)
                    System.out.printf("\t- %s\n", game);
            }
            case 3 -> {
                System.out.println("Данные по всем издателям(имя издателя - средняя цена игр):");
                for(var data : manager.getPublisherData()) {
                    System.out.printf("%s - %f$\n", data.publisherName(), data.avgPrice());
                }
            }
            case 4 -> {
                System.out.println("Названия всех таблиц в базе данных:");
                for(var name : manager.getTableNames())
                    System.out.printf("\t- %s\n", name);
            }
            case 5 -> {
                System.out.println("Введите id игры для которой хотите получить цену: ");
                int id = scanner.nextInt();
                scanner.nextLine();
                double value = manager.callGetPriceById(id);
                System.out.println("Цена игры: " + value);
            }
            case 6 -> {
                System.out.println("Введите точный возраст ограничения для жанра:");
                int restriction = scanner.nextInt();
                scanner.nextLine();
                var data = manager.callGetGenreByRestriction(restriction);
                System.out.println("Жанры:");
                for(var genre : data)
                    System.out.printf("\t- %s\n",genre.genre());
            }
            case 7 -> {
                System.out.println("Введите процент увеличения цен на все игры:");
                int percent = scanner.nextInt();
                scanner.nextLine();
                manager.adjustPrice(percent);
                System.out.println("Цена повышена");
            }
            case 8 ->
                System.out.printf("Текущая версия базы данных:\n%s\n", manager.getVersion());
            case 9 -> {
                System.out.println("Создание новой таблицы");
                manager.createTable();
                System.out.println("Таблица создана успешно");
            }
            case 10 -> {
                System.out.println("Введите логин пользователя которому надо добавить комментарий:");
                String login = scanner.nextLine();
                System.out.println("Введите текст комментария:");
                String comment = scanner.nextLine();

                manager.insertComment(login, comment);
                System.out.println("Комменатрий добавлен успешно");
            }
            case 11 -> {
                System.out.println("Введите название базы данных для удаления:");
                String database = scanner.nextLine();

                manager.deleteDatabase(database);
            }
            case 12 -> {
                System.out.println("Введите название базы данных для создания:");
                String database = scanner.nextLine();

                manager.createDatabase(database);
            }
        }
    }
}
