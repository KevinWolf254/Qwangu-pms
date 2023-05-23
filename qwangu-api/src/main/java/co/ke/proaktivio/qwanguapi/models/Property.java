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
    private String neighbourhood;
    private RealtorInfo realtor;
    
    private Boolean hasBedsitters;
    private Boolean hasOneBedrooms;
    private Boolean hasTwoBedrooms;
    private Boolean hasThreeBedrooms;
    private Boolean hasFourBedrooms;
    private Boolean hasFiveBedrooms;
    
    private Integer noOfFloors;
    private Amenities amenities;
    private String description;
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
    
    @Data
    public class RealtorInfo {
    	private String names;
    	private String emailAddress;
    	private String mobileNumber;    	
    }

    @Data
    public class Amenities {
    	private Boolean hasSwimmingpool;
    	private Boolean hasGym;
    }
    
    public static class PropertyBuilder {
        private PropertyType type;
        private String name;
        private String description;

        public PropertyBuilder type(PropertyType type) {
            this.type = type;
            return this;
        }

        public PropertyBuilder name(String name) {
            this.name = name;
            return this;
        }

        public PropertyBuilder description(String description) {
            this.description = description;
            return this;
        }

        public Property build() {
            var property = new Property();
            property.type = this.type;
            property.name = this.name;
            property.description = this.description;
            return property;
        }
    }
}
