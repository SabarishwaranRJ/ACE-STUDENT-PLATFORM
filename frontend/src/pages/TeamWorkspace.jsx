import React, { useState, useEffect, useRef } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { workspaceApi } from '../api/workspace';
import { createStompClient } from '../utils/websocket';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import Modal from '../components/common/Modal';
import {
  LayoutDashboard,
  MessageSquare,
  MessageCircle,
  CheckSquare,
  FolderOpen,
  Megaphone,
  Video,
  Users,
  Send,
  Plus,
  Trash2,
  Paperclip,
  Pin,
  Clock,
  Calendar,
  ExternalLink,
  Download,
  AlertCircle,
  FileText,
  Code,
  Palette,
  File,
  Check,
  ChevronRight,
  ShieldCheck,
  User,
  ArrowLeft
} from 'lucide-react';

const TABS = [
  { id: 'overview', name: 'Overview', icon: LayoutDashboard },
  { id: 'chat', name: 'Group Chat', icon: MessageSquare },
  { id: 'dm', name: 'Direct Messages', icon: MessageCircle },
  { id: 'tasks', name: 'Tasks (Kanban)', icon: CheckSquare },
  { id: 'files', name: 'Shared Files', icon: FolderOpen },
  { id: 'announcements', name: 'Announcements', icon: Megaphone },
  { id: 'meetings', name: 'Meetings', icon: Video },
  { id: 'members', name: 'Team Members', icon: Users },
];

const TASK_STATUSES = [
  { key: 'TODO', label: 'To Do', color: 'border-slate-400 bg-slate-500/10 text-slate-500' },
  { key: 'IN_PROGRESS', label: 'In Progress', color: 'border-blue-500 bg-blue-500/10 text-blue-500' },
  { key: 'IN_REVIEW', label: 'In Review', color: 'border-amber-500 bg-amber-500/10 text-amber-500' },
  { key: 'COMPLETED', label: 'Completed', color: 'border-emerald-500 bg-emerald-500/10 text-emerald-500' },
];

const PRIORITY_BADGES = {
  LOW: 'bg-slate-500/15 text-slate-500 border-slate-500/30',
  MEDIUM: 'bg-blue-500/15 text-blue-500 border-blue-500/30',
  HIGH: 'bg-amber-500/15 text-amber-500 border-amber-500/30',
  URGENT: 'bg-red-500/15 text-red-500 border-red-500/30 animate-pulse',
};

const FILE_CATEGORIES = ['All', 'Documents', 'Code', 'Design', 'General'];

