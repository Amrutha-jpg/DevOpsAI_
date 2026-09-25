import React, { useState } from 'react';
import { githubService } from '../services/githubService';
import { Github, Key, GitBranch, X, CheckCircle, AlertTriangle } from 'lucide-react';

export const ConnectGithubModal = ({ isOpen, onClose, projectId, onConnected }) => {
  const [ownerName, setOwnerName] = useState('devopsai');
  const [repoName, setRepoName] = useState('smarthealth-platform');
  const [defaultBranch, setDefaultBranch] = useState('main');
  const [accessToken, setAccessToken] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const connected = await githubService.connectRepo(projectId, {
        ownerName,
        repoName,
        defaultBranch,
        accessToken
      });
      onConnected(connected);
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Failed to connect GitHub repository');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-zinc-950/80 backdrop-blur-sm">
      <div className="w-full max-w-md bg-zinc-900 border border-zinc-800 rounded-lg p-6 shadow-xl space-y-4">
        
        <div className="flex items-center justify-between pb-3 border-b border-zinc-800">
          <div className="flex items-center space-x-2.5">
            <div className="p-1.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              <Github className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-zinc-100">Connect GitHub Repo</h2>
              <p className="text-xs text-zinc-400">Link your GitHub repository to project #{projectId}</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1 rounded text-zinc-400 hover:text-zinc-200 hover:bg-zinc-800 transition-colors">
            <X className="h-4 w-4" />
          </button>
        </div>

        {error && (
          <div className="p-3 rounded bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs flex items-center space-x-2">
            <AlertTriangle className="h-4 w-4 text-rose-400 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-[11px] font-semibold text-zinc-400 uppercase tracking-wider mb-1">
              Repository Owner
            </label>
            <input
              type="text"
              required
              value={ownerName}
              onChange={(e) => setOwnerName(e.target.value)}
              placeholder="e.g. devopsai or octocat"
              className="w-full bg-zinc-950 border border-zinc-800 rounded-md py-1.5 px-3 text-xs text-zinc-100 focus:outline-none focus:border-zinc-700 font-mono"
            />
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-zinc-400 uppercase tracking-wider mb-1">
              Repository Name
            </label>
            <input
              type="text"
              required
              value={repoName}
              onChange={(e) => setRepoName(e.target.value)}
              placeholder="e.g. smarthealth-platform"
              className="w-full bg-zinc-950 border border-zinc-800 rounded-md py-1.5 px-3 text-xs text-zinc-100 focus:outline-none focus:border-zinc-700 font-mono"
            />
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-zinc-400 uppercase tracking-wider mb-1">
              Default Branch
            </label>
            <div className="relative">
              <input
                type="text"
                required
                value={defaultBranch}
                onChange={(e) => setDefaultBranch(e.target.value)}
                placeholder="main"
                className="w-full bg-zinc-950 border border-zinc-800 rounded-md py-1.5 pl-8 pr-3 text-xs text-zinc-100 focus:outline-none focus:border-zinc-700 font-mono"
              />
              <GitBranch className="h-3.5 w-3.5 text-zinc-500 absolute left-2.5 top-2" />
            </div>
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-zinc-400 uppercase tracking-wider mb-1 flex items-center justify-between">
              <span>Personal Access Token</span>
              <span className="text-[10px] text-emerald-400 font-mono font-normal lowercase">Encrypted @ Rest</span>
            </label>
            <div className="relative">
              <input
                type="password"
                value={accessToken}
                onChange={(e) => setAccessToken(e.target.value)}
                placeholder="ghp_********************************"
                className="w-full bg-zinc-950 border border-zinc-800 rounded-md py-1.5 pl-8 pr-3 text-xs text-zinc-100 focus:outline-none focus:border-zinc-700 font-mono"
              />
              <Key className="h-3.5 w-3.5 text-zinc-500 absolute left-2.5 top-2" />
            </div>
            <p className="text-[10px] text-zinc-500 mt-1 font-mono">
              Token is encrypted at rest using AES JPA converter and masked as <code className="text-zinc-300">ghp_****abcd</code> in APIs.
            </p>
          </div>

          <div className="flex justify-end space-x-2.5 pt-3 border-t border-zinc-800">
            <button
              type="button"
              onClick={onClose}
              className="px-3 py-1.5 rounded-md bg-zinc-800 hover:bg-zinc-700 text-zinc-300 text-xs font-medium transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-1.5 rounded-md bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-medium shadow-sm flex items-center space-x-1.5 transition-colors disabled:opacity-50"
            >
              {loading ? (
                <span>Connecting...</span>
              ) : (
                <>
                  <CheckCircle className="h-3.5 w-3.5" />
                  <span>Connect Repository</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

