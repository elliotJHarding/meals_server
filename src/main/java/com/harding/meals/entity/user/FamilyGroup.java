package com.harding.meals.entity.user;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
public class FamilyGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @OneToMany(mappedBy = "familyGroup")
    List<AppUser> users;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public List<AppUser> getUsers() {
        return users;
    }

    public void setUsers(List<AppUser> users) {
        this.users = users;
    }
}
