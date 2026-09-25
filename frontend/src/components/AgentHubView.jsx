import React, { useState, useEffect } from 'react';
import {
  Bot,
  ShieldCheck,
  TestTube,
  FileText,
  Bug,
  Play,
  CheckCircle2,
  XCircle,
  Clock,
  ChevronRight,
  Cpu,
  Code2,
  Terminal,
  ShieldAlert,
  Sparkles,
  FileCode2,
  Check,
  X,
  AlertTriangle
} from 'lucide-react';
import {
  executeAgentTask,
  getTaskLogsForProject,
  approveTask,
  rejectTask
} from '../services/agentService';

export const AgentHubView = ({ projectId }) => {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [executingAgent, setExecutingAgent] = useState(null);
  const [selectedTask, setSelectedTask] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [feedbackText, setFeedbackText] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  // Agent form inputs
  const [debuggingInput, setDebuggingInput] = useState(
    'java.lang.NoSuchElementException: No value present at com.devopsai.backend.service.PatientService.getPatient(PatientService.java:35)'
  );
  const [testTargetClass, setTestTargetClass] = useState('PriorityCalculator');
  const [docControllerName, setDocControllerName] = useState('IssueController');

  useEffect(() => {
    if (projectId) {
      fetchTasks();
    }
  }, [projectId]);

  const fetchTasks = async () => {
    setLoading(true);
    try {
      const data = await getTaskLogsForProject(projectId);
      setTasks(data);
    } catch (err) {
      console.error("Failed to fetch agent tasks:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleLaunchAgent = async (agentType, inputPayload = '', targetId = null) => {
    setExecutingAgent(agentType);
    try {
      const newTask = await executeAgentTask(projectId, agentType, targetId, inputPayload);
      setTasks((prev) => [newTask, ...prev]);
      setSelectedTask(newTask);
      setModalOpen(true);
    } catch (err) {
      console.error("Failed to execute agent task:", err);
    } finally {
      setExecutingAgent(null);
    }
  };

  const handleApprove = async () => {
    if (!selectedTask) return;
    setActionLoading(true);
    try {
      const updated = await approveTask(selectedTask.taskId, feedbackText);
      setTasks((prev) => prev.map((t) => (t.taskId === updated.taskId ? updated : t)));
      setSelectedTask(updated);
      setFeedbackText('');
    } catch (err) {
      console.error("Failed to approve task:", err);
    } finally {
      setActionLoading(false);
    }
  };

  const handleReject = async () => {
    if (!selectedTask) return;
    setActionLoading(true);
    try {
      const updated = await rejectTask(selectedTask.taskId, feedbackText);
      setTasks((prev) => prev.map((t) => (t.taskId === updated.taskId ? updated : t)));
      setSelectedTask(updated);
      setFeedbackText('');
    } catch (err) {
      console.error("Failed to reject task:", err);
    } finally {
      setActionLoading(false);
    }
  };

  const parseOutputResult = (jsonStr) => {
    if (!jsonStr) return {};
    try {
      return JSON.parse(jsonStr);
    } catch (err) {
      return { raw: jsonStr };
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'APPROVED':
        return <span className="px-2 py-0.5 text-[10px] font-mono font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded flex items-center gap-1"><CheckCircle2 className="w-3 h-3" /> Approved</span>;
      case 'REJECTED':
        return <span className="px-2 py-0.5 text-[10px] font-mono font-medium bg-rose-500/10 text-rose-400 border border-rose-500/20 rounded flex items-center gap-1"><XCircle className="w-3 h-3" /> Rejected</span>;
      case 'REQUIRES_HUMAN_APPROVAL':
        return <span className="px-2 py-0.5 text-[10px] font-mono font-medium bg-amber-500/10 text-amber-400 border border-amber-500/20 rounded flex items-center gap-1"><AlertTriangle className="w-3 h-3" /> Oversight Required</span>;
      case 'RUNNING':
        return <span className="px-2 py-0.5 text-[10px] font-mono font-medium bg-zinc-800 text-zinc-300 border border-zinc-700 rounded flex items-center gap-1"><Bot className="w-3 h-3 animate-spin text-emerald-400" /> Running...</span>;
      default:
        return <span className="px-2 py-0.5 text-[10px] font-mono font-medium bg-zinc-800 text-zinc-300 rounded">{status}</span>;
    }
  };

  return (
    <div className="space-y-6 text-zinc-100">

      {/* Header Banner */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-5 flex flex-col lg:flex-row items-start lg:items-center justify-between gap-4 shadow-sm">
        <div className="flex items-center space-x-3.5">
          <div className="p-2.5 bg-emerald-500/10 border border-emerald-500/20 rounded-md text-emerald-400">
            <Cpu className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-base font-bold text-zinc-100 flex items-center gap-2">
              Specialized AI Autonomous Agents
              <span className="px-2 py-0.5 text-[10px] font-mono font-medium bg-zinc-800 text-zinc-300 border border-zinc-700 rounded">
                Human Oversight Guards Active
              </span>
            </h2>
            <p className="text-xs text-zinc-400 mt-0.5">
              Autonomous tool-using agents for PR code reviews, unit test generation, API documentation, and stack trace debugging.
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-3 text-xs text-zinc-400 font-mono bg-zinc-950 px-3 py-1.5 rounded-md border border-zinc-800">
          <span>Active Agents: <strong className="text-emerald-400">4</strong></span>
          <span className="text-zinc-700">|</span>
          <span>Logged Tasks: <strong className="text-emerald-400">{tasks.length}</strong></span>
        </div>
      </div>

      {/* Agent Launchpad Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">

        {/* Agent 1: Code Review Agent */}
        <div className="bg-zinc-900 border border-zinc-800 hover:border-zinc-700 rounded-lg p-4 transition-colors flex flex-col justify-between shadow-sm">
          <div>
            <div className="flex items-center justify-between mb-2.5">
              <div className="p-2 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 rounded-md">
                <ShieldCheck className="w-4 h-4" />
              </div>
              <span className="text-[10px] font-mono px-2 py-0.5 bg-zinc-950 text-zinc-400 border border-zinc-800 rounded">Agent 1</span>
            </div>
            <h3 className="text-xs font-bold text-zinc-100 mb-1">Code Review Agent</h3>
            <p className="text-xs text-zinc-400 mb-4 leading-relaxed">
              Scans PR diffs, executes security rules, detects memory leaks and anti-patterns, and suggests fixes.
            </p>
          </div>

          <button
            onClick={() => handleLaunchAgent('CODE_REVIEW_AGENT', '', 100)}
            disabled={executingAgent === 'CODE_REVIEW_AGENT'}
            className="w-full py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-md text-xs font-medium flex items-center justify-center space-x-1.5 transition-colors disabled:opacity-50"
          >
            <Play className={`w-3.5 h-3.5 fill-white ${executingAgent === 'CODE_REVIEW_AGENT' ? 'animate-spin' : ''}`} />
            <span>{executingAgent === 'CODE_REVIEW_AGENT' ? 'Running Agent...' : 'Review PR #42'}</span>
          </button>
        </div>

        {/* Agent 2: Test Agent */}
        <div className="bg-zinc-900 border border-zinc-800 hover:border-zinc-700 rounded-lg p-4 transition-colors flex flex-col justify-between shadow-sm">
          <div>
            <div className="flex items-center justify-between mb-2.5">
              <div className="p-2 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 rounded-md">
                <TestTube className="w-4 h-4" />
              </div>
              <span className="text-[10px] font-mono px-2 py-0.5 bg-zinc-950 text-zinc-400 border border-zinc-800 rounded">Agent 2</span>
            </div>
            <h3 className="text-xs font-bold text-zinc-100 mb-1">Test Generation Agent</h3>
            <p className="text-xs text-zinc-400 mb-3 leading-relaxed">
              Reads target source code and synthesizes complete JUnit 5 & Mockito test suites.
            </p>

            <input
              type="text"
              value={testTargetClass}
              onChange={(e) => setTestTargetClass(e.target.value)}
              placeholder="Target Class (e.g. PriorityCalculator)"
              className="w-full bg-zinc-950 border border-zinc-800 rounded-md px-2.5 py-1 text-xs font-mono text-zinc-200 mb-3 focus:outline-none focus:border-zinc-700"
            />
          </div>

          <button
            onClick={() => handleLaunchAgent('TEST_AGENT', testTargetClass)}
            disabled={executingAgent === 'TEST_AGENT'}
            className="w-full py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-md text-xs font-medium flex items-center justify-center space-x-1.5 transition-colors disabled:opacity-50"
          >
            <Play className={`w-3.5 h-3.5 fill-white ${executingAgent === 'TEST_AGENT' ? 'animate-spin' : ''}`} />
            <span>{executingAgent === 'TEST_AGENT' ? 'Running Agent...' : 'Generate Tests'}</span>
          </button>
        </div>

        {/* Agent 3: Documentation Agent */}
        <div className="bg-zinc-900 border border-zinc-800 hover:border-zinc-700 rounded-lg p-4 transition-colors flex flex-col justify-between shadow-sm">
          <div>
            <div className="flex items-center justify-between mb-2.5">
              <div className="p-2 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 rounded-md">
                <FileText className="w-4 h-4" />
              </div>
              <span className="text-[10px] font-mono px-2 py-0.5 bg-zinc-950 text-zinc-400 border border-zinc-800 rounded">Agent 3</span>
            </div>
            <h3 className="text-xs font-bold text-zinc-100 mb-1">Documentation Agent</h3>
            <p className="text-xs text-zinc-400 mb-3 leading-relaxed">
              Analyzes REST controllers and auto-generates OpenAPI Markdown specs.
            </p>

            <input
              type="text"
              value={docControllerName}
              onChange={(e) => setDocControllerName(e.target.value)}
              placeholder="Controller Name (e.g. IssueController)"
              className="w-full bg-zinc-950 border border-zinc-800 rounded-md px-2.5 py-1 text-xs font-mono text-zinc-200 mb-3 focus:outline-none focus:border-zinc-700"
            />
          </div>

          <button
            onClick={() => handleLaunchAgent('DOCUMENTATION_AGENT', docControllerName)}
            disabled={executingAgent === 'DOCUMENTATION_AGENT'}
            className="w-full py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-md text-xs font-medium flex items-center justify-center space-x-1.5 transition-colors disabled:opacity-50"
          >
            <Play className={`w-3.5 h-3.5 fill-white ${executingAgent === 'DOCUMENTATION_AGENT' ? 'animate-spin' : ''}`} />
            <span>{executingAgent === 'DOCUMENTATION_AGENT' ? 'Running Agent...' : 'Generate Docs'}</span>
          </button>
        </div>

        {/* Agent 4: Debugging Agent */}
        <div className="bg-zinc-900 border border-zinc-800 hover:border-zinc-700 rounded-lg p-4 transition-colors flex flex-col justify-between shadow-sm">
          <div>
            <div className="flex items-center justify-between mb-2.5">
              <div className="p-2 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 rounded-md">
                <Bug className="w-4 h-4" />
              </div>
              <span className="text-[10px] font-mono px-2 py-0.5 bg-zinc-950 text-zinc-400 border border-zinc-800 rounded">Agent 4</span>
            </div>
            <h3 className="text-xs font-bold text-zinc-100 mb-1">Debugging Agent</h3>
            <p className="text-xs text-zinc-400 mb-3 leading-relaxed">
              Parses stack traces, pinpoints root cause line numbers, and generates code patch diffs.
            </p>

            <input
              type="text"
              value={debuggingInput}
              onChange={(e) => setDebuggingInput(e.target.value)}
              placeholder="Paste Java Stack Trace"
              className="w-full bg-zinc-950 border border-zinc-800 rounded-md px-2.5 py-1 text-xs font-mono text-zinc-200 mb-3 focus:outline-none focus:border-zinc-700 truncate"
            />
          </div>

          <button
            onClick={() => handleLaunchAgent('DEBUGGING_AGENT', debuggingInput)}
            disabled={executingAgent === 'DEBUGGING_AGENT'}
            className="w-full py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-md text-xs font-medium flex items-center justify-center space-x-1.5 transition-colors disabled:opacity-50"
          >
            <Play className={`w-3.5 h-3.5 fill-white ${executingAgent === 'DEBUGGING_AGENT' ? 'animate-spin' : ''}`} />
            <span>{executingAgent === 'DEBUGGING_AGENT' ? 'Running Agent...' : 'Debug Stack Trace'}</span>
          </button>
        </div>

      </div>

      {/* Execution Logs & Human Oversight Feed */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-5 space-y-4 shadow-sm">
        <div className="flex items-center justify-between pb-3 border-b border-zinc-800">
          <h3 className="text-xs font-semibold text-zinc-400 uppercase tracking-wider flex items-center gap-2">
            <Terminal className="w-3.5 h-3.5 text-emerald-400" />
            Agent Task Logs & Execution Feed
          </h3>
          <span className="text-xs text-zinc-400 font-mono">Showing recent agent runs</span>
        </div>

        {tasks.length === 0 ? (
          <div className="py-10 text-center text-zinc-500 text-xs font-mono">
            No agent tasks executed yet. Launch any agent above to trigger tool workflows.
          </div>
        ) : (
          <div className="space-y-2.5">
            {tasks.map((task) => {
              const outputData = parseOutputResult(task.outputResult);
              return (
                <div
                  key={task.taskId}
                  onClick={() => {
                    setSelectedTask(task);
                    setModalOpen(true);
                  }}
                  className="bg-zinc-950 border border-zinc-800 hover:border-zinc-700 rounded-md p-3.5 flex flex-col md:flex-row md:items-center justify-between gap-3 cursor-pointer transition-colors"
                >
                  <div className="flex items-start space-x-3">
                    <div className="p-1.5 bg-zinc-800 rounded text-emerald-400 shrink-0 border border-zinc-700">
                      <Bot className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="flex items-center space-x-2">
                        <span className="font-bold text-zinc-200 text-xs font-mono">Task #{task.taskId}</span>
                        <span className="text-[10px] font-mono px-2 py-0.5 bg-zinc-900 text-zinc-300 rounded border border-zinc-800">{task.agentType}</span>
                        {getStatusBadge(task.status)}
                      </div>
                      <p className="text-xs text-zinc-400 mt-0.5 line-clamp-1">
                        Input: {task.inputPayload || 'Default target parameters'}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center space-x-3 shrink-0">
                    <span className="text-[11px] text-zinc-500 font-mono">
                      {new Date(task.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                    <button className="px-2.5 py-1 bg-zinc-900 hover:bg-zinc-800 text-zinc-200 border border-zinc-800 rounded text-xs font-medium flex items-center space-x-1 transition-colors">
                      <span>View Output & Oversight</span>
                      <ChevronRight className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Human Oversight & Output Details Modal */}
      {modalOpen && selectedTask && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-zinc-950/80 backdrop-blur-sm">
          <div className="w-full max-w-3xl bg-zinc-900 border border-zinc-800 rounded-lg p-6 shadow-xl space-y-5 max-h-[90vh] overflow-y-auto">
            
            {/* Modal Header */}
            <div className="flex items-start justify-between pb-3 border-b border-zinc-800">
              <div>
                <div className="flex items-center space-x-2">
                  <h2 className="text-base font-bold text-zinc-100">Agent Task #{selectedTask.taskId} Details</h2>
                  {getStatusBadge(selectedTask.status)}
                </div>
                <p className="text-xs text-zinc-400 font-mono mt-0.5">
                  Agent Type: {selectedTask.agentType} | Created: {new Date(selectedTask.createdAt).toLocaleString()}
                </p>
              </div>
              <button
                onClick={() => setModalOpen(false)}
                className="p-1 text-zinc-400 hover:text-zinc-200 bg-zinc-800 hover:bg-zinc-700 rounded transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Step-by-Step Reasoning Log */}
            <div className="space-y-2">
              <h4 className="text-xs font-semibold text-zinc-400 uppercase tracking-wider flex items-center gap-1.5">
                <Terminal className="w-3.5 h-3.5 text-emerald-400" />
                Step-by-Step Tool Execution Trail
              </h4>
              <div className="bg-zinc-950 border border-zinc-800 rounded-md p-3 space-y-1 font-mono text-xs text-zinc-300">
                {selectedTask.executionSteps && selectedTask.executionSteps.length > 0 ? (
                  selectedTask.executionSteps.map((step, idx) => (
                    <div key={idx} className="flex items-start space-x-2">
                      <span className="text-emerald-400">›</span>
                      <span>{step}</span>
                    </div>
                  ))
                ) : (
                  <span className="text-zinc-500 italic">No step logs recorded.</span>
                )}
              </div>
            </div>

            {/* Generated Code / Patch / Docs Output */}
            <div className="space-y-2">
              <h4 className="text-xs font-semibold text-zinc-400 uppercase tracking-wider flex items-center gap-1.5">
                <Code2 className="w-3.5 h-3.5 text-emerald-400" />
                Agent Generated Output
              </h4>

              <div className="bg-zinc-950 border border-zinc-800 rounded-md p-3.5 font-mono text-xs text-zinc-200 overflow-x-auto max-h-64">
                <pre className="whitespace-pre-wrap">
                  {(() => {
                    const parsed = parseOutputResult(selectedTask.outputResult);
                    if (parsed.generatedTestCode) return parsed.generatedTestCode;
                    if (parsed.proposedPatchDiff) return parsed.proposedPatchDiff;
                    if (parsed.generatedDocs) return parsed.generatedDocs;
                    return JSON.stringify(parsed, null, 2);
                  })()}
                </pre>
              </div>
            </div>

            {/* Human Oversight Controls */}
            <div className="bg-zinc-950 border border-zinc-800 rounded-md p-4 space-y-3">
              <h4 className="text-xs font-semibold text-zinc-400 uppercase tracking-wider flex items-center gap-1.5">
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
                Human Oversight Validation
              </h4>

              {selectedTask.status === 'APPROVED' ? (
                <div className="p-3 bg-emerald-500/10 border border-emerald-500/20 text-emerald-300 rounded-md text-xs flex items-center justify-between font-mono">
                  <span>✓ Action approved and applied by <strong>{selectedTask.approvedBy || 'Developer'}</strong>.</span>
                  <span className="text-[11px] text-emerald-400">{selectedTask.humanFeedback}</span>
                </div>
              ) : selectedTask.status === 'REJECTED' ? (
                <div className="p-3 bg-rose-500/10 border border-rose-500/20 text-rose-300 rounded-md text-xs flex items-center justify-between font-mono">
                  <span>✗ Action rejected by <strong>{selectedTask.approvedBy || 'Developer'}</strong>.</span>
                  <span className="text-[11px] text-rose-400">{selectedTask.humanFeedback}</span>
                </div>
              ) : (
                <div className="space-y-3">
                  <input
                    type="text"
                    value={feedbackText}
                    onChange={(e) => setFeedbackText(e.target.value)}
                    placeholder="Optional human review feedback / notes..."
                    className="w-full bg-zinc-900 border border-zinc-800 rounded-md px-3 py-1.5 text-xs text-zinc-200 focus:outline-none focus:border-zinc-700"
                  />

                  <div className="flex items-center justify-end space-x-2.5">
                    <button
                      onClick={handleReject}
                      disabled={actionLoading}
                      className="px-3 py-1.5 bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/20 rounded-md text-xs font-medium flex items-center space-x-1 transition-colors"
                    >
                      <X className="w-3.5 h-3.5" />
                      <span>Reject Proposal</span>
                    </button>

                    <button
                      onClick={handleApprove}
                      disabled={actionLoading}
                      className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-md text-xs font-medium flex items-center space-x-1 shadow-sm transition-colors"
                    >
                      <Check className="w-3.5 h-3.5" />
                      <span>Approve & Apply Action</span>
                    </button>
                  </div>
                </div>
              )}
            </div>

          </div>
        </div>
      )}

    </div>
  );
};

