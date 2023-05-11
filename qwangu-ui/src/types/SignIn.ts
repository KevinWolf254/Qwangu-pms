export class SignInRequest {
    constructor(
        public username?: string,
        public password?: string
    ){}
}

export interface SignInResponse {
    token: string;
}