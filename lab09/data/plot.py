import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages
import os

# Функция для чтения данных из файла
def read_data(filename):
    postgres_times = []
    redis_times = []
    try:
        with open(filename, 'r') as f:
            for line in f:
                if ',' in line:
                    parts = line.strip().split(',')
                    if len(parts) >= 2:
                        postgres_times.append(int(parts[0]))
                        redis_times.append(int(parts[1]))
    except FileNotFoundError:
        print(f"File {filename} not found.")
    return postgres_times, redis_times

# Названия файлов
files = ['select.txt', 'insert.txt', 'update.txt', 'delete.txt']
titles = ['Select Operation', 'Insert Operation', 'Update Operation', 'Delete Operation']

# Создание PDF файла
with PdfPages('execution_times.pdf') as pdf:
    for filename, title in zip(files, titles):
        pg_times, redis_times = read_data(filename)

        if not pg_times:
            continue

        iterations = list(range(1, len(pg_times) + 1))

        plt.figure(figsize=(10, 6))
        plt.plot(iterations, pg_times, marker='o', label='PostgreSQL', color='blue')
        plt.plot(iterations, redis_times, marker='s', label='Redis', color='red')

        plt.title(f'Execution Time Comparison: {title}')
        plt.xlabel('Query Number')
        plt.ylabel('Time (nanoseconds)')
        plt.legend()
        plt.grid(True)

        pdf.savefig()  # Сохраняем текущий график в PDF
        plt.close()

print("PDF file 'execution_times.pdf' generated successfully.")