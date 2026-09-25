import React, { useState, useEffect } from 'react';
import { issueService } from '../services/issueService';
import { Flame, AlertTriangle, ShieldAlert, Calendar, GitCommit, UserCheck, RefreshCw } from 'lucide-react';

export const PriorityBacklog = ({ projectId }) => {
  const [prioritizedIssues, setPrioritizedIssues] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchPrioritizedBacklog = async () => {
    try {
      setLoading(true);
      const data = await issueService.getPrioritizedBacklog(projectId);
      setPrioritizedIssues(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch Max-Heap prioritized backlog.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (projectId) {
      fetchPrioritizedBacklog();
    }
  }, [projectId]);

  if (loading) {
    return (
      <div className="p-8 text-center text-zinc-400 font-mono text-xs">
        <div className="animate-spin inline-block w-5 h-5 border-2 border-amber-500 border-t-transparent rounded-full mb-2"></div>
        <p>Executing DSA Max-Heap Priority Ranking Algorithm...</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Header Info */}
      <div className="flex flex-wrap items-center justify-between gap-3 bg-zinc-900 p-4 rounded-lg border border-zinc-800">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <Flame className="w-4 h-4 text-amber-400" />
            <h3 className="text-sm font-semibold text-zinc-100">DSA Max-Heap Prioritized Backlog</h3>
          </div>
          <p className="text-xs text-zinc-400 font-sans">
            Issues ranked via Max-Heap sorting formula:{' '}
            <span className="font-mono text-amber-400 font-medium text-[11px]">
              Priority Score = (Severity + Impact) / max(DaysRemaining, 1.0) + Dependencies
            </span>
          </p>
        </div>

        <button
          onClick={fetchPrioritizedBacklog}
          className="px-3 py-1.5 bg-zinc-800 hover:bg-zinc-700 text-zinc-200 rounded-md text-xs font-medium border border-zinc-700 transition-colors flex items-center gap-1.5"
        >
          <RefreshCw className="w-3.5 h-3.5 text-amber-400" /> Re-evaluate Heap
        </button>
      </div>

      {error && (
        <div className="p-3 bg-rose-500/10 border border-rose-500/20 rounded-md text-rose-400 text-xs font-mono">
          {error}
        </div>
      )}

      {/* Prioritized Issues List */}
      <div className="space-y-2.5">
        {prioritizedIssues.length === 0 ? (
          <div className="p-8 text-center bg-zinc-900 border border-zinc-800 rounded-lg text-zinc-500 text-xs font-mono">
            No issues found in project backlog.
          </div>
        ) : (
          prioritizedIssues.map((issue, index) => {
            const isTopRanked = index === 0;
            return (
              <div
                key={issue.id}
                className={`p-3.5 rounded-md border transition-colors flex flex-col md:flex-row md:items-center justify-between gap-3 ${
                  isTopRanked
                    ? 'bg-zinc-900 border-amber-500/50 shadow-sm'
                    : 'bg-zinc-900 border-zinc-800 hover:border-zinc-700'
                }`}
              >
                {/* Left Rank & Details */}
                <div className="flex items-start gap-3">
                  <div className={`w-7 h-7 rounded-md font-mono font-bold flex items-center justify-center text-xs ${
                    isTopRanked
                      ? 'bg-amber-500 text-zinc-950 font-bold'
                      : 'bg-zinc-800 text-zinc-400 border border-zinc-700'
                  }`}>
                    #{index + 1}
                  </div>

                  <div>
                    <div className="flex flex-wrap items-center gap-1.5 mb-1">
                      <span className={`text-[9px] font-mono font-semibold px-1.5 py-0.5 rounded uppercase tracking-wider ${
                        issue.type === 'BUG'
                          ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                          : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                      }`}>
                        {issue.type}
                      </span>

                      <span className={`text-[9px] font-mono font-semibold px-1.5 py-0.5 rounded uppercase ${
                        issue.status === 'DONE' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' : 'bg-zinc-800 text-zinc-400 border border-zinc-700'
                      }`}>
                        {issue.status}
                      </span>

                      {issue.epicName && (
                        <span className="text-[9px] font-mono px-1.5 py-0.5 rounded bg-zinc-800 text-zinc-300 border border-zinc-700">
                          Epic: {issue.epicName}
                        </span>
                      )}
                    </div>

                    <h4 className="text-xs font-semibold text-zinc-100">
                      {issue.title}
                    </h4>

                    {issue.description && (
                      <p className="text-[11px] text-zinc-400 mt-0.5 line-clamp-1 font-sans">
                        {issue.description}
                      </p>
                    )}
                  </div>
                </div>

                {/* Right Parameters Breakdown & Calculated Score */}
                <div className="flex items-center gap-4 self-end md:self-center border-t md:border-t-0 border-zinc-800 pt-2 md:pt-0">
                  <div className="grid grid-cols-3 gap-2 text-center text-xs">
                    <div className="bg-zinc-950 px-2 py-1 rounded-md border border-zinc-800">
                      <span className="text-[9px] text-zinc-500 uppercase block font-mono">Severity</span>
                      <span className="font-mono font-bold text-rose-400 text-xs">{issue.severity}/10</span>
                    </div>

                    <div className="bg-zinc-950 px-2 py-1 rounded-md border border-zinc-800">
                      <span className="text-[9px] text-zinc-500 uppercase block font-mono">Impact</span>
                      <span className="font-mono font-bold text-cyan-400 text-xs">{issue.businessImpact}/10</span>
                    </div>

                    <div className="bg-zinc-950 px-2 py-1 rounded-md border border-zinc-800">
                      <span className="text-[9px] text-zinc-500 uppercase block font-mono font-mono">Blocked</span>
                      <span className="font-mono font-bold text-zinc-300 text-xs">{issue.dependenciesCount}</span>
                    </div>
                  </div>

                  <div className="bg-zinc-950 border border-zinc-800 p-2 rounded-md text-right min-w-[95px]">
                    <span className="text-[9px] uppercase font-mono font-semibold text-amber-400 block tracking-wider">
                      Priority Score
                    </span>
                    <span className="text-base font-mono font-bold text-amber-400">
                      {issue.calculatedPriorityScore.toFixed(2)}
                    </span>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};
