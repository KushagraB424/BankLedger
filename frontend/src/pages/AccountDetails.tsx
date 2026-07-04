import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { accountService, transactionService } from '../services/api';
import type { Account, Transaction } from '../types';
import { ArrowLeft, Loader2, ArrowDownLeft, ArrowUpRight } from 'lucide-react';
import { formatCurrency, formatDate } from '../utils/formatters';

export const AccountDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [account, setAccount] = useState<Account | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      if (!id) return;
      try {
        setIsLoading(true);
        const [acc, txs] = await Promise.all([
          accountService.getAccount(id),
          transactionService.getTransactions(id)
        ]);
        setAccount(acc);
        // Sort newest first
        txs.sort((a: Transaction, b: Transaction) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        setTransactions(txs);
      } catch (err: any) {
        setError('Failed to load account details');
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, [id]);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-slate-400" />
      </div>
    );
  }

  if (error || !account) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md">
        {error || 'Account not found'}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center text-sm text-slate-500 mb-4">
        <Link to="/accounts" className="hover:text-slate-900 flex items-center">
          <ArrowLeft className="mr-1 h-4 w-4" /> Back to Accounts
        </Link>
      </div>

      <div className="bg-white border border-slate-200 rounded-lg p-6 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight capitalize">
            {account.accountType.toLowerCase()} Account
          </h1>
          <p className="text-sm text-slate-500 mt-1">****{account.accountNumber.slice(-4)}</p>
        </div>
        <div className="text-right">
          <p className="text-sm font-medium text-slate-500 uppercase tracking-wider">Available Balance</p>
          <p className="text-3xl font-semibold text-slate-900 tabular-nums">{formatCurrency(account.balance)}</p>
        </div>
      </div>

      <div className="flex items-center justify-between mt-8 mb-4">
        <h2 className="text-lg font-semibold text-slate-900">Transaction History</h2>
      </div>

      <div className="bg-white border border-slate-200 rounded-lg overflow-hidden">
        {transactions.length === 0 ? (
          <div className="p-8 text-center text-sm text-slate-500">No transactions found for this account.</div>
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
                  const isPositive = tx.type === 'DEPOSIT' || (tx.type === 'TRANSFER' && tx.destinationAccountId === account.id);
                  return (
                    <tr key={tx.id} className="hover:bg-slate-50">
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-500 tabular-nums">
                        {formatDate(tx.createdAt)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-slate-900">
                        {tx.description}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="inline-flex items-center text-sm text-slate-500 capitalize">
                          {isPositive ? <ArrowDownLeft className="mr-1.5 h-4 w-4 text-emerald-500" /> : <ArrowUpRight className="mr-1.5 h-4 w-4 text-slate-400" />}
                          {tx.type.toLowerCase()}
                        </span>
                      </td>
                      <td className={`px-6 py-4 whitespace-nowrap text-sm font-medium text-right tabular-nums ${isPositive ? 'text-emerald-600' : 'text-slate-900'}`}>
                        {isPositive ? '+' : '-'} {formatCurrency(tx.amount)}
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
  );
};
