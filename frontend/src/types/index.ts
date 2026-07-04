export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  role: string;
}

export interface AuthResponse {
  accessToken: string;
  user: User;
}

export interface Account {
  id: string;
  accountNumber: string;
  accountType: 'CHECKING' | 'SAVINGS' | 'CURRENT';
  balance: number;
  status: 'ACTIVE' | 'CLOSED' | 'FROZEN';
  createdAt: string;
  updatedAt: string;
  customerId: string;
}

export interface Transaction {
  id: string;
  sourceAccountId: string | null;
  destinationAccountId: string | null;
  amount: number;
  type: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER';
  status: 'PENDING' | 'SUCCESS' | 'FAILED';
  description: string;
  createdAt?: string;
  timestamp: string;
}
