import csv
from faker import Faker
import random
from datetime import datetime, timedelta
import os

class LibraryDataGenerator:
    def __init__(self):
        self.fake = Faker()
        self.used_logins = set()
        self.used_publisher_names = set()
        
        # Predefined genres as per your schema
        self.base_genres = [
            "Action", "Adventure", "RPG", "Strategy", "Simulation",
            "Sports", "Racing", "Puzzle", "Horror", "FPS",
            "MMORPG", "Platformer", "Sandbox", "Survival", "Battle Royale",
            "Visual Novel", "Rhythm", "Tower Defense", "Card Game", "Educational",
            "Stealth", "Open World", "Roguelike", "Metroidvania", "Shooter",
            "Fighting", "Flight Simulator", "Tycoon", "Party", "Trivia"
        ]
        
    def _generate_unique_login(self):
        """Генерация уникального логина"""
        login = f"{self.fake.user_name()}"
        counter = 1
        while login in self.used_logins:
            login = f"{self.fake.user_name()}_{counter}"
            counter += 1
        self.used_logins.add(login)
        return login
    
    def _generate_unique_publisher_name(self):
        """Генерация уникального названия издателя"""
        companies = ["Games", "Interactive", "Studios", "Entertainment", "Software", 
                    "Digital", "Creations", "Productions", "Works", "Labs", "Tech",
                    "Network", "Group", "Co", "Inc", "Corp", "LLC"]
        
        publisher_name = f"{self.fake.last_name()} {random.choice(companies)}"
        counter = 1
        while publisher_name in self.used_publisher_names:
            publisher_name = f"{self.fake.last_name()} {random.choice(companies)} {counter}"
            counter += 1
        self.used_publisher_names.add(publisher_name)
        return publisher_name

    def generate_users(self, count=1000, filename='../postgres/users.csv'):
        """Генерация пользователей"""
        os.makedirs(os.path.dirname(filename), exist_ok=True)
        
        with open(filename, 'w', newline='', encoding='utf-8') as file:
            writer = csv.writer(file)
            writer.writerow(['login', 'password', 'nickname', 'registration_date'])
            
            for i in range(count):
                unique_login = self._generate_unique_login()
                
                writer.writerow([
                    unique_login,
                    self.fake.password(length=12),
                    self.fake.user_name(),  # nickname
                    self.fake.date_between(start_date='-5y', end_date='today')
                ])
        print(f"Generated {count} users in {filename}")

    def generate_games(self, count=1000, filename='../postgres/games.csv'):
        """Генерация игр"""
        os.makedirs(os.path.dirname(filename), exist_ok=True)
        
        game_templates = [
            "Epic Adventure", "Space Odyssey", "Dragon Quest", "Cyber Revolution",
            "Ancient Legends", "Future Warfare", "Mystic Realm", "Ocean Explorer",
            "Desert Survival", "Mountain Climber", "City Builder", "Fantasy World",
            "Robot Invasion", "Magic Academy", "Pirate Treasure", "Wild West",
            "Medieval Kingdom", "Space Station", "Underwater City", "Post-Apocalyptic"
        ]
        
        with open(filename, 'w', newline='', encoding='utf-8') as file:
            writer = csv.writer(file)
            writer.writerow(['id', 'name', 'description', 'price'])
            
            for i in range(count):
                game_name = f"{random.choice(game_templates)} {i+1}"
                description = self.fake.text(max_nb_chars=200)
                price = round(random.uniform(4.99, 99.99), 2)
                
                writer.writerow([
                    i,
                    game_name,
                    description,
                    price
                ])
        print(f"Generated {count} games in {filename}")

    def generate_genres(self, filename='../postgres/genres.csv'):
        """Генерация жанров"""
        os.makedirs(os.path.dirname(filename), exist_ok=True)
        
        with open(filename, 'w', newline='', encoding='utf-8') as file:
            writer = csv.writer(file)
            writer.writerow(['name', 'description'])
            
            for genre_name in self.base_genres:
                description = f"Games in the {genre_name} genre featuring {self.fake.word()} and {self.fake.word()}"
                
                writer.writerow([
                    genre_name,
                    description
                ])
        print(f"Generated {len(self.base_genres)} genres in {filename}")

    def generate_publishers(self, count=1000, filename='../postgres/publishers.csv'):
        """Генерация издателей"""
        os.makedirs(os.path.dirname(filename), exist_ok=True)
        
        with open(filename, 'w', newline='', encoding='utf-8') as file:
            writer = csv.writer(file)
            writer.writerow(['name', 'site', 'description', 'rating', 'country'])
            
            for i in range(count):
                unique_name = self._generate_unique_publisher_name()
                site = f"www.{unique_name.lower().replace(' ', '').replace('.', '')}.com"
                rating = round(random.uniform(2.5, 5.0), 2)
                country = self.fake.country()
                
                writer.writerow([
                    unique_name,
                    site,
                    self.fake.text(max_nb_chars=150),
                    rating,
                    country
                ])
        print(f"Generated {count} publishers in {filename}")

    def generate_users_games(self, count=1000, users_count=1000, games_count=1000, filename='../postgres/users_games.csv'):
        """Генерация связей пользователей и игр"""
        os.makedirs(os.path.dirname(filename), exist_ok=True)
        
        # Read user logins
        user_logins = []
        with open('../postgres/users.csv', 'r', encoding='utf-8') as file:
            reader = csv.DictReader(file)
            user_logins = [row['login'] for row in reader]
        
        with open(filename, 'w', newline='', encoding='utf-8') as file:
            writer = csv.writer(file)
            writer.writerow(['id', 'user_id', 'game_id', 'purchase_date'])
            
            pairs = set()
            for i in range(count):
                while True:
                    user_login = random.choice(user_logins)
                    game_id = random.randint(1, games_count)
                    if (user_login, game_id) not in pairs:
                        pairs.add((user_login, game_id))
                        break
                
                purchase_date = self.fake.date_between(
                    start_date=datetime(2020, 1, 1),
                    end_date='today'
                )
                
                writer.writerow([
                    i,
                    user_login,
                    game_id,
                    purchase_date
                ])
        print(f"Generated {count} user-game relations in {filename}")

    def generate_publishers_games_genres(self, count=1000, publishers_count=1000, games_count=1000, filename='../postgres/publishers_games_genres.csv'):
        """Генерация связей издателей, игр и жанров"""
        os.makedirs(os.path.dirname(filename), exist_ok=True)
        
        # Read publisher names
        publisher_names = []
        with open('../postgres/publishers.csv', 'r', encoding='utf-8') as file:
            reader = csv.DictReader(file)
            publisher_names = [row['name'] for row in reader]
        
        with open(filename, 'w', newline='', encoding='utf-8') as file:
            writer = csv.writer(file)
            writer.writerow(['id', 'publisher_id', 'game_id', 'genre_id', 'publish_date'])
            
            # Создаем уникальные тройки publisher_id + game_id + genre_id
            triples = set()
            for i in range(count):
                while True:
                    publisher_name = random.choice(publisher_names)
                    game_id = random.randint(1, games_count)
                    genre_name = random.choice(self.base_genres)
                    if (publisher_name, game_id, genre_name) not in triples:
                        triples.add((publisher_name, game_id, genre_name))
                        break
                
                publish_date = self.fake.date_between(
                    start_date=datetime(2015, 1, 1),
                    end_date='today'
                )
                
                writer.writerow([
                    i,
                    publisher_name,
                    game_id,
                    genre_name,
                    publish_date
                ])
        print(f"Generated {count} publisher-game-genre relations in {filename}")

    def generate_all_data(self, count_per_table=1000):
        """Генерация всех данных"""
        print("Starting data generation...")
        
        # Generate main tables first
        self.generate_users(count_per_table)
        self.generate_games(count_per_table)
        self.generate_genres()  # Uses predefined genres
        self.generate_publishers(count_per_table)
        
        # Generate relationship tables (need to read from previously generated files)
        self.generate_users_games(count_per_table, count_per_table, count_per_table)
        self.generate_publishers_games_genres(count_per_table, count_per_table, count_per_table)
        
        print("Data generation completed!")
        print("Files saved in ../postgres/ directory")

# Использование генератора
if __name__ == "__main__":
    # Настройки генерации
    RECORDS_PER_TABLE = 1000
    
    generator = LibraryDataGenerator()
    generator.generate_all_data(RECORDS_PER_TABLE)
