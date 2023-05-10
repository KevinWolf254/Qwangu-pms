import http from './http';
import { User } from '../types/user'
import { Response } from '../types/response';

const userUrl = '/v1/users';

export const getUsers = async (params?: any): Promise<User[]> => {
    try {
        const response = await http.get<Response<User[]>>(userUrl);
        return response.data.data;
    } catch (error) {
        console.error(error);
        return [];
    }
}
