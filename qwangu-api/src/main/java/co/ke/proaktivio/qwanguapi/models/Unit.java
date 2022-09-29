package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@Document(value = "UNIT")
public class Unit {
    @Id
    private String id;
    private Status status;
    private Boolean isBooked;
    @Indexed(unique = true)
    private String accountNo;
    private Type type;
    private Identifier identifier;
    private Integer floorNo;
    private Integer noOfBedrooms;
    private Integer noOfBathrooms;
    private Integer advanceInMonths;
    private Currency currency;
    private BigDecimal rentPerMonth;
    private BigDecimal securityPerMonth;
    private BigDecimal garbagePerMonth;
    @CreatedDate
    private LocalDateTime createdOn;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    @LastModifiedBy
    private String modifiedBy;
    private String apartmentId;

    @Getter
    @RequiredArgsConstructor
    public enum Status {
        VACANT("VACANT"),
        OCCUPIED("OCCUPIED");

        private final String state;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Type{
        APARTMENT_UNIT("APARTMENT_UNIT"),
        TOWN_HOUSE("TOWN_HOUSE"),
        MAISONETTES("MAISONETTES"),
        VILLA("VILLA");

        private final String type;
    }

    public enum Identifier {
        A,B,C,D,E,F,G,H,I,J
    }

    public enum Currency {
        KES,
        DOLLAR,
        POUND
    }

    @NoArgsConstructor
    public static class UnitBuilder {
        private Status status;
        private Boolean isBooked;
        private String accountNo;
        private Type type;
        private Identifier identifier;
        private Integer floorNo;
        private Integer noOfBedrooms;
        private Integer noOfBathrooms;
        private Integer advanceInMonths;
        private Currency currency;
        private BigDecimal rentPerMonth;
        private BigDecimal securityPerMonth;
        private BigDecimal garbagePerMonth;
        private String apartmentId;

        public UnitBuilder status(Status status) {
            this.status = status;
            return this;
        }

        public UnitBuilder booked(Boolean booked) {
            isBooked = booked;
            return this;
        }

        public UnitBuilder accountNo(String accountNo) {
            this.accountNo = accountNo;
            return this;
        }

        public UnitBuilder type(Type type) {
            this.type = type;
            return this;
        }

        public UnitBuilder identifier(Identifier identifier) {
            this.identifier = identifier;
            return this;
        }

        public UnitBuilder floorNo(Integer floorNo) {
            this.floorNo = floorNo;
            return this;
        }

        public UnitBuilder noOfBedrooms(Integer noOfBedrooms) {
            this.noOfBedrooms = noOfBedrooms;
            return this;
        }

        public UnitBuilder noOfBathrooms(Integer noOfBathrooms) {
            this.noOfBathrooms = noOfBathrooms;
            return this;
        }

        public UnitBuilder advanceInMonths(Integer advanceInMonths) {
            this.advanceInMonths = advanceInMonths;
            return this;
        }

        public UnitBuilder currency(Currency currency) {
            this.currency = currency;
            return this;
        }

        public UnitBuilder rentPerMonth(BigDecimal rentPerMonth) {
            this.rentPerMonth = rentPerMonth;
            return this;
        }

        public UnitBuilder securityPerMonth(BigDecimal securityPerMonth) {
            this.securityPerMonth = securityPerMonth;
            return this;
        }

        public UnitBuilder garbagePerMonth(BigDecimal garbagePerMonth) {
            this.garbagePerMonth = garbagePerMonth;
            return this;
        }

        public UnitBuilder apartmentId(String apartmentId) {
            this.apartmentId = apartmentId;
            return this;
        }

        public Unit build() {
            var unit = new Unit();
            unit.setStatus(this.status);
            unit.setIsBooked(this.isBooked);
            unit.setAccountNo(this.accountNo);
            unit.setType(this.type);
            unit.setIdentifier(this.identifier);
            unit.setFloorNo(this.floorNo);
            unit.setNoOfBedrooms(this.noOfBedrooms);
            unit.setNoOfBathrooms(this.noOfBathrooms);
            unit.setAdvanceInMonths(this.advanceInMonths);
            unit.setCurrency(this.currency);
            unit.setRentPerMonth(this.rentPerMonth);
            unit.setSecurityPerMonth(this.securityPerMonth);
            unit.setGarbagePerMonth(this.garbagePerMonth);
            unit.setApartmentId(this.apartmentId);
            return unit;
        }
    }
}
