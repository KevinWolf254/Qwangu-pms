import http from './http';
import { Response } from '../types/Response';
import { AxiosError } from 'axios';
import { CreateUserRoleRequest, UserRole } from '../types/UserRole';

const rolesUrl = '/v1/roles';

export const createUserRole = async (request: CreateUserRoleRequest): Promise<UserRole | null> => {
    try {
        const response = await http.post<Response<UserRole>>(rolesUrl, request);
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

export const getUserRoles = async (name?: string): Promise<UserRole[]> => {
    try {
        const response = await http.get<Response<UserRole[]>>(`${rolesUrl}?name=${name ? name : ''}&order=DESC`);
        return response.data.data;
    } catch (error) {
        console.error(error);
        return [];
    }
}

export const getUserRoleById = async (roleId: string): Promise<UserRole | null> => {
    try {
        const response = await http.get<Response<UserRole>>(`${rolesUrl}/${roleId}`);

        return response.data.data;
    } catch (error) {
        console.error(error);
        return null;
    }
}