import { Tenant } from "./Tenant";
import { Unit } from "./Unit";

export class Occupation {
    constructor(
        public id?: string,
        public status?: OccupationStatus,
        public number?: string,
        public startDate?: Date,
        public endDate?: Date,
        public tenantId?: string | Tenant,
        public unitId?: string | Unit,
        public createdOn?: Date,
        public createdBy?: string,
        public modifiedOn?: Date,
        public modifiedBy?: string,
    ) { }
}

export enum OccupationStatus {
    PENDING_OCCUPATION = 'PENDING_OCCUPATION', CURRENT = 'CURRENT', PENDING_VACATING = 'PENDING_VACATING', VACATED = 'VACATED'
}