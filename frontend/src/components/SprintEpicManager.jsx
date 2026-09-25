import React, { useState, useEffect } from 'react';
import { issueService } from '../services/issueService';
import { Layers, Calendar, Plus, CheckCircle, Clock } from 'lucide-react';

export const SprintEpicManager = ({ projectId }) => {
  const [epics, setEpics] = useState([]);
  const [sprints, setSprints] = useState([]);
  const [epicName, setEpicName] = useState('');
  const [epicDesc, setEpicDesc] = useState('');
  const [sprintName, setSprintName] = useState('');
  const [sprintGoal, setSprintGoal] = useState('');
  const [sprintStart, setSprintStart] = useState('');
  const [sprintEnd, setSprintEnd] = useState('');

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (projectId) {
      loadData();
    }
  }, [projectId]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [epicsData, sprintsData] = await Promise.all([
        issueService.getEpics(projectId),
        issueService.getSprints(projectId)
      ]);
      setEpics(epicsData);
      setSprints(sprintsData);
    } catch (err) {
      console.error('Failed to load epics/sprints', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateEpic = async (e) => {
    e.preventDefault();
    if (!epicName.trim()) return;
    try {
      await issueService.createEpic(projectId, { name: epicName, description: epicDesc });
      setEpicName('');
      setEpicDesc('');
      loadData();
    } catch (err) {
      alert('Failed to create epic.');
    }
  };

  const handleCreateSprint = async (e) => {
    e.preventDefault();
    if (!sprintName.trim()) return;
    try {
      await issueService.createSprint(projectId, {
        name: sprintName,
        goal: sprintGoal,
        startDate: sprintStart || null,
        endDate: sprintEnd || null
      });
      setSprintName('');
      setSprintGoal('');
      setSprintStart('');
      setSprintEnd('');
      loadData();
    } catch (err) {
      alert('Failed to create sprint.');
    }
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      {/* Epics Section */}
      <div className="p-4 bg-zinc-900 border border-zinc-800 rounded-lg space-y-3.5">
        <h3 className="text-sm font-semibold text-zinc-100 flex items-center gap-2">
          <Layers className="w-4 h-4 text-cyan-400" /> Epics Management
        </h3>

        <form onSubmit={handleCreateEpic} className="space-y-2.5 bg-zinc-950 p-3 rounded-md border border-zinc-800">
          <div>
            <input
              type="text"
              required
              placeholder="Epic Name (e.g. Core Auth Module)"
              value={epicName}
              onChange={(e) => setEpicName(e.target.value)}
              className="w-full bg-zinc-900 border border-zinc-800 rounded-md px-3 py-1.5 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500 font-sans"
            />
          </div>
          <div>
            <input
              type="text"
              placeholder="Epic Description..."
              value={epicDesc}
              onChange={(e) => setEpicDesc(e.target.value)}
              className="w-full bg-zinc-900 border border-zinc-800 rounded-md px-3 py-1.5 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500 font-sans"
            />
          </div>
          <button
            type="submit"
            className="w-full py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-md font-medium text-xs flex items-center justify-center gap-1 transition-colors shadow-sm"
          >
            <Plus className="w-3.5 h-3.5" /> Create Epic
          </button>
        </form>

        <div className="space-y-2 max-h-[280px] overflow-y-auto pr-1">
          {epics.map(epic => (
            <div key={epic.id} className="p-3 bg-zinc-950 rounded-md border border-zinc-800">
              <h4 className="font-semibold text-zinc-200 text-xs">{epic.name}</h4>
              {epic.description && <p className="text-[11px] text-zinc-400 mt-0.5 font-sans">{epic.description}</p>}
            </div>
          ))}
        </div>
      </div>

      {/* Sprints Section */}
      <div className="p-4 bg-zinc-900 border border-zinc-800 rounded-lg space-y-3.5">
        <h3 className="text-sm font-semibold text-zinc-100 flex items-center gap-2">
          <Calendar className="w-4 h-4 text-emerald-400" /> Sprints Management
        </h3>

        <form onSubmit={handleCreateSprint} className="space-y-2.5 bg-zinc-950 p-3 rounded-md border border-zinc-800">
          <div>
            <input
              type="text"
              required
              placeholder="Sprint Name (e.g. Sprint 1 - Foundation)"
              value={sprintName}
              onChange={(e) => setSprintName(e.target.value)}
              className="w-full bg-zinc-900 border border-zinc-800 rounded-md px-3 py-1.5 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500 font-sans"
            />
          </div>
          <div>
            <input
              type="text"
              placeholder="Sprint Goal..."
              value={sprintGoal}
              onChange={(e) => setSprintGoal(e.target.value)}
              className="w-full bg-zinc-900 border border-zinc-800 rounded-md px-3 py-1.5 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500 font-sans"
            />
          </div>
          <div className="grid grid-cols-2 gap-2 font-mono text-xs">
            <input
              type="date"
              value={sprintStart}
              onChange={(e) => setSprintStart(e.target.value)}
              className="bg-zinc-900 border border-zinc-800 rounded-md px-2 py-1 text-xs text-zinc-200"
            />
            <input
              type="date"
              value={sprintEnd}
              onChange={(e) => setSprintEnd(e.target.value)}
              className="bg-zinc-900 border border-zinc-800 rounded-md px-2 py-1 text-xs text-zinc-200"
            />
          </div>
          <button
            type="submit"
            className="w-full py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-md font-medium text-xs flex items-center justify-center gap-1 transition-colors shadow-sm"
          >
            <Plus className="w-3.5 h-3.5" /> Create Sprint
          </button>
        </form>

        <div className="space-y-2 max-h-[280px] overflow-y-auto pr-1">
          {sprints.map(sprint => (
            <div key={sprint.id} className="p-3 bg-zinc-950 rounded-md border border-zinc-800 flex items-center justify-between">
              <div>
                <h4 className="font-semibold text-zinc-200 text-xs">{sprint.name}</h4>
                {sprint.goal && <p className="text-[11px] text-zinc-400 font-sans">{sprint.goal}</p>}
              </div>
              <span className={`text-[10px] font-mono px-2 py-0.5 rounded border uppercase ${
                sprint.status === 'ACTIVE' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' : 'bg-zinc-800 text-zinc-400 border-zinc-700'
              }`}>
                {sprint.status}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
