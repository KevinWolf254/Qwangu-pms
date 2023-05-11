export class UserRoleAuthority {
    constructor(
            public name?: string,
            public create?: boolean,
            public read?: boolean,
            public update?: boolean,
            public isDelete?: boolean,
            public authorize?: boolean,
            public roleId?: string,
    ){}
}