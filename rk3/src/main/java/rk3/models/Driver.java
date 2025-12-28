package rk3.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.List;

@Getter
@Setter
@Table(name = "drivers", schema = "rk3")
@Entity
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "birth")
    private Date birth;

    @Column(name = "enter_job")
    private Date enterJob;

    @Column(name = "region")
    private String region;

    @OneToMany(mappedBy = "driver")
    private List<Route> routes;
}
