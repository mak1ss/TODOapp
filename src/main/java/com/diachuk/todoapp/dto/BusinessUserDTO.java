package com.diachuk.todoapp.dto;

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
public class BusinessUserDTO extends UserDTO{
    private Set<BusinessUserDTO> friends = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BusinessUserDTO that = (BusinessUserDTO) o;
        return Objects.equals(friends, that.friends);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), friends);
    }

    @Override
    public String toString() {
        return "BusinessUserDTO{" +
                "friends=" + friends +
                '}';
    }
}
