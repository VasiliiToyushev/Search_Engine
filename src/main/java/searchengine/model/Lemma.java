package searchengine.model;

import lombok.Data;
import lombok.NonNull;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "lemma")
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NonNull
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", referencedColumnName = "id")
    @NonNull
    private Site siteId;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String lemma;

    @NonNull
    private int frequency;

    @OneToMany(mappedBy = "lemmaId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Index> indexList = new ArrayList<>();

    public Lemma(){
    }
}
