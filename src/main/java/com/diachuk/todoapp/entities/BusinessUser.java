package com.diachuk.todoapp.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class BusinessUser extends User{
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<BusinessUser> friends = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BusinessUser that = (BusinessUser) o;
        return friends.equals(that.friends);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), friends);
    }
}