const TeamWorkspace = () => {
  const { teamId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  // Active section tab
  const [activeTab, setActiveTab] = useState('overview');

  // Overview data
  const [overview, setOverview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // WebSocket STOMP client
  const stompClientRef = useRef(null);

  // Group Chat State
  const [messages, setMessages] = useState([]);
  const [chatInput, setChatInput] = useState('');
  const [replyTo, setReplyTo] = useState(null);
  const [typingUsers, setTypingUsers] = useState({});
  const messagesEndRef = useRef(null);
  const typingTimeoutRef = useRef(null);

  // Direct Messaging State
  const [conversations, setConversations] = useState([]);
  const [selectedConversation, setSelectedConversation] = useState(null);
  const [dmList, setDmList] = useState([]);
  const [dmInput, setDmInput] = useState('');
  const dmEndRef = useRef(null);

  // Tasks (Kanban) State
  const [tasks, setTasks] = useState([]);
  const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);
  const [taskForm, setTaskForm] = useState({
    title: '',
    description: '',
    priority: 'MEDIUM',
    status: 'TODO',
    dueDate: '',
    assignedToId: '',
  });

  // Shared Files State
  const [files, setFiles] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
  const [uploadCategory, setUploadCategory] = useState('Documents');
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef(null);

  // Announcements State
  const [announcements, setAnnouncements] = useState([]);
  const [isAnnouncementModalOpen, setIsAnnouncementModalOpen] = useState(false);
  const [announcementForm, setAnnouncementForm] = useState({
    title: '',
    content: '',
    isPinned: false,
  });

  // Meetings State
  const [meetings, setMeetings] = useState([]);
  const [isMeetingModalOpen, setIsMeetingModalOpen] = useState(false);
  const [meetingForm, setMeetingForm] = useState({
    title: '',
    description: '',
    meetingLink: '',
    startTime: '',
    endTime: '',
  });

  // Load Initial Workspace Data
  const loadWorkspace = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await workspaceApi.getWorkspace(teamId);
      if (res.success && res.data) {
        setOverview(res.data);
      }
    } catch (err) {
      setError(err.message || 'Failed to access workspace. Are you an active team member?');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadWorkspace();
  }, [teamId]);

  // Connect STOMP WebSocket for Team Channel
  useEffect(() => {
    if (!teamId) return;

    const client = createStompClient(
      (stomp) => {
        // Subscribe to Group Chat Messages
        stomp.subscribe(`/topic/team/${teamId}/messages`, (msg) => {
          try {
            const data = JSON.parse(msg.body);
            if (data.messageType === 'DELETE') {
              setMessages((prev) => prev.filter((m) => m.messageId !== data.messageId));
            } else {
              setMessages((prev) => {
                if (prev.some((m) => m.messageId === data.messageId)) return prev;
                return [...prev, data];
              });
            }
          } catch (e) {
            console.error('Failed to parse STOMP chat message', e);
          }
        });

        // Subscribe to Group Chat Typing
        stomp.subscribe(`/topic/team/${teamId}/typing`, (msg) => {
          try {
            const data = JSON.parse(msg.body);
            if (data.studentId !== user?.studentId) {
              setTypingUsers((prev) => ({
                ...prev,
                [data.studentName]: data.isTyping,
              }));
            }
          } catch (e) {
            console.error('Failed to parse typing event', e);
          }
        });

        // Subscribe to Task Updates
        stomp.subscribe(`/topic/team/${teamId}/tasks`, (msg) => {
          try {
            const data = JSON.parse(msg.body);
            if (data.title === 'DELETED') {
              setTasks((prev) => prev.filter((t) => t.taskId !== data.taskId));
            } else {
              setTasks((prev) => {
                const idx = prev.findIndex((t) => t.taskId === data.taskId);
                if (idx >= 0) {
                  const updated = [...prev];
                  updated[idx] = data;
                  return updated;
                }
                return [data, ...prev];
              });
            }
          } catch (e) {
            console.error('Failed to parse task socket update', e);
          }
        });

        // Subscribe to File Updates
        stomp.subscribe(`/topic/team/${teamId}/files`, (msg) => {
          try {
            const data = JSON.parse(msg.body);
            if (data.fileName === 'DELETED') {
              setFiles((prev) => prev.filter((f) => f.fileId !== data.fileId));
            } else {
              setFiles((prev) => [data, ...prev.filter((f) => f.fileId !== data.fileId)]);
            }
          } catch (e) {
            console.error('Failed to parse file socket update', e);
          }
        });

        // Subscribe to Announcement Updates
        stomp.subscribe(`/topic/team/${teamId}/announcements`, (msg) => {
          try {
            const data = JSON.parse(msg.body);
            if (data.title === 'DELETED') {
              setAnnouncements((prev) => prev.filter((a) => a.announcementId !== data.announcementId));
            } else {
              setAnnouncements((prev) => {
                const filtered = prev.filter((a) => a.announcementId !== data.announcementId);
                return [data, ...filtered];
              });
            }
          } catch (e) {
            console.error('Failed to parse announcement socket update', e);
          }
        });

        // Subscribe to Meeting Updates
        stomp.subscribe(`/topic/team/${teamId}/meetings`, (msg) => {
          try {
            const data = JSON.parse(msg.body);
            if (data.title === 'CANCELLED') {
              setMeetings((prev) => prev.filter((m) => m.meetingId !== data.meetingId));
            } else {
              setMeetings((prev) => [data, ...prev.filter((m) => m.meetingId !== data.meetingId)]);
            }
          } catch (e) {
            console.error('Failed to parse meeting socket update', e);
          }
        });
      },
      (err) => console.warn('Workspace STOMP Error:', err)
    );

    client.activate();
    stompClientRef.current = client;

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
    };
  }, [teamId, user?.studentId]);

  // Load Section Specific Data
  useEffect(() => {
    if (activeTab === 'chat') {
      workspaceApi.getChatMessages(teamId).then((res) => {
        if (res.success) setMessages(res.data || []);
      });
    } else if (activeTab === 'tasks') {
      workspaceApi.getTasks(teamId).then((res) => {
        if (res.success) setTasks(res.data || []);
      });
    } else if (activeTab === 'files') {
      workspaceApi.getFiles(teamId, selectedCategory === 'All' ? null : selectedCategory).then((res) => {
        if (res.success) setFiles(res.data || []);
      });
    } else if (activeTab === 'announcements') {
      workspaceApi.getAnnouncements(teamId).then((res) => {
        if (res.success) setAnnouncements(res.data || []);
      });
    } else if (activeTab === 'meetings') {
      workspaceApi.getMeetings(teamId).then((res) => {
        if (res.success) setMeetings(res.data || []);
      });
    } else if (activeTab === 'dm') {
      loadConversations();
    }
  }, [activeTab, teamId, selectedCategory]);

  // Auto-scroll chat
  useEffect(() => {
    if (activeTab === 'chat') {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, activeTab]);

  // Auto-scroll DM
  useEffect(() => {
    if (activeTab === 'dm' && selectedConversation) {
      dmEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [dmList, activeTab, selectedConversation]);

  // ---------------------------------------------------------------------------
  // Chat Handlers
  // ---------------------------------------------------------------------------
  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!chatInput.trim()) return;

    const payload = {
      messageContent: chatInput.trim(),
      messageType: 'TEXT',
      replyToMessageId: replyTo ? replyTo.messageId : null,
    };

    try {
      if (stompClientRef.current && stompClientRef.current.connected) {
        stompClientRef.current.publish({
          destination: `/app/chat.send/${teamId}`,
          body: JSON.stringify(payload),
        });
      } else {
        await workspaceApi.sendMessage(teamId, payload);
      }
      setChatInput('');
      setReplyTo(null);

      // Stop typing
      if (stompClientRef.current && stompClientRef.current.connected) {
        stompClientRef.current.publish({
          destination: `/app/chat.typing/${teamId}`,
          body: JSON.stringify(false),
        });
      }
    } catch (err) {
      alert(err.message || 'Failed to send message');
    }
  };

  const handleChatInputChange = (e) => {
    setChatInput(e.target.value);
    if (stompClientRef.current && stompClientRef.current.connected) {
      stompClientRef.current.publish({
        destination: `/app/chat.typing/${teamId}`,
        body: JSON.stringify(true),
      });

      if (typingTimeoutRef.current) clearTimeout(typingTimeoutRef.current);
      typingTimeoutRef.current = setTimeout(() => {
        if (stompClientRef.current && stompClientRef.current.connected) {
          stompClientRef.current.publish({
            destination: `/app/chat.typing/${teamId}`,
            body: JSON.stringify(false),
          });
        }
      }, 2000);
    }
  };

  const handleDeleteMessage = async (messageId) => {
    if (!window.confirm('Delete this message?')) return;
    try {
      await workspaceApi.deleteMessage(teamId, messageId);
      setMessages((prev) => prev.filter((m) => m.messageId !== messageId));
    } catch (err) {
      alert(err.message || 'Failed to delete message');
    }
  };

  // ---------------------------------------------------------------------------
  // Direct Messages Handlers
  // ---------------------------------------------------------------------------
  const loadConversations = async () => {
    try {
      const res = await workspaceApi.getConversations();
      if (res.success) {
        setConversations(res.data || []);
      }
    } catch (err) {
      console.warn('Failed to load conversations', err);
    }
  };

  const startConversationWith = async (studentId) => {
    try {
      const res = await workspaceApi.getOrCreateConversation(studentId);
      if (res.success && res.data) {
        setActiveTab('dm');
        setSelectedConversation(res.data);
        loadDmMessages(res.data.conversationId);
        loadConversations();
      }
    } catch (err) {
      alert(err.message || 'Failed to start conversation');
    }
  };

  const loadDmMessages = async (conversationId) => {
    try {
      const res = await workspaceApi.getConversationMessages(conversationId);
      if (res.success) {
        setDmList(res.data || []);
      }
    } catch (err) {
      console.warn('Failed to load DM messages', err);
    }
  };

  const handleSendDm = async (e) => {
    e.preventDefault();
    if (!dmInput.trim() || !selectedConversation) return;

    const payload = { messageContent: dmInput.trim() };
    try {
      const res = await workspaceApi.sendDirectMessage(selectedConversation.conversationId, payload);
      if (res.success && res.data) {
        setDmList((prev) => [...prev, res.data]);
        setDmInput('');
        loadConversations();
      }
    } catch (err) {
      alert(err.message || 'Failed to send message');
    }
  };

  // ---------------------------------------------------------------------------
  // Task Handlers
  // ---------------------------------------------------------------------------
  const handleCreateTask = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        title: taskForm.title.trim(),
        description: taskForm.description,
        priority: taskForm.priority,
        status: taskForm.status,
        dueDate: taskForm.dueDate || null,
        assignedToId: taskForm.assignedToId ? Number(taskForm.assignedToId) : null,
      };
      const res = await workspaceApi.createTask(teamId, payload);
      if (res.success && res.data) {
        setTasks((prev) => [res.data, ...prev]);
        setIsTaskModalOpen(false);
        setTaskForm({
          title: '',
          description: '',
          priority: 'MEDIUM',
          status: 'TODO',
          dueDate: '',
          assignedToId: '',
        });
        loadWorkspace();
      }
    } catch (err) {
      alert(err.message || 'Failed to create task');
    }
  };

  const handleUpdateTaskStatus = async (taskId, newStatus) => {
    try {
      const res = await workspaceApi.updateTaskStatus(teamId, taskId, newStatus);
      if (res.success && res.data) {
        setTasks((prev) => prev.map((t) => (t.taskId === taskId ? res.data : t)));
        loadWorkspace();
      }
    } catch (err) {
      alert(err.message || 'Failed to update status');
    }
  };

  const handleDeleteTask = async (taskId) => {
    if (!window.confirm('Delete this task?')) return;
    try {
      await workspaceApi.deleteTask(teamId, taskId);
      setTasks((prev) => prev.filter((t) => t.taskId !== taskId));
      loadWorkspace();
    } catch (err) {
      alert(err.message || 'Failed to delete task');
    }
  };

  // ---------------------------------------------------------------------------
  // File Handlers
  // ---------------------------------------------------------------------------
  const handleFileUpload = async (e) => {
    e.preventDefault();
    const file = fileInputRef.current?.files?.[0];
    if (!file) {
      alert('Please select a file');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);
    formData.append('category', uploadCategory);

    setUploading(true);
    try {
      const res = await workspaceApi.uploadFile(teamId, formData);
      if (res.success && res.data) {
        setFiles((prev) => [res.data, ...prev]);
        setIsUploadModalOpen(false);
        if (fileInputRef.current) fileInputRef.current.value = '';
        loadWorkspace();
      }
    } catch (err) {
      alert(err.message || 'Failed to upload file');
    } finally {
      setUploading(false);
    }
  };

  const handleDeleteFile = async (fileId) => {
    if (!window.confirm('Delete this file?')) return;
    try {
      await workspaceApi.deleteFile(teamId, fileId);
      setFiles((prev) => prev.filter((f) => f.fileId !== fileId));
      loadWorkspace();
    } catch (err) {
      alert(err.message || 'Failed to delete file');
    }
  };

  // ---------------------------------------------------------------------------
  // Announcement Handlers
  // ---------------------------------------------------------------------------
  const handleCreateAnnouncement = async (e) => {
    e.preventDefault();
    try {
      const res = await workspaceApi.createAnnouncement(teamId, announcementForm);
      if (res.success && res.data) {
        setAnnouncements((prev) => [res.data, ...prev]);
        setIsAnnouncementModalOpen(false);
        setAnnouncementForm({ title: '', content: '', isPinned: false });
        loadWorkspace();
      }
    } catch (err) {
      alert(err.message || 'Failed to publish announcement');
    }
  };

  const handleTogglePinAnnouncement = async (announcementId) => {
    try {
      const res = await workspaceApi.togglePinAnnouncement(teamId, announcementId);
      if (res.success && res.data) {
        setAnnouncements((prev) =>
          prev.map((a) => (a.announcementId === announcementId ? res.data : a))
        );
      }
    } catch (err) {
      alert(err.message || 'Failed to update pin');
    }
  };

  const handleDeleteAnnouncement = async (announcementId) => {
    if (!window.confirm('Delete this announcement?')) return;
    try {
      await workspaceApi.deleteAnnouncement(teamId, announcementId);
      setAnnouncements((prev) => prev.filter((a) => a.announcementId !== announcementId));
      loadWorkspace();
    } catch (err) {
      alert(err.message || 'Failed to delete announcement');
    }
  };

  // ---------------------------------------------------------------------------
  // Meeting Handlers
  // ---------------------------------------------------------------------------
  const handleScheduleMeeting = async (e) => {
    e.preventDefault();
    try {
      const res = await workspaceApi.scheduleMeeting(teamId, meetingForm);
      if (res.success && res.data) {
        setMeetings((prev) => [res.data, ...prev]);
        setIsMeetingModalOpen(false);
        setMeetingForm({ title: '', description: '', meetingLink: '', startTime: '', endTime: '' });
        loadWorkspace();
      }
    } catch (err) {
      alert(err.message || 'Failed to schedule meeting');
    }
  };

  const handleCancelMeeting = async (meetingId) => {
    if (!window.confirm('Cancel this meeting?')) return;
    try {
      await workspaceApi.cancelMeeting(teamId, meetingId);
      setMeetings((prev) => prev.filter((m) => m.meetingId !== meetingId));
      loadWorkspace();
    } catch (err) {
      alert(err.message || 'Failed to cancel meeting');
    }
  };

  if (loading) return <LoadingSpinner text="Connecting to team workspace..." />;
  if (error) {
    return (
      <div className="max-w-2xl mx-auto mt-12 bg-card rounded-[var(--radius)] border border-destructive/30 p-8 text-center space-y-4">
        <AlertCircle className="w-12 h-12 text-destructive mx-auto" />
        <h2 className="text-xl font-black text-foreground">Workspace Access Restricted</h2>
        <p className="text-sm text-muted-foreground leading-relaxed">{error}</p>
        <Link
          to="/teams"
          className="inline-flex items-center space-x-2 px-4 py-2 bg-primary text-primary-foreground font-bold rounded-[calc(var(--radius)-0.125rem)] text-xs"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to My Teams</span>
        </Link>
      </div>
    );
  }

  const team = overview?.team;
  const members = overview?.members || [];
  const taskSummary = overview?.taskSummary || { totalTasks: 0, completionPercentage: 0 };
  const typingList = Object.keys(typingUsers).filter((name) => typingUsers[name]);

  return (
    <div className="space-y-6">
      {/* Top Workspace Header */}
      <div className="bg-card rounded-[var(--radius)] border border-border p-6 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2 text-xs mb-1.5">
            <Link to="/teams" className="text-muted-foreground hover:text-primary font-semibold flex items-center gap-1">
              <ArrowLeft className="w-3.5 h-3.5" />
              Teams
            </Link>
            <span className="text-muted-foreground">•</span>
            <span className="font-bold text-primary">{team?.event?.eventName || 'Hackathon Event'}</span>
            <span className="text-muted-foreground">•</span>
            <span className="px-2 py-0.5 rounded-full text-[10px] font-bold border border-primary/30 bg-primary/10 text-primary">
              {team?.teamStatus}
            </span>
          </div>

          <div className="flex items-center space-x-3">
            <h1 className="text-2xl sm:text-3xl font-black text-foreground tracking-tight">{team?.teamName}</h1>
            <span className="text-xs font-semibold px-2.5 py-1 rounded-md bg-secondary text-secondary-foreground">
              {overview?.currentUserRole}
            </span>
          </div>
          <p className="text-xs text-muted-foreground mt-1 line-clamp-1 max-w-2xl">{team?.description}</p>
        </div>

        <div className="flex items-center space-x-2">
          <Link
            to={`/teams/${teamId}`}
            className="px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold text-foreground hover:bg-muted transition"
          >
            Roster & Skills
          </Link>
          <Link
            to={`/builder?teamId=${teamId}&eventId=${team?.event?.eventId}`}
            className="px-3.5 py-2 bg-primary text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold hover:opacity-90 transition shadow-xs"
          >
            AI Match Suggestions
          </Link>
        </div>
      </div>

      {/* Main Workspace Grid (Left Sub-nav + Content + Right Quick Panel) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        
        {/* LEFT SUB-NAVIGATION (8 Sections) */}
        <div className="lg:col-span-3">
          <div className="bg-card rounded-[var(--radius)] border border-border p-3 shadow-xs space-y-1 sticky top-20">
            <div className="px-3 py-2 text-[10px] font-bold uppercase tracking-wider text-muted-foreground">
              Workspace Sections
            </div>
            {TABS.map((tab) => {
              const Icon = tab.icon;
              const isActive = activeTab === tab.id;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-[calc(var(--radius)-0.125rem)] text-xs font-semibold transition ${
                    isActive
                      ? 'bg-primary text-primary-foreground font-bold shadow-xs'
                      : 'text-muted-foreground hover:text-foreground hover:bg-muted'
                  }`}
                >
                  <div className="flex items-center space-x-2.5">
                    <Icon className="w-4 h-4 flex-shrink-0" />
                    <span>{tab.name}</span>
                  </div>
                  {tab.id === 'tasks' && taskSummary.totalTasks > 0 && (
                    <span className={`text-[10px] px-1.5 py-0.5 rounded-full ${isActive ? 'bg-white/20 text-white' : 'bg-muted text-muted-foreground'}`}>
                      {taskSummary.totalTasks}
                    </span>
                  )}
                  {tab.id === 'files' && overview?.recentFiles?.length > 0 && (
                    <span className={`text-[10px] px-1.5 py-0.5 rounded-full ${isActive ? 'bg-white/20 text-white' : 'bg-muted text-muted-foreground'}`}>
                      {overview.recentFiles.length}
                    </span>
                  )}
                </button>
              );
            })}
          </div>
        </div>

        {/* CENTER MAIN CONTENT AREA */}
        <div className="lg:col-span-6 space-y-6">

          {/* 1. OVERVIEW SECTION */}
          {activeTab === 'overview' && (
            <div className="space-y-6">
              {/* Task Progress Bar Card */}
              <div className="bg-card rounded-[var(--radius)] border border-border p-6 shadow-xs">
                <div className="flex items-center justify-between mb-3">
                  <h3 className="text-sm font-bold text-foreground uppercase tracking-wider flex items-center gap-2">
                    <CheckSquare className="w-4 h-4 text-primary" />
                    Project Task Progress
                  </h3>
                  <span className="text-sm font-black text-primary">{taskSummary.completionPercentage}%</span>
                </div>
                <div className="w-full bg-input rounded-full h-3 overflow-hidden border border-border">
                  <div
                    className="bg-primary h-full transition-all duration-500 rounded-full"
                    style={{ width: `${taskSummary.completionPercentage}%` }}
                  />
                </div>
                <div className="grid grid-cols-4 gap-2 mt-4 text-center">
                  <div className="p-2 bg-muted/60 rounded-md">
                    <div className="text-base font-black text-foreground">{taskSummary.todoCount}</div>
                    <div className="text-[10px] font-semibold text-muted-foreground uppercase">To Do</div>
                  </div>
                  <div className="p-2 bg-blue-500/10 rounded-md text-blue-500">
                    <div className="text-base font-black">{taskSummary.inProgressCount}</div>
                    <div className="text-[10px] font-semibold uppercase">In Progress</div>
                  </div>
                  <div className="p-2 bg-amber-500/10 rounded-md text-amber-500">
                    <div className="text-base font-black">{taskSummary.inReviewCount}</div>
                    <div className="text-[10px] font-semibold uppercase">Review</div>
                  </div>
                  <div className="p-2 bg-emerald-500/10 rounded-md text-emerald-500">
                    <div className="text-base font-black">{taskSummary.completedCount}</div>
                    <div className="text-[10px] font-semibold uppercase">Done</div>
                  </div>
                </div>
              </div>

              {/* Quick Actions */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                <button
                  onClick={() => setActiveTab('chat')}
                  className="p-4 bg-card border border-border rounded-[var(--radius)] text-left hover:border-primary/50 transition group"
                >
                  <MessageSquare className="w-5 h-5 text-primary mb-2 group-hover:scale-110 transition" />
                  <div className="text-xs font-bold text-foreground">Group Chat</div>
                  <div className="text-[10px] text-muted-foreground">Live messaging</div>
                </button>
                <button
                  onClick={() => {
                    setActiveTab('tasks');
                    setIsTaskModalOpen(true);
                  }}
                  className="p-4 bg-card border border-border rounded-[var(--radius)] text-left hover:border-primary/50 transition group"
                >
                  <CheckSquare className="w-5 h-5 text-emerald-500 mb-2 group-hover:scale-110 transition" />
                  <div className="text-xs font-bold text-foreground">Add Task</div>
                  <div className="text-[10px] text-muted-foreground">Kanban assign</div>
                </button>
                <button
                  onClick={() => {
                    setActiveTab('files');
                    setIsUploadModalOpen(true);
                  }}
                  className="p-4 bg-card border border-border rounded-[var(--radius)] text-left hover:border-primary/50 transition group"
                >
                  <FolderOpen className="w-5 h-5 text-blue-500 mb-2 group-hover:scale-110 transition" />
                  <div className="text-xs font-bold text-foreground">Share File</div>
                  <div className="text-[10px] text-muted-foreground">Upload code/doc</div>
                </button>
                <button
                  onClick={() => {
                    setActiveTab('meetings');
                    setIsMeetingModalOpen(true);
                  }}
                  className="p-4 bg-card border border-border rounded-[var(--radius)] text-left hover:border-primary/50 transition group"
                >
                  <Video className="w-5 h-5 text-purple-500 mb-2 group-hover:scale-110 transition" />
                  <div className="text-xs font-bold text-foreground">Meet Call</div>
                  <div className="text-[10px] text-muted-foreground">Google/Zoom link</div>
                </button>
              </div>

              {/* Latest Announcements */}
              <div className="bg-card rounded-[var(--radius)] border border-border p-6 shadow-xs">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-sm font-bold text-foreground uppercase tracking-wider flex items-center gap-2">
                    <Megaphone className="w-4 h-4 text-primary" />
                    Latest Announcements
                  </h3>
                  <button
                    onClick={() => setActiveTab('announcements')}
                    className="text-xs font-bold text-primary hover:underline"
                  >
                    View All
                  </button>
                </div>
                {overview?.recentAnnouncements?.length > 0 ? (
                  <div className="space-y-3">
                    {overview.recentAnnouncements.map((a) => (
                      <div key={a.announcementId} className="p-3.5 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-xs">
                        <div className="flex items-center justify-between mb-1">
                          <span className="font-bold text-foreground flex items-center gap-1.5">
                            {a.isPinned && <Pin className="w-3.5 h-3.5 text-primary fill-primary" />}
                            {a.title}
                          </span>
                          <span className="text-[10px] text-muted-foreground">
                            {new Date(a.createdAt).toLocaleDateString()}
                          </span>
                        </div>
                        <p className="text-muted-foreground leading-relaxed line-clamp-2">{a.content}</p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-xs text-muted-foreground italic">No announcements published yet.</p>
                )}
              </div>
            </div>
          )}

          {/* 2. GROUP CHAT SECTION */}
          {activeTab === 'chat' && (
            <div className="bg-card rounded-[var(--radius)] border border-border flex flex-col h-[650px] shadow-xs overflow-hidden">
              <div className="p-4 border-b border-border bg-card flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-bold text-foreground">Team Group Chat</h3>
                  <p className="text-[11px] text-muted-foreground">Private encrypted channel for {team?.teamName}</p>
                </div>
                <div className="flex items-center space-x-1.5 text-xs text-muted-foreground font-medium">
                  <span className="w-2 h-2 rounded-full bg-emerald-500" />
                  <span>STOMP Real-Time Active</span>
                </div>
              </div>

              {/* Chat Message Stream */}
              <div className="flex-1 p-4 overflow-y-auto space-y-3.5">
                {messages.length === 0 ? (
                  <div className="h-full flex flex-col items-center justify-center text-muted-foreground space-y-2">
                    <MessageSquare className="w-10 h-10 opacity-30" />
                    <p className="text-xs">No messages yet. Send a greeting to start collaborating!</p>
                  </div>
                ) : (
                  messages.map((msg) => {
                    const isOwn = msg.senderId === user?.studentId;
                    return (
                      <div key={msg.messageId} className={`flex flex-col ${isOwn ? 'items-end' : 'items-start'}`}>
                        <div className="flex items-baseline space-x-2 text-[11px] text-muted-foreground mb-1">
                          <span className="font-bold text-foreground">{isOwn ? 'You' : msg.senderName}</span>
                          <span>{new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                        </div>
                        <div
                          className={`max-w-[80%] rounded-[var(--radius)] p-3 text-xs leading-relaxed shadow-xs relative group ${
                            isOwn
                              ? 'bg-primary text-primary-foreground rounded-br-none'
                              : 'bg-input text-foreground border border-border rounded-bl-none'
                          }`}
                        >
                          {msg.replyToMessageId && (
                            <div className="mb-2 p-1.5 rounded bg-black/10 text-[10px] border-l-2 border-white/50">
                              Replying to message #{msg.replyToMessageId}
                            </div>
                          )}
                          <p className="whitespace-pre-wrap">{msg.messageContent}</p>

                          {/* Hover Action Buttons */}
                          <div
                            className={`absolute top-1 ${isOwn ? '-left-12' : '-right-12'} hidden group-hover:flex items-center space-x-1 bg-card border border-border rounded px-1.5 py-0.5 shadow-sm`}
                          >
                            <button
                              onClick={() => setReplyTo(msg)}
                              className="text-[10px] text-muted-foreground hover:text-foreground"
                              title="Reply"
                            >
                              ↩
                            </button>
                            {(isOwn || overview?.isCurrentUserLeader) && (
                              <button
                                onClick={() => handleDeleteMessage(msg.messageId)}
                                className="text-[10px] text-muted-foreground hover:text-destructive"
                                title="Delete"
                              >
                                <Trash2 className="w-3 h-3" />
                              </button>
                            )}
                          </div>
                        </div>
                      </div>
                    );
                  })
                )}
                <div ref={messagesEndRef} />
              </div>

              {/* Typing indicator */}
              {typingList.length > 0 && (
                <div className="px-4 py-1.5 bg-muted/30 text-[11px] text-muted-foreground italic flex items-center gap-1.5">
                  <span className="w-1.5 h-1.5 rounded-full bg-primary animate-ping" />
                  <span>{typingList.join(', ')} {typingList.length > 1 ? 'are' : 'is'} typing...</span>
                </div>
              )}

              {/* Reply Preview Banner */}
              {replyTo && (
                <div className="px-4 py-2 bg-accent/40 border-t border-border flex items-center justify-between text-xs">
                  <span className="text-muted-foreground truncate">
                    Replying to <strong>{replyTo.senderName}</strong>: &quot;{replyTo.messageContent}&quot;
                  </span>
                  <button onClick={() => setReplyTo(null)} className="text-muted-foreground hover:text-foreground font-bold">
                    ✕
                  </button>
                </div>
              )}

              {/* Message Input Box */}
              <form onSubmit={handleSendMessage} className="p-3 border-t border-border bg-card flex items-center space-x-2">
                <input
                  type="text"
                  value={chatInput}
                  onChange={handleChatInputChange}
                  placeholder="Type message to team... (Enter to send)"
                  className="flex-1 px-3.5 py-2.5 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-xs text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                />
                <button
                  type="submit"
                  disabled={!chatInput.trim()}
                  className="px-4 py-2.5 bg-primary text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold hover:opacity-90 disabled:opacity-50 transition flex items-center gap-1.5"
                >
                  <span>Send</span>
                  <Send className="w-3.5 h-3.5" />
                </button>
              </form>
            </div>
          )}

          {/* 3. DIRECT MESSAGES SECTION */}
          {activeTab === 'dm' && (
            <div className="bg-card rounded-[var(--radius)] border border-border h-[650px] shadow-xs grid grid-cols-1 md:grid-cols-3 overflow-hidden">
              {/* Conversations Left Panel */}
              <div className="border-r border-border p-3 flex flex-col h-full bg-card">
                <div className="font-bold text-xs uppercase tracking-wider text-muted-foreground mb-3 px-2">
                  Teammate DMs
                </div>
                <div className="flex-1 overflow-y-auto space-y-1">
                  {conversations.length === 0 ? (
                    <div className="p-4 text-center text-xs text-muted-foreground">
                      No active private conversations. Click a teammate below to start!
                    </div>
                  ) : (
                    conversations.map((conv) => {
                      const isSelected = selectedConversation?.conversationId === conv.conversationId;
                      return (
                        <button
                          key={conv.conversationId}
                          onClick={() => {
                            setSelectedConversation(conv);
                            loadDmMessages(conv.conversationId);
                            workspaceApi.markConversationAsRead(conv.conversationId);
                          }}
                          className={`w-full text-left p-2.5 rounded-[calc(var(--radius)-0.125rem)] text-xs transition flex items-center justify-between ${
                            isSelected
                              ? 'bg-accent text-accent-foreground font-bold'
                              : 'hover:bg-muted text-foreground'
                          }`}
                        >
                          <div className="truncate">
                            <div className="font-bold">{conv.otherParticipant?.name || 'Teammate'}</div>
                            <div className="text-[10px] text-muted-foreground truncate">
                              {conv.lastMessage?.messageContent || 'No messages yet'}
                            </div>
                          </div>
                          {conv.unreadCount > 0 && (
                            <span className="w-4 h-4 rounded-full bg-primary text-white text-[9px] font-bold flex items-center justify-center">
                              {conv.unreadCount}
                            </span>
                          )}
                        </button>
                      );
                    })
                  )}
                </div>

                {/* Direct message teammate shortcuts */}
                <div className="pt-3 border-t border-border">
                  <div className="text-[10px] font-bold text-muted-foreground uppercase mb-1.5 px-2">Teammates:</div>
                  <div className="flex flex-wrap gap-1">
                    {members
                      .filter((m) => m.studentId !== user?.studentId)
                      .map((m) => (
                        <button
                          key={m.studentId}
                          onClick={() => startConversationWith(m.studentId)}
                          className="px-2 py-1 rounded bg-input border border-border text-[11px] font-medium hover:border-primary transition"
                        >
                          + {m.name.split(' ')[0]}
                        </button>
                      ))}
                  </div>
                </div>
              </div>

              {/* Chat View Right Panel */}
              <div className="md:col-span-2 flex flex-col h-full bg-card">
                {selectedConversation ? (
                  <>
                    <div className="p-3.5 border-b border-border flex items-center justify-between bg-card">
                      <div>
                        <div className="font-bold text-xs text-foreground">
                          {selectedConversation.otherParticipant?.name}
                        </div>
                        <div className="text-[10px] text-muted-foreground">
                          {selectedConversation.otherParticipant?.department}
                        </div>
                      </div>
                    </div>

                    <div className="flex-1 p-4 overflow-y-auto space-y-3">
                      {dmList.length === 0 ? (
                        <div className="h-full flex items-center justify-center text-xs text-muted-foreground">
                          Send a private message to {selectedConversation.otherParticipant?.name}
                        </div>
                      ) : (
                        dmList.map((dm) => {
                          const isOwn = dm.senderId === user?.studentId;
                          return (
                            <div key={dm.messageId} className={`flex flex-col ${isOwn ? 'items-end' : 'items-start'}`}>
                              <div className="text-[10px] text-muted-foreground mb-0.5">
                                {new Date(dm.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                              </div>
                              <div
                                className={`max-w-[80%] rounded-[var(--radius)] p-3 text-xs leading-relaxed shadow-xs ${
                                  isOwn
                                    ? 'bg-primary text-primary-foreground rounded-br-none'
                                    : 'bg-input text-foreground border border-border rounded-bl-none'
                                }`}
                              >
                                {dm.messageContent}
                              </div>
                            </div>
                          );
                        })
                      )}
                      <div ref={dmEndRef} />
                    </div>

                    <form onSubmit={handleSendDm} className="p-3 border-t border-border flex items-center space-x-2 bg-card">
                      <input
                        type="text"
                        value={dmInput}
                        onChange={(e) => setDmInput(e.target.value)}
                        placeholder={`Message ${selectedConversation.otherParticipant?.name}...`}
                        className="flex-1 px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-xs text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                      />
                      <button
                        type="submit"
                        disabled={!dmInput.trim()}
                        className="px-4 py-2 bg-primary text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold hover:opacity-90 disabled:opacity-50 transition"
                      >
                        Send
                      </button>
                    </form>
                  </>
                ) : (
                  <div className="h-full flex flex-col items-center justify-center text-muted-foreground space-y-2 p-6 text-center">
                    <MessageCircle className="w-12 h-12 opacity-30" />
                    <p className="text-xs font-medium">Select a conversation or click a teammate on the left to start direct messaging.</p>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* 4. TASKS (KANBAN) SECTION */}
          {activeTab === 'tasks' && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-bold text-foreground">Kanban Task Board</h3>
                  <p className="text-[11px] text-muted-foreground">Manage deliverables, deadlines and assignees</p>
                </div>
                <button
                  onClick={() => setIsTaskModalOpen(true)}
                  className="flex items-center space-x-1.5 px-3.5 py-2 bg-primary text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold hover:opacity-90 transition shadow-xs"
                >
                  <Plus className="w-3.5 h-3.5" />
                  <span>Add Task</span>
                </button>
              </div>

              {/* Kanban 4 Columns */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {TASK_STATUSES.map((statusCol) => {
                  const colTasks = tasks.filter((t) => t.status === statusCol.key);
                  return (
                    <div
                      key={statusCol.key}
                      className="bg-card rounded-[var(--radius)] border border-border p-3.5 shadow-xs flex flex-col min-h-[320px]"
                    >
                      <div className="flex items-center justify-between pb-2 mb-3 border-b border-border">
                        <span className={`text-xs font-bold px-2 py-0.5 rounded-full border ${statusCol.color}`}>
                          {statusCol.label}
                        </span>
                        <span className="text-xs font-bold text-muted-foreground">{colTasks.length}</span>
                      </div>

                      <div className="flex-1 space-y-2.5 overflow-y-auto">
                        {colTasks.length === 0 ? (
                          <div className="text-[11px] text-muted-foreground italic text-center py-8">
                            No tasks in this lane
                          </div>
                        ) : (
                          colTasks.map((task) => (
                            <div
                              key={task.taskId}
                              className="p-3 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-xs space-y-2 shadow-xs hover:border-primary/40 transition group"
                            >
                              <div className="flex items-center justify-between">
                                <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded border ${PRIORITY_BADGES[task.priority] || ''}`}>
                                  {task.priority}
                                </span>
                                <div className="flex items-center space-x-1 opacity-0 group-hover:opacity-100 transition">
                                  <button
                                    onClick={() => handleDeleteTask(task.taskId)}
                                    className="p-1 text-muted-foreground hover:text-destructive rounded"
                                    title="Delete task"
                                  >
                                    <Trash2 className="w-3 h-3" />
                                  </button>
                                </div>
                              </div>

                              <div className="font-bold text-foreground text-xs leading-snug">{task.title}</div>
                              {task.description && (
                                <p className="text-[11px] text-muted-foreground line-clamp-2 leading-relaxed">
                                  {task.description}
                                </p>
                              )}

                              <div className="pt-2 border-t border-border flex items-center justify-between text-[10px] text-muted-foreground">
                                <span className="flex items-center gap-1">
                                  <User className="w-3 h-3" />
                                  {task.assignedToName || 'Unassigned'}
                                </span>
                                {task.dueDate && (
                                  <span className="flex items-center gap-1 font-semibold text-primary">
                                    <Clock className="w-3 h-3" />
                                    {new Date(task.dueDate).toLocaleDateString()}
                                  </span>
                                )}
                              </div>

                              {/* Quick Move Status Buttons */}
                              <div className="pt-1 flex items-center justify-end space-x-1">
                                {TASK_STATUSES.filter((s) => s.key !== task.status).map((s) => (
                                  <button
                                    key={s.key}
                                    onClick={() => handleUpdateTaskStatus(task.taskId, s.key)}
                                    className="text-[9px] px-1.5 py-0.5 rounded bg-muted hover:bg-accent text-foreground font-semibold"
                                  >
                                    → {s.label}
                                  </button>
                                ))}
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
          )}

          {/* 5. SHARED FILES SECTION */}
          {activeTab === 'files' && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-bold text-foreground">Project Shared Resources</h3>
                  <p className="text-[11px] text-muted-foreground">Documents, assets, specifications and code archives</p>
                </div>
                <button
                  onClick={() => setIsUploadModalOpen(true)}
                  className="flex items-center space-x-1.5 px-3.5 py-2 bg-primary text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold hover:opacity-90 transition shadow-xs"
                >
                  <Plus className="w-3.5 h-3.5" />
                  <span>Upload Resource</span>
                </button>
              </div>

              {/* Category Filter Pills */}
              <div className="flex items-center space-x-1.5 overflow-x-auto pb-1">
                {FILE_CATEGORIES.map((cat) => (
                  <button
                    key={cat}
                    onClick={() => setSelectedCategory(cat)}
                    className={`px-3 py-1.5 rounded-full text-xs font-semibold transition ${
                      selectedCategory === cat
                        ? 'bg-primary text-primary-foreground'
                        : 'bg-card border border-border text-muted-foreground hover:text-foreground'
                    }`}
                  >
                    {cat}
                  </button>
                ))}
              </div>

              {/* Files Table / Cards */}
              <div className="bg-card rounded-[var(--radius)] border border-border p-4 shadow-xs">
                {files.length === 0 ? (
                  <div className="text-center py-12 text-muted-foreground space-y-2">
                    <FolderOpen className="w-12 h-12 opacity-30 mx-auto" />
                    <p className="text-xs">No files uploaded in this category yet.</p>
                  </div>
                ) : (
                  <div className="divide-y divide-border">
                    {files.map((file) => (
                      <div key={file.fileId} className="py-3 flex items-center justify-between text-xs">
                        <div className="flex items-center space-x-3">
                          <div className="w-8 h-8 rounded bg-input border border-border flex items-center justify-center text-primary">
                            <File className="w-4 h-4" />
                          </div>
                          <div>
                            <div className="font-bold text-foreground">{file.fileName}</div>
                            <div className="text-[10px] text-muted-foreground flex items-center space-x-2">
                              <span>{file.category}</span>
                              <span>•</span>
                              <span>{(file.fileSize / 1024).toFixed(1)} KB</span>
                              <span>•</span>
                              <span>by {file.uploadedByName}</span>
                            </div>
                          </div>
                        </div>

                        <div className="flex items-center space-x-2">
                          <a
                            href={file.fileUrl}
                            download={file.fileName}
                            className="p-1.5 rounded hover:bg-muted text-primary transition"
                            title="Download file"
                          >
                            <Download className="w-4 h-4" />
                          </a>
                          {(file.uploadedById === user?.studentId || overview?.isCurrentUserLeader) && (
                            <button
                              onClick={() => handleDeleteFile(file.fileId)}
                              className="p-1.5 rounded hover:bg-muted text-muted-foreground hover:text-destructive transition"
                              title="Delete file"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* 6. ANNOUNCEMENTS SECTION */}
          {activeTab === 'announcements' && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-bold text-foreground">Team Announcements</h3>
                  <p className="text-[11px] text-muted-foreground">Broadcast deadlines, meeting notes, and team news</p>
                </div>
                <button
                  onClick={() => setIsAnnouncementModalOpen(true)}
                  className="flex items-center space-x-1.5 px-3.5 py-2 bg-primary text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold hover:opacity-90 transition shadow-xs"
                >
                  <Plus className="w-3.5 h-3.5" />
                  <span>Publish Announcement</span>
                </button>
              </div>

              <div className="space-y-3">
                {announcements.length === 0 ? (
                  <div className="bg-card rounded-[var(--radius)] border border-border p-8 text-center text-xs text-muted-foreground">
                    No announcements published yet. Team leaders can broadcast updates here.
                  </div>
                ) : (
                  announcements.map((ann) => (
                    <div
                      key={ann.announcementId}
                      className={`bg-card rounded-[var(--radius)] border p-5 shadow-xs space-y-2.5 transition ${
                        ann.isPinned ? 'border-primary/50 bg-primary/5' : 'border-border'
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-2">
                          {ann.isPinned && <Pin className="w-4 h-4 text-primary fill-primary" />}
                          <h4 className="text-sm font-black text-foreground">{ann.title}</h4>
                        </div>
                        <div className="flex items-center space-x-2">
                          <button
                            onClick={() => handleTogglePinAnnouncement(ann.announcementId)}
                            className="p-1 text-muted-foreground hover:text-primary rounded text-xs"
                            title={ann.isPinned ? 'Unpin' : 'Pin to top'}
                          >
                            <Pin className={`w-3.5 h-3.5 ${ann.isPinned ? 'fill-primary text-primary' : ''}`} />
                          </button>
                          {(ann.createdById === user?.studentId || overview?.isCurrentUserLeader) && (
                            <button
                              onClick={() => handleDeleteAnnouncement(ann.announcementId)}
                              className="p-1 text-muted-foreground hover:text-destructive rounded"
                              title="Delete announcement"
                            >
                              <Trash2 className="w-3.5 h-3.5" />
                            </button>
                          )}
                        </div>
                      </div>

                      <p className="text-xs text-muted-foreground leading-relaxed whitespace-pre-wrap">{ann.content}</p>

                      <div className="pt-2 border-t border-border flex items-center justify-between text-[10px] text-muted-foreground">
                        <span>Posted by <strong>{ann.createdByName}</strong></span>
                        <span>{new Date(ann.createdAt).toLocaleString()}</span>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}

          {/* 7. MEETINGS SECTION */}
          {activeTab === 'meetings' && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-bold text-foreground">Video Meetings & Standups</h3>
                  <p className="text-[11px] text-muted-foreground">Coordinate virtual team syncs with external call links</p>
                </div>
                <button
                  onClick={() => setIsMeetingModalOpen(true)}
                  className="flex items-center space-x-1.5 px-3.5 py-2 bg-primary text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold hover:opacity-90 transition shadow-xs"
                >
                  <Plus className="w-3.5 h-3.5" />
                  <span>Schedule Meeting</span>
                </button>
              </div>

              <div className="space-y-3">
                {meetings.length === 0 ? (
                  <div className="bg-card rounded-[var(--radius)] border border-border p-8 text-center text-xs text-muted-foreground">
                    No meetings scheduled. Click &quot;Schedule Meeting&quot; to arrange a Google Meet or Zoom sync.
                  </div>
                ) : (
                  meetings.map((m) => {
                    const isUpcoming = new Date(m.startTime) >= new Date();
                    return (
                      <div
                        key={m.meetingId}
                        className="bg-card rounded-[var(--radius)] border border-border p-5 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4"
                      >
                        <div className="space-y-1.5">
                          <div className="flex items-center space-x-2">
                            <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${isUpcoming ? 'bg-emerald-500/10 text-emerald-500 border border-emerald-500/30' : 'bg-muted text-muted-foreground'}`}>
                              {isUpcoming ? 'Upcoming' : 'Past'}
                            </span>
                            <h4 className="text-sm font-black text-foreground">{m.title}</h4>
                          </div>
                          {m.description && <p className="text-xs text-muted-foreground leading-relaxed">{m.description}</p>}
                          <div className="text-[11px] font-semibold text-primary flex items-center space-x-3">
                            <span className="flex items-center gap-1">
                              <Calendar className="w-3.5 h-3.5 text-muted-foreground" />
                              {new Date(m.startTime).toLocaleString()}
                            </span>
                            <span>•</span>
                            <span className="text-muted-foreground">Organized by {m.createdByName}</span>
                          </div>
                        </div>

                        <div className="flex items-center space-x-2">
                          <a
                            href={m.meetingLink.startsWith('http') ? m.meetingLink : `https://${m.meetingLink}`}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="flex items-center space-x-1.5 px-3.5 py-2 bg-primary text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold hover:opacity-90 transition shadow-xs"
                          >
                            <ExternalLink className="w-3.5 h-3.5" />
                            <span>Join Video Call</span>
                          </a>
                          {(m.createdById === user?.studentId || overview?.isCurrentUserLeader) && (
                            <button
                              onClick={() => handleCancelMeeting(m.meetingId)}
                              className="p-2 text-muted-foreground hover:text-destructive rounded transition"
                              title="Cancel meeting"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          )}
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          )}

          {/* 8. TEAM MEMBERS ROSTER SECTION */}
          {activeTab === 'members' && (
            <div className="space-y-4">
              <div>
                <h3 className="text-sm font-bold text-foreground">Team Members Roster</h3>
                <p className="text-[11px] text-muted-foreground">Collaborators currently approved in this team workspace</p>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {members.map((m) => {
                  const isLeader = m.studentId === team?.leader?.studentId;
                  const isMe = m.studentId === user?.studentId;
                  return (
                    <div
                      key={m.studentId}
                      className="bg-card rounded-[var(--radius)] border border-border p-4 shadow-xs flex items-center justify-between"
                    >
                      <div className="flex items-center space-x-3">
                        <div className="w-10 h-10 rounded-[var(--radius)] bg-primary text-primary-foreground font-black text-sm flex items-center justify-center">
                          {m.name.charAt(0)}
                        </div>
                        <div>
                          <div className="font-bold text-xs text-foreground flex items-center gap-1.5">
                            <span>{m.name}</span>
                            {isLeader && (
                              <span className="text-[9px] px-1.5 py-0.2 rounded bg-primary/10 text-primary font-bold">
                                Leader
                              </span>
                            )}
                            {isMe && <span className="text-[9px] text-muted-foreground font-semibold">(You)</span>}
                          </div>
                          <div className="text-[11px] text-muted-foreground">{m.assignedRole || 'Active Member'}</div>
                          <div className="text-[10px] text-muted-foreground truncate">{m.email}</div>
                        </div>
                      </div>

                      {!isMe && (
                        <button
                          onClick={() => startConversationWith(m.studentId)}
                          className="p-2 rounded bg-input border border-border hover:border-primary text-primary transition"
                          title={`Direct message ${m.name}`}
                        >
                          <MessageCircle className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          )}

        </div>

        {/* RIGHT SIDEBAR QUICK WIDGETS */}
        <div className="lg:col-span-3 space-y-6">

          {/* Active Teammates Roster Widget */}
          <div className="bg-card rounded-[var(--radius)] border border-border p-4 shadow-xs space-y-3">
            <div className="flex items-center justify-between pb-2 border-b border-border">
              <span className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
                <Users className="w-3.5 h-3.5 text-primary" />
                Teammates ({members.length})
              </span>
              <button onClick={() => setActiveTab('members')} className="text-[10px] font-bold text-primary hover:underline">
                View
              </button>
            </div>

            <div className="space-y-2">
              {members.map((m) => {
                const isLeader = m.studentId === team?.leader?.studentId;
                const isMe = m.studentId === user?.studentId;
                return (
                  <div key={m.studentId} className="flex items-center justify-between text-xs">
                    <div className="flex items-center space-x-2 truncate">
                      <span className="w-2 h-2 rounded-full bg-emerald-500" />
                      <span className="font-medium text-foreground truncate">{m.name}</span>
                      {isLeader && <span className="text-[9px] text-primary font-bold">[Leader]</span>}
                    </div>
                    {!isMe && (
                      <button
                        onClick={() => startConversationWith(m.studentId)}
                        className="text-muted-foreground hover:text-primary text-[10px] p-1"
                        title="Direct Message"
                      >
                        <MessageCircle className="w-3.5 h-3.5" />
                      </button>
                    )}
                  </div>
                );
              })}
            </div>
          </div>

          {/* Upcoming Meetings Widget */}
          <div className="bg-card rounded-[var(--radius)] border border-border p-4 shadow-xs space-y-3">
            <div className="flex items-center justify-between pb-2 border-b border-border">
              <span className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
                <Video className="w-3.5 h-3.5 text-purple-500" />
                Upcoming Syncs
              </span>
              <button onClick={() => setActiveTab('meetings')} className="text-[10px] font-bold text-primary hover:underline">
                All
              </button>
            </div>

            {overview?.upcomingMeetings?.length > 0 ? (
              <div className="space-y-2">
                {overview.upcomingMeetings.map((meet) => (
                  <div key={meet.meetingId} className="p-2.5 bg-input rounded text-xs space-y-1">
                    <div className="font-bold text-foreground text-[11px] truncate">{meet.title}</div>
                    <div className="text-[10px] text-primary flex items-center gap-1">
                      <Clock className="w-3 h-3" />
                      {new Date(meet.startTime).toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
                    </div>
                    <a
                      href={meet.meetingLink.startsWith('http') ? meet.meetingLink : `https://${meet.meetingLink}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center gap-1 text-[10px] font-bold text-primary hover:underline pt-1"
                    >
                      <span>Join Call</span>
                      <ExternalLink className="w-2.5 h-2.5" />
                    </a>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-[11px] text-muted-foreground italic">No upcoming meetings scheduled.</p>
            )}
          </div>

          {/* Shared Files Quick Links */}
          <div className="bg-card rounded-[var(--radius)] border border-border p-4 shadow-xs space-y-3">
            <div className="flex items-center justify-between pb-2 border-b border-border">
              <span className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
                <FolderOpen className="w-3.5 h-3.5 text-blue-500" />
                Recent Files
              </span>
              <button onClick={() => setActiveTab('files')} className="text-[10px] font-bold text-primary hover:underline">
                All
              </button>
            </div>

            {overview?.recentFiles?.length > 0 ? (
              <div className="space-y-2">
                {overview.recentFiles.slice(0, 4).map((f) => (
                  <div key={f.fileId} className="flex items-center justify-between text-xs p-1.5 bg-input rounded">
                    <span className="truncate text-[11px] text-foreground font-medium max-w-[140px]">{f.fileName}</span>
                    <a href={f.fileUrl} download={f.fileName} className="text-primary hover:opacity-80 p-1">
                      <Download className="w-3.5 h-3.5" />
                    </a>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-[11px] text-muted-foreground italic">No files uploaded yet.</p>
            )}
          </div>

        </div>

      </div>

      {/* --------------------------------------------------------------------- */}
      {/* MODALS */}
      {/* --------------------------------------------------------------------- */}

      {/* Create Task Modal */}
      <Modal isOpen={isTaskModalOpen} onClose={() => setIsTaskModalOpen(false)} title="Create New Task">
        <form onSubmit={handleCreateTask} className="space-y-3.5 text-xs">
          <div>
            <label className="block font-bold text-foreground mb-1">Task Title *</label>
            <input
              type="text"
              required
              value={taskForm.title}
              onChange={(e) => setTaskForm({ ...taskForm, title: e.target.value })}
              placeholder="e.g. Implement authentication JWT tokens"
              className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>

          <div>
            <label className="block font-bold text-foreground mb-1">Description</label>
            <textarea
              rows={3}
              value={taskForm.description}
              onChange={(e) => setTaskForm({ ...taskForm, description: e.target.value })}
              placeholder="Details or deliverables required..."
              className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block font-bold text-foreground mb-1">Priority</label>
              <select
                value={taskForm.priority}
                onChange={(e) => setTaskForm({ ...taskForm, priority: e.target.value })}
                className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="URGENT">Urgent</option>
              </select>
            </div>

            <div>
              <label className="block font-bold text-foreground mb-1">Status</label>
              <select
                value={taskForm.status}
                onChange={(e) => setTaskForm({ ...taskForm, status: e.target.value })}
                className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              >
                <option value="TODO">To Do</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="IN_REVIEW">In Review</option>
                <option value="COMPLETED">Completed</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block font-bold text-foreground mb-1">Due Date</label>
              <input
                type="date"
                value={taskForm.dueDate}
                onChange={(e) => setTaskForm({ ...taskForm, dueDate: e.target.value })}
                className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              >
              </input>
            </div>

            <div>
              <label className="block font-bold text-foreground mb-1">Assign To</label>
              <select
                value={taskForm.assignedToId}
                onChange={(e) => setTaskForm({ ...taskForm, assignedToId: e.target.value })}
                className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              >
                <option value="">(Unassigned)</option>
                {members.map((m) => (
                  <option key={m.studentId} value={m.studentId}>
                    {m.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="flex justify-end space-x-2 pt-3 border-t border-border">
            <button
              type="button"
              onClick={() => setIsTaskModalOpen(false)}
              className="px-4 py-2 border border-border rounded text-muted-foreground hover:bg-muted font-bold"
            >
              Cancel
            </button>
            <button type="submit" className="px-4 py-2 bg-primary text-primary-foreground rounded font-bold hover:opacity-90">
              Create Task
            </button>
          </div>
        </form>
      </Modal>

      {/* Upload File Modal */}
      <Modal isOpen={isUploadModalOpen} onClose={() => setIsUploadModalOpen(false)} title="Upload Shared Resource">
        <form onSubmit={handleFileUpload} className="space-y-4 text-xs">
          <div>
            <label className="block font-bold text-foreground mb-1">Select File (Max 25MB)</label>
            <input
              ref={fileInputRef}
              type="file"
              required
              className="w-full px-3 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-foreground file:mr-3 file:py-1 file:px-2.5 file:rounded file:border-0 file:bg-primary file:text-primary-foreground file:text-xs"
            />
          </div>

          <div>
            <label className="block font-bold text-foreground mb-1">Category</label>
            <select
              value={uploadCategory}
              onChange={(e) => setUploadCategory(e.target.value)}
              className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
            >
              <option value="Documents">Documents (PDF, Word, Specs)</option>
              <option value="Code">Code & Repositories</option>
              <option value="Design">Design Assets (Figma, PNG, SVG)</option>
              <option value="General">General</option>
            </select>
          </div>

          <div className="flex justify-end space-x-2 pt-3 border-t border-border">
            <button
              type="button"
              onClick={() => setIsUploadModalOpen(false)}
              className="px-4 py-2 border border-border rounded text-muted-foreground hover:bg-muted font-bold"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={uploading}
              className="px-4 py-2 bg-primary text-primary-foreground rounded font-bold hover:opacity-90 disabled:opacity-50"
            >
              {uploading ? 'Uploading...' : 'Upload'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Publish Announcement Modal */}
      <Modal isOpen={isAnnouncementModalOpen} onClose={() => setIsAnnouncementModalOpen(false)} title="Publish Team Announcement">
        <form onSubmit={handleCreateAnnouncement} className="space-y-3.5 text-xs">
          <div>
            <label className="block font-bold text-foreground mb-1">Announcement Title *</label>
            <input
              type="text"
              required
              value={announcementForm.title}
              onChange={(e) => setAnnouncementForm({ ...announcementForm, title: e.target.value })}
              placeholder="e.g. Hackathon submission deadline reminder"
              className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>

          <div>
            <label className="block font-bold text-foreground mb-1">Content / Message *</label>
            <textarea
              rows={4}
              required
              value={announcementForm.content}
              onChange={(e) => setAnnouncementForm({ ...announcementForm, content: e.target.value })}
              placeholder="Important notice details for all team members..."
              className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>

          <div className="flex items-center space-x-2 pt-1">
            <input
              type="checkbox"
              id="isPinned"
              checked={announcementForm.isPinned}
              onChange={(e) => setAnnouncementForm({ ...announcementForm, isPinned: e.target.checked })}
              className="rounded border-border text-primary focus:ring-primary"
            />
            <label htmlFor="isPinned" className="text-xs font-semibold text-foreground cursor-pointer">
              Pin to top of workspace
            </label>
          </div>

          <div className="flex justify-end space-x-2 pt-3 border-t border-border">
            <button
              type="button"
              onClick={() => setIsAnnouncementModalOpen(false)}
              className="px-4 py-2 border border-border rounded text-muted-foreground hover:bg-muted font-bold"
            >
              Cancel
            </button>
            <button type="submit" className="px-4 py-2 bg-primary text-primary-foreground rounded font-bold hover:opacity-90">
              Publish
            </button>
          </div>
        </form>
      </Modal>

      {/* Schedule Meeting Modal */}
      <Modal isOpen={isMeetingModalOpen} onClose={() => setIsMeetingModalOpen(false)} title="Schedule Team Meeting">
        <form onSubmit={handleScheduleMeeting} className="space-y-3.5 text-xs">
          <div>
            <label className="block font-bold text-foreground mb-1">Meeting Title *</label>
            <input
              type="text"
              required
              value={meetingForm.title}
              onChange={(e) => setMeetingForm({ ...meetingForm, title: e.target.value })}
              placeholder="e.g. Daily Standup / Architecture Review"
              className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>

          <div>
            <label className="block font-bold text-foreground mb-1">Meeting Link (Google Meet / Zoom) *</label>
            <input
              type="url"
              required
              value={meetingForm.meetingLink}
              onChange={(e) => setMeetingForm({ ...meetingForm, meetingLink: e.target.value })}
              placeholder="https://meet.google.com/xyz-abc or Zoom URL"
              className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block font-bold text-foreground mb-1">Start Date & Time *</label>
              <input
                type="datetime-local"
                required
                value={meetingForm.startTime}
                onChange={(e) => setMeetingForm({ ...meetingForm, startTime: e.target.value })}
                className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              />
            </div>
            <div>
              <label className="block font-bold text-foreground mb-1">End Date & Time</label>
              <input
                type="datetime-local"
                value={meetingForm.endTime}
                onChange={(e) => setMeetingForm({ ...meetingForm, endTime: e.target.value })}
                className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              />
            </div>
          </div>

          <div>
            <label className="block font-bold text-foreground mb-1">Agenda / Description</label>
            <textarea
              rows={2}
              value={meetingForm.description}
              onChange={(e) => setMeetingForm({ ...meetingForm, description: e.target.value })}
              placeholder="Meeting discussion topics..."
              className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>

          <div className="flex justify-end space-x-2 pt-3 border-t border-border">
            <button
              type="button"
              onClick={() => setIsMeetingModalOpen(false)}
              className="px-4 py-2 border border-border rounded text-muted-foreground hover:bg-muted font-bold"
            >
              Cancel
            </button>
            <button type="submit" className="px-4 py-2 bg-primary text-primary-foreground rounded font-bold hover:opacity-90">
              Schedule Sync
            </button>
          </div>
        </form>
      </Modal>

    </div>
  );
};

export default TeamWorkspace;

