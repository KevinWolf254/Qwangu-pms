import http from './http';
import { User } from '../types/User';
import { Response } from '../types/Response';
import { SignInRequest, SignInResponse } from '../types/SignIn';
import { AxiosError } from 'axios';

const userUrl = '/v1/users';

export const signInUser = async (signInRequest: SignInRequest): Promise<SignInResponse | null> => {
    try {
        const response = await http.post<Response<SignInResponse>>('/v1/sign-in', signInRequest);
        return response.data.data;
    } catch (error) {
        if(error instanceof AxiosError) {
            console.log((error.response?.data as Response<any>).message)
        } else {
            console.error(error);
        }
        return null;
    }
}

export const getUsers = async (emailAddress?: any): Promise<User[]> => {
    try {
        const response = await http.get<Response<User[]>>(`${userUrl}?emailAddress=${emailAddress ? emailAddress : ''}&order=DESC`);
        return response.data.data;
    } catch (error) {
        console.error(error);
        return [];
    }
}