import React, { useState, useEffect, useRef } from 'react';
import {
  Sparkles,
  Bot,
  User,
  Send,
  RefreshCw,
  FileCode2,
  CheckCircle2,
  Code2,
  Terminal,
  HelpCircle,
  Cpu,
  Layers,
  ExternalLink,
  ChevronRight,
  ChevronDown
} from 'lucide-react';
import { queryCodebase, reindexCodebase, getIndexStatus, getSuggestedQuestions } from '../services/ragService';

export const CodebaseAssistantView = ({ project }) => {
  const [messages, setMessages] = useState([
    {
      id: 1,
      sender: 'ai',
      text: `Hello! I am your RAG-based Codebase Assistant for **${project?.name || 'DevOpsAI'}**. Ask me anything about file locations, security configurations, algorithms, or API endpoints.`,
      sources: [],
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    }
  ]);
  const [inputValue, setInputValue] = useState('');
  const [loading, setLoading] = useState(false);
  const [indexStatus, setIndexStatus] = useState(null);
  const [indexing, setIndexing] = useState(false);
  const [suggestedQuestions, setSuggestedQuestions] = useState([]);
  const [expandedSources, setExpandedSources] = useState({});

  const chatEndRef = useRef(null);

  useEffect(() => {
    if (project?.id) {
      fetchStatusAndPrompts();
    }
  }, [project?.id]);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  const fetchStatusAndPrompts = async () => {
    try {
      const status = await getIndexStatus(project.id);
      setIndexStatus(status);
      const questions = await getSuggestedQuestions(project.id);
      setSuggestedQuestions(questions);
    } catch (err) {
      console.error("Failed to load RAG index status:", err);
    }
  };

  const handleReindex = async () => {
    setIndexing(true);
    try {
      const updatedStatus = await reindexCodebase(project.id);
      setIndexStatus(updatedStatus);
    } catch (err) {
      console.error("Failed to reindex codebase:", err);
    } finally {
      setIndexing(false);
    }
  };

  const handleSend = async (questionText = inputValue) => {
    const query = questionText.trim();
    if (!query || loading || !project?.id) return;

    const userMsg = {
      id: Date.now(),
      sender: 'user',
      text: query,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMsg]);
    setInputValue('');
    setLoading(true);

    try {
      const response = await queryCodebase(project.id, query);
      const aiMsg = {
        id: Date.now() + 1,
        sender: 'ai',
        text: response.answer,
        confidence: response.confidenceScore,
        sources: response.sources || [],
        followups: response.suggestedFollowups || [],
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, aiMsg]);
    } catch (err) {
      const errorMsg = {
        id: Date.now() + 1,
        sender: 'ai',
        text: "I encountered an error querying the vector codebase index. Please check backend connections.",
        sources: [],
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setLoading(false);
    }
  };

  const toggleSourceExpand = (msgId, sourceIdx) => {
    const key = `${msgId}-${sourceIdx}`;
    setExpandedSources((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  return (
    <div className="flex flex-col h-[calc(100vh-140px)] bg-zinc-900 text-zinc-100 rounded-lg border border-zinc-800 shadow-sm overflow-hidden font-sans">
      
      {/* Index Status Bar */}
      <div className="bg-zinc-950 px-5 py-3.5 border-b border-zinc-800 flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-zinc-900 border border-zinc-800 rounded-md text-emerald-400">
            <Cpu className="w-4 h-4" />
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <h2 className="font-semibold text-zinc-100 text-sm">Codebase AI RAG Assistant</h2>
              <span className="px-2 py-0.5 text-[10px] font-mono font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded">
                Index Active
              </span>
            </div>
            <p className="text-[11px] text-zinc-400">
              Retrieval-Augmented Vector Search over source code, commits, and pull requests
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-3 font-mono">
          {indexStatus && (
            <div className="hidden sm:flex items-center space-x-3 text-xs text-zinc-400 bg-zinc-900 px-3 py-1 rounded-md border border-zinc-800">
              <span className="flex items-center space-x-1">
                <Layers className="w-3.5 h-3.5 text-emerald-400" />
                <span><strong className="text-zinc-200">{indexStatus.totalChunks}</strong> Chunks</span>
              </span>
              <span className="text-zinc-700">|</span>
              <span className="flex items-center space-x-1">
                <FileCode2 className="w-3.5 h-3.5 text-cyan-400" />
                <span><strong className="text-zinc-200">{indexStatus.indexedFilesCount}</strong> Files</span>
              </span>
            </div>
          )}

          <button
            onClick={handleReindex}
            disabled={indexing}
            className="flex items-center space-x-1.5 px-3 py-1 bg-zinc-800 hover:bg-zinc-700 text-zinc-200 border border-zinc-700 rounded-md text-xs font-mono font-medium transition-colors disabled:opacity-50 cursor-pointer"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${indexing ? 'animate-spin' : ''}`} />
            <span>{indexing ? 'Re-indexing...' : 'Re-index Codebase'}</span>
          </button>
        </div>
      </div>

      {/* Main Chat Stream */}
      <div className="flex-1 overflow-y-auto p-5 space-y-4">
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`flex ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}
          >
            <div className={`flex items-start space-x-2.5 max-w-3xl ${msg.sender === 'user' ? 'flex-row-reverse space-x-reverse' : ''}`}>
              {/* Avatar */}
              <div className={`w-7 h-7 rounded-md flex items-center justify-center text-xs font-mono font-semibold shrink-0 border ${
                msg.sender === 'user'
                  ? 'bg-zinc-800 text-zinc-200 border-zinc-700'
                  : 'bg-zinc-950 text-emerald-400 border-zinc-800'
              }`}>
                {msg.sender === 'user' ? <User className="w-3.5 h-3.5" /> : <Bot className="w-3.5 h-3.5" />}
              </div>

              {/* Message Content */}
              <div className={`flex flex-col space-y-2 ${msg.sender === 'user' ? 'items-end' : 'items-start'}`}>
                <div className={`px-3.5 py-2.5 rounded-md text-xs leading-relaxed ${
                  msg.sender === 'user'
                    ? 'bg-zinc-800 text-zinc-100 border border-zinc-700'
                    : 'bg-zinc-950 border border-zinc-800 text-zinc-200 shadow-sm'
                }`}>
                  <p className="whitespace-pre-wrap">{msg.text}</p>
                  <span className="block mt-1.5 text-[9px] font-mono text-zinc-500 text-right">
                    {msg.timestamp}
                  </span>
                </div>

                {/* Sources Section */}
                {msg.sources && msg.sources.length > 0 && (
                  <div className="w-full bg-zinc-950 border border-zinc-800 rounded-md p-3 space-y-2 font-mono text-xs">
                    <div className="flex items-center justify-between text-xs font-semibold text-emerald-400">
                      <span className="flex items-center space-x-1.5">
                        <FileCode2 className="w-3.5 h-3.5 text-emerald-400" />
                        <span>Cited Codebase Sources ({msg.sources.length})</span>
                      </span>
                      {msg.confidence && (
                        <span className="text-[10px] px-1.5 py-0.5 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded">
                          {(msg.confidence * 100).toFixed(0)}% Similarity Match
                        </span>
                      )}
                    </div>

                    <div className="space-y-1.5">
                      {msg.sources.map((src, idx) => {
                        const isExpanded = expandedSources[`${msg.id}-${idx}`];
                        return (
                          <div key={idx} className="bg-zinc-900 border border-zinc-800 rounded overflow-hidden text-xs">
                            <button
                              onClick={() => toggleSourceExpand(msg.id, idx)}
                              className="w-full px-2.5 py-1.5 flex items-center justify-between hover:bg-zinc-800 transition-colors text-left"
                            >
                              <div className="flex items-center space-x-2 truncate">
                                <Code2 className="w-3.5 h-3.5 text-cyan-400 shrink-0" />
                                <span className="font-mono text-zinc-200 font-semibold truncate">{src.filename}</span>
                                <span className="text-zinc-500 text-[10px]">L{src.startLine}-L{src.endLine}</span>
                              </div>
                              <div className="flex items-center space-x-2 shrink-0">
                                <span className="px-1.5 py-0.5 text-[9px] bg-zinc-950 text-zinc-400 border border-zinc-800 rounded font-mono">
                                  {(src.relevanceScore * 100).toFixed(0)}% Match
                                </span>
                                {isExpanded ? <ChevronDown className="w-3.5 h-3.5 text-zinc-400" /> : <ChevronRight className="w-3.5 h-3.5 text-zinc-400" />}
                              </div>
                            </button>

                            {isExpanded && (
                              <div className="p-2.5 bg-zinc-950 border-t border-zinc-800 font-mono text-[11px] text-zinc-300 overflow-x-auto">
                                <pre className="whitespace-pre-wrap">{src.snippet}</pre>
                              </div>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  </div>
                )}

                {/* Followups */}
                {msg.followups && msg.followups.length > 0 && (
                  <div className="flex flex-wrap gap-1.5 pt-1 font-mono">
                    {msg.followups.map((f, i) => (
                      <button
                        key={i}
                        onClick={() => handleSend(f)}
                        className="text-[11px] bg-zinc-950 hover:bg-zinc-800 text-zinc-300 border border-zinc-800 hover:border-zinc-700 px-2.5 py-0.5 rounded transition-colors text-left cursor-pointer"
                      >
                        + {f}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        ))}

        {loading && (
          <div className="flex items-center space-x-2 text-zinc-400 font-mono text-xs py-2">
            <div className="w-6 h-6 rounded bg-zinc-950 border border-zinc-800 flex items-center justify-center text-emerald-400">
              <Bot className="w-3.5 h-3.5 animate-spin" />
            </div>
            <span>Searching vector embeddings & synthesizing answer...</span>
          </div>
        )}

        <div ref={chatEndRef} />
      </div>

      {/* Suggested Prompt Chips */}
      {suggestedQuestions.length > 0 && (
        <div className="px-4 py-2 bg-zinc-950 border-t border-zinc-800 flex items-center space-x-2 overflow-x-auto font-mono">
          <Sparkles className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
          <span className="text-[10px] text-zinc-500 uppercase shrink-0">Quick Prompts:</span>
          {suggestedQuestions.map((q, idx) => (
            <button
              key={idx}
              onClick={() => handleSend(q)}
              className="text-[11px] font-normal whitespace-nowrap bg-zinc-900 hover:bg-zinc-800 text-zinc-300 border border-zinc-800 px-2.5 py-0.5 rounded transition-colors cursor-pointer shrink-0"
            >
              {q}
            </button>
          ))}
        </div>
      )}

      {/* Input Bar */}
      <div className="p-3 bg-zinc-950 border-t border-zinc-800 flex items-center space-x-2">
        <input
          type="text"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSend()}
          placeholder="Ask a question about code architecture, algorithms, or API routes..."
          className="flex-1 bg-zinc-900 border border-zinc-800 rounded-md px-3 py-2 text-xs text-zinc-100 placeholder-zinc-500 focus:outline-none focus:border-emerald-500 font-sans transition-colors"
        />
        <button
          onClick={() => handleSend()}
          disabled={!inputValue.trim() || loading}
          className="px-3.5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-md font-medium text-xs flex items-center space-x-1.5 transition-colors disabled:opacity-50 cursor-pointer shadow-sm"
        >
          <span>Ask</span>
          <Send className="w-3.5 h-3.5" />
        </button>
      </div>
    </div>
  );
};
