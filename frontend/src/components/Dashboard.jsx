import React from 'react';
import { useAuth } from '../context/AuthContext';
import { 
  Shield, Key, Lock, CheckCircle, Flame, Layers, 
  Kanban, FolderGit2, Calendar, UserCheck 
} from 'lucide-react';

export const Dashboard = ({ onNavigateProjects }) => {
  const { user } = useAuth();

  const roleCapabilities = {
    ADMIN: [
      { text: 'Full System Access & RBAC Admin', allowed: true },
      { text: 'Create & Delete Projects', allowed: true },
      { text: 'Manage Sprints & Epics', allowed: true },
      { text: 'Execute DSA Max-Heap Prioritization', allowed: true },
    ],
    PROJECT_MANAGER: [
      { text: 'Create & Update Managed Projects', allowed: true },
      { text: 'Manage Sprints & Team Members', allowed: true },
      { text: 'Full Platform Admin', allowed: false },
      { text: 'Execute DSA Max-Heap Prioritization', allowed: true },
    ],
    DEVELOPER: [
      { text: 'View Assigned Projects & Kanban Board', allowed: true },
      { text: 'Create Tasks & Bugs', allowed: true },
      { text: 'Transition Issue Workflow Status', allowed: true },
      { text: 'Full Platform Admin', allowed: false },
    ],
    VIEWER: [
      { text: 'View Projects & Kanban Boards', allowed: true },
      { text: 'View DSA Priority Backlog', allowed: true },
      { text: 'Modify Project Settings', allowed: false },
      { text: 'Full Platform Admin', allowed: false },
    ]
  };

  const capabilities = roleCapabilities[user?.role] || [];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 space-y-6">
      
      {/* Welcome Header Banner */}
      <div className="rounded-lg bg-zinc-900 border border-zinc-800 p-5 sm:p-6 relative overflow-hidden">
        <div className="relative z-10 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div>
            <div className="inline-flex items-center space-x-2 px-2.5 py-0.5 rounded bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-mono mb-2.5">
              <Flame className="h-3.5 w-3.5 text-amber-400" />
              <span>RBAC + Project Management & Max-Heap Priority Engine</span>
            </div>
            <h1 className="text-xl sm:text-2xl font-semibold text-zinc-100 tracking-tight">
              Welcome back, <span className="text-emerald-400 font-mono">{user?.username}</span>
            </h1>
            <p className="text-zinc-400 text-xs mt-1">
              DevOpsAI developer productivity & code intelligence platform
            </p>
          </div>

          <button
            onClick={onNavigateProjects}
            className="px-3.5 py-2 rounded-md bg-emerald-600 hover:bg-emerald-500 text-white font-medium text-xs transition-colors flex items-center space-x-1.5 shadow-sm"
          >
            <Kanban className="w-3.5 h-3.5" />
            <span>Open Kanban Workspace</span>
          </button>
        </div>
      </div>

      {/* Metrics Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3.5">
        
        <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-4">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-mono uppercase text-zinc-400">Security Risk</span>
            <div className="p-1.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              <Shield className="h-4 w-4" />
            </div>
          </div>
          <div className="mt-2 text-xl font-bold font-mono text-emerald-400">LOW</div>
          <div className="text-[11px] text-zinc-500 mt-1 font-mono">SAST & Secret Scanner Active</div>
        </div>

        <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-4">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-mono uppercase text-zinc-400">Assigned Role</span>
            <div className="p-1.5 rounded bg-zinc-800 text-zinc-300 border border-zinc-700">
              <Key className="h-4 w-4" />
            </div>
          </div>
          <div className="mt-2 text-xl font-bold font-mono text-zinc-100">{user?.role}</div>
          <div className="text-[11px] text-zinc-500 mt-1 font-mono">Role-Based Access Control</div>
        </div>

        <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-4">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-mono uppercase text-zinc-400">DSA Algorithm</span>
            <div className="p-1.5 rounded bg-amber-500/10 text-amber-400 border border-amber-500/20">
              <Flame className="h-4 w-4" />
            </div>
          </div>
          <div className="mt-2 text-xl font-bold font-mono text-amber-400">Max-Heap</div>
          <div className="text-[11px] text-zinc-500 mt-1 font-mono">Priority Queue Engine</div>
        </div>

        <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-4">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-mono uppercase text-zinc-400">Workflow Stages</span>
            <div className="p-1.5 rounded bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
              <Kanban className="h-4 w-4" />
            </div>
          </div>
          <div className="mt-2 text-xl font-bold font-mono text-zinc-100">5 Stages</div>
          <div className="text-[11px] text-zinc-500 mt-1 font-mono">TODO → IN_PROGRESS → DONE</div>
        </div>
      </div>

      {/* Role Capabilities Section */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-5">
        <h2 className="text-sm font-semibold text-zinc-100 mb-3 flex items-center space-x-2">
          <Shield className="h-4 w-4 text-emerald-400" />
          <span>Active Role Capabilities & Permissions</span>
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-2.5">
          {capabilities.map((cap, index) => (
            <div
              key={index}
              className={`p-3 rounded-md border flex items-center justify-between text-xs ${
                cap.allowed
                  ? 'bg-zinc-950 border-zinc-800/80 text-zinc-200'
                  : 'bg-zinc-950/50 border-zinc-900 text-zinc-500 opacity-60'
              }`}
            >
              <span className="font-medium font-sans">{cap.text}</span>
              <span
                className={`text-[10px] font-mono px-2 py-0.5 rounded border uppercase ${
                  cap.allowed
                    ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                    : 'bg-zinc-800 text-zinc-500 border-zinc-700'
                }`}
              >
                {cap.allowed ? 'GRANTED' : 'DENIED'}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
