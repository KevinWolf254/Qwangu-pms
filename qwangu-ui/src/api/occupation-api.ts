import http from './http';
import { Response } from '../types/Response';
import { Occupation, OccupationStatus } from '../types/Occupation';

const occupationUrl = '/v1/occupations';

export const getOccupations = async (tenantId?: string, unitId?: string, status?: OccupationStatus): Promise<Occupation[]> => {
        const response = await http.get<Response<Occupation[]>>(`${occupationUrl}?tenantId=${tenantId ? tenantId : ''}&unitId=${unitId ? unitId : ''}&status=${status ? status.toString() : ''}&order=DESC`);
        return response.data.data;
}

export const createOccupation = async (request: Occupation): Promise<Occupation> => {
        const response = await http.post<Response<Occupation>>(occupationUrl, request);
        return response.data.data;
}

export const updateOccupation = async (occupationId: string, request: Occupation): Promise<Occupation> => {
        const response = await http.put<Response<Occupation>>(`${occupationUrl}/${occupationId}`, request);
        return response.data.data;
}