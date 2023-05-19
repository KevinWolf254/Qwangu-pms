export class Tenant {
    constructor(
        public id?: string,
        public firstName?: string,
        public middleName?: string,
        public surname?: string,
        public mobileNumber?: string,
        public emailAddress?: string,
        public createdOn?: Date,
        public createdBy?: string,
        public modifiedOn?: Date,
        public modifiedBy?: string
    ) { }
}