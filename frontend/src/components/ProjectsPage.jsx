import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import { KanbanBoard } from './KanbanBoard';
import { PriorityBacklog } from './PriorityBacklog';
import { SprintEpicManager } from './SprintEpicManager';
import { GithubIntegrationView } from './GithubIntegrationView';
import { AiCodeReviewView } from './AiCodeReviewView';
import { CodebaseAssistantView } from './CodebaseAssistantView';
import { AgentHubView } from './AgentHubView';
import TestPipelineView from './TestPipelineView';
import SecurityScannerView from './SecurityScannerView';
import CloudDeploymentView from './CloudDeploymentView';
import { IssueModal } from './IssueModal';
import { 
  FolderGit2, Plus, Edit2, ShieldAlert, Check, X, 
  Shield, Lock, Layers, Flame, Calendar, Kanban, Github, Bot, Sparkles, Cpu, Zap, Cloud, Trash2, Loader2
} from 'lucide-react';

export const ProjectsPage = () => {
  const { user, hasRole } = useAuth();
  const [projects, setProjects] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState(null);
  const [activeTab, setActiveTab] = useState('kanban'); // 'kanban', 'backlog', 'sprints', 'github', 'ai-review', 'projects'

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Modal states
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showIssueModal, setShowIssueModal] = useState(false);
  const [selectedProject, setSelectedProject] = useState(null);

  // Form states for Project
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [projectKey, setProjectKey] = useState('');

  // Delete modal states
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [projectToDelete, setProjectToDelete] = useState(null);
  const [deleteConfirmName, setDeleteConfirmName] = useState('');
  const [isDeleting, setIsDeleting] = useState(false);

  const canCreate = hasRole('ADMIN', 'PROJECT_MANAGER');
  const isAdmin = user?.role === 'ADMIN';

  const fetchProjects = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await apiClient.get('/projects');
      setProjects(response.data);
      if (response.data.length > 0 && !selectedProjectId) {
        setSelectedProjectId(response.data[0].id);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load projects');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProjects();
  }, []);

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const response = await apiClient.post('/projects', { name, description, projectKey });
      setSuccess(`Project '${name}' successfully created!`);
      setShowCreateModal(false);
      setName('');
      setDescription('');
      setProjectKey('');
      fetchProjects();
      setSelectedProjectId(response.data.id);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Failed to create project');
    }
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    if (!selectedProject) return;
    setError('');
    setSuccess('');
    try {
      await apiClient.put(`/projects/${selectedProject.id}`, { name, description, projectKey });
      setSuccess(`Project '${name}' successfully updated!`);
      setShowEditModal(false);
      setSelectedProject(null);
      fetchProjects();
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Failed to update project');
    }
  };

  const openEditModal = (project) => {
    setSelectedProject(project);
    setName(project.name);
    setDescription(project.description || '');
    setProjectKey(project.projectKey);
    setShowEditModal(true);
  };

  const openDeleteModal = (project) => {
    setProjectToDelete(project);
    setDeleteConfirmName('');
    setShowDeleteModal(true);
  };

  const handleDeleteSubmit = async (e) => {
    e.preventDefault();
    if (!projectToDelete || deleteConfirmName.trim() !== projectToDelete.name) return;
    setError('');
    setSuccess('');
    setIsDeleting(true);
    try {
      await apiClient.delete(`/projects/${projectToDelete.id}`);
      setSuccess(`Project '${projectToDelete.name}' was successfully deleted.`);
      const remainingProjects = projects.filter(p => p.id !== projectToDelete.id);
      setProjects(remainingProjects);
      if (selectedProjectId === projectToDelete.id) {
        setSelectedProjectId(remainingProjects.length > 0 ? remainingProjects[0].id : null);
      }
      setShowDeleteModal(false);
      setProjectToDelete(null);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Failed to delete project');
    } finally {
      setIsDeleting(false);
    }
  };

  const currentProject = projects.find(p => p.id === selectedProjectId);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 space-y-5">
      
      {/* Top Header & Project Selector */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b border-zinc-800">
        <div>
          <h1 className="text-xl font-semibold text-zinc-100 flex items-center space-x-2 tracking-tight">
            <FolderGit2 className="h-5 w-5 text-emerald-400" />
            <span>DevOpsAI Project Workspace</span>
          </h1>
          <div className="text-xs text-zinc-400 mt-1 flex items-center space-x-2">
            <span>Role Access:</span>
            <span className="font-mono text-[10px] bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 px-1.5 py-0.5 rounded uppercase">{user?.role}</span>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          {/* Active Project Dropdown */}
          {projects.length > 0 && (
            <div className="flex items-center gap-2 bg-zinc-900 border border-zinc-800 rounded-md px-2.5 py-1.5">
              <span className="text-xs text-zinc-400 font-mono">Active Project:</span>
              <select
                value={selectedProjectId || ''}
                onChange={(e) => setSelectedProjectId(parseInt(e.target.value))}
                className="bg-transparent text-xs font-semibold text-emerald-400 focus:outline-none cursor-pointer font-mono"
              >
                {projects.map(p => (
                  <option key={p.id} value={p.id} className="bg-zinc-900 text-zinc-100">
                    [{p.projectKey}] {p.name}
                  </option>
                ))}
              </select>
            </div>
          )}

          <button
            onClick={() => {
              setName('');
              setDescription('');
              setProjectKey('');
              setShowCreateModal(true);
            }}
            disabled={!canCreate}
            className={`px-3 py-1.5 rounded-md font-medium text-xs flex items-center space-x-1.5 transition-colors ${
              canCreate
                ? 'bg-emerald-600 hover:bg-emerald-500 text-white shadow-sm'
                : 'bg-zinc-800 text-zinc-500 cursor-not-allowed border border-zinc-700'
            }`}
          >
            {canCreate ? <Plus className="h-3.5 w-3.5" /> : <Lock className="h-3.5 w-3.5" />}
            <span>{canCreate ? 'New Project' : 'New Project (Restricted)'}</span>
          </button>
        </div>
      </div>

      {/* Notifications */}
      {error && (
        <div className="p-3 rounded-md bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-mono flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <ShieldAlert className="h-4 w-4 text-rose-400 flex-shrink-0" />
            <span>{error}</span>
          </div>
          <button onClick={() => setError('')}><X className="h-4 w-4 text-zinc-400 hover:text-zinc-100" /></button>
        </div>
      )}

      {success && (
        <div className="p-3 rounded-md bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-mono flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Check className="h-4 w-4 text-emerald-400 flex-shrink-0" />
            <span>{success}</span>
          </div>
          <button onClick={() => setSuccess('')}><X className="h-4 w-4 text-zinc-400 hover:text-zinc-100" /></button>
        </div>
      )}

      {/* Navigation Tabs */}
      <div className="flex flex-wrap items-center gap-1.5 border-b border-zinc-800/80 pb-2.5">
        <button
          onClick={() => setActiveTab('kanban')}
          className={`px-3 py-1.5 rounded-md text-xs font-medium flex items-center gap-1.5 transition-colors ${
            activeTab === 'kanban'
              ? 'bg-zinc-800 text-zinc-100 border border-zinc-700/70'
              : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-900'
          }`}
        >
          <Kanban className="w-3.5 h-3.5 text-emerald-400" /> Kanban Board
        </button>

        <button
          onClick={() => setActiveTab('backlog')}
          className={`px-3 py-1.5 rounded-md text-xs font-medium flex items-center gap-1.5 transition-colors ${
            activeTab === 'backlog'
              ? 'bg-zinc-800 text-zinc-100 border border-zinc-700/70'
              : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-900'
          }`}
        >
          <Flame className="w-3.5 h-3.5 text-amber-400" /> Max-Heap Backlog
        </button>

        <button
          onClick={() => setActiveTab('sprints')}
          className={`px-3 py-1.5 rounded-md text-xs font-medium flex items-center gap-1.5 transition-colors ${
            activeTab === 'sprints'
              ? 'bg-zinc-800 text-zinc-100 border border-zinc-700/70'
              : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-900'
          }`}
        >
          <Layers className="w-3.5 h-3.5 text-cyan-400" /> Epics & Sprints
        </button>

        <button
          onClick={() => setActiveTab('github')}
          className={`px-3 py-1.5 rounded-md text-xs font-medium flex items-center gap-1.5 transition-colors ${
            activeTab === 'github'
              ? 'bg-zinc-800 text-zinc-100 border border-zinc-700/70'
              : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-900'
          }`}
        >
          <Github className="w-3.5 h-3.5 text-zinc-300" /> GitHub Automation
        </button>

        <button
          onClick={() => setActiveTab('ai-review')}
          className={`px-3 py-1.5 rounded-md text-xs font-medium flex items-center gap-1.5 transition-colors ${
            activeTab === 'ai-review'
              ? 'bg-zinc-800 text-zinc-100 border border-zinc-700/70'
              : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-900'
          }`}
        >
          <Bot className="w-3.5 h-3.5 text-emerald-400" /> AI Code Reviewer
        </button>

        <button
          onClick={() => setActiveTab('rag-assistant')}
          className={`px-3 py-1.5 rounded-md text-xs font-medium flex items-center gap-1.5 transition-colors ${
            activeTab === 'rag-assistant'
              ? 'bg-zinc-800 text-zinc-100 border border-zinc-700/70'
              : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-900'
          }`}
        >
          <Sparkles className="w-3.5 h-3.5 text-emerald-400" /> Codebase AI (RAG)
        </button>

        <button
          onClick={() => setActiveTab('agents-hub')}
          className={`px-3 py-1.5 rounded-md text-xs font-medium flex items-center gap-1.5 transition-colors ${
            activeTab === 'agents-hub'
              ? 'bg-zinc-800 text-zinc-100 border border-zinc-700/70'
              : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-900'
          }`}
        >
          <Cpu className="w-3.5 h-3.5 text-cyan-400" /> AI Agents Hub
        </button>

        <button
          onClick={() => setActiveTab('testing-pipeline')}
          className={`px-3 py-1.5 rounded-md text-xs font-medium flex items-center gap-1.5 transition-colors ${
            activeTab === 'testing-pipeline'
              ? 'bg-zinc-800 text-zinc-100 border border-zinc-700/70'
              : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-900'
          }`}
        >
          <Zap className="w-3.5 h-3.5 text-emerald-400" /> Automated CI/CD
        </button>

        <button
          onClick={() => setActiveTab('security-scanner')}
          className={`px-3 py-1.5 rounded-md text-xs font-medium flex items-center gap-1.5 transition-colors ${
            activeTab === 'security-scanner'
              ? 'bg-zinc-800 text-zinc-100 border border-zinc-700/70'
              : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-900'
          }`}
        >
          <Shield className="w-3.5 h-3.5 text-amber-400" /> Security Scanner
        </button>

        <button
          onClick={() => setActiveTab('cloud-deployment')}
          className={`px-3 py-1.5 rounded-md text-xs font-medium flex items-center gap-1.5 transition-colors ${
            activeTab === 'cloud-deployment'
              ? 'bg-zinc-800 text-zinc-100 border border-zinc-700/70'
              : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-900'
          }`}
        >
          <Cloud className="w-3.5 h-3.5 text-cyan-400" /> Cloud Deployment
        </button>

        <button
          onClick={() => setActiveTab('projects')}
          className={`px-3 py-1.5 rounded-md text-xs font-medium flex items-center gap-1.5 transition-colors ${
            activeTab === 'projects'
              ? 'bg-zinc-800 text-zinc-100 border border-zinc-700/70'
              : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-900'
          }`}
        >
          <FolderGit2 className="w-3.5 h-3.5" /> Projects Overview ({projects.length})
        </button>
      </div>

      {/* Main Tab Content */}
      {selectedProjectId && activeTab === 'kanban' && (
        <KanbanBoard
          projectId={selectedProjectId}
          onOpenCreateIssue={() => setShowIssueModal(true)}
        />
      )}

      {selectedProjectId && activeTab === 'backlog' && (
        <PriorityBacklog projectId={selectedProjectId} />
      )}

      {selectedProjectId && activeTab === 'sprints' && (
        <SprintEpicManager projectId={selectedProjectId} />
      )}

      {selectedProjectId && activeTab === 'github' && (
        <GithubIntegrationView projectId={selectedProjectId} />
      )}

      {selectedProjectId && activeTab === 'ai-review' && (
        <AiCodeReviewView projectId={selectedProjectId} />
      )}

      {selectedProjectId && activeTab === 'rag-assistant' && (
        <CodebaseAssistantView project={currentProject} />
      )}

      {selectedProjectId && activeTab === 'agents-hub' && (
        <AgentHubView projectId={selectedProjectId} />
      )}

      {selectedProjectId && activeTab === 'testing-pipeline' && (
        <TestPipelineView project={currentProject} />
      )}

      {selectedProjectId && activeTab === 'security-scanner' && (
        <SecurityScannerView project={currentProject} />
      )}

      {selectedProjectId && activeTab === 'cloud-deployment' && (
        <CloudDeploymentView project={currentProject} />
      )}

      {activeTab === 'projects' && (
        <div className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {projects.map((project) => (
              <div
                key={project.id}
                onClick={() => {
                  setSelectedProjectId(project.id);
                  setActiveTab('kanban');
                }}
                className={`bg-zinc-900 border rounded-lg p-4 transition-colors cursor-pointer flex flex-col justify-between ${
                  selectedProjectId === project.id
                    ? 'border-emerald-500/80 shadow-sm'
                    : 'border-zinc-800 hover:border-zinc-700'
                }`}
              >
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <span className="px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-[10px] font-mono font-semibold uppercase">
                      {project.projectKey}
                    </span>

                    <div className="flex items-center space-x-1">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          openEditModal(project);
                        }}
                        className="p-1 text-zinc-400 hover:text-zinc-100 hover:bg-zinc-800 rounded transition-colors"
                        title="Edit Project"
                      >
                        <Edit2 className="h-3.5 w-3.5" />
                      </button>
                      {isAdmin && (
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            openDeleteModal(project);
                          }}
                          className="p-1 text-rose-400 hover:text-rose-300 hover:bg-rose-500/10 rounded transition-colors"
                          title="Delete Project (Admin Only)"
                        >
                          <Trash2 className="h-3.5 w-3.5" />
                        </button>
                      )}
                    </div>
                  </div>

                  <h3 className="text-sm font-semibold text-zinc-100 mb-1">{project.name}</h3>
                  <p className="text-zinc-400 text-xs line-clamp-2 mb-3 font-sans">
                    {project.description || 'No description provided.'}
                  </p>
                </div>

                <div className="pt-3 border-t border-zinc-800/80 flex items-center justify-between text-[11px] text-zinc-500 font-mono">
                  <span>Owner: {project.owner?.username || 'System'}</span>
                  <span className="text-emerald-400 font-medium hover:underline">Select Board →</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Modal dialog for creating issues */}
      <IssueModal
        isOpen={showIssueModal}
        onClose={() => setShowIssueModal(false)}
        projectId={selectedProjectId}
        onIssueCreated={() => {
          setActiveTab('kanban');
        }}
      />

      {/* Create Project Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-zinc-950/80 backdrop-blur-sm">
          <div className="w-full max-w-lg bg-zinc-900 border border-zinc-800 rounded-lg p-5 shadow-sm">
            <div className="flex items-center justify-between pb-3 border-b border-zinc-800">
              <h2 className="text-sm font-semibold text-zinc-100">Create New Project</h2>
              <button onClick={() => setShowCreateModal(false)}><X className="h-4 w-4 text-zinc-400 hover:text-zinc-100" /></button>
            </div>

            <form onSubmit={handleCreateSubmit} className="space-y-3.5 mt-3.5">
              <div>
                <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">Project Name</label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="SmartHealth Engine"
                  className="w-full bg-zinc-950 border border-zinc-800 rounded-md py-1.5 px-3 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">Project Key (UPPERCASE)</label>
                <input
                  type="text"
                  required
                  value={projectKey}
                  onChange={(e) => setProjectKey(e.target.value.toUpperCase())}
                  placeholder="SMART"
                  className="w-full bg-zinc-950 border border-zinc-800 rounded-md py-1.5 px-3 text-xs text-zinc-100 font-mono uppercase focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">Description</label>
                <textarea
                  rows={3}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Detailed project description..."
                  className="w-full bg-zinc-950 border border-zinc-800 rounded-md py-1.5 px-3 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="flex justify-end space-x-2 pt-3">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-3 py-1.5 rounded-md bg-zinc-800 hover:bg-zinc-700 text-zinc-300 text-xs font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-3.5 py-1.5 rounded-md bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-medium shadow-sm"
                >
                  Save Project
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit Project Modal */}
      {showEditModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-zinc-950/80 backdrop-blur-sm">
          <div className="w-full max-w-lg bg-zinc-900 border border-zinc-800 rounded-lg p-5 shadow-sm">
            <div className="flex items-center justify-between pb-3 border-b border-zinc-800">
              <h2 className="text-sm font-semibold text-zinc-100 font-mono">Edit Project #{selectedProject?.id}</h2>
              <button onClick={() => setShowEditModal(false)}><X className="h-4 w-4 text-zinc-400 hover:text-zinc-100" /></button>
            </div>

            <form onSubmit={handleEditSubmit} className="space-y-3.5 mt-3.5">
              <div>
                <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">Project Name</label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="w-full bg-zinc-950 border border-zinc-800 rounded-md py-1.5 px-3 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">Project Key</label>
                <input
                  type="text"
                  required
                  value={projectKey}
                  onChange={(e) => setProjectKey(e.target.value.toUpperCase())}
                  className="w-full bg-zinc-950 border border-zinc-800 rounded-md py-1.5 px-3 text-xs text-zinc-100 font-mono uppercase focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">Description</label>
                <textarea
                  rows={3}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full bg-zinc-950 border border-zinc-800 rounded-md py-1.5 px-3 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="flex justify-end space-x-2 pt-3">
                <button
                  type="button"
                  onClick={() => setShowEditModal(false)}
                  className="px-3 py-1.5 rounded-md bg-zinc-800 hover:bg-zinc-700 text-zinc-300 text-xs font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-3.5 py-1.5 rounded-md bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-medium shadow-sm"
                >
                  Update Project
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Safeguard Modal */}
      {showDeleteModal && projectToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-zinc-950/80 backdrop-blur-sm">
          <div className="w-full max-w-lg bg-zinc-900 border border-rose-900/40 rounded-lg p-5 shadow-lg">
            <div className="flex items-center justify-between pb-3 border-b border-zinc-800">
              <div className="flex items-center space-x-2 text-rose-400">
                <ShieldAlert className="h-5 w-5" />
                <h2 className="text-sm font-semibold text-zinc-100 font-mono">Delete Project: {projectToDelete.name}</h2>
              </div>
              <button
                onClick={() => !isDeleting && setShowDeleteModal(false)}
                disabled={isDeleting}
                className="text-zinc-400 hover:text-zinc-100 disabled:opacity-50"
              >
                <X className="h-4 w-4" />
              </button>
            </div>

            <form onSubmit={handleDeleteSubmit} className="space-y-4 mt-4">
              <div className="p-3 bg-rose-500/10 border border-rose-500/20 rounded-md text-xs text-rose-300 space-y-1 font-sans">
                <p className="font-semibold">This action cannot be undone.</p>
                <p>
                  This will permanently delete the project <span className="font-bold text-zinc-100">[{projectToDelete.projectKey}] {projectToDelete.name}</span> and all associated records (issues, backlog rankings, AI reviews, pipeline runs, security scan reports, and code embeddings).
                </p>
              </div>

              <div>
                <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1.5">
                  To confirm, type <span className="font-bold text-zinc-200 select-all">{projectToDelete.name}</span> below:
                </label>
                <input
                  type="text"
                  required
                  disabled={isDeleting}
                  value={deleteConfirmName}
                  onChange={(e) => setDeleteConfirmName(e.target.value)}
                  placeholder={projectToDelete.name}
                  className="w-full bg-zinc-950 border border-zinc-800 focus:border-rose-500 rounded-md py-1.5 px-3 text-xs text-zinc-100 focus:outline-none font-sans"
                />
              </div>

              <div className="flex justify-end space-x-2 pt-2">
                <button
                  type="button"
                  disabled={isDeleting}
                  onClick={() => setShowDeleteModal(false)}
                  className="px-3 py-1.5 rounded-md bg-zinc-800 hover:bg-zinc-700 text-zinc-300 text-xs font-medium disabled:opacity-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isDeleting || deleteConfirmName.trim() !== projectToDelete.name}
                  className="px-3.5 py-1.5 rounded-md bg-rose-600 hover:bg-rose-500 disabled:bg-zinc-800 disabled:text-zinc-600 disabled:border disabled:border-zinc-700/50 text-white text-xs font-medium shadow-sm flex items-center space-x-1.5 transition-colors"
                >
                  {isDeleting ? (
                    <>
                      <Loader2 className="h-3.5 w-3.5 animate-spin" />
                      <span>Deleting Project...</span>
                    </>
                  ) : (
                    <>
                      <Trash2 className="h-3.5 w-3.5" />
                      <span>Permanently Delete Project</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
};
