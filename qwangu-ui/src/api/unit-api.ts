import http from './http';
import { Response } from '../types/Response';
import { Unit, UnitStatus } from '../types/Unit';

const unitUrl = '/v1/units';

export const getUnits = async (accountNo?: string, status?: UnitStatus): Promise<Unit[]> => {
        const response = await http.get<Response<Unit[]>>(`${unitUrl}?accountNo=${accountNo ? accountNo : ''}&status=${status ? status.toString() : ''}&order=DESC`);
        return response.data.data;
}

export const createUnit = async (request: Unit): Promise<Unit> => {
        console.log("Requesting: " +request);
        const response = await http.post<Response<Unit>>(unitUrl, request);
        return response.data.data;
}

export const updateUnit = async (unitId: string, request: Unit): Promise<Unit> => {
        const response = await http.put<Response<Unit>>(`${unitUrl}/${unitId}`, request);
        return response.data.data;
}