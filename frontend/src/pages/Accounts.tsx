import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { accountService } from '../services/api';
import type { Account } from '../types';
import { Loader2, Plus, Wallet } from 'lucide-react';
import { formatCurrency, formatDate } from '../utils/formatters';

export const Accounts: React.FC = () => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const navigate = useNavigate();

  const fetchAccounts = async () => {
    try {
      setIsLoading(true);
      const data = await accountService.getAccounts();
      setAccounts(data);
    } catch (err: any) {
      setError('Failed to load accounts');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchAccounts();
  }, []);

  const handleCreateAccount = async (type: string) => {
    try {
      setIsCreating(true);
      await accountService.createAccount(type);
      await fetchAccounts();
    } catch (err: any) {
      alert('Failed to create account');
    } finally {
      setIsCreating(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-slate-400" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Accounts</h1>
          <p className="text-sm text-slate-500 mt-1">Manage your checking and savings accounts</p>
        </div>
        <div className="flex gap-3">
          <button
            onClick={() => handleCreateAccount('CHECKING')}
            disabled={isCreating}
            className="inline-flex items-center justify-center rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 shadow-sm hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-slate-900 focus:ring-offset-2 disabled:opacity-50"
          >
            <Plus className="mr-2 h-4 w-4" /> Open Checking
          </button>
          <button
            onClick={() => handleCreateAccount('SAVINGS')}
            disabled={isCreating}
            className="inline-flex items-center justify-center rounded-md border border-transparent bg-slate-900 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-slate-900 focus:ring-offset-2 disabled:opacity-50"
          >
            <Plus className="mr-2 h-4 w-4" /> Open Savings
          </button>
        </div>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md">
          {error}
        </div>
      )}

      {accounts.length === 0 ? (
        <div className="text-center py-12 bg-white border border-slate-200 rounded-lg">
          <Wallet className="mx-auto h-12 w-12 text-slate-400" />
          <h3 className="mt-2 text-sm font-medium text-slate-900">No accounts</h3>
          <p className="mt-1 text-sm text-slate-500">Get started by opening a new account.</p>
        </div>
      ) : (
        <div className="bg-white border border-slate-200 rounded-lg overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200">
              <thead className="bg-slate-50">
                <tr>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Account</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Type</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Status</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Opened</th>
                  <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase tracking-wider">Balance</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-slate-200">
                {accounts.map(account => (
                  <tr 
                    key={account.id} 
                    onClick={() => navigate(`/accounts/${account.id}`)}
                    className="hover:bg-slate-50 cursor-pointer transition-colors"
                  >
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 h-8 w-8 bg-slate-100 rounded-full flex items-center justify-center">
                          <Wallet className="h-4 w-4 text-slate-600" />
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-slate-900">
                            ****{account.accountNumber.slice(-4)}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="text-sm text-slate-900 capitalize">{account.accountType.toLowerCase()}</span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium capitalize
                        ${account.status === 'ACTIVE' ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-100 text-slate-800'}`}>
                        {account.status.toLowerCase()}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-500 tabular-nums">
                      {formatDate(account.createdAt)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-right text-slate-900 tabular-nums">
                      {formatCurrency(account.balance)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};
