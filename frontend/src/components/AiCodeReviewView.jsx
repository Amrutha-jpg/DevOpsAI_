import React, { useState, useEffect } from 'react';
import { aiReviewService } from '../services/aiReviewService';
import { githubService } from '../services/githubService';
import { 
  Bot, ShieldAlert, Sparkles, CheckCircle2, AlertTriangle, 
  Code2, FileCode, Play, RefreshCw, Zap, Lightbulb, ChevronDown, 
  ChevronUp, Bug, Shield, Gauge, CheckCircle, Terminal, Layers
} from 'lucide-react';

export const AiCodeReviewView = ({ projectId }) => {
  const [reviews, setReviews] = useState([]);
  const [activeReview, setActiveReview] = useState(null);
  const [pullRequests, setPullRequests] = useState([]);
  const [selectedPrId, setSelectedPrId] = useState(null);
  
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [analyzing, setAnalyzing] = useState(false);
  const [expandedFindingId, setExpandedFindingId] = useState(null);

  // Ad-hoc Sandbox state
  const [sandboxFilename, setSandboxFilename] = useState('PatientService.java');
  const [sandboxSnippet, setSandboxSnippet] = useState(
`public class PatientService {
    @Autowired
    private PatientRepository repository;

    public Patient getPatientDiagnostic(Long patientId) {
        // Flagship Anti-Pattern: Raw Optional.get() without check
        return repository.findById(patientId).get();
    }
}`
  );
  const [sandboxResult, setSandboxResult] = useState(null);
  const [sandboxAnalyzing, setSandboxAnalyzing] = useState(false);

  const fetchInitialData = async () => {
    if (!projectId) return;
    setLoading(true);
    try {
      const [reviewsData, prsData] = await Promise.all([
        aiReviewService.getReviewsForProject(projectId),
        githubService.getPullRequests(projectId)
      ]);
      setReviews(reviewsData);
      setPullRequests(prsData);

      if (prsData.length > 0) {
        setSelectedPrId(prsData[0].id);
      }

      if (reviewsData.length > 0) {
        setActiveReview(reviewsData[0]);
      }
    } catch (err) {
      console.error('Error loading AI Code Review data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInitialData();
  }, [projectId]);

  const handleRunPrReview = async () => {
    if (!selectedPrId) return;
    setAnalyzing(true);
    try {
      const initiated = await aiReviewService.triggerPrReview(projectId, selectedPrId);
      setActiveReview(initiated);

      // Poll status until COMPLETED or FAILED
      const pollInterval = setInterval(async () => {
        try {
          const updated = await aiReviewService.getReview(initiated.id);
          setActiveReview(updated);
          if (updated.status === 'COMPLETED' || updated.status === 'FAILED') {
            clearInterval(pollInterval);
            setAnalyzing(false);
            fetchInitialData();
          }
        } catch (err) {
          clearInterval(pollInterval);
          setAnalyzing(false);
        }
      }, 1500);

    } catch (err) {
      console.error('Failed to trigger PR review:', err);
      setAnalyzing(false);
    }
  };

  const handleRunSandboxReview = async (e) => {
    e.preventDefault();
    setSandboxAnalyzing(true);
    setSandboxResult(null);
    try {
      const result = await aiReviewService.analyzeSnippet(sandboxFilename, sandboxSnippet, projectId);
      setSandboxResult(result);
    } catch (err) {
      console.error('Sandbox analysis failed:', err);
    } finally {
      setSandboxAnalyzing(false);
    }
  };

  const categories = ['ALL', 'BUG', 'SECURITY', 'PERFORMANCE', 'MAINTAINABILITY', 'CODE_STYLE', 'BEST_PRACTICE'];

  const getSeverityBadge = (severity) => {
    switch (severity) {
      case 'CRITICAL':
        return <span className="px-2 py-0.5 rounded bg-rose-500/10 text-rose-400 border border-rose-500/20 text-[10px] font-mono uppercase">CRITICAL</span>;
      case 'HIGH':
        return <span className="px-2 py-0.5 rounded bg-amber-500/10 text-amber-400 border border-amber-500/20 text-[10px] font-mono uppercase">HIGH</span>;
      case 'MEDIUM':
        return <span className="px-2 py-0.5 rounded bg-zinc-800 text-zinc-300 border border-zinc-700 text-[10px] font-mono uppercase">MEDIUM</span>;
      case 'LOW':
        return <span className="px-2 py-0.5 rounded bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 text-[10px] font-mono uppercase">LOW</span>;
      default:
        return <span className="px-2 py-0.5 rounded bg-zinc-800 text-zinc-400 border border-zinc-700 text-[10px] font-mono uppercase">INFO</span>;
    }
  };

  const getCategoryIcon = (category) => {
    switch (category) {
      case 'BUG': return <Bug className="h-4 w-4 text-rose-400" />;
      case 'SECURITY': return <Shield className="h-4 w-4 text-amber-400" />;
      case 'PERFORMANCE': return <Zap className="h-4 w-4 text-cyan-400" />;
      default: return <Lightbulb className="h-4 w-4 text-emerald-400" />;
    }
  };

  const filteredFindings = activeReview?.findings
    ? activeReview.findings.filter(f => selectedCategory === 'ALL' || f.category === selectedCategory)
    : [];

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16 font-mono text-xs text-zinc-400">
        <RefreshCw className="h-5 w-5 text-emerald-400 animate-spin" />
        <span className="ml-3">Initializing AI Code Reviewer Subsystem...</span>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      
      {/* Top Banner & PR Review Selector */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-5 flex flex-col lg:flex-row lg:items-center justify-between gap-4">
        <div className="flex items-start space-x-3.5">
          <div className="p-2.5 rounded-md bg-zinc-950 border border-zinc-800 text-emerald-400">
            <Bot className="h-6 w-6" />
          </div>
          <div>
            <div className="flex items-center space-x-2.5">
              <h2 className="text-base font-semibold text-zinc-100 flex items-center space-x-2">
                <span>AI Automated Code Reviewer</span>
                <span className="px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-[10px] font-mono">
                  v4.0 LLM + Static Engine
                </span>
              </h2>
            </div>
            <p className="text-zinc-400 text-xs mt-0.5">
              Automated multi-category analysis: Bugs, Security, Performance, Maintainability, Code Style, Best Practice.
            </p>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          {pullRequests.length > 0 && (
            <div className="flex items-center space-x-2 bg-zinc-950 border border-zinc-800 rounded-md px-2.5 py-1.5 font-mono">
              <span className="text-xs text-zinc-400">Target PR:</span>
              <select
                value={selectedPrId || ''}
                onChange={(e) => setSelectedPrId(parseInt(e.target.value))}
                className="bg-transparent text-xs font-semibold text-emerald-400 focus:outline-none cursor-pointer"
              >
                {pullRequests.map(pr => (
                  <option key={pr.id} value={pr.id} className="bg-zinc-900 text-zinc-100">
                    PR #{pr.number}: {pr.title}
                  </option>
                ))}
              </select>
            </div>
          )}

          <button
            onClick={handleRunPrReview}
            disabled={analyzing || !selectedPrId}
            className="px-3.5 py-1.5 rounded-md bg-emerald-600 hover:bg-emerald-500 text-white font-medium text-xs flex items-center space-x-1.5 transition-colors shadow-sm disabled:opacity-50"
          >
            {analyzing ? (
              <>
                <RefreshCw className="h-3.5 w-3.5 animate-spin" />
                <span>AI Analyzing Code (@Async)...</span>
              </>
            ) : (
              <>
                <Sparkles className="h-3.5 w-3.5 text-emerald-300" />
                <span>Run AI Review on PR #{pullRequests.find(p => p.id === selectedPrId)?.number || 42}</span>
              </>
            )}
          </button>
        </div>
      </div>

      {/* Progress Alert when ANALYZING */}
      {activeReview?.status === 'ANALYZING' && (
        <div className="p-4 rounded-md bg-zinc-900 border border-emerald-500/40 text-emerald-300 text-xs flex items-center space-x-3 shadow-sm font-mono">
          <RefreshCw className="h-5 w-5 text-emerald-400 animate-spin flex-shrink-0" />
          <div className="space-y-0.5">
            <h4 className="font-semibold text-zinc-100">AI Engine actively scanning files...</h4>
            <p className="text-[11px] text-zinc-400">
              Parsing AST tokens, evaluating Optional.get() safety, scanning SQL query parameters, and structuring LLM JSON findings.
            </p>
          </div>
        </div>
      )}

      {/* Active Review Dashboard Metrics */}
      {activeReview && activeReview.status === 'COMPLETED' && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-3.5">
          
          <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-4 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-[11px] font-mono text-zinc-400 uppercase tracking-wider block">Quality Score</span>
              <div className="text-2xl font-bold font-mono text-zinc-100 flex items-baseline space-x-1">
                <span>{activeReview.qualityScore.toFixed(1)}</span>
                <span className="text-xs text-zinc-500">%</span>
              </div>
            </div>
            <div className="w-10 h-10 rounded-md bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center justify-center font-bold">
              <Gauge className="h-5 w-5" />
            </div>
          </div>

          <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-4 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-[11px] font-mono text-zinc-400 uppercase tracking-wider block">Total Findings</span>
              <div className="text-2xl font-bold font-mono text-emerald-400">
                {activeReview.findings ? activeReview.findings.length : 0}
              </div>
            </div>
            <div className="w-10 h-10 rounded-md bg-zinc-800 border border-zinc-700 text-zinc-300 flex items-center justify-center">
              <Code2 className="h-5 w-5" />
            </div>
          </div>

          <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-4 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-[11px] font-mono text-zinc-400 uppercase tracking-wider block">High Risk Issues</span>
              <div className="text-2xl font-bold font-mono text-rose-400">
                {activeReview.findings ? activeReview.findings.filter(f => f.severity === 'CRITICAL' || f.severity === 'HIGH').length : 0}
              </div>
            </div>
            <div className="w-10 h-10 rounded-md bg-rose-500/10 border border-rose-500/20 text-rose-400 flex items-center justify-center">
              <ShieldAlert className="h-5 w-5" />
            </div>
          </div>

          <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-4 flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-[11px] font-mono text-zinc-400 uppercase tracking-wider block">Target Review</span>
              <div className="text-xs font-mono font-semibold text-zinc-200 truncate max-w-[130px]">
                {activeReview.prTitle ? `PR #${activeReview.prNumber}` : 'Ad-hoc Snippet'}
              </div>
            </div>
            <div className="w-10 h-10 rounded-md bg-zinc-800 border border-zinc-700 text-zinc-300 flex items-center justify-center">
              <CheckCircle2 className="h-5 w-5" />
            </div>
          </div>

        </div>
      )}

      {/* Review Findings Inspector Section */}
      {activeReview && activeReview.status === 'COMPLETED' && (
        <div className="space-y-3.5">
          
          {/* Category Filter Pills */}
          <div className="flex flex-wrap items-center gap-1.5 pb-2 border-b border-zinc-800 font-mono">
            <span className="text-xs text-zinc-400 mr-2">Category Filter:</span>
            {categories.map(cat => (
              <button
                key={cat}
                onClick={() => setSelectedCategory(cat)}
                className={`px-2.5 py-1 rounded-md text-[11px] font-medium transition-colors ${
                  selectedCategory === cat
                    ? 'bg-zinc-800 text-zinc-100 border border-zinc-700'
                    : 'bg-zinc-950 text-zinc-400 hover:text-zinc-200 border border-zinc-850'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>

          {/* Findings List */}
          <div className="space-y-3">
            {filteredFindings.length === 0 ? (
              <div className="p-6 text-center text-zinc-500 text-xs font-mono bg-zinc-900 border border-zinc-800 rounded-lg">
                No findings matching category '{selectedCategory}'.
              </div>
            ) : (
              filteredFindings.map((finding) => (
                <div
                  key={finding.id || finding.title}
                  className="bg-zinc-900 border border-zinc-800 rounded-lg overflow-hidden shadow-sm hover:border-zinc-700 transition-colors"
                >
                  {/* Header Bar */}
                  <div className="px-4 py-3 bg-zinc-950 border-b border-zinc-800 flex items-center justify-between gap-3">
                    <div className="flex items-center space-x-3">
                      <div className="p-1.5 rounded bg-zinc-900 border border-zinc-800">
                        {getCategoryIcon(finding.category)}
                      </div>
                      <div>
                        <div className="flex items-center space-x-2">
                          <h4 className="text-xs font-semibold text-zinc-100">{finding.title}</h4>
                          {getSeverityBadge(finding.severity)}
                          <span className="px-1.5 py-0.5 rounded bg-zinc-900 text-zinc-400 border border-zinc-800 text-[10px] font-mono">
                            {finding.category}
                          </span>
                        </div>
                        <p className="text-[11px] font-mono text-emerald-400 mt-0.5 flex items-center space-x-1.5">
                          <FileCode className="h-3 w-3" />
                          <span>{finding.filename} : line {finding.lineNumber || 1}</span>
                        </p>
                      </div>
                    </div>

                    <button
                      onClick={() => setExpandedFindingId(expandedFindingId === finding.id ? null : finding.id)}
                      className="p-1.5 text-zinc-400 hover:text-zinc-100 rounded hover:bg-zinc-800 transition-colors flex items-center space-x-1 text-xs font-mono"
                    >
                      <span>{expandedFindingId === finding.id ? 'Hide Details' : 'View AI Fix'}</span>
                      {expandedFindingId === finding.id ? <ChevronUp className="h-3.5 w-3.5" /> : <ChevronDown className="h-3.5 w-3.5" />}
                    </button>
                  </div>

                  {/* Body Content */}
                  <div className="p-4 space-y-3">
                    <p className="text-xs text-zinc-300 leading-relaxed font-sans">{finding.description}</p>

                    {/* Target Code Snippet */}
                    {finding.codeSnippet && (
                      <div className="bg-zinc-950 p-2.5 rounded-md border border-zinc-800 font-mono text-xs text-rose-400 flex items-center space-x-2 overflow-x-auto">
                        <span className="text-zinc-600 font-bold select-none">&gt;</span>
                        <span>{finding.codeSnippet}</span>
                      </div>
                    )}

                    {/* Expandable AI Recommendation Box */}
                    {(expandedFindingId === finding.id || finding.category === 'BUG' || finding.severity === 'HIGH' || finding.severity === 'CRITICAL') && (
                      <div className="p-3 rounded-md bg-zinc-950 border border-zinc-800 text-xs space-y-1.5 font-mono">
                        <div className="flex items-center space-x-2 text-emerald-400 font-semibold">
                          <Sparkles className="h-3.5 w-3.5" />
                          <span>AI Recommendation & Suggested Pattern:</span>
                        </div>
                        <p className="text-zinc-300 leading-normal pl-5 text-[11px]">
                          {finding.recommendation}
                        </p>
                      </div>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>

        </div>
      )}

      {/* Ad-Hoc Real-time AI Code Review Sandbox */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-5 space-y-3.5 shadow-sm">
        <div className="flex items-center justify-between pb-2.5 border-b border-zinc-800">
          <div className="flex items-center space-x-2">
            <Terminal className="h-4 w-4 text-emerald-400" />
            <h3 className="text-xs font-semibold text-zinc-100 uppercase font-mono tracking-wider">Interactive AI Code Review Sandbox</h3>
          </div>
          <span className="text-[11px] text-zinc-500 font-mono">Instant Real-time Analysis</span>
        </div>

        <form onSubmit={handleRunSandboxReview} className="space-y-3">
          <div>
            <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">Target Filename</label>
            <input
              type="text"
              required
              value={sandboxFilename}
              onChange={(e) => setSandboxFilename(e.target.value)}
              className="w-full bg-zinc-950 border border-zinc-800 rounded-md py-1.5 px-3 text-xs text-zinc-100 font-mono focus:outline-none focus:border-emerald-500"
            />
          </div>

          <div>
            <label className="block text-[11px] font-mono text-zinc-400 uppercase tracking-wider mb-1">Source Code</label>
            <textarea
              rows={6}
              required
              value={sandboxSnippet}
              onChange={(e) => setSandboxSnippet(e.target.value)}
              className="w-full bg-zinc-950 border border-zinc-800 rounded-md p-3 text-xs text-emerald-300 font-mono focus:outline-none focus:border-emerald-500 leading-relaxed"
            />
          </div>

          <div className="flex justify-end">
            <button
              type="submit"
              disabled={sandboxAnalyzing}
              className="px-3.5 py-1.5 rounded-md bg-emerald-600 hover:bg-emerald-500 text-white font-medium text-xs shadow-sm flex items-center space-x-1.5 disabled:opacity-50 transition-colors"
            >
              {sandboxAnalyzing ? (
                <>
                  <RefreshCw className="h-3.5 w-3.5 animate-spin" />
                  <span>Analyzing Snippet...</span>
                </>
              ) : (
                <>
                  <Play className="h-3.5 w-3.5 fill-white" />
                  <span>Execute Instant AI Code Review</span>
                </>
              )}
            </button>
          </div>
        </form>

        {/* Sandbox Output Result */}
        {sandboxResult && (
          <div className="mt-5 p-4 rounded-md bg-zinc-950 border border-zinc-800 space-y-3 font-mono">
            <div className="flex items-center justify-between border-b border-zinc-800 pb-2.5">
              <div className="flex items-center space-x-2">
                <CheckCircle className="h-4 w-4 text-emerald-400" />
                <h4 className="text-xs font-semibold text-zinc-100">Sandbox AI Review Results</h4>
              </div>
              <span className="text-xs font-semibold text-emerald-400">
                Quality Score: {sandboxResult.qualityScore.toFixed(1)}%
              </span>
            </div>

            <p className="text-xs text-zinc-400 font-sans">{sandboxResult.summary}</p>

            <div className="space-y-2.5">
              {sandboxResult.findings?.map(f => (
                <div key={f.id || f.title} className="p-3 rounded-md bg-zinc-900 border border-zinc-800 text-xs space-y-1">
                  <div className="flex items-center justify-between">
                    <span className="font-semibold text-zinc-100">{f.title}</span>
                    {getSeverityBadge(f.severity)}
                  </div>
                  <p className="text-zinc-400 font-sans">{f.description}</p>
                  <p className="text-emerald-400 font-mono text-[11px] pt-1">Fix: {f.recommendation}</p>
                </div>
              ))}
            </div>
          </div>
        )}

      </div>

    </div>
  );
};
