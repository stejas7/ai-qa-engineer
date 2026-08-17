package com.aiqa.requirement;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="requirements")
public class Requirement {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String title;
    @Column(columnDefinition="TEXT", nullable=false) private String description;
    @ElementCollection
    @CollectionTable(name="requirement_acceptance_criteria", joinColumns=@JoinColumn(name="requirement_id"))
    @Column(name="criteria", columnDefinition="TEXT") private List<String> acceptanceCriteria = new ArrayList<>();
    @Column(nullable=false) private Instant createdAt = Instant.now();
    public Long getId(){return id;} public String getTitle(){return title;} public String getDescription(){return description;}
    public List<String> getAcceptanceCriteria(){return acceptanceCriteria;} public Instant getCreatedAt(){return createdAt;}
    public void setTitle(String v){title=v;} public void setDescription(String v){description=v;}
    public void setAcceptanceCriteria(List<String> v){acceptanceCriteria=v==null?new ArrayList<>():v;}
}
