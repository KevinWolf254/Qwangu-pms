package co.ke.proaktivio.qwanguapi.pojos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class Person {
    private String firstName;
    private String otherNames;
    private String surname;
}
