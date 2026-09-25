import React, { useState, useEffect } from 'react';
import { issueService } from '../services/issueService';
import { 
  Plus, AlertCircle, Clock, ArrowRight, CheckCircle2, 
  Code2, TestTube2, Layers, Flame, UserCheck 
} from 'lucide-react';

const COLUMNS = [
  { key: 'TODO', label: 'To Do', color: 'text-zinc-300', bg: 'bg-zinc-900/40' },
  { key: 'IN_PROGRESS', label: 'In Progress', color: 'text-cyan-400', bg: 'bg-zinc-900/40' },
  { key: 'CODE_REVIEW', label: 'Code Review', color: 'text-purple-400', bg: 'bg-zinc-900/40' },
  { key: 'TESTING', label: 'Testing', color: 'text-amber-400', bg: 'bg-zinc-900/40' },
  { key: 'DONE', label: 'Done', color: 'text-emerald-400', bg: 'bg-zinc-900/40' }
];

export const KanbanBoard = ({ projectId, onOpenCreateIssue }) => {
  const [issues, setIssues] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchIssues = async () => {
    try {
      setLoading(true);
      const data = await issueService.getIssues(projectId);
      setIssues(data);
      setError(null);
    } catch (err) {
      setError('Failed to load Kanban board issues.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (projectId) {
      fetchIssues();
    }
  }, [projectId]);

  const handleStatusTransition = async (issueId, currentStatus) => {
    const statusOrder = ['TODO', 'IN_PROGRESS', 'CODE_REVIEW', 'TESTING', 'DONE'];
    const currentIndex = statusOrder.indexOf(currentStatus);
    if (currentIndex === -1 || currentIndex === statusOrder.length - 1) return;

    const nextStatus = statusOrder[currentIndex + 1];
    try {
      await issueService.updateIssueStatus(issueId, nextStatus);
      fetchIssues();
    } catch (err) {
      alert('Failed to update issue status.');
    }
  };

  if (loading) {
    return (
      <div className="p-8 text-center text-zinc-400 font-mono text-xs">
        <div className="animate-spin inline-block w-5 h-5 border-2 border-emerald-500 border-t-transparent rounded-full mb-2"></div>
        <p>Loading Kanban Board...</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Top Action Bar */}
      <div className="flex flex-wrap items-center justify-between gap-3 bg-zinc-900 p-3.5 rounded-lg border border-zinc-800">
        <div>
          <h2 className="text-sm font-semibold text-zinc-100 flex items-center gap-2">
            <Layers className="w-4 h-4 text-emerald-400" /> Project Kanban Board
          </h2>
          <p className="text-xs text-zinc-400 font-mono mt-0.5">5-Stage Workflow Lifecycle: TODO → IN_PROGRESS → CODE_REVIEW → TESTING → DONE</p>
        </div>

        <button
          onClick={onOpenCreateIssue}
          className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-md font-medium text-xs shadow-sm transition-colors flex items-center gap-1.5"
        >
          <Plus className="w-3.5 h-3.5" /> Create Issue
        </button>
      </div>

      {error && (
        <div className="p-3 bg-rose-500/10 border border-rose-500/20 rounded-md text-rose-400 text-xs font-mono">
          {error}
        </div>
      )}

      {/* 5 Column Board Grid */}
      <div className="grid grid-cols-1 md:grid-cols-5 gap-3">
        {COLUMNS.map((col) => {
          const colIssues = issues.filter(i => i.status === col.key);
          return (
            <div key={col.key} className={`rounded-lg border border-zinc-800 p-3 flex flex-col min-h-[480px] ${col.bg}`}>
              <div className="flex items-center justify-between pb-2.5 border-b border-zinc-800 mb-3">
                <span className={`font-mono text-xs font-semibold uppercase tracking-wider ${col.color}`}>
                  {col.label}
                </span>
                <span className="px-1.5 py-0.5 text-[10px] font-mono font-semibold bg-zinc-800 text-zinc-400 border border-zinc-700 rounded-md">
                  {colIssues.length}
                </span>
              </div>

              <div className="space-y-2.5 flex-1">
                {colIssues.length === 0 ? (
                  <div className="p-3 text-center text-[11px] text-zinc-600 font-mono italic border border-dashed border-zinc-800/80 rounded-md">
                    No issues in {col.label}
                  </div>
                ) : (
                  colIssues.map((issue) => (
                    <div
                      key={issue.id}
                      className="p-3 rounded-md bg-zinc-950 border border-zinc-800 hover:border-zinc-700 transition-colors group flex flex-col justify-between"
                    >
                      <div>
                        {/* Type & Priority Score Badges */}
                        <div className="flex items-center justify-between mb-2">
                          <span className={`text-[9px] font-mono font-semibold px-1.5 py-0.5 rounded uppercase tracking-wider ${
                            issue.type === 'BUG' 
                              ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20' 
                              : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                          }`}>
                            {issue.type}
                          </span>

                          <span className="text-[9px] font-mono px-1.5 py-0.5 rounded bg-zinc-900 text-amber-400 border border-zinc-800 flex items-center gap-1">
                            <Flame className="w-2.5 h-2.5" /> Score: {issue.calculatedPriorityScore.toFixed(2)}
                          </span>
                        </div>

                        {/* Title & Description */}
                        <h4 className="font-medium text-zinc-100 text-xs mb-1 line-clamp-2 group-hover:text-emerald-400 transition-colors">
                          {issue.title}
                        </h4>
                        {issue.description && (
                          <p className="text-[11px] text-zinc-400 line-clamp-2 mb-2">
                            {issue.description}
                          </p>
                        )}
                      </div>

                      {/* Footer Details */}
                      <div className="pt-2.5 border-t border-zinc-900 flex items-center justify-between text-[11px] text-zinc-500 font-mono">
                        <div className="flex items-center gap-1.5">
                          <UserCheck className="w-3.5 h-3.5 text-zinc-400" />
                          <span className="truncate max-w-[80px]">{issue.assigneeUsername || 'Unassigned'}</span>
                        </div>

                        {col.key !== 'DONE' && (
                          <button
                            onClick={() => handleStatusTransition(issue.id, issue.status)}
                            className="p-1 rounded bg-zinc-900 hover:bg-zinc-800 text-zinc-300 hover:text-emerald-400 border border-zinc-800 transition-colors"
                            title="Move to Next Stage"
                          >
                            <ArrowRight className="w-3.5 h-3.5" />
                          </button>
                        )}
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
