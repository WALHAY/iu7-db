package com.walhay.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "publishers", schema = "library")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Publisher {

    @Id
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "site")
    private String site;

    @Column(name = "description")
    private String description;

    @Column(name = "rating", precision = 3, scale = 1)
    private BigDecimal rating;

    @Column(name = "country", nullable = false)
    private String country;

    @OneToMany(mappedBy = "publisher", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PublisherGameGenre> publisherGenres = new HashSet<>();
}
