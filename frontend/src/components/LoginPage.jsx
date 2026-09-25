import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { LogIn, Key, Mail, Lock, ShieldCheck, UserCheck } from 'lucide-react';

export const LoginPage = ({ onSwitchToRegister }) => {
  const { login } = useAuth();
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await login(usernameOrEmail, password);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Invalid username or password');
    } finally {
      setSubmitting(false);
    }
  };

  const handleQuickLogin = async (username, pass) => {
    setUsernameOrEmail(username);
    setPassword(pass);
    setError('');
    setSubmitting(true);
    try {
      await login(username, pass);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Login failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4 bg-[#09090b]">
      <div className="w-full max-w-md bg-zinc-900 border border-zinc-800 rounded-lg p-6 shadow-sm relative">
        
        <div className="text-center mb-6">
          <div className="inline-flex items-center justify-center w-10 h-10 rounded-md bg-zinc-950 border border-zinc-800 text-emerald-400 mb-3">
            <ShieldCheck className="h-5 w-5" />
          </div>
          <h2 className="text-xl font-semibold text-zinc-100 tracking-tight">Sign In to DevOpsAI</h2>
          <p className="text-xs text-zinc-400 mt-1">Developer productivity & engineering intelligence control plane</p>
        </div>

        {error && (
          <div className="mb-5 p-3 rounded-md bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-mono">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">
              Username or Email
            </label>
            <div className="relative">
              <Mail className="absolute left-3 top-2.5 h-3.5 w-3.5 text-zinc-500" />
              <input
                type="text"
                required
                value={usernameOrEmail}
                onChange={(e) => setUsernameOrEmail(e.target.value)}
                placeholder="admin or user@devopsai.io"
                className="w-full bg-zinc-950 border border-zinc-800 rounded-md py-2 pl-9 pr-3 text-xs text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-colors"
              />
            </div>
          </div>

          <div>
            <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">
              Password
            </label>
            <div className="relative">
              <Lock className="absolute left-3 top-2.5 h-3.5 w-3.5 text-zinc-500" />
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full bg-zinc-950 border border-zinc-800 rounded-md py-2 pl-9 pr-3 text-xs text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-colors"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="w-full mt-2 bg-emerald-600 hover:bg-emerald-500 text-white font-medium py-2 rounded-md transition-colors text-xs flex items-center justify-center space-x-1.5 disabled:opacity-50"
          >
            <LogIn className="h-3.5 w-3.5" />
            <span>{submitting ? 'Authenticating...' : 'Sign In'}</span>
          </button>
        </form>

        {/* Demo Quick Logins */}
        <div className="mt-6 pt-5 border-t border-zinc-800">
          <div className="text-[11px] font-mono text-zinc-400 mb-2.5 flex items-center space-x-1">
            <UserCheck className="h-3.5 w-3.5 text-emerald-400" />
            <span>Quick Demo Logins (Auto-fill)</span>
          </div>

          <div className="grid grid-cols-2 gap-2 text-xs">
            <button
              onClick={() => handleQuickLogin('admin', 'password123')}
              className="px-2.5 py-1.5 rounded-md bg-zinc-950 hover:bg-zinc-800 border border-zinc-800 text-zinc-300 font-mono transition-colors text-left"
            >
              <div className="font-semibold text-emerald-400 text-[11px]">admin</div>
              <div className="text-[9px] text-zinc-500 uppercase tracking-wider">ROLE: ADMIN</div>
            </button>

            <button
              onClick={() => handleQuickLogin('pm_user', 'password123')}
              className="px-2.5 py-1.5 rounded-md bg-zinc-950 hover:bg-zinc-800 border border-zinc-800 text-zinc-300 font-mono transition-colors text-left"
            >
              <div className="font-semibold text-amber-400 text-[11px]">pm_user</div>
              <div className="text-[9px] text-zinc-500 uppercase tracking-wider">ROLE: PM</div>
            </button>

            <button
              onClick={() => handleQuickLogin('dev_user', 'password123')}
              className="px-2.5 py-1.5 rounded-md bg-zinc-950 hover:bg-zinc-800 border border-zinc-800 text-zinc-300 font-mono transition-colors text-left"
            >
              <div className="font-semibold text-cyan-400 text-[11px]">dev_user</div>
              <div className="text-[9px] text-zinc-500 uppercase tracking-wider">ROLE: DEV</div>
            </button>

            <button
              onClick={() => handleQuickLogin('viewer_user', 'password123')}
              className="px-2.5 py-1.5 rounded-md bg-zinc-950 hover:bg-zinc-800 border border-zinc-800 text-zinc-300 font-mono transition-colors text-left"
            >
              <div className="font-semibold text-zinc-400 text-[11px]">viewer_user</div>
              <div className="text-[9px] text-zinc-500 uppercase tracking-wider">ROLE: VIEWER</div>
            </button>
          </div>
        </div>

        <div className="mt-5 text-center text-xs text-zinc-400">
          Don't have an account?{' '}
          <button onClick={onSwitchToRegister} className="text-emerald-400 hover:underline font-medium">
            Register now
          </button>
        </div>

      </div>
    </div>
  );
};
