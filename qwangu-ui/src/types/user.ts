export class User {
    constructor(
        public id?: number,
        public person?: Person,
        public emailAddress?: string,
        public roleId?: string,
        public isAccountExpired?: boolean,
        public isCredentialsExpired?: boolean,
        public isAccountLocked?: boolean,
        public isEnabled?: boolean,
        public createdOn?: Date,
        public modifiedOn?: Date,
    ) {};
}

export class Person {
    constructor(
        public firstName?: string,
        public otherNames?: string,
        public surname?: string,
    ) { };
}