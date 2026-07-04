import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { Building2, AlertCircle } from 'lucide-react';
import { authService } from '../services/api';

export const Register: React.FC = () => {
  const { register, handleSubmit, formState: { errors } } = useForm();
  const [authError, setAuthError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const onSubmit = async (data: any) => {
    try {
      setIsLoading(true);
      setAuthError('');
      await authService.register(data);
      navigate('/login');
    } catch (err: any) {
      setAuthError(err.response?.data?.message || 'Registration failed');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md flex flex-col items-center">
        <div className="h-12 w-12 bg-slate-900 rounded-lg flex items-center justify-center">
          <Building2 className="text-white h-8 w-8" />
        </div>
        <h2 className="mt-6 text-center text-3xl font-bold tracking-tight text-slate-900">
          Open an Account
        </h2>
        <p className="mt-2 text-center text-sm text-slate-600">
          Register with BankLedger today
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow-sm border border-slate-200 sm:rounded-xl sm:px-10">
          <form className="space-y-5" onSubmit={handleSubmit(onSubmit)}>
            
            {authError && (
              <div className="bg-red-50 p-4 rounded-md flex items-start gap-3">
                <AlertCircle className="h-5 w-5 text-red-600 mt-0.5" />
                <p className="text-sm text-red-700">{authError}</p>
              </div>
            )}

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="firstName" className="block text-sm font-medium text-slate-700">First name</label>
                <input
                  id="firstName"
                  type="text"
                  {...register('firstName', { required: 'First name required' })}
                  className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 placeholder-slate-400 shadow-sm focus:border-slate-500 focus:outline-none focus:ring-slate-500 sm:text-sm"
                />
                {errors.firstName && <p className="mt-1 text-xs text-red-600">{errors.firstName.message as string}</p>}
              </div>
              <div>
                <label htmlFor="lastName" className="block text-sm font-medium text-slate-700">Last name</label>
                <input
                  id="lastName"
                  type="text"
                  {...register('lastName', { required: 'Last name required' })}
                  className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 placeholder-slate-400 shadow-sm focus:border-slate-500 focus:outline-none focus:ring-slate-500 sm:text-sm"
                />
                {errors.lastName && <p className="mt-1 text-xs text-red-600">{errors.lastName.message as string}</p>}
              </div>
            </div>

            <div>
              <label htmlFor="email" className="block text-sm font-medium text-slate-700">Email address</label>
              <input
                id="email"
                type="email"
                {...register('email', { required: 'Email required' })}
                className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 placeholder-slate-400 shadow-sm focus:border-slate-500 focus:outline-none focus:ring-slate-500 sm:text-sm"
              />
              {errors.email && <p className="mt-1 text-xs text-red-600">{errors.email.message as string}</p>}
            </div>

            <div>
              <label htmlFor="phoneNumber" className="block text-sm font-medium text-slate-700">Phone number</label>
              <input
                id="phoneNumber"
                type="tel"
                {...register('phoneNumber', { required: 'Phone required' })}
                className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 placeholder-slate-400 shadow-sm focus:border-slate-500 focus:outline-none focus:ring-slate-500 sm:text-sm"
              />
              {errors.phoneNumber && <p className="mt-1 text-xs text-red-600">{errors.phoneNumber.message as string}</p>}
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-slate-700">Password</label>
              <input
                id="password"
                type="password"
                {...register('password', { required: 'Password required', minLength: { value: 6, message: 'Minimum 6 characters' } })}
                className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 placeholder-slate-400 shadow-sm focus:border-slate-500 focus:outline-none focus:ring-slate-500 sm:text-sm"
              />
              {errors.password && <p className="mt-1 text-xs text-red-600">{errors.password.message as string}</p>}
            </div>

            <div>
              <button
                type="submit"
                disabled={isLoading}
                className="flex w-full justify-center rounded-md border border-transparent bg-slate-900 py-2.5 px-4 text-sm font-medium text-white shadow-sm hover:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-slate-900 focus:ring-offset-2 disabled:opacity-70 disabled:cursor-not-allowed transition-colors"
              >
                {isLoading ? 'Registering...' : 'Register'}
              </button>
            </div>
          </form>

          <div className="mt-6 text-center">
            <Link to="/login" className="text-sm font-medium text-slate-900 hover:text-slate-700">
              Already have an account? Sign in
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};
