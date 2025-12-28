package com.walhay.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "publishers_games_genres", schema = "library")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublisherGameGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "publisher_name", referencedColumnName = "name")
    private Publisher publisher;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne
    @JoinColumn(name = "genre_name", referencedColumnName = "name")
    private Genre genre;

    @Column(name = "publish_date")
    private LocalDate publishDate;
}
