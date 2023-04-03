package searchengine.model;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "page")
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NonNull
    private int id;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", referencedColumnName = "id")
    private Site siteId;

    @Column(columnDefinition = "TEXT NOT NULL, index(path(150))")
    private String path;

    @NonNull
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT NOT NULL")
    private String content;

    @OneToMany(mappedBy = "pageId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Index> indexList = new ArrayList<>();

    public Page() {
    }


}
