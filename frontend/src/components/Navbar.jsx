import React from 'react';
import { useAuth } from '../context/AuthContext';
import { LogOut, FolderGit2, Cpu, LayoutDashboard } from 'lucide-react';

const roleBadgeColor = {
  ADMIN: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
  PROJECT_MANAGER: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
  DEVELOPER: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20',
  VIEWER: 'bg-zinc-800 text-zinc-400 border-zinc-700',
};

export const Navbar = ({ activeTab, setActiveTab }) => {
  const { user, logout } = useAuth();

  return (
    <header className="sticky top-0 z-40 w-full border-b border-zinc-800/80 bg-[#09090b]/90 backdrop-blur-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-14 flex items-center justify-between">
        
        {/* Brand logo */}
        <div className="flex items-center space-x-2.5 cursor-pointer" onClick={() => setActiveTab('dashboard')}>
          <div className="h-8 w-8 rounded-md bg-zinc-900 border border-zinc-800 flex items-center justify-center text-emerald-400">
            <Cpu className="h-4 w-4" />
          </div>
          <div className="flex items-baseline space-x-1.5">
            <span className="text-sm font-semibold tracking-tight text-zinc-100">DevOps<span className="text-emerald-400 font-mono">AI</span></span>
            <span className="text-[10px] font-mono text-zinc-500 uppercase tracking-widest">Platform</span>
          </div>
        </div>

        {/* Navigation Tabs */}
        {user && (
          <nav className="flex items-center space-x-1">
            <button
              onClick={() => setActiveTab('dashboard')}
              className={`px-3 py-1.5 rounded-md text-xs font-medium transition-colors flex items-center space-x-1.5 ${
                activeTab === 'dashboard'
                  ? 'bg-zinc-800 text-zinc-100 border border-zinc-700/70 shadow-sm'
                  : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-900'
              }`}
            >
              <LayoutDashboard className="h-3.5 w-3.5" />
              <span>Dashboard</span>
            </button>

            <button
              onClick={() => setActiveTab('projects')}
              className={`px-3 py-1.5 rounded-md text-xs font-medium transition-colors flex items-center space-x-1.5 ${
                activeTab === 'projects'
                  ? 'bg-zinc-800 text-zinc-100 border border-zinc-700/70 shadow-sm'
                  : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-900'
              }`}
            >
              <FolderGit2 className="h-3.5 w-3.5" />
              <span>Projects</span>
            </button>
          </nav>
        )}

        {/* User Info & Logout */}
        {user && (
          <div className="flex items-center space-x-3">
            <div className="flex items-center space-x-2 bg-zinc-900/90 border border-zinc-800 px-2.5 py-1 rounded-md">
              <div className="h-5 w-5 rounded bg-zinc-800 border border-zinc-700 flex items-center justify-center text-zinc-300 font-mono text-[10px] font-semibold">
                {user.username.charAt(0).toUpperCase()}
              </div>
              <div className="text-left hidden sm:flex items-center space-x-2">
                <span className="text-xs font-medium text-zinc-200 leading-none">{user.username}</span>
                <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded border leading-none uppercase ${roleBadgeColor[user.role] || 'bg-zinc-800 text-zinc-400 border-zinc-700'}`}>
                  {user.role}
                </span>
              </div>
            </div>

            <button
              onClick={logout}
              title="Logout"
              className="p-1.5 text-zinc-400 hover:text-zinc-100 hover:bg-zinc-900 rounded-md transition-colors border border-transparent hover:border-zinc-800"
            >
              <LogOut className="h-3.5 w-3.5" />
            </button>
          </div>
        )}

      </div>
    </header>
  );
};
