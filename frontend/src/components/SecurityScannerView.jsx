import React, { useState, useEffect } from 'react';
import { 
  ShieldAlert, 
  ShieldCheck, 
  Key, 
  Database, 
  Lock, 
  Globe, 
  PackageCheck, 
  RefreshCw, 
  AlertTriangle, 
  CheckCircle2, 
  XCircle, 
  Info, 
  ChevronDown, 
  ChevronRight, 
  Code, 
  ExternalLink,
  Filter,
  Shield
} from 'lucide-react';
import { runSecurityScan, getReportsForProject, getLatestReportForProject } from '../services/securityService';

export default function SecurityScannerView({ project }) {
  const [latestReport, setLatestReport] = useState(null);
  const [reportsHistory, setReportsHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [scanning, setScanning] = useState(false);
  const [simulateVulnerabilities, setSimulateVulnerabilities] = useState(false);
  const [selectedSeverity, setSelectedSeverity] = useState('ALL');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [expandedFindingId, setExpandedFindingId] = useState(null);

  useEffect(() => {
    if (project?.id) {
      loadSecurityData();
    }
  }, [project?.id]);

  const loadSecurityData = async () => {
    setLoading(true);
    try {
      const latest = await getLatestReportForProject(project.id);
      const history = await getReportsForProject(project.id);
      setLatestReport(latest);
      setReportsHistory(history);
    } catch (err) {
      console.error('Failed to load security scan data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRunScan = async () => {
    setScanning(true);
    try {
      const commitSha = simulateVulnerabilities ? 'commit-vuln-test' : 'c0de' + Math.random().toString(16).substring(2, 8);
      const report = await runSecurityScan(project.id, commitSha, simulateVulnerabilities);
      setLatestReport(report);
      await loadSecurityData();
    } catch (err) {
      console.error('Failed to run security scan:', err);
    } finally {
      setScanning(false);
    }
  };

  const getRiskBadge = (riskLevel) => {
    switch (riskLevel) {
      case 'CRITICAL':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded text-[10px] font-mono font-semibold bg-rose-500/10 text-rose-400 border border-rose-500/20 uppercase">
            <XCircle className="w-3.5 h-3.5" /> Risk: CRITICAL
          </span>
        );
      case 'HIGH':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded text-[10px] font-mono font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/20 uppercase">
            <AlertTriangle className="w-3.5 h-3.5" /> Risk: HIGH
          </span>
        );
      case 'MEDIUM':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded text-[10px] font-mono font-semibold bg-zinc-800 text-zinc-300 border border-zinc-700 uppercase">
            <Info className="w-3.5 h-3.5" /> Risk: MEDIUM
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded text-[10px] font-mono font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 uppercase">
            <ShieldCheck className="w-3.5 h-3.5" /> Risk: LOW
          </span>
        );
    }
  };

  const getSeverityBadge = (severity) => {
    switch (severity) {
      case 'CRITICAL':
        return <span className="px-2 py-0.5 rounded text-[10px] font-mono font-semibold bg-rose-500/10 text-rose-400 border border-rose-500/20 uppercase">CRITICAL</span>;
      case 'HIGH':
        return <span className="px-2 py-0.5 rounded text-[10px] font-mono font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/20 uppercase">HIGH</span>;
      case 'MEDIUM':
        return <span className="px-2 py-0.5 rounded text-[10px] font-mono font-semibold bg-zinc-800 text-zinc-300 border border-zinc-700 uppercase">MEDIUM</span>;
      case 'LOW':
        return <span className="px-2 py-0.5 rounded text-[10px] font-mono font-semibold bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 uppercase">LOW</span>;
      default:
        return <span className="px-2 py-0.5 rounded text-[10px] font-mono font-semibold bg-zinc-800 text-zinc-400 border border-zinc-700 uppercase">INFO</span>;
    }
  };

  const getCategoryIcon = (category) => {
    switch (category) {
      case 'HARDCODED_SECRETS': return <Key className="w-4 h-4 text-amber-400" />;
      case 'SQL_INJECTION': return <Database className="w-4 h-4 text-rose-400" />;
      case 'WEAK_AUTHENTICATION': return <Lock className="w-4 h-4 text-purple-400" />;
      case 'INSECURE_API': return <Globe className="w-4 h-4 text-cyan-400" />;
      case 'DEPENDENCY_CVE': return <PackageCheck className="w-4 h-4 text-emerald-400" />;
      default: return <ShieldAlert className="w-4 h-4 text-zinc-400" />;
    }
  };

  const filteredFindings = latestReport?.findings?.filter(f => {
    const matchesSeverity = selectedSeverity === 'ALL' || f.severity === selectedSeverity;
    const matchesCategory = selectedCategory === 'ALL' || f.category === selectedCategory;
    return matchesSeverity && matchesCategory;
  }) || [];

  return (
    <div className="space-y-4">
      {/* Top Banner & Control Card */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-5 shadow-sm">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-md bg-zinc-950 border border-zinc-800 text-emerald-400">
              <Shield className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2.5">
                <h2 className="text-base font-semibold text-zinc-100">
                  Security Vulnerability Scanner
                </h2>
                {latestReport && getRiskBadge(latestReport.overallRiskLevel)}
              </div>
              <p className="text-xs text-zinc-400 mt-0.5 font-sans">
                Static Application Security Testing (SAST), Secrets Detection, SQLi Rules, & Dependency CVE Scanning
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="flex items-center gap-2 bg-zinc-950 px-3 py-1.5 rounded-md border border-zinc-800">
              <input
                type="checkbox"
                id="simVuln"
                checked={simulateVulnerabilities}
                onChange={(e) => setSimulateVulnerabilities(e.target.checked)}
                className="w-3.5 h-3.5 rounded bg-zinc-900 border-zinc-700 text-emerald-500 focus:ring-emerald-500/20"
              />
              <label htmlFor="simVuln" className="text-xs text-zinc-300 font-medium cursor-pointer">
                Simulate Vulnerabilities
              </label>
            </div>

            <button
              onClick={handleRunScan}
              disabled={scanning}
              className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-md bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white font-medium text-xs transition-colors shadow-sm"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${scanning ? 'animate-spin' : ''}`} />
              {scanning ? 'Scanning AST & CVEs...' : 'Run Security Scan'}
            </button>
          </div>
        </div>

        {/* Latest Report Key Metrics Header */}
        {latestReport && (
          <div className="mt-5 pt-4 border-t border-zinc-800 grid grid-cols-2 sm:grid-cols-6 gap-3 font-mono">
            <div className="bg-zinc-950 p-2.5 rounded-md border border-zinc-800">
              <span className="text-[10px] text-zinc-500 uppercase block font-semibold">Security Score</span>
              <span className="text-xl font-bold text-emerald-400">
                {latestReport.overallSecurityScore.toFixed(0)}<span className="text-xs text-zinc-600 font-normal">/100</span>
              </span>
            </div>

            <div className="bg-zinc-950 p-2.5 rounded-md border border-zinc-800">
              <span className="text-[10px] text-rose-400 uppercase block font-semibold">Critical</span>
              <span className="text-xl font-bold text-rose-400">{latestReport.criticalCount}</span>
            </div>

            <div className="bg-zinc-950 p-2.5 rounded-md border border-zinc-800">
              <span className="text-[10px] text-amber-400 uppercase block font-semibold">High</span>
              <span className="text-xl font-bold text-amber-400">{latestReport.highCount}</span>
            </div>

            <div className="bg-zinc-950 p-2.5 rounded-md border border-zinc-800">
              <span className="text-[10px] text-zinc-300 uppercase block font-semibold">Medium</span>
              <span className="text-xl font-bold text-zinc-300">{latestReport.mediumCount}</span>
            </div>

            <div className="bg-zinc-950 p-2.5 rounded-md border border-zinc-800">
              <span className="text-[10px] text-cyan-400 uppercase block font-semibold">Low</span>
              <span className="text-xl font-bold text-cyan-400">{latestReport.lowCount}</span>
            </div>

            <div className="bg-zinc-950 p-2.5 rounded-md border border-zinc-800">
              <span className="text-[10px] text-zinc-500 uppercase block font-semibold">Commit SHA</span>
              <span className="text-xs text-zinc-300 block truncate font-bold mt-1">
                {latestReport.scannedCommitSha.substring(0, 10)}
              </span>
            </div>
          </div>
        )}
      </div>

      {/* 5 Security Detector Categories Summary */}
      <div className="grid grid-cols-1 sm:grid-cols-5 gap-2.5">
        <div className="bg-zinc-900 border border-zinc-800 p-3 rounded-md">
          <div className="flex items-center gap-2 mb-1.5">
            <Key className="w-3.5 h-3.5 text-amber-400" />
            <h4 className="text-xs font-semibold text-zinc-200">Hardcoded Secrets</h4>
          </div>
          <p className="text-[11px] text-zinc-400 font-sans">API keys & DB passwords</p>
        </div>

        <div className="bg-zinc-900 border border-zinc-800 p-3 rounded-md">
          <div className="flex items-center gap-2 mb-1.5">
            <Database className="w-3.5 h-3.5 text-rose-400" />
            <h4 className="text-xs font-semibold text-zinc-200">SQL Injection</h4>
          </div>
          <p className="text-[11px] text-zinc-400 font-sans">Raw String SQL Concatenation</p>
        </div>

        <div className="bg-zinc-900 border border-zinc-800 p-3 rounded-md">
          <div className="flex items-center gap-2 mb-1.5">
            <Lock className="w-3.5 h-3.5 text-purple-400" />
            <h4 className="text-xs font-semibold text-zinc-200">Weak Authentication</h4>
          </div>
          <p className="text-[11px] text-zinc-400 font-sans">Missing @PreAuthorize RBAC</p>
        </div>

        <div className="bg-zinc-900 border border-zinc-800 p-3 rounded-md">
          <div className="flex items-center gap-2 mb-1.5">
            <Globe className="w-3.5 h-3.5 text-cyan-400" />
            <h4 className="text-xs font-semibold text-zinc-200">Insecure APIs</h4>
          </div>
          <p className="text-[11px] text-zinc-400 font-sans">Unvalidated HTTP endpoints</p>
        </div>

        <div className="bg-zinc-900 border border-zinc-800 p-3 rounded-md">
          <div className="flex items-center gap-2 mb-1.5">
            <PackageCheck className="w-3.5 h-3.5 text-emerald-400" />
            <h4 className="text-xs font-semibold text-zinc-200">Dependency CVEs</h4>
          </div>
          <p className="text-[11px] text-zinc-400 font-sans">Maven & npm package CVEs</p>
        </div>
      </div>

      {/* Security Findings & Remediation Table */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-5 shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-zinc-800">
          <div className="flex items-center gap-2">
            <ShieldAlert className="w-4 h-4 text-emerald-400" />
            <h3 className="text-sm font-semibold text-zinc-100">
              Security Vulnerability Findings ({filteredFindings.length})
            </h3>
          </div>

          <div className="flex items-center gap-2 font-mono">
            {/* Severity Filter */}
            <select
              value={selectedSeverity}
              onChange={(e) => setSelectedSeverity(e.target.value)}
              className="bg-zinc-950 border border-zinc-800 text-xs text-zinc-300 rounded-md px-2.5 py-1 focus:outline-none"
            >
              <option value="ALL">All Severities</option>
              <option value="CRITICAL">Critical Only</option>
              <option value="HIGH">High Only</option>
              <option value="MEDIUM">Medium Only</option>
              <option value="LOW">Low Only</option>
            </select>

            {/* Category Filter */}
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              className="bg-zinc-950 border border-zinc-800 text-xs text-zinc-300 rounded-md px-2.5 py-1 focus:outline-none"
            >
              <option value="ALL">All Categories</option>
              <option value="HARDCODED_SECRETS">Secrets</option>
              <option value="SQL_INJECTION">SQL Injection</option>
              <option value="WEAK_AUTHENTICATION">Weak Auth</option>
              <option value="INSECURE_API">Insecure API</option>
              <option value="DEPENDENCY_CVE">Dependency CVE</option>
            </select>
          </div>
        </div>

        {/* Findings List */}
        {filteredFindings.length === 0 ? (
          <div className="py-10 text-center text-zinc-500 text-xs font-mono">
            <CheckCircle2 className="w-8 h-8 text-emerald-400 mx-auto mb-2 opacity-80" />
            No security findings matching selected filters. System secure!
          </div>
        ) : (
          <div className="mt-3.5 space-y-2.5">
            {filteredFindings.map((finding) => {
              const isExpanded = expandedFindingId === finding.id;
              return (
                <div
                  key={finding.id}
                  className="bg-zinc-950 border border-zinc-800 rounded-md overflow-hidden transition-colors hover:border-zinc-700"
                >
                  <div
                    onClick={() => setExpandedFindingId(isExpanded ? null : finding.id)}
                    className="p-3 flex items-center justify-between cursor-pointer select-none"
                  >
                    <div className="flex items-center gap-2.5">
                      {isExpanded ? <ChevronDown className="w-4 h-4 text-emerald-400" /> : <ChevronRight className="w-4 h-4 text-zinc-500" />}
                      {getCategoryIcon(finding.category)}
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="text-[11px] font-mono text-zinc-500">{finding.id}</span>
                          <h4 className="text-xs font-semibold text-zinc-100">{finding.title}</h4>
                          {finding.cveId && (
                            <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-rose-500/10 text-rose-400 border border-rose-500/20">
                              {finding.cveId}
                            </span>
                          )}
                        </div>
                        <p className="text-[11px] text-zinc-400 mt-0.5 font-sans">
                          File: <span className="font-mono text-emerald-400">{finding.filePath}</span> (Line {finding.lineNumber})
                        </p>
                      </div>
                    </div>

                    <div>
                      {getSeverityBadge(finding.severity)}
                    </div>
                  </div>

                  {/* Expanded Remediation & Code Details */}
                  {isExpanded && (
                    <div className="p-3.5 bg-zinc-900 border-t border-zinc-800 space-y-3 text-xs font-sans">
                      <div>
                        <span className="text-zinc-400 font-mono font-semibold block mb-1 text-[11px] uppercase">Description:</span>
                        <p className="text-zinc-300 leading-relaxed text-xs">{finding.description}</p>
                      </div>

                      {finding.snippet && (
                        <div>
                          <span className="text-zinc-400 font-mono font-semibold block mb-1 text-[11px] uppercase">Vulnerable Code Snippet:</span>
                          <pre className="p-2.5 bg-zinc-950 border border-zinc-800 rounded-md font-mono text-rose-400 text-xs overflow-x-auto">
                            {finding.snippet}
                          </pre>
                        </div>
                      )}

                      <div className="bg-zinc-950 border border-zinc-800 p-3 rounded-md">
                        <span className="text-emerald-400 font-mono font-semibold block mb-1 flex items-center gap-1.5 text-xs">
                          <CheckCircle2 className="w-3.5 h-3.5" /> Remediation Recommendation:
                        </span>
                        <p className="text-zinc-300 leading-relaxed font-mono text-[11px]">{finding.recommendation}</p>
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
