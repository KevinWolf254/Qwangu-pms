import http from './http';
import { Property, PropertyType } from '../types/Property';
import { Response } from '../types/Response';

const propertyUrl = '/v1/properties';

export const getProperties = async (type?: PropertyType, name?: string): Promise<Property[]> => {
        const response = await http.get<Response<Property[]>>(`${propertyUrl}?type=${type ? type.toString() : ''}&name=${name ? name : ''}&order=DESC`);
        return response.data.data;
}

export const createProperty = async (request: Property): Promise<Property> => {
        console.log("Requesting: " +request);
        const response = await http.post<Response<Property>>(propertyUrl, request);
        return response.data.data;
}

export const updateProperty = async (propertyId: string, request: Property): Promise<Property> => {
        const response = await http.put<Response<Property>>(`${propertyUrl}/${propertyId}`, request);
        return response.data.data;
}