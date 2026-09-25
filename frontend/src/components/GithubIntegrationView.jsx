import React, { useState, useEffect } from 'react';
import { githubService } from '../services/githubService';
import { ConnectGithubModal } from './ConnectGithubModal';
import { 
  Github, GitPullRequest, GitCommit, RefreshCw, ExternalLink, 
  FileCode, Plus, Minus, CheckCircle, Clock, AlertCircle, 
  GitBranch, Lock, ShieldCheck, ChevronRight, Code 
} from 'lucide-react';

export const GithubIntegrationView = ({ projectId }) => {
  const [repo, setRepo] = useState(null);
  const [commits, setCommits] = useState([]);
  const [pullRequests, setPullRequests] = useState([]);
  const [selectedPr, setSelectedPr] = useState(null);
  const [changedFiles, setChangedFiles] = useState([]);
  
  const [activeTab, setActiveTab] = useState('pulls'); // 'pulls' or 'commits'
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);
  const [syncNotice, setSyncNotice] = useState('');
  const [showConnectModal, setShowConnectModal] = useState(false);

  const fetchGithubData = async () => {
    if (!projectId) return;
    setLoading(true);
    try {
      const repoData = await githubService.getRepo(projectId);
      setRepo(repoData);

      if (repoData) {
        const [commitsData, prsData] = await Promise.all([
          githubService.getCommits(projectId),
          githubService.getPullRequests(projectId)
        ]);
        setCommits(commitsData);
        setPullRequests(prsData);

        if (prsData.length > 0) {
          setSelectedPr(prsData[0]);
          fetchChangedFiles(prsData[0].id);
        }
      }
    } catch (err) {
      console.error('Error fetching GitHub data:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchChangedFiles = async (prId) => {
    try {
      const files = await githubService.getChangedFiles(prId);
      setChangedFiles(files);
    } catch (err) {
      console.error('Error fetching changed files:', err);
    }
  };

  useEffect(() => {
    fetchGithubData();
  }, [projectId]);

  const handleTriggerSync = async () => {
    setSyncing(true);
    setSyncNotice('');
    try {
      const response = await githubService.triggerSync(projectId);
      setSyncNotice(response.message || 'GitHub async sync task launched successfully (HTTP 202 Accepted).');
      // Refresh repository metadata after trigger
      setTimeout(() => {
        fetchGithubData();
        setSyncing(false);
      }, 1500);
    } catch (err) {
      console.error('Sync failed:', err);
      setSyncing(false);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'COMPLETED':
        return <span className="px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-[10px] font-mono font-medium flex items-center space-x-1"><CheckCircle className="h-3 w-3" /><span>COMPLETED</span></span>;
      case 'IN_PROGRESS':
        return <span className="px-2 py-0.5 rounded bg-zinc-800 text-zinc-300 border border-zinc-700 text-[10px] font-mono font-medium flex items-center space-x-1"><RefreshCw className="h-3 w-3 animate-spin text-emerald-400" /><span>IN_PROGRESS</span></span>;
      case 'FAILED':
        return <span className="px-2 py-0.5 rounded bg-rose-500/10 text-rose-400 border border-rose-500/20 text-[10px] font-mono font-medium flex items-center space-x-1"><AlertCircle className="h-3 w-3" /><span>FAILED</span></span>;
      default:
        return <span className="px-2 py-0.5 rounded bg-amber-500/10 text-amber-400 border border-amber-500/20 text-[10px] font-mono font-medium flex items-center space-x-1"><Clock className="h-3 w-3" /><span>PENDING</span></span>;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16">
        <RefreshCw className="h-6 w-6 text-emerald-500 animate-spin" />
        <span className="ml-3 text-zinc-400 text-xs font-mono">Loading GitHub Integration workspace...</span>
      </div>
    );
  }

  if (!repo) {
    return (
      <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-10 text-center space-y-5 max-w-xl mx-auto my-8 shadow-sm">
        <div className="w-12 h-12 rounded-md bg-zinc-800 border border-zinc-700 flex items-center justify-center mx-auto text-zinc-200">
          <Github className="h-6 w-6" />
        </div>
        <div>
          <h2 className="text-base font-bold text-zinc-100">No GitHub Repository Connected</h2>
          <p className="text-zinc-400 text-xs mt-1 max-w-md mx-auto">
            Connect a GitHub repository to automatically sync commits, track pull requests, inspect changed file diff patches, and automate workflows.
          </p>
        </div>
        <button
          onClick={() => setShowConnectModal(true)}
          className="px-4 py-2 rounded-md bg-emerald-600 hover:bg-emerald-500 text-white font-medium text-xs shadow-sm flex items-center space-x-2 mx-auto transition-colors"
        >
          <Github className="h-4 w-4" />
          <span>Connect Repository</span>
        </button>

        <ConnectGithubModal
          isOpen={showConnectModal}
          onClose={() => setShowConnectModal(false)}
          projectId={projectId}
          onConnected={(connectedRepo) => {
            setRepo(connectedRepo);
            fetchGithubData();
          }}
        />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      
      {/* Connected Repo Header */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-5 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="flex items-start space-x-4">
          <div className="p-2.5 rounded-md bg-zinc-950 border border-zinc-800 text-zinc-200">
            <Github className="h-6 w-6" />
          </div>
          <div className="space-y-1">
            <div className="flex items-center space-x-3">
              <h2 className="text-lg font-bold text-zinc-100 font-mono">{repo.ownerName}/{repo.repoName}</h2>
              {getStatusBadge(repo.syncStatus)}
            </div>
            <div className="flex flex-wrap items-center gap-3 text-xs text-zinc-400 font-mono">
              <span className="flex items-center space-x-1 bg-zinc-950 px-2 py-0.5 rounded border border-zinc-800">
                <GitBranch className="h-3.5 w-3.5 text-emerald-400" />
                <span>{repo.defaultBranch}</span>
              </span>
              <span className="flex items-center space-x-1 bg-zinc-950 px-2 py-0.5 rounded border border-zinc-800">
                <ShieldCheck className="h-3.5 w-3.5 text-emerald-400" />
                <span>Token: <code className="text-zinc-200 font-bold">{repo.maskedAccessToken || 'ghp_****demo'}</code></span>
              </span>
              {repo.lastSyncedAt && (
                <span className="text-zinc-500">
                  Last Synced: {new Date(repo.lastSyncedAt).toLocaleTimeString()}
                </span>
              )}
            </div>
          </div>
        </div>

        <div className="flex items-center space-x-3">
          <a
            href={repo.htmlUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="px-3 py-1.5 rounded-md bg-zinc-950 hover:bg-zinc-800 border border-zinc-800 text-zinc-300 text-xs font-medium flex items-center space-x-1.5 transition-colors"
          >
            <ExternalLink className="h-3.5 w-3.5" />
            <span>GitHub URL</span>
          </a>

          <button
            onClick={handleTriggerSync}
            disabled={syncing}
            className="px-4 py-2 rounded-md bg-emerald-600 hover:bg-emerald-500 text-white font-medium text-xs shadow-sm flex items-center space-x-2 transition-colors disabled:opacity-50"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${syncing ? 'animate-spin' : ''}`} />
            <span>{syncing ? 'Syncing...' : 'Trigger Background Sync'}</span>
          </button>
        </div>
      </div>

      {/* Sync Status Banner */}
      {syncNotice && (
        <div className="p-3.5 rounded-md bg-emerald-500/10 border border-emerald-500/20 text-emerald-300 text-xs flex items-center space-x-3">
          <CheckCircle className="h-4 w-4 text-emerald-400 flex-shrink-0" />
          <div className="flex-1 font-mono">
            <span className="font-bold text-emerald-200">HTTP 202 Accepted:</span> {syncNotice}
          </div>
        </div>
      )}

      {/* Sub Navigation */}
      <div className="flex items-center space-x-2 border-b border-zinc-800 pb-3">
        <button
          onClick={() => setActiveTab('pulls')}
          className={`px-3 py-1.5 rounded-md text-xs font-medium flex items-center space-x-2 transition-colors ${
            activeTab === 'pulls'
              ? 'bg-zinc-800 text-zinc-100 border border-zinc-700'
              : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-800/40'
          }`}
        >
          <GitPullRequest className="h-3.5 w-3.5 text-emerald-400" />
          <span>Pull Requests ({pullRequests.length})</span>
        </button>

        <button
          onClick={() => setActiveTab('commits')}
          className={`px-3 py-1.5 rounded-md text-xs font-medium flex items-center space-x-2 transition-colors ${
            activeTab === 'commits'
              ? 'bg-zinc-800 text-zinc-100 border border-zinc-700'
              : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-800/40'
          }`}
        >
          <GitCommit className="h-3.5 w-3.5 text-emerald-400" />
          <span>Commit History ({commits.length})</span>
        </button>
      </div>

      {/* Tab 1: Pull Requests & Diff Patch Inspector */}
      {activeTab === 'pulls' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          
          {/* Left Column: PR List */}
          <div className="lg:col-span-5 space-y-3">
            <h3 className="text-xs font-semibold text-zinc-400 uppercase tracking-wider">Pull Requests</h3>
            
            {pullRequests.length === 0 ? (
              <div className="p-6 text-center text-zinc-500 text-xs bg-zinc-900 border border-zinc-800 rounded-md font-mono">
                No pull requests found.
              </div>
            ) : (
              pullRequests.map((pr) => (
                <div
                  key={pr.id}
                  onClick={() => {
                    setSelectedPr(pr);
                    fetchChangedFiles(pr.id);
                  }}
                  className={`p-4 rounded-md border transition-colors cursor-pointer space-y-2.5 ${
                    selectedPr?.id === pr.id
                      ? 'bg-zinc-900 border-zinc-700'
                      : 'bg-zinc-900/60 border-zinc-800 hover:border-zinc-700'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-mono font-medium text-emerald-400">
                      PR #{pr.number}
                    </span>
                    <span className={`px-2 py-0.5 rounded text-[10px] font-mono font-medium uppercase ${
                      pr.state === 'MERGED'
                        ? 'bg-purple-500/10 text-purple-400 border border-purple-500/20'
                        : pr.state === 'OPEN'
                        ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                        : 'bg-zinc-800 text-zinc-400'
                    }`}>
                      {pr.state}
                    </span>
                  </div>

                  <h4 className="text-xs font-semibold text-zinc-200 leading-snug">{pr.title}</h4>

                  <p className="text-zinc-400 text-xs line-clamp-2">{pr.description}</p>

                  <div className="pt-2.5 border-t border-zinc-800/80 flex items-center justify-between text-xs text-zinc-400 font-mono">
                    <div className="flex items-center space-x-1.5 text-zinc-400">
                      <GitBranch className="h-3 w-3 text-emerald-400" />
                      <span>{pr.headBranch}</span>
                      <ChevronRight className="h-3 w-3 text-zinc-600" />
                      <span>{pr.baseBranch}</span>
                    </div>

                    <div className="flex items-center space-x-2">
                      <span className="text-emerald-400 font-medium">+{pr.additions}</span>
                      <span className="text-rose-400 font-medium">-{pr.deletions}</span>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>

          {/* Right Column: Changed Files Diff Inspector */}
          <div className="lg:col-span-7 space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="text-xs font-semibold text-zinc-400 uppercase tracking-wider">
                Changed Files Patch Inspector {selectedPr ? `(PR #${selectedPr.number})` : ''}
              </h3>
              <span className="text-xs font-mono text-emerald-400">
                {changedFiles.length} files changed
              </span>
            </div>

            {changedFiles.length === 0 ? (
              <div className="p-10 text-center text-zinc-500 text-xs bg-zinc-900 border border-zinc-800 rounded-md font-mono">
                Select a pull request to inspect changed files and unified diff patches.
              </div>
            ) : (
              <div className="space-y-3">
                {changedFiles.map((file) => (
                  <div key={file.id} className="bg-zinc-950 border border-zinc-800 rounded-md overflow-hidden shadow-sm">
                    
                    {/* File Header */}
                    <div className="bg-zinc-900 px-3.5 py-2.5 border-b border-zinc-800 flex items-center justify-between">
                      <div className="flex items-center space-x-2 text-xs font-mono">
                        <FileCode className="h-4 w-4 text-emerald-400" />
                        <span className="text-zinc-200 font-medium">{file.filename}</span>
                        <span className={`px-1.5 py-0.5 rounded text-[10px] uppercase font-mono font-medium ${
                          file.status === 'added'
                            ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                            : 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                        }`}>
                          {file.status}
                        </span>
                      </div>

                      <div className="flex items-center space-x-2 text-xs font-mono">
                        <span className="text-emerald-400 font-medium">+{file.additions}</span>
                        <span className="text-rose-400 font-medium">-{file.deletions}</span>
                      </div>
                    </div>

                    {/* Diff Patch Code Inspector */}
                    <div className="p-3 bg-zinc-950 overflow-x-auto text-xs font-mono leading-relaxed">
                      {file.patch ? (
                        <pre className="text-zinc-300">
                          {file.patch.split('\n').map((line, idx) => {
                            let lineStyle = 'text-zinc-400';
                            if (line.startsWith('+')) lineStyle = 'text-emerald-400 bg-emerald-500/10 px-1 py-0.5 rounded block';
                            else if (line.startsWith('-')) lineStyle = 'text-rose-400 bg-rose-500/10 px-1 py-0.5 rounded block';
                            else if (line.startsWith('@@')) lineStyle = 'text-zinc-300 font-semibold bg-zinc-800 px-1 py-0.5 rounded block my-1';
                            
                            return (
                              <span key={idx} className={lineStyle}>
                                {line}
                              </span>
                            );
                          })}
                        </pre>
                      ) : (
                        <span className="text-zinc-500 italic">No patch diff preview available.</span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

        </div>
      )}

      {/* Tab 2: Commits Timeline */}
      {activeTab === 'commits' && (
        <div className="space-y-3">
          <h3 className="text-xs font-semibold text-zinc-400 uppercase tracking-wider">Commit Timeline</h3>
          
          {commits.length === 0 ? (
            <div className="p-10 text-center text-zinc-500 text-xs bg-zinc-900 border border-zinc-800 rounded-md font-mono">
              No commit history synchronized yet. Click "Trigger Background Sync" above.
            </div>
          ) : (
            <div className="relative border-l border-zinc-800 ml-4 space-y-4 pl-5 py-1">
              {commits.map((commit) => (
                <div key={commit.id} className="relative group">
                  <div className="absolute -left-[25px] top-2 w-2.5 h-2.5 rounded-full bg-emerald-500 ring-4 ring-zinc-950" />
                  
                  <div className="bg-zinc-900 border border-zinc-800 rounded-md p-3.5 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-3">
                    <div className="space-y-1">
                      <div className="flex items-center space-x-2">
                        <span className="font-mono text-[11px] font-medium text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
                          {commit.sha?.substring(0, 7)}
                        </span>
                        <h4 className="text-xs font-semibold text-zinc-200">{commit.message}</h4>
                      </div>
                      <div className="flex items-center space-x-2 text-[11px] text-zinc-400 font-mono">
                        <span>Author: <strong className="text-zinc-300">{commit.authorName}</strong></span>
                        <span>•</span>
                        <span>{commit.authorEmail}</span>
                        {commit.commitDate && (
                          <>
                            <span>•</span>
                            <span>{new Date(commit.commitDate).toLocaleString()}</span>
                          </>
                        )}
                      </div>
                    </div>

                    {commit.htmlUrl && (
                      <a
                        href={commit.htmlUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="px-2.5 py-1 rounded bg-zinc-950 hover:bg-zinc-800 border border-zinc-800 text-zinc-300 text-xs font-medium flex items-center space-x-1 self-start md:self-center transition-colors"
                      >
                        <ExternalLink className="h-3 w-3" />
                        <span>View Commit</span>
                      </a>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

    </div>
  );
};

