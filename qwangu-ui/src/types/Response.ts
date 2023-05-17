export interface Response<T> {
    timestamp: Date;
    path: string;
    status: number;
    success: boolean;
    message: string;
    data: T;
}