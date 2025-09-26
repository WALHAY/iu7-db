# Семинар 2

* [DDL](#DDL)
* [DML](#DML)

## DDL

### Create

```SQL
CREATE TABLE имя_таблицы (
    {<имя_атрибута> <тип> <ограничения> [,] }
    [<ограничение на таблицу целиком>]
)
[<тип сжатия>]
[<тип добавления записи в таблицу>]
[<тип распределения>]
```

имя_таблицы - db.имя_схемы.имя_объекта\
default db = postgres 

имя_схемы\
default schema = public

в рамках разных схем таблицы могут повторяться

### Типы данных

- число
    * целое
        - serial
        - int
    * вещественное
        - float
        - decimal
    * число с точностью
        - numeric(n - всего, m - точность)
        - number = numeric(38, 0)
- строка
    * строка с фиксированным количеством символов
        - varchar(n) | nvarchar(n)
    * строка переменной длины
        - text
    * символ
        - char
- enum
- дата/время
    * Дата 
        - date
    * Время
        - time
    * Датавремя
        - datetime
        - timestamp(G)
- логический тип
    * bool
- json | jsonb | xml
- массивы
    * например int[]
- UUID

### Ограничения (constraint)

1. [NOT] null\
    !! ВАЖНО !!\
    null != null
2. Check
3. Unique
4. Primary Key | Foreign Key
5. Default

### Drop

```SQL
DROP TABLE [IF EXISTS] <имя>;
```

### Alter

```SQL
ALTER TABLE <имя> 
    ADD COLUMN <имя> <тип>
    DROP COLUMN <имя>
    RENAME <имя> to <имя>
```

## DML

| Команда | Порядок использования | Выполнение СУБД |
|-|-|-|
|Select|1|5|
|From|2|1|
|Where|3|2|
|Group by|4|3|
|Having|5|4|
|Order by|6|6|

### Select

```SQL
SELECT * FROM P as p

SELECT p.Pno as ID, Pname FROM P as p
```

### Where

Предикаты:
1. Сравнениe: <, >, <=, >=, =, !=\
2. Between: between 10 and 12\
    эквивалент attr >= 10 and attr <= 12\
3. In: color in 'K', '3'
4. Is [not] null
5. Like
6. Exists

```SQL
SELECT * FROM P WHERE Color = 'K'
```
