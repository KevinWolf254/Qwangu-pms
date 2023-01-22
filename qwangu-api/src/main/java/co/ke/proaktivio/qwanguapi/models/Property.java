package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(value = "PROPERTY")
public class Property {
    @Id
    private String id;
    private PropertyType type;
    @Indexed(unique = true)
    private String name;
    private String description;
    // TODO INCLUDE NO OF FLOORS
    // TODO INCLUDE CONTACTS
    // TODO INCLUDE LOCATION
    // TODO INCLUDE AMENITIES = SWIMMING_POOL, GYM, E.T.C PS: INCLUDE PHOTOS
    // TODO ADD OBJECT OF PARKING - INCLUDE PHOTOS
    // TODO ADD OBJECT OF NEARBY
    @CreatedDate
    private LocalDateTime createdOn;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    @LastModifiedBy
    private String modifiedBy;

    @Getter
    @RequiredArgsConstructor
    public enum PropertyType {
        APARTMENT("APARTMENT"),
        HOUSE("HOUSE");

        private final String name;
    }

    public Property(String name) {
        this.name = name;
    }
}
