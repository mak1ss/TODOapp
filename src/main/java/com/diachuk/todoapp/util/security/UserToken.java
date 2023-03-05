package com.diachuk.todoapp.util.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserToken {
    private int id;
    private String name;
    private Date timeOfCreation;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserToken userToken = (UserToken) o;
        return id == userToken.id && name.equals(userToken.name) && timeOfCreation.equals(userToken.timeOfCreation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, timeOfCreation);
    }

    @Override
    public String toString() {
        return "UserToken{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", timeOfCreation=" + timeOfCreation +
                '}';
    }
}
