import http from './http';
import { Response } from '../types/Response';
import { Tenant } from '../types/Tenant';

const tenantUrl = '/v1/tenants';

export const getTenants = async (mobileNumber?: string, emailAddress?: string): Promise<Tenant[]> => {
        const response = await http.get<Response<Tenant[]>>(`${tenantUrl}?mobileNumber=${mobileNumber ? mobileNumber : ''}&emailAddress=${emailAddress ? emailAddress : ''}&order=DESC`);
        return response.data.data;
}

export const getTenant = async (tenantId: string): Promise<Tenant> => {
        const response = await http.get<Response<Tenant>>(`${tenantUrl}/${tenantId}`);
        return response.data.data;
}

export const createTenant = async (request: Tenant): Promise<Tenant> => {
        const response = await http.post<Response<Tenant>>(tenantUrl, request);
        return response.data.data;
}

export const updateTenant = async (tenantId: string, request: Tenant): Promise<Tenant> => {
        const response = await http.put<Response<Tenant>>(`${tenantUrl}/${tenantId}`, request);
        return response.data.data;
}