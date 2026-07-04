import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { accountService, transactionService } from '../services/api';
import type { Account, Transaction } from '../types';
import { ArrowRight, ArrowDownLeft, ArrowUpRight, ArrowLeftRight, Loader2, Plus } from 'lucide-react';
import { formatCurrency, formatDate } from '../utils/formatters';

export const Dashboard: React.FC = () => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        const accs = await accountService.getAccounts();
        setAccounts(accs);

        // Fetch transactions for all accounts to show recent ones
        let allTx: Transaction[] = [];
        for (const acc of accs) {
          const txs = await transactionService.getTransactions(acc.id);
          allTx = [...allTx, ...txs];
        }
        
        // Sort newest first and take top 5
        allTx.sort((a, b) => new Date(b.timestamp || b.createdAt || '').getTime() - new Date(a.timestamp || a.createdAt || '').getTime());
        setTransactions(allTx.slice(0, 5));
      } catch (err: any) {
        setError('Failed to load dashboard data');
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, []);

  const totalBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-slate-400" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md">
        {error}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Dashboard</h1>
          <p className="text-sm text-slate-500 mt-1">Overview of your finances</p>
        </div>
        <div className="flex gap-6 text-right">
          <div>
            <p className="text-sm font-medium text-slate-500 uppercase tracking-wider">Total Balance</p>
            <p className="text-2xl font-semibold text-slate-900 tabular-nums">{formatCurrency(totalBalance)}</p>
          </div>
          <div>
            <p className="text-sm font-medium text-slate-500 uppercase tracking-wider">Active Accounts</p>
            <p className="text-2xl font-semibold text-slate-900 tabular-nums">{accounts.length}</p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Accounts List */}
        <div className="lg:col-span-2 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-semibold text-slate-900">Your Accounts</h2>
            <Link to="/accounts" className="text-sm font-medium text-slate-600 hover:text-slate-900 flex items-center">
              View all <ArrowRight className="ml-1 h-4 w-4" />
            </Link>
          </div>
          
          {accounts.length === 0 ? (
            <div className="bg-white p-6 border border-slate-200 rounded-lg text-center">
              <p className="text-sm text-slate-500">You don't have any accounts yet.</p>
              <Link to="/accounts" className="mt-3 inline-flex items-center text-sm font-medium text-emerald-600 hover:text-emerald-500">
                <Plus className="mr-1 h-4 w-4" /> Open an account
              </Link>
            </div>
          ) : (
            <div className="bg-white border border-slate-200 rounded-lg overflow-hidden">
              <ul className="divide-y divide-slate-200">
                {accounts.map(account => (
                  <li key={account.id} className="p-4 hover:bg-slate-50 transition-colors flex justify-between items-center">
                    <div>
                      <p className="text-sm font-medium text-slate-900 capitalize">{account.accountType.toLowerCase()} Account</p>
                      <p className="text-xs text-slate-500 mt-1">****{account.accountNumber.slice(-4)}</p>
                    </div>
                    <div className="text-right">
                      <p className="text-base font-semibold text-slate-900 tabular-nums">{formatCurrency(account.balance)}</p>
                      <p className="text-xs text-slate-500 mt-1 uppercase">{account.status}</p>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>

        {/* Quick Actions */}
        <div className="space-y-4">
          <h2 className="text-base font-semibold text-slate-900">Quick Actions</h2>
          <div className="bg-white border border-slate-200 rounded-lg p-2 flex flex-col space-y-1">
            <Link to="/transfers" className="flex items-center px-3 py-3 text-sm font-medium text-slate-700 hover:bg-slate-50 rounded-md transition-colors">
              <ArrowLeftRight className="h-5 w-5 mr-3 text-slate-400" />
              Transfer Money
            </Link>
            <Link to="/transfers" className="flex items-center px-3 py-3 text-sm font-medium text-slate-700 hover:bg-slate-50 rounded-md transition-colors">
              <ArrowDownLeft className="h-5 w-5 mr-3 text-slate-400" />
              Deposit Funds
            </Link>
            <Link to="/transfers" className="flex items-center px-3 py-3 text-sm font-medium text-slate-700 hover:bg-slate-50 rounded-md transition-colors">
              <ArrowUpRight className="h-5 w-5 mr-3 text-slate-400" />
              Withdraw Funds
            </Link>
          </div>
        </div>
      </div>

      {/* Recent Transactions */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-base font-semibold text-slate-900">Recent Transactions</h2>
        </div>
        
        <div className="bg-white border border-slate-200 rounded-lg overflow-hidden">
          {transactions.length === 0 ? (
            <div className="p-6 text-center text-sm text-slate-500">No recent transactions.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200">
                <thead className="bg-slate-50">
                  <tr>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Date</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Description</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Type</th>
                    <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase tracking-wider">Amount</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-slate-200">
                  {transactions.map(tx => {
                    const isPositive = tx.type === 'DEPOSIT'; // simplified
                    const isTransferIn = tx.type === 'TRANSFER' && accounts.some(a => a.id === tx.destinationAccountId);
                    const finalPositive = isPositive || isTransferIn;
                    
                    return (
                      <tr key={tx.id} className="hover:bg-slate-50">
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-500 tabular-nums">
                          {formatDate(tx.timestamp || tx.createdAt || '')}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-slate-900">
                          {tx.description}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-500 capitalize">
                          {tx.type.toLowerCase()}
                        </td>
                        <td className={`px-6 py-4 whitespace-nowrap text-sm font-medium text-right tabular-nums ${finalPositive ? 'text-emerald-600' : 'text-slate-900'}`}>
                          {finalPositive ? '+' : '-'} {formatCurrency(tx.amount)}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
