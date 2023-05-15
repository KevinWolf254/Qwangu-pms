import http from './http';
import { CreateUserRequest, UpdateUserRequest, User } from '../types/User';
import { Response } from '../types/Response';
import { SignInRequest, SignInResponse } from '../types/SignIn';

const userUrl = '/v1/users';

export const signInUser = async (signInRequest: SignInRequest): Promise<SignInResponse> => {
        const response = await http.post<Response<SignInResponse>>('/v1/sign-in', signInRequest);
        return response.data.data;
}

export const getUsers = async (emailAddress?: any): Promise<User[]> => {
        const response = await http.get<Response<User[]>>(`${userUrl}?emailAddress=${emailAddress ? emailAddress : ''}&order=DESC`);
        return response.data.data;
}

export const createUser = async (request: CreateUserRequest): Promise<User> => {
        const response = await http.post<Response<User>>(userUrl, request);
        return response.data.data;
}

export const updateUser = async (userId: string, request: UpdateUserRequest): Promise<User> => {
        const response = await http.put<Response<User>>(`${userUrl}/${userId}`, request);
        return response.data.data;
}

export const deleteUser = async (userId: string): Promise<User> => {
        const response = await http.delete<Response<User>>(`${userUrl}/${userId}`);
        return response.data.data;
}