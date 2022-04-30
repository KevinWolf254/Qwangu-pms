package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.bson.types.ObjectId;

@EqualsAndHashCode
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BasicEntity {
    private ObjectId id;
}
