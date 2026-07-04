import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';

import { accountService, transactionService } from '../services/api';
import type { Account } from '../types';
import { Loader2, CheckCircle2, AlertCircle } from 'lucide-react';
import { formatCurrency } from '../utils/formatters';

export const Transfers: React.FC = () => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  
  const { register, handleSubmit, reset, watch, formState: { errors } } = useForm();
  
  const sourceAccountId = watch('sourceAccountId');

  useEffect(() => {
    const fetchAccounts = async () => {
      try {
        const data = await accountService.getAccounts();
        setAccounts(data);
      } catch (err) {
        setErrorMsg('Failed to load accounts');
      } finally {
        setIsLoading(false);
      }
    };
    fetchAccounts();
  }, []);

  const onSubmit = async (data: any) => {
    try {
      setIsSubmitting(true);
      setErrorMsg('');
      setSuccessMsg('');
      
      const amount = parseFloat(data.amount);
      
      if (data.transferType === 'INTERNAL') {
        await transactionService.transfer(data.sourceAccountId, data.destinationAccountId, amount, data.description || 'Internal Transfer');
      } else if (data.transferType === 'DEPOSIT') {
        await transactionService.deposit(data.sourceAccountId, amount, data.description || 'Deposit');
      } else if (data.transferType === 'WITHDRAW') {
        await transactionService.withdraw(data.sourceAccountId, amount, data.description || 'Withdrawal');
      }
      
      setSuccessMsg('Transaction completed successfully.');
      reset();
      
      // refresh accounts balance
      const updatedAccounts = await accountService.getAccounts();
      setAccounts(updatedAccounts);
      
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Transaction failed. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-slate-400" />
      </div>
    );
  }

  const selectedAccount = accounts.find(a => a.id === sourceAccountId);

  return (
    <div className="space-y-6 max-w-3xl mx-auto">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Transfers</h1>
        <p className="text-sm text-slate-500 mt-1">Move money between accounts or make deposits/withdrawals</p>
      </div>

      <div className="bg-white border border-slate-200 shadow-sm rounded-lg overflow-hidden">
        <div className="p-6 sm:p-8">
          {successMsg && (
            <div className="mb-6 bg-emerald-50 border border-emerald-200 rounded-md p-4 flex items-start">
              <CheckCircle2 className="h-5 w-5 text-emerald-600 mt-0.5 mr-3" />
              <div>
                <h3 className="text-sm font-medium text-emerald-800">Success</h3>
                <p className="text-sm text-emerald-700 mt-1">{successMsg}</p>
              </div>
            </div>
          )}

          {errorMsg && (
            <div className="mb-6 bg-red-50 border border-red-200 rounded-md p-4 flex items-start">
              <AlertCircle className="h-5 w-5 text-red-600 mt-0.5 mr-3" />
              <div>
                <h3 className="text-sm font-medium text-red-800">Error</h3>
                <p className="text-sm text-red-700 mt-1">{errorMsg}</p>
              </div>
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Transaction Type</label>
              <select
                {...register('transferType', { required: true })}
                className="block w-full rounded-md border border-slate-300 px-3 py-2 focus:border-slate-500 focus:outline-none focus:ring-slate-500 sm:text-sm bg-white"
                defaultValue="INTERNAL"
              >
                <option value="INTERNAL">Transfer between accounts</option>
                <option value="DEPOSIT">Deposit Funds (External)</option>
                <option value="WITHDRAW">Withdraw Funds (External)</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Source Account</label>
              <select
                {...register('sourceAccountId', { required: 'Source account is required' })}
                className="block w-full rounded-md border border-slate-300 px-3 py-2 focus:border-slate-500 focus:outline-none focus:ring-slate-500 sm:text-sm bg-white"
                defaultValue=""
              >
                <option value="" disabled>Select an account</option>
                {accounts.map(acc => (
                  <option key={acc.id} value={acc.id}>
                    {acc.accountType} (****{acc.accountNumber.slice(-4)}) - {formatCurrency(acc.balance)}
                  </option>
                ))}
              </select>
              {errors.sourceAccountId && <p className="mt-1 text-xs text-red-600">{errors.sourceAccountId.message as string}</p>}
            </div>

            {watch('transferType') === 'INTERNAL' && (
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">Destination Account</label>
                <select
                  {...register('destinationAccountId', { 
                    required: watch('transferType') === 'INTERNAL' ? 'Destination is required' : false,
                    validate: value => value !== sourceAccountId || 'Cannot transfer to the same account'
                  })}
                  className="block w-full rounded-md border border-slate-300 px-3 py-2 focus:border-slate-500 focus:outline-none focus:ring-slate-500 sm:text-sm bg-white"
                  defaultValue=""
                >
                  <option value="" disabled>Select an account</option>
                  {accounts.map(acc => (
                    <option key={acc.id} value={acc.id} disabled={acc.id === sourceAccountId}>
                      {acc.accountType} (****{acc.accountNumber.slice(-4)})
                    </option>
                  ))}
                </select>
                {errors.destinationAccountId && <p className="mt-1 text-xs text-red-600">{errors.destinationAccountId.message as string}</p>}
              </div>
            )}

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Amount</label>
              <div className="relative rounded-md shadow-sm">
                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                  <span className="text-slate-500 sm:text-sm">$</span>
                </div>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  {...register('amount', { 
                    required: 'Amount is required',
                    min: { value: 0.01, message: 'Amount must be greater than 0' }
                  })}
                  className="block w-full rounded-md border border-slate-300 pl-7 pr-12 py-2 focus:border-slate-500 focus:outline-none focus:ring-slate-500 sm:text-sm"
                  placeholder="0.00"
                />
              </div>
              {errors.amount && <p className="mt-1 text-xs text-red-600">{errors.amount.message as string}</p>}
              
              {/* Contextual help */}
              {selectedAccount && watch('transferType') !== 'DEPOSIT' && watch('amount') && parseFloat(watch('amount')) > selectedAccount.balance && (
                 <p className="mt-1 text-xs text-amber-600">Warning: Amount exceeds available balance.</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Description (Optional)</label>
              <input
                type="text"
                {...register('description')}
                className="block w-full rounded-md border border-slate-300 px-3 py-2 focus:border-slate-500 focus:outline-none focus:ring-slate-500 sm:text-sm"
                placeholder="e.g. Rent payment"
                maxLength={255}
              />
            </div>

            <div className="pt-4 border-t border-slate-200">
              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full sm:w-auto flex justify-center rounded-md border border-transparent bg-slate-900 py-2 px-6 text-sm font-medium text-white shadow-sm hover:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-slate-900 focus:ring-offset-2 disabled:opacity-70 disabled:cursor-not-allowed transition-colors"
              >
                {isSubmitting ? 'Processing...' : 'Confirm Transaction'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
