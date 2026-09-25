import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { UserPlus, User, Mail, Lock, Shield } from 'lucide-react';

export const RegisterPage = ({ onSwitchToLogin }) => {
  const { register, login } = useAuth();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('DEVELOPER');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await register(username, email, password, role);
      // Automatically log in after registration
      await login(username, password);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Registration failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4 bg-[#09090b]">
      <div className="w-full max-w-md bg-zinc-900 border border-zinc-800 rounded-lg p-6 shadow-sm relative">
        
        <div className="text-center mb-6">
          <div className="inline-flex items-center justify-center w-10 h-10 rounded-md bg-zinc-950 border border-zinc-800 text-emerald-400 mb-3">
            <UserPlus className="h-5 w-5" />
          </div>
          <h2 className="text-xl font-semibold text-zinc-100 tracking-tight">Create Account</h2>
          <p className="text-xs text-zinc-400 mt-1">Register user credentials for DevOpsAI Platform</p>
        </div>

        {error && (
          <div className="mb-5 p-3 rounded-md bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-mono">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">
              Username
            </label>
            <div className="relative">
              <User className="absolute left-3 top-2.5 h-3.5 w-3.5 text-zinc-500" />
              <input
                type="text"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="john_doe"
                className="w-full bg-zinc-950 border border-zinc-800 rounded-md py-2 pl-9 pr-3 text-xs text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-colors"
              />
            </div>
          </div>

          <div>
            <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">
              Email Address
            </label>
            <div className="relative">
              <Mail className="absolute left-3 top-2.5 h-3.5 w-3.5 text-zinc-500" />
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="john@example.com"
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
                placeholder="At least 6 characters"
                className="w-full bg-zinc-950 border border-zinc-800 rounded-md py-2 pl-9 pr-3 text-xs text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-colors"
              />
            </div>
          </div>

          <div>
            <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">
              Initial Role Assignment
            </label>
            <div className="relative">
              <Shield className="absolute left-3 top-2.5 h-3.5 w-3.5 text-zinc-500" />
              <select
                value={role}
                onChange={(e) => setRole(e.target.value)}
                className="w-full bg-zinc-950 border border-zinc-800 rounded-md py-2 pl-9 pr-3 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-colors font-mono"
              >
                <option value="VIEWER">VIEWER (Read-only)</option>
                <option value="DEVELOPER">DEVELOPER (Code & Tasks)</option>
                <option value="PROJECT_MANAGER">PROJECT_MANAGER (Project Admin)</option>
                <option value="ADMIN">ADMIN (Full Platform Access)</option>
              </select>
            </div>
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="w-full mt-2 bg-emerald-600 hover:bg-emerald-500 text-white font-medium py-2 rounded-md transition-colors text-xs flex items-center justify-center space-x-1.5 disabled:opacity-50"
          >
            <UserPlus className="h-3.5 w-3.5" />
            <span>{submitting ? 'Creating Account...' : 'Register Account'}</span>
          </button>
        </form>

        <div className="mt-5 text-center text-xs text-zinc-400">
          Already have an account?{' '}
          <button onClick={onSwitchToLogin} className="text-emerald-400 hover:underline font-medium">
            Sign In
          </button>
        </div>

      </div>
    </div>
  );
};
