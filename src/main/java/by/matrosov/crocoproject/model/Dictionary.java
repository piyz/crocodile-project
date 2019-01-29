package by.matrosov.crocoproject.model;

import javax.persistence.*;

@Entity
@Table(name = "dictionary")
public class Dictionary {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long id;

    @Column(name = "value")
    private String value;
}
