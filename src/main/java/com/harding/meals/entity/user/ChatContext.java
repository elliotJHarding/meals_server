package com.harding.meals.entity.user;

import jakarta.persistence.*;

@Entity
@Table(indexes = {
        @Index(name = "idx_app_user_id", columnList = "user_id")
})
public class ChatContext {

    @Id
    @GeneratedValue
    Long id;

    @OneToOne
    AppUser user;

    String context;

}
