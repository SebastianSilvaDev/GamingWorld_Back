package com.gamingworld.app.gamingworld.profile.domain.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "streaming_categories")
public class StreamingCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @ManyToOne()
    private Profile profile;

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof StreamingCategory))
            return false;
        StreamingCategory streamingCategory = (StreamingCategory) obj;
        return streamingCategory.id.equals(id);
    }
}