import React, { useState, useEffect } from 'react';
import { 
  Cloud, Server, Database, HardDrive, Cpu, Activity, ShieldCheck, Play, 
  CheckCircle2, Clock, AlertTriangle, RefreshCw, Terminal, Layers, ArrowRight, Box
} from 'lucide-react';
import { getDeploymentStatus, triggerDeploymentPipeline, getPipelineHistory } from '../services/deploymentService';

const CloudDeploymentView = ({ project }) => {
  const [status, setStatus] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [triggering, setTriggering] = useState(false);
  const [targetEnv, setTargetEnv] = useState('PRODUCTION');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const fetchData = async () => {
    try {
      setError('');
      const [statusData, historyData] = await Promise.all([
        getDeploymentStatus(),
        getPipelineHistory()
      ]);
      setStatus(statusData);
      setHistory(historyData);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch cloud deployment status.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 4000);
    return () => clearInterval(interval);
  }, []);

  const handleTriggerPipeline = async () => {
    setTriggering(true);
    setError('');
    setSuccess('');
    try {
      const res = await triggerDeploymentPipeline(targetEnv);
      setSuccess(`Cloud deployment pipeline #${res.id} triggered successfully!`);
      fetchData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to trigger cloud deployment pipeline.');
    } finally {
      setTriggering(false);
    }
  };

  if (loading && !status) {
    return (
      <div className="flex flex-col items-center justify-center p-12 space-y-4">
        <RefreshCw className="w-6 h-6 text-emerald-500 animate-spin" />
        <p className="text-zinc-400 text-xs font-mono">Connecting to DevOpsAI Cloud Operations Engine...</p>
      </div>
    );
  }

  const memoryPercentage = status ? Math.round((status.jvmMemoryUsedMb / (status.jvmMemoryMaxMb || 1)) * 100) : 0;
  const uptimeHours = status ? (status.systemUptimeSeconds / 3600).toFixed(1) : '0.0';

  return (
    <div className="space-y-6">
      
      {/* Top Banner & Control Panel */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-6 shadow-sm">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
          <div className="space-y-2">
            <div className="flex items-center gap-3">
              <span className="p-2 rounded-md bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                <Cloud className="w-5 h-5" />
              </span>
              <div>
                <h2 className="text-lg font-bold text-zinc-100 flex items-center gap-2">
                  <span>Cloud Operations & Docker Orchestration</span>
                  <span className={`px-2 py-0.5 rounded text-[10px] font-mono font-semibold uppercase tracking-wider ${
                    status?.environmentStatus === 'PRODUCTION'
                      ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                      : 'bg-zinc-800 text-zinc-300 border border-zinc-700'
                  }`}>
                    {status?.environmentStatus || 'DOCKER_CONTAINERIZED'}
                  </span>
                </h2>
                <p className="text-xs text-zinc-400">
                  Target Project: <span className="text-zinc-200 font-mono font-medium">{project?.name || 'SmartHealth Engine'}</span>
                </p>
              </div>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <select
              value={targetEnv}
              onChange={(e) => setTargetEnv(e.target.value)}
              className="bg-zinc-950 border border-zinc-800 text-zinc-200 text-xs rounded-md px-3 py-2 font-mono focus:outline-none focus:border-zinc-700"
            >
              <option value="PRODUCTION">Target: PRODUCTION (Cloud)</option>
              <option value="STAGING">Target: STAGING (K8s)</option>
            </select>

            <button
              onClick={handleTriggerPipeline}
              disabled={triggering}
              className="px-4 py-2 rounded-md text-xs font-medium text-white bg-emerald-600 hover:bg-emerald-500 transition-colors shadow-sm flex items-center gap-2 disabled:opacity-50"
            >
              {triggering ? (
                <>
                  <RefreshCw className="w-4 h-4 animate-spin" />
                  <span>Deploying...</span>
                </>
              ) : (
                <>
                  <Play className="w-4 h-4 fill-white" />
                  <span>Trigger Cloud Deployment</span>
                </>
              )}
            </button>
          </div>
        </div>

        {/* System Health Overview Strip */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mt-6 pt-6 border-t border-zinc-800/80">
          
          <div className="bg-zinc-950 border border-zinc-800/80 rounded-md p-3.5 flex items-center gap-3">
            <div className={`p-2.5 rounded-md ${status?.dbConnected ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'}`}>
              <Database className="w-4 h-4" />
            </div>
            <div>
              <p className="text-[11px] text-zinc-400 font-medium">PostgreSQL Database</p>
              <p className="text-xs font-semibold text-zinc-200 flex items-center gap-1.5 mt-0.5">
                {status?.dbConnected ? (
                  <span className="text-emerald-400 flex items-center gap-1 font-mono text-[11px]"><CheckCircle2 className="w-3.5 h-3.5" /> Connected</span>
                ) : (
                  <span className="text-rose-400 flex items-center gap-1 font-mono text-[11px]"><AlertTriangle className="w-3.5 h-3.5" /> Disconnected</span>
                )}
              </p>
            </div>
          </div>

          <div className="bg-zinc-950 border border-zinc-800/80 rounded-md p-3.5 flex items-center gap-3">
            <div className={`p-2.5 rounded-md ${status?.redisHealthy ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' : 'bg-amber-500/10 text-amber-400 border border-amber-500/20'}`}>
              <HardDrive className="w-4 h-4" />
            </div>
            <div>
              <p className="text-[11px] text-zinc-400 font-medium">Redis Cache Manager</p>
              <p className="text-xs font-semibold text-zinc-200 mt-0.5">
                {status?.redisHealthy ? (
                  <span className="text-emerald-400 flex items-center gap-1 font-mono text-[11px]"><CheckCircle2 className="w-3.5 h-3.5" /> Active</span>
                ) : (
                  <span className="text-amber-400 flex items-center gap-1 font-mono text-[11px]"><ShieldCheck className="w-3.5 h-3.5" /> In-Memory Fallback</span>
                )}
              </p>
            </div>
          </div>

          <div className="bg-zinc-950 border border-zinc-800/80 rounded-md p-3.5 flex items-center gap-3">
            <div className="p-2.5 rounded-md bg-zinc-800 text-zinc-300 border border-zinc-700">
              <Cpu className="w-4 h-4" />
            </div>
            <div className="flex-1">
              <div className="flex justify-between text-[11px] text-zinc-400 font-medium mb-1">
                <span>JVM Heap</span>
                <span className="text-emerald-400 font-mono text-[11px]">{status?.jvmMemoryUsedMb} / {status?.jvmMemoryMaxMb} MB</span>
              </div>
              <div className="w-full bg-zinc-800 h-1.5 rounded-full overflow-hidden">
                <div 
                  className="bg-emerald-500 h-full rounded-full transition-all duration-500" 
                  style={{ width: `${Math.min(memoryPercentage, 100)}%` }}
                />
              </div>
            </div>
          </div>

          <div className="bg-zinc-950 border border-zinc-800/80 rounded-md p-3.5 flex items-center gap-3">
            <div className="p-2.5 rounded-md bg-zinc-800 text-zinc-300 border border-zinc-700">
              <Clock className="w-4 h-4" />
            </div>
            <div>
              <p className="text-[11px] text-zinc-400 font-medium">System Uptime</p>
              <p className="text-xs font-semibold text-zinc-200 font-mono mt-0.5">
                {uptimeHours} <span className="text-[11px] text-zinc-400 font-normal">Hours</span>
              </p>
            </div>
          </div>

        </div>
      </div>

      {/* Notifications */}
      {error && (
        <div className="p-3.5 rounded-md bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs flex items-center gap-2">
          <AlertTriangle className="h-4 w-4 text-rose-400 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div className="p-3.5 rounded-md bg-emerald-500/10 border border-emerald-500/20 text-emerald-300 text-xs flex items-center gap-2">
          <CheckCircle2 className="h-4 w-4 text-emerald-400 flex-shrink-0" />
          <span>{success}</span>
        </div>
      )}

      {/* Architecture Topology View */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-5 shadow-sm">
        <h3 className="text-xs font-semibold text-zinc-400 flex items-center gap-2 uppercase tracking-wider mb-4">
          <Layers className="w-3.5 h-3.5 text-emerald-400" /> Deployment Architecture Topology
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-5 gap-4 items-center">
          
          <div className="bg-zinc-950 border border-zinc-800 rounded-md p-4 text-center hover:border-zinc-700 transition-colors">
            <div className="w-9 h-9 rounded-md bg-zinc-800 text-zinc-300 flex items-center justify-center mx-auto mb-2">
              <Box className="w-4 h-4" />
            </div>
            <h4 className="text-xs font-semibold text-zinc-200">Frontend Service</h4>
            <p className="text-[11px] text-zinc-400 font-mono mt-1">React SPA + Nginx</p>
            <span className="inline-block mt-2.5 px-2 py-0.5 rounded bg-zinc-800 text-zinc-400 text-[10px] font-mono border border-zinc-700">Port 80</span>
          </div>

          <div className="hidden md:flex justify-center text-zinc-600">
            <ArrowRight className="w-4 h-4" />
          </div>

          <div className="bg-zinc-950 border border-zinc-700 rounded-md p-4 text-center transition-colors">
            <div className="w-9 h-9 rounded-md bg-emerald-500/10 text-emerald-400 flex items-center justify-center mx-auto mb-2 border border-emerald-500/20">
              <Server className="w-4 h-4" />
            </div>
            <h4 className="text-xs font-semibold text-zinc-100">Backend Container</h4>
            <p className="text-[11px] text-zinc-400 font-mono mt-1">Spring Boot Docker</p>
            <span className="inline-block mt-2.5 px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 text-[10px] font-mono border border-emerald-500/20">Port 8080</span>
          </div>

          <div className="hidden md:flex justify-center text-zinc-600">
            <ArrowRight className="w-4 h-4" />
          </div>

          <div className="bg-zinc-950 border border-zinc-800 rounded-md p-4 text-center hover:border-zinc-700 transition-colors">
            <div className="w-9 h-9 rounded-md bg-zinc-800 text-zinc-300 flex items-center justify-center mx-auto mb-2">
              <Database className="w-4 h-4" />
            </div>
            <h4 className="text-xs font-semibold text-zinc-200">Data & Cache Layer</h4>
            <p className="text-[11px] text-zinc-400 font-mono mt-1">Postgres + Redis</p>
            <span className="inline-block mt-2.5 px-2 py-0.5 rounded bg-zinc-800 text-zinc-400 text-[10px] font-mono border border-zinc-700">Ports 5432 / 6379</span>
          </div>

        </div>
      </div>

      {/* Docker Containers Status Table */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-5 shadow-sm">
        <h3 className="text-xs font-semibold text-zinc-400 flex items-center gap-2 uppercase tracking-wider mb-4">
          <Terminal className="w-3.5 h-3.5 text-emerald-400" /> Orchestrated Docker Containers
        </h3>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-zinc-300">
            <thead className="bg-zinc-950 text-zinc-400 font-mono uppercase text-[10px] border-b border-zinc-800">
              <tr>
                <th className="px-3 py-2.5">Container Name</th>
                <th className="px-3 py-2.5">Docker Image</th>
                <th className="px-3 py-2.5">Status</th>
                <th className="px-3 py-2.5">Port Mapping</th>
                <th className="px-3 py-2.5">Health Check</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-800/60">
              {status?.containers?.map((c, i) => (
                <tr key={i} className="hover:bg-zinc-800/40 transition-colors">
                  <td className="px-3 py-2.5 font-mono font-medium text-zinc-200 flex items-center gap-2">
                    <Box className="w-3.5 h-3.5 text-emerald-400" />
                    {c.name}
                  </td>
                  <td className="px-3 py-2.5 font-mono text-zinc-400 text-[11px]">{c.image}</td>
                  <td className="px-3 py-2.5">
                    <span className="px-2 py-0.5 rounded text-[10px] font-mono font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                      {c.status}
                    </span>
                  </td>
                  <td className="px-3 py-2.5 font-mono text-zinc-300 text-[11px]">{c.port}</td>
                  <td className="px-3 py-2.5">
                    <span className="flex items-center gap-1.5 text-emerald-400 font-mono text-[11px]">
                      <CheckCircle2 className="w-3.5 h-3.5" /> {c.health}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Deployment Pipeline Execution History */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-5 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-xs font-semibold text-zinc-400 flex items-center gap-2 uppercase tracking-wider">
            <Activity className="w-3.5 h-3.5 text-emerald-400" /> Deployment Pipeline History
          </h3>
          <span className="text-xs text-zinc-400 font-mono">Total Runs: {history.length}</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-zinc-300">
            <thead className="bg-zinc-950 text-zinc-400 font-mono uppercase text-[10px] border-b border-zinc-800">
              <tr>
                <th className="px-3 py-2.5">Run ID</th>
                <th className="px-3 py-2.5">Workflow Name</th>
                <th className="px-3 py-2.5">Target Env</th>
                <th className="px-3 py-2.5">Commit SHA</th>
                <th className="px-3 py-2.5">Status</th>
                <th className="px-3 py-2.5">Triggered By</th>
                <th className="px-3 py-2.5">Duration</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-800/60">
              {history.map((run) => (
                <tr key={run.id} className="hover:bg-zinc-800/40 transition-colors">
                  <td className="px-3 py-2.5 font-mono text-emerald-400 font-medium">{run.id}</td>
                  <td className="px-3 py-2.5 text-zinc-200">{run.pipelineName}</td>
                  <td className="px-3 py-2.5">
                    <span className="px-2 py-0.5 rounded text-[10px] font-mono bg-zinc-950 text-zinc-300 border border-zinc-800">
                      {run.targetEnvironment}
                    </span>
                  </td>
                  <td className="px-3 py-2.5 font-mono text-zinc-400 text-[11px]">{run.commitSha}</td>
                  <td className="px-3 py-2.5">
                    {run.status === 'DEPLOYED' && (
                      <span className="px-2 py-0.5 rounded text-[10px] font-mono font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center gap-1 w-fit">
                        <CheckCircle2 className="w-3 h-3" /> DEPLOYED
                      </span>
                    )}
                    {run.status === 'PENDING' && (
                      <span className="px-2 py-0.5 rounded text-[10px] font-mono font-medium bg-amber-500/10 text-amber-400 border border-amber-500/20 flex items-center gap-1 w-fit">
                        <Clock className="w-3 h-3 animate-spin" /> PENDING
                      </span>
                    )}
                    {(run.status === 'BUILDING' || run.status === 'TESTING' || run.status === 'SECURITY_SCAN') && (
                      <span className="px-2 py-0.5 rounded text-[10px] font-mono font-medium bg-zinc-800 text-zinc-300 border border-zinc-700 flex items-center gap-1 w-fit">
                        <RefreshCw className="w-3 h-3 animate-spin text-emerald-400" /> {run.status}
                      </span>
                    )}
                    {run.status === 'FAILED' && (
                      <span className="px-2 py-0.5 rounded text-[10px] font-mono font-medium bg-rose-500/10 text-rose-400 border border-rose-500/20 flex items-center gap-1 w-fit">
                        <AlertTriangle className="w-3 h-3" /> FAILED
                      </span>
                    )}
                  </td>
                  <td className="px-3 py-2.5 text-zinc-400">{run.triggeredBy}</td>
                  <td className="px-3 py-2.5 font-mono text-zinc-400">
                    {run.durationSeconds ? `${run.durationSeconds}s` : '--'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

    </div>
  );
};

export default CloudDeploymentView;

