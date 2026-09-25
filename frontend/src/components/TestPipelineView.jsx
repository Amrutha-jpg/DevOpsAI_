import React, { useState, useEffect } from 'react';
import { 
  Play, 
  CheckCircle2, 
  XCircle, 
  Clock, 
  Terminal, 
  FileCode, 
  ShieldCheck, 
  Layers, 
  Cpu, 
  AlertCircle, 
  RefreshCw, 
  Copy, 
  Check, 
  BarChart3,
  ExternalLink,
  Zap,
  Code
} from 'lucide-react';
import { triggerPipeline, getPipelineRunsForProject, getGitHubWorkflowYaml } from '../services/testingService';

export default function TestPipelineView({ project }) {
  const [runs, setRuns] = useState([]);
  const [selectedRun, setSelectedRun] = useState(null);
  const [loading, setLoading] = useState(true);
  const [triggering, setTriggering] = useState(false);
  const [branch, setBranch] = useState('main');
  const [commitSha, setCommitSha] = useState('');
  const [simulateFail, setSimulateFail] = useState(false);
  const [workflowYaml, setWorkflowYaml] = useState('');
  const [showWorkflowModal, setShowWorkflowModal] = useState(false);
  const [copiedYaml, setCopiedYaml] = useState(false);
  const [activeStageFilter, setActiveStageFilter] = useState(null);

  useEffect(() => {
    if (project?.id) {
      fetchPipelineRuns();
    }
  }, [project?.id]);

  // Polling mechanism for async pipeline state transitions (BUILDING -> TESTING -> PASSED/FAILED)
  useEffect(() => {
    if (!project?.id) return;
    const hasActiveRun = runs.some(r => r.status === 'PENDING' || r.status === 'BUILDING' || r.status === 'TESTING');
    if (!hasActiveRun) return;

    const intervalId = setInterval(() => {
      fetchPipelineRuns();
    }, 2000);

    return () => clearInterval(intervalId);
  }, [project?.id, runs]);

  const fetchPipelineRuns = async () => {
    setLoading(true);
    try {
      const data = await getPipelineRunsForProject(project.id);
      setRuns(data);
      if (data.length > 0 && !selectedRun) {
        setSelectedRun(data[0]);
      } else if (data.length > 0) {
        const updatedSelected = data.find(r => r.id === selectedRun.id) || data[0];
        setSelectedRun(updatedSelected);
      }
    } catch (err) {
      console.error('Failed to fetch pipeline runs:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleTrigger = async (e) => {
    e.preventDefault();
    setTriggering(true);
    try {
      const sha = simulateFail ? 'commit-fail-test' : (commitSha || Math.random().toString(16).substring(2, 10));
      const newRun = await triggerPipeline(project.id, branch, sha);
      await fetchPipelineRuns();
      setSelectedRun(newRun);
      setCommitSha('');
    } catch (err) {
      console.error('Failed to trigger pipeline:', err);
    } finally {
      setTriggering(false);
    }
  };

  const handleFetchWorkflow = async () => {
    try {
      const yaml = await getGitHubWorkflowYaml();
      setWorkflowYaml(yaml);
      setShowWorkflowModal(true);
    } catch (err) {
      console.error('Failed to fetch GitHub workflow YAML:', err);
    }
  };

  const copyWorkflowYaml = () => {
    navigator.clipboard.writeText(workflowYaml);
    setCopiedYaml(true);
    setTimeout(() => setCopiedYaml(false), 2000);
  };

  const getStageIcon = (stageName) => {
    switch (stageName) {
      case 'BUILD': return <Cpu className="w-5 h-5 text-indigo-400" />;
      case 'UNIT_TESTS': return <CheckCircle2 className="w-5 h-5 text-emerald-400" />;
      case 'INTEGRATION_TESTS': return <Layers className="w-5 h-5 text-cyan-400" />;
      case 'API_CONTRACT_TESTS': return <FileCode className="w-5 h-5 text-amber-400" />;
      case 'AI_SECURITY_SCAN': return <ShieldCheck className="w-5 h-5 text-purple-400" />;
      default: return <Terminal className="w-5 h-5 text-slate-400" />;
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'PASSED':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-mono font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 uppercase">
            <CheckCircle2 className="w-3 h-3" /> PASSED
          </span>
        );
      case 'FAILED':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-mono font-semibold bg-rose-500/10 text-rose-400 border border-rose-500/20 uppercase">
            <XCircle className="w-3 h-3" /> FAILED
          </span>
        );
      case 'BUILDING':
      case 'TESTING':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-mono font-semibold bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 uppercase">
            <RefreshCw className="w-3 h-3 animate-spin" /> {status}
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-mono font-semibold bg-zinc-800 text-zinc-400 border border-zinc-700 uppercase">
            <Clock className="w-3 h-3" /> PENDING
          </span>
        );
    }
  };

  return (
    <div className="space-y-4">
      {/* Top Banner / Actions */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-5 shadow-sm">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-md bg-zinc-950 border border-zinc-800 text-emerald-400">
                <Zap className="w-5 h-5" />
              </div>
              <div>
                <h2 className="text-base font-semibold text-zinc-100 flex items-center gap-2">
                  Automated CI/CD Pipeline
                  <span className="text-[10px] px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-mono">
                    CI/CD Engine
                  </span>
                </h2>
                <p className="text-xs text-zinc-400 mt-0.5 font-sans">
                  Multi-stage build, unit tests, integration tests, Postman API contracts & AI security scan
                </p>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2.5 font-mono">
            <button
              onClick={handleFetchWorkflow}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-md bg-zinc-800 hover:bg-zinc-700 text-zinc-200 text-xs font-medium border border-zinc-700 transition-colors"
            >
              <Code className="w-3.5 h-3.5 text-cyan-400" />
              Workflow YAML
            </button>
            <button
              onClick={fetchPipelineRuns}
              className="p-1.5 rounded-md bg-zinc-800 hover:bg-zinc-700 text-zinc-300 border border-zinc-700 transition-colors"
              title="Refresh Pipeline Runs"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            </button>
          </div>
        </div>

        {/* Trigger Pipeline Control Form */}
        <form onSubmit={handleTrigger} className="mt-4 pt-4 border-t border-zinc-800 grid grid-cols-1 md:grid-cols-4 gap-3 items-end font-mono">
          <div>
            <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">
              Target Branch
            </label>
            <input
              type="text"
              value={branch}
              onChange={(e) => setBranch(e.target.value)}
              placeholder="main"
              className="w-full bg-zinc-950 border border-zinc-800 rounded-md px-3 py-1.5 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500"
            />
          </div>

          <div>
            <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">
              Commit SHA (Optional)
            </label>
            <input
              type="text"
              value={commitSha}
              onChange={(e) => setCommitSha(e.target.value)}
              placeholder="e.g. a1b2c3d4e5f6"
              className="w-full bg-zinc-950 border border-zinc-800 rounded-md px-3 py-1.5 text-xs text-zinc-100 focus:outline-none focus:border-emerald-500"
            />
          </div>

          <div className="flex items-center gap-2 pb-1.5">
            <input
              type="checkbox"
              id="simulateFail"
              checked={simulateFail}
              onChange={(e) => setSimulateFail(e.target.checked)}
              className="w-3.5 h-3.5 rounded bg-zinc-950 border-zinc-800 text-rose-500 focus:ring-rose-500/20"
            />
            <label htmlFor="simulateFail" className="text-xs text-zinc-300 font-sans cursor-pointer">
              Simulate Failure
            </label>
          </div>

          <div>
            <button
              type="submit"
              disabled={triggering}
              className="w-full inline-flex items-center justify-center gap-1.5 px-3.5 py-1.5 rounded-md bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white font-medium text-xs transition-colors shadow-sm font-sans"
            >
              {triggering ? (
                <>
                  <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                  Running Pipeline...
                </>
              ) : (
                <>
                  <Play className="w-3.5 h-3.5 fill-white" />
                  Run CI Pipeline
                </>
              )}
            </button>
          </div>
        </form>
      </div>

      {/* Main Content: Pipeline Runs & Detailed View */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-4">
        {/* Left Column: Pipeline Runs History */}
        <div className="lg:col-span-4 space-y-3 font-mono">
          <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-3.5">
            <h3 className="text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-2.5 flex items-center justify-between">
              <span>Pipeline Run History</span>
              <span className="text-[10px] px-2 py-0.5 rounded bg-zinc-800 text-zinc-400 border border-zinc-700">
                {runs.length} Runs
              </span>
            </h3>

            {loading && runs.length === 0 ? (
              <div className="py-8 text-center text-zinc-500 text-xs">
                <RefreshCw className="w-4 h-4 animate-spin mx-auto mb-2 text-emerald-400" />
                Loading pipeline runs...
              </div>
            ) : runs.length === 0 ? (
              <div className="py-8 text-center text-zinc-500 text-xs">
                No pipeline runs executed yet. Trigger one above!
              </div>
            ) : (
              <div className="space-y-2 max-h-[600px] overflow-y-auto pr-1">
                {runs.map((run) => {
                  const isSelected = selectedRun?.id === run.id;
                  return (
                    <div
                      key={run.id}
                      onClick={() => setSelectedRun(run)}
                      className={`p-3 rounded-md border cursor-pointer transition-colors ${
                        isSelected
                          ? 'bg-zinc-800 border-emerald-500/80 shadow-sm'
                          : 'bg-zinc-950 border-zinc-800 hover:bg-zinc-900'
                      }`}
                    >
                      <div className="flex items-center justify-between mb-1.5">
                        <span className="text-[11px] font-mono text-emerald-400 bg-emerald-500/10 px-1.5 py-0.5 rounded border border-emerald-500/20">
                          #{run.id} • {run.commitSha ? run.commitSha.substring(0, 7) : 'commit'}
                        </span>
                        {getStatusBadge(run.status)}
                      </div>

                      <div className="flex items-center justify-between text-[11px] text-zinc-400 font-sans">
                        <span className="font-medium text-zinc-300">Branch: {run.branch}</span>
                        <span className="font-mono">{new Date(run.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                      </div>

                      <div className="mt-2 pt-1.5 border-t border-zinc-800 flex items-center justify-between text-[10px] font-mono text-zinc-400">
                        <span>Tests: <strong className="text-emerald-400">{run.passedCount}</strong>/{run.totalTestsCount}</span>
                        <span>Coverage: <strong className="text-cyan-400">{run.coveragePercent}%</strong></span>
                        <span>Duration: <strong>{(run.durationMs / 1000).toFixed(1)}s</strong></span>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Selected Run Stage Pipeline & Log Console */}
        <div className="lg:col-span-8 space-y-4 font-mono">
          {selectedRun ? (
            <>
              {/* Selected Run Metrics Header */}
              <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-4 shadow-sm">
                <div className="flex flex-wrap items-center justify-between gap-3 pb-3 border-b border-zinc-800">
                  <div>
                    <div className="flex items-center gap-2">
                      <h3 className="text-sm font-semibold text-zinc-100">
                        Pipeline Run #{selectedRun.id}
                      </h3>
                      {getStatusBadge(selectedRun.status)}
                    </div>
                    <p className="text-[11px] text-zinc-400 mt-0.5 font-sans">
                      Commit <code className="text-emerald-400 bg-zinc-950 px-1.5 py-0.5 rounded border border-zinc-800 font-mono">{selectedRun.commitSha}</code> on branch <span className="text-zinc-200 font-medium">{selectedRun.branch}</span>
                    </p>
                  </div>

                  <div className="flex items-center gap-4 text-xs font-mono">
                    <div className="text-right">
                      <span className="text-[10px] text-zinc-500 uppercase block">Pass Rate</span>
                      <span className="text-base font-bold text-emerald-400">
                        {selectedRun.totalTestsCount > 0 ? ((selectedRun.passedCount / selectedRun.totalTestsCount) * 100).toFixed(0) : 100}%
                      </span>
                    </div>

                    <div className="text-right">
                      <span className="text-[10px] text-zinc-500 uppercase block">Coverage</span>
                      <span className="text-base font-bold text-cyan-400">
                        {selectedRun.coveragePercent}%
                      </span>
                    </div>

                    <div className="text-right">
                      <span className="text-[10px] text-zinc-500 uppercase block">Duration</span>
                      <span className="text-base font-bold text-zinc-200">
                        {(selectedRun.durationMs / 1000).toFixed(2)}s
                      </span>
                    </div>
                  </div>
                </div>

                {/* 5-Stage Execution Visual Workflow Tree */}
                <div className="mt-4">
                  <h4 className="text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-2">
                    5-Stage CI/CD Execution Tree
                  </h4>

                  <div className="grid grid-cols-1 sm:grid-cols-5 gap-2">
                    {selectedRun.stages?.map((stage, idx) => {
                      const isFilterSelected = activeStageFilter === stage.stageName;
                      return (
                        <div
                          key={idx}
                          onClick={() => setActiveStageFilter(isFilterSelected ? null : stage.stageName)}
                          className={`p-2.5 rounded-md border text-left cursor-pointer transition-colors ${
                            stage.status === 'PASSED'
                              ? isFilterSelected ? 'bg-zinc-800 border-emerald-500 text-emerald-200' : 'bg-zinc-950 border-zinc-800 hover:border-emerald-500/50'
                              : isFilterSelected ? 'bg-zinc-800 border-rose-500 text-rose-200' : 'bg-zinc-950 border-zinc-800 hover:border-rose-500/50'
                          }`}
                        >
                          <div className="flex items-center justify-between mb-1">
                            {getStageIcon(stage.stageName)}
                            <span className="text-[10px] font-mono text-zinc-500">
                              {(stage.durationMs / 1000).toFixed(2)}s
                            </span>
                          </div>
                          <div className="text-xs font-semibold text-zinc-200 truncate font-mono">
                            {stage.stageName.replace('_', ' ')}
                          </div>
                          <div className="text-[10px] mt-1 font-mono flex items-center gap-1">
                            {stage.status === 'PASSED' ? (
                              <span className="text-emerald-400 flex items-center gap-1">
                                <CheckCircle2 className="w-3 h-3" /> Passed
                              </span>
                            ) : (
                              <span className="text-rose-400 flex items-center gap-1">
                                <XCircle className="w-3 h-3" /> Failed
                              </span>
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>

              {/* Stage Terminal Output Logs Console */}
              <div className="bg-zinc-950 border border-zinc-800 rounded-lg overflow-hidden shadow-sm font-mono">
                <div className="bg-zinc-900 px-3.5 py-2.5 border-b border-zinc-800 flex items-center justify-between">
                  <div className="flex items-center gap-2 text-zinc-300 text-xs">
                    <Terminal className="w-4 h-4 text-emerald-400" />
                    <span>Pipeline Execution Terminal Logs</span>
                    {activeStageFilter && (
                      <span className="px-2 py-0.5 rounded bg-zinc-800 text-zinc-200 text-[10px] border border-zinc-700">
                        Filter: {activeStageFilter}
                      </span>
                    )}
                  </div>
                  {activeStageFilter && (
                    <button
                      onClick={() => setActiveStageFilter(null)}
                      className="text-[10px] text-zinc-400 hover:text-zinc-200 underline"
                    >
                      Clear Filter
                    </button>
                  )}
                </div>

                <div className="p-3.5 text-xs text-zinc-300 space-y-2.5 max-h-[400px] overflow-y-auto leading-relaxed">
                  {selectedRun.stages
                    ?.filter(stage => !activeStageFilter || stage.stageName === activeStageFilter)
                    .map((stage, sIdx) => (
                      <div key={sIdx} className="space-y-1 bg-zinc-900/50 p-2.5 rounded-md border border-zinc-800">
                        <div className="flex items-center justify-between text-zinc-200 font-semibold border-b border-zinc-800/80 pb-1 mb-1.5">
                          <span className="flex items-center gap-1.5">
                            {getStageIcon(stage.stageName)}
                            Stage: {stage.stageName}
                          </span>
                          <span className="text-[10px] text-zinc-400">
                            Status: <span className={stage.status === 'PASSED' ? 'text-emerald-400' : 'text-rose-400'}>{stage.status}</span> ({stage.durationMs}ms)
                          </span>
                        </div>
                        {stage.logs?.map((logLine, lIdx) => {
                          const isError = logLine.includes('[ERROR]');
                          const isInfo = logLine.includes('[INFO]');
                          return (
                            <div
                              key={lIdx}
                              className={`pl-2 border-l-2 ${
                                isError
                                  ? 'border-rose-500 text-rose-400 bg-rose-950/20'
                                  : isInfo
                                  ? 'border-zinc-700 text-zinc-300'
                                  : 'border-zinc-800 text-zinc-500'
                              }`}
                            >
                              {logLine}
                            </div>
                          );
                        })}
                      </div>
                    ))}
                </div>
              </div>
            </>
          ) : (
            <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-10 text-center text-zinc-500 text-xs font-mono">
              Select a pipeline run from the left panel to inspect execution logs and test results.
            </div>
          )}
        </div>
      </div>

      {/* GitHub Workflow YAML Modal */}
      {showWorkflowModal && (
        <div className="fixed inset-0 z-50 bg-zinc-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-zinc-900 border border-zinc-800 rounded-lg max-w-3xl w-full p-5 shadow-sm max-h-[85vh] flex flex-col font-mono">
            <div className="flex items-center justify-between pb-3 border-b border-zinc-800 mb-3">
              <div className="flex items-center gap-2.5">
                <div className="p-1.5 rounded bg-zinc-950 text-emerald-400 border border-zinc-800">
                  <Code className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="text-xs font-semibold text-zinc-100">
                    .github/workflows/ci-pipeline.yml
                  </h3>
                  <p className="text-[11px] text-zinc-400 font-sans">
                    GitHub Actions workflow configuration for DevOpsAI platform
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <button
                  onClick={copyWorkflowYaml}
                  className="inline-flex items-center gap-1.5 px-3 py-1 rounded-md bg-zinc-800 hover:bg-zinc-700 text-zinc-200 text-xs font-medium border border-zinc-700 transition-colors"
                >
                  {copiedYaml ? (
                    <>
                      <Check className="w-3.5 h-3.5 text-emerald-400" /> Copied!
                    </>
                  ) : (
                    <>
                      <Copy className="w-3.5 h-3.5" /> Copy YAML
                    </>
                  )}
                </button>
                <button
                  onClick={() => setShowWorkflowModal(false)}
                  className="p-1 rounded text-zinc-400 hover:text-zinc-200 hover:bg-zinc-800"
                >
                  <XCircle className="w-4 h-4" />
                </button>
              </div>
            </div>

            <div className="flex-1 overflow-y-auto bg-zinc-950 border border-zinc-800 rounded-md p-3.5 font-mono text-xs text-zinc-300 leading-relaxed">
              <pre>{workflowYaml}</pre>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
