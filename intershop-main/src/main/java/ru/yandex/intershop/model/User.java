package ru.yandex.intershop.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;

@Data
@Table("users")
public class User {
    @Id
    private Long id;
    private String username;
    private String password;
    private Set<String> roles;

}