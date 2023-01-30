package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@ToString
@NoArgsConstructor
@Document(value = "UNIT")
public class Unit {
    @Id
    private String id;
    private Status status;
    @Indexed(unique = true)
    private String number;
    private UnitType type;
    private Identifier identifier;
    private Integer floorNo;
    // PS: ALL OBJECTS TO INCLUDE PHOTOS
    // TODO OBJECT OF LIVING_ROOM
    // TODO ADD LIST<OBJECT> OF BEDROOM - IS_EN_SUITE
    // TODO ADD LIST<OBJECT> OF BATHROOMS
    // TODO ADD OBJECT OF KITCHEN
    private Integer noOfBedrooms;
    private Integer noOfBathrooms;
    // TODO ADD IS_FURNISHED
    private Currency currency;
    // payments per month
    private BigDecimal rentPerMonth;
    private BigDecimal securityPerMonth;
    private BigDecimal garbagePerMonth;
    private Map<String, BigDecimal> otherAmountsPerMonth; // TODO ON UI ADD WATER AND ELECTRICITY
    // advance payments
    private Integer advanceInMonths;
    private BigDecimal securityAdvance;
    private BigDecimal garbageAdvance;
    private Map<String, BigDecimal> otherAmountsAdvance; // TODO ON UI ADD WATER AND ELECTRICITY
    @CreatedDate
    private LocalDateTime createdOn;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    @LastModifiedBy
    private String modifiedBy;
    // TODO CHANGE TO ESTATE_ID
    private String apartmentId;

    public class PaymentPerMonth {
        private BigDecimal rent; // 27000
        private BigDecimal security; // 510
        private BigDecimal garbage; // 300
        private Map<String, BigDecimal> otherAmounts; // TODO ON UI ADD WATER AND ELECTRICITY OR MAKE IT DYNAMIC
    }

    public class AdvancePayment {
        private Integer rentAdvanceInMonths; // 27000 * 2
        private BigDecimal security;
        private BigDecimal garbage;
        private Map<String, BigDecimal> otherAmounts; // TODO ON UI ADD WATER AND ELECTRICITY OR MAKE IT DYNAMIC
    }

    @Getter
    @RequiredArgsConstructor
    public enum Status {
        VACANT("VACANT"),
        OCCUPIED("OCCUPIED");

        private final String state;
    }

    @Getter
    @RequiredArgsConstructor
    public enum UnitType {
        APARTMENT_UNIT("APARTMENT_UNIT"),
        TOWN_HOUSE("TOWN_HOUSE"),
        MAISONETTES("MAISONETTES"),
        VILLA("VILLA");

        private final String type;
    }

    public enum Identifier {
        A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z
    }

    public enum Currency {
        KES,
        DOLLAR,
        POUND
    }

    // TODO ENSURE ALL FIELDS ARE CAPTURED
    @NoArgsConstructor
    public static class UnitBuilder {
        private Status status;
        private String number;
        private UnitType type;
        private Identifier identifier;
        private Integer floorNo;
        private Integer noOfBedrooms;
        private Integer noOfBathrooms;
        private String apartmentId;

        private Currency currency;
        // payment per month
        private BigDecimal rentPerMonth;
        private BigDecimal securityPerMonth;
        private BigDecimal garbagePerMonth;
        private Map<String, BigDecimal> otherAmounts;
        // advance payments
        private Integer advanceInMonths;
        private BigDecimal securityAdvance;
        private BigDecimal garbageAdvance;
        private Map<String, BigDecimal> otherAmountsAdvance;

        public UnitBuilder status(Status status) {
            this.status = status;
            return this;
        }

        public UnitBuilder number(String number) {
            this.number = number;
            return this;
        }

        public UnitBuilder type(UnitType type) {
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

        public UnitBuilder otherAmounts(Map<String, BigDecimal> otherAmounts) {
            this.otherAmounts = otherAmounts;
            return this;
        }

        public UnitBuilder apartmentId(String apartmentId) {
            this.apartmentId = apartmentId;
            return this;
        }

        public UnitBuilder securityAdvance(BigDecimal securityAdvance) {
            this.securityAdvance = securityAdvance;
            return this;
        }

        public UnitBuilder garbageAdvance(BigDecimal garbageAdvance) {
            this.garbageAdvance = garbageAdvance;
            return this;
        }

        public UnitBuilder otherAmountsAdvance(Map<String, BigDecimal> otherAmountsAdvance) {
            this.otherAmountsAdvance = otherAmountsAdvance;
            return this;
        }

        public Unit build() {
            var unit = new Unit();
            unit.setStatus(this.status);
//            unit.setIsBooked(this.isBooked);
            unit.setNumber(this.number);
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
            unit.setOtherAmountsPerMonth(this.otherAmounts);
            unit.setSecurityAdvance(this.securityAdvance);
            unit.setGarbageAdvance(this.garbageAdvance);
            unit.setOtherAmountsAdvance(this.otherAmountsAdvance);
            return unit;
        }
    }
}
