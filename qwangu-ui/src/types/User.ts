export class User {
    constructor(
        public id?: string,
        public person?: Person,
        public emailAddress?: string,
        public roleId?: string,
        public isAccountExpired?: boolean,
        public isCredentialsExpired?: boolean,
        public isAccountLocked?: boolean,
        public isEnabled?: boolean,
        public createdOn?: Date,
        public modifiedOn?: Date,
    ) {
        this.person = this.person ? this.person : new Person();
        this.roleId = this.roleId ? this.roleId : '';
        this.isEnabled = this.isEnabled != null && this.isEnabled != undefined ? this.isEnabled : false;
    };
}

export class Person {
    constructor(
        public firstName?: string,
        public otherNames?: string,
        public surname?: string,
    ) { };
}

export class CreateUserRequest {
    constructor(
        public emailAddress?: string,
        public roleId?: string,
        public person?: Person
    ){}
  }

export class UpdateUserRequest extends CreateUserRequest {
    constructor(
        public emailAddress?: string,
        public roleId?: string,
        public person?: Person,
        public isEnabled?: boolean){
        super();
    }
}