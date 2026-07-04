import { api } from '../api/axios';
import type { AuthResponse } from '../types';

export const authService = {
  login: async (credentials: any): Promise<AuthResponse> => {
    const { data } = await api.post('/auth/login', credentials);
    return data;
  },
  register: async (userData: any): Promise<void> => {
    await api.post('/auth/register', userData);
  },
};

export const accountService = {
  getAccounts: async (): Promise<any[]> => {
    const { data } = await api.get('/accounts');
    return data;
  },
  createAccount: async (type: string): Promise<any> => {
    const { data } = await api.post('/accounts', { accountType: type });
    return data;
  },
  getAccount: async (id: string): Promise<any> => {
    const { data } = await api.get(`/accounts/${id}`);
    return data;
  }
};

export const transactionService = {
  deposit: async (accountId: string, amount: number, description: string): Promise<any> => {
    const { data } = await api.post(`/accounts/${accountId}/deposit`, { amount, description });
    return data;
  },
  withdraw: async (accountId: string, amount: number, description: string): Promise<any> => {
    const { data } = await api.post(`/accounts/${accountId}/withdraw`, { amount, description });
    return data;
  },
  transfer: async (sourceAccountId: string, destinationAccountId: string, amount: number, description: string): Promise<any> => {
    const { data } = await api.post('/transfers', { sourceAccountId, destinationAccountId, amount, description });
    return data;
  },
  getTransactions: async (accountId: string): Promise<any[]> => {
    const { data } = await api.get(`/accounts/${accountId}/transactions`);
    return data;
  }
};
