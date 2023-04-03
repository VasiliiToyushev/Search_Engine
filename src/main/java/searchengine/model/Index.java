package searchengine.model;
import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;

@Data
@Entity
@Table(name = "`index`")
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NonNull
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", referencedColumnName = "id")
    @NonNull
    private Page pageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", referencedColumnName = "id")
    @NonNull
    private Lemma lemmaId;

    @NonNull
    @Column(name = "`rank`")
    private float rank;

    public Index(){
    }
}
