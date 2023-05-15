export class Property {
    constructor(
        public id?: string,
        public type?: PropertyType | string,
        public name?: string,
        public description?: string,
        public createdOn?: Date,
        public createdBy?: string,
        public modifiedOn?: Date,
        public modifiedBy?: string
    ){
        this.type = this.type ? this.type : ''
    }
}

export enum PropertyType {
    APARTMENT = 'APARTMENT',
    HOUSE = 'HOUSE'
}