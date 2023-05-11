import { UserRoleAuthority } from "./UserRoleAuthority";

export class UserRole {
    constructor(
        public id?: string,
        public name?: string,
        public createdOn?: Date,
        public createdBy?: string,
        public modifiedOn?: Date,
        public modifiedBy?: string
    ) { }
}

export class CreateUserRoleRequest {
    constructor(
        public name?: string,
        public authorities?: Array<UserRoleAuthority>
    ){}
}