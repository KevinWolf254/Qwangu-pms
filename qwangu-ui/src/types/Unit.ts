import { Currency } from "./Currency"

export class Unit {
    constructor(
        public id?: string,
        public status?: UnitStatus,
        public number?: string,
        public type?: UnitType,
        public identifier?: UnitIdentifier,
        public floorNo?: number,
        public noOfBedrooms?: number,
        public noOfBathrooms?: number,
        public currency?: Currency,
        public rentPerMonth?: number,
        public securityPerMonth?: number,
        public garbagePerMonth?: number,
        public otherAmountsPerMonth?: Map<string, number>,
        public advanceInMonths?: number,
        public securityAdvance?: number,
        public garbageAdvance?: number,
        public otherAmountsAdvance?: Map<string, number>,
        public createdOn?: Date,
        public createdBy?: string,
        public modifiedOn?: Date,
        public modifiedBy?: string,
        public propertyId?: string
    ) { }
}

export enum UnitStatus {
    VACANT = "VACANT",
    OCCUPIED = "OCCUPIED"
}

export enum UnitType {
    APARTMENT_UNIT = "APARTMENT_UNIT",
    TOWN_HOUSE = "TOWN_HOUSE",
    MAISONETTES = "MAISONETTES",
    VILLA = "VILLA"
}

export enum UnitIdentifier {
    A = 'A', B = 'B', C = 'C', D = 'D', E = 'E', F = 'F', G = 'G', H = 'H', I = 'I', J = 'J', K = 'K', L = 'L', M = 'M',
    N = 'N', O = 'O', P = 'P', Q = 'Q', R = 'R', S = 'S', T = 'T', U = 'U', V = 'V', W = 'W', X = 'X', Y = 'Y', Z = 'Z'
}