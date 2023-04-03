package searchengine.model;
import lombok.Data;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Component
@Table(name = "site")
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NonNull
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum ('INDEXING', 'INDEXED', 'FAILED') NOT NULL")
    private Status status;

    @Column(columnDefinition = "DATETIME NOT NULL",name = "status_time")
    private LocalDateTime statusTime = LocalDateTime.now();

    @Column(columnDefinition = "TEXT", name = "last_error")
    private String lastError;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String url;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String name;

    @OneToMany(mappedBy = "siteId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Page> pageList = new ArrayList<>();

    @OneToMany(mappedBy = "siteId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lemma> lemmaList = new ArrayList<>();

    public Site() {
    }

}
