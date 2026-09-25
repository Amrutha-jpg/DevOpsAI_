import React, { useState, useEffect } from 'react';
import { issueService } from '../services/issueService';
import { X, Flame, AlertCircle } from 'lucide-react';

export const IssueModal = ({ isOpen, onClose, projectId, onIssueCreated }) => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    type: 'TASK',
    status: 'TODO',
    priority: 'MEDIUM',
    severity: 5,
    businessImpact: 5,
    dueDate: '',
    dependenciesCount: 0,
    epicId: '',
    sprintId: ''
  });

  const [epics, setEpics] = useState([]);
  const [sprints, setSprints] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isOpen && projectId) {
      loadEpicsAndSprints();
    }
  }, [isOpen, projectId]);

  const loadEpicsAndSprints = async () => {
    try {
      const [epicsData, sprintsData] = await Promise.all([
        issueService.getEpics(projectId),
        issueService.getSprints(projectId)
      ]);
      setEpics(epicsData);
      setSprints(sprintsData);
    } catch (err) {
      console.error('Error loading epics/sprints:', err);
    }
  };

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const payload = {
        ...formData,
        epicId: formData.epicId ? parseInt(formData.epicId) : null,
        sprintId: formData.sprintId ? parseInt(formData.sprintId) : null
      };

      await issueService.createIssue(projectId, payload);
      onIssueCreated();
      onClose();
      // Reset form
      setFormData({
        title: '',
        description: '',
        type: 'TASK',
        status: 'TODO',
        priority: 'MEDIUM',
        severity: 5,
        businessImpact: 5,
        dueDate: '',
        dependenciesCount: 0,
        epicId: '',
        sprintId: ''
      });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create issue.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-zinc-950/80 backdrop-blur-sm">
      <div className="bg-zinc-900 border border-zinc-800 rounded-lg w-full max-w-2xl overflow-hidden shadow-sm">
        <div className="flex items-center justify-between p-4 border-b border-zinc-800 bg-zinc-950">
          <h3 className="text-sm font-semibold text-zinc-100 flex items-center gap-2">
            <Flame className="w-4 h-4 text-emerald-400" /> Create Task or Bug Issue
          </h3>
          <button onClick={onClose} className="p-1 rounded text-zinc-400 hover:text-zinc-100 hover:bg-zinc-800">
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 space-y-3.5 max-h-[80vh] overflow-y-auto">
          {error && (
            <div className="p-3 bg-rose-500/10 border border-rose-500/20 rounded-md text-rose-400 text-xs font-mono">
              {error}
            </div>
          )}

          <div>
            <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">Issue Title *</label>
            <input
              type="text"
              required
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              placeholder="e.g. Critical: Fix Memory Leak in Telemetry Stream"
              className="w-full bg-zinc-950 border border-zinc-800 rounded-md px-3 py-1.5 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500 font-sans"
            />
          </div>

          <div>
            <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">Description</label>
            <textarea
              rows="3"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Provide technical context, steps to reproduce, or requirements..."
              className="w-full bg-zinc-950 border border-zinc-800 rounded-md px-3 py-1.5 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500 font-sans"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div>
              <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">Issue Type</label>
              <select
                value={formData.type}
                onChange={(e) => setFormData({ ...formData, type: e.target.value })}
                className="w-full bg-zinc-950 border border-zinc-800 rounded-md px-3 py-1.5 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500 font-mono"
              >
                <option value="TASK">TASK</option>
                <option value="BUG">BUG</option>
              </select>
            </div>

            <div>
              <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">Initial Status</label>
              <select
                value={formData.status}
                onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                className="w-full bg-zinc-950 border border-zinc-800 rounded-md px-3 py-1.5 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500 font-mono"
              >
                <option value="TODO">To Do</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="CODE_REVIEW">Code Review</option>
                <option value="TESTING">Testing</option>
                <option value="DONE">Done</option>
              </select>
            </div>

            <div>
              <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">Priority Tag</label>
              <select
                value={formData.priority}
                onChange={(e) => setFormData({ ...formData, priority: e.target.value })}
                className="w-full bg-zinc-950 border border-zinc-800 rounded-md px-3 py-1.5 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500 font-mono"
              >
                <option value="LOW">LOW</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="HIGH">HIGH</option>
                <option value="CRITICAL">CRITICAL</option>
              </select>
            </div>
          </div>

          {/* DSA Priority Formula Parameters */}
          <div className="p-3.5 rounded-md bg-zinc-950 border border-zinc-800 space-y-2.5">
            <h4 className="text-[11px] font-mono font-semibold text-amber-400 uppercase tracking-wider">
              DSA Priority Ranking Parameters
            </h4>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div>
                <label className="block text-[11px] font-mono text-zinc-400 mb-1">
                  Severity (1 - 10): <span className="font-mono text-emerald-400 font-bold">{formData.severity}</span>
                </label>
                <input
                  type="range"
                  min="1"
                  max="10"
                  value={formData.severity}
                  onChange={(e) => setFormData({ ...formData, severity: parseInt(e.target.value) })}
                  className="w-full accent-emerald-500"
                />
              </div>

              <div>
                <label className="block text-[11px] font-mono text-zinc-400 mb-1">
                  Business Impact (1 - 10): <span className="font-mono text-emerald-400 font-bold">{formData.businessImpact}</span>
                </label>
                <input
                  type="range"
                  min="1"
                  max="10"
                  value={formData.businessImpact}
                  onChange={(e) => setFormData({ ...formData, businessImpact: parseInt(e.target.value) })}
                  className="w-full accent-emerald-500"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div>
                <label className="block text-[11px] font-mono text-zinc-400 mb-1">Due Date (Deadline)</label>
                <input
                  type="date"
                  value={formData.dueDate}
                  onChange={(e) => setFormData({ ...formData, dueDate: e.target.value })}
                  className="w-full bg-zinc-900 border border-zinc-800 rounded-md px-3 py-1 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500 font-mono"
                />
              </div>

              <div>
                <label className="block text-[11px] font-mono text-zinc-400 mb-1">Dependencies Count</label>
                <input
                  type="number"
                  min="0"
                  value={formData.dependenciesCount}
                  onChange={(e) => setFormData({ ...formData, dependenciesCount: parseInt(e.target.value) || 0 })}
                  className="w-full bg-zinc-900 border border-zinc-800 rounded-md px-3 py-1 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500 font-mono"
                />
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">Associate Epic</label>
              <select
                value={formData.epicId}
                onChange={(e) => setFormData({ ...formData, epicId: e.target.value })}
                className="w-full bg-zinc-950 border border-zinc-800 rounded-md px-3 py-1.5 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500 font-mono"
              >
                <option value="">-- No Epic --</option>
                {epics.map(epic => (
                  <option key={epic.id} value={epic.id}>{epic.name}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">Associate Sprint</label>
              <select
                value={formData.sprintId}
                onChange={(e) => setFormData({ ...formData, sprintId: e.target.value })}
                className="w-full bg-zinc-950 border border-zinc-800 rounded-md px-3 py-1.5 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500 font-mono"
              >
                <option value="">-- No Sprint --</option>
                {sprints.map(sprint => (
                  <option key={sprint.id} value={sprint.id}>{sprint.name} ({sprint.status})</option>
                ))}
              </select>
            </div>
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-zinc-800">
            <button
              type="button"
              onClick={onClose}
              className="px-3 py-1.5 bg-zinc-800 hover:bg-zinc-700 text-zinc-300 rounded-md text-xs font-medium transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-md text-xs font-medium shadow-sm transition-colors"
            >
              {loading ? 'Creating...' : 'Create Issue'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default IssueModal;
