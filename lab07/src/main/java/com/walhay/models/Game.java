package com.walhay.models;

import com.walhay.dto.SaleInfo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "games", schema = "library")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, columnDefinition = "MONEY")
    private BigDecimal price;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sale")
    @Embedded
    private SaleInfo sale;

    @ToString.Exclude
    @ManyToMany
    @JoinTable(
            name = "users_games",
            schema = "library",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "user_login")
    )
    private Set<User> users = new HashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PublisherGameGenre> publisherGenres = new HashSet<>();
}
