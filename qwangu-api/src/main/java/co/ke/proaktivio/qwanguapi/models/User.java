package co.ke.proaktivio.qwanguapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Document(value = "USER")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class User {
    @Id
    private String id;
    private String firstName;
    private String otherNames;
    private String surnameName;
    private Set<Role> roles;
    private LocalDateTime created;
    private LocalDateTime modified;
}
