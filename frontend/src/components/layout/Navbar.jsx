import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import { workspaceApi } from '../../api/workspace';
import { createStompClient } from '../../utils/websocket';
import {
  Sparkles,
  LogOut,
  Menu,
  X,
  PlusCircle,
  Sun,
  Moon,
  Bell,
  CheckCheck,
  Calendar,
  Megaphone,
  CheckSquare,
  FileText,
  MessageSquare
} from 'lucide-react';

const Navbar = ({ onToggleSidebar, isSidebarOpen }) => {
  const { user, isAuthenticated, logout } = useAuth();
  const { theme, toggleTheme, isDark } = useTheme();
  const navigate = useNavigate();

  // Notification state
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [isNotifOpen, setIsNotifOpen] = useState(false);
  const notifDropdownRef = useRef(null);

  // Load unread count on auth
  useEffect(() => {
    if (!isAuthenticated) return;

    const loadUnread = async () => {
      try {
        const res = await workspaceApi.getUnreadNotificationCount();
        if (res.success && res.data) {
          setUnreadCount(res.data.unreadCount || 0);
        }
      } catch (e) {
        // quiet ignore
      }
    };
    loadUnread();

    // Subscribe to real-time notification socket
    let client = null;
    if (user?.studentId) {
      client = createStompClient((stomp) => {
        stomp.subscribe(`/topic/notifications/${user.studentId}`, (msg) => {
          try {
            const newNotif = JSON.parse(msg.body);
            setNotifications((prev) => [newNotif, ...prev]);
            setUnreadCount((c) => c + 1);
          } catch (e) {
            console.error(e);
          }
        });
      });
      client.activate();
    }

    return () => {
      if (client) client.deactivate();
    };
  }, [isAuthenticated, user?.studentId]);

  // Click outside to close notification panel
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (notifDropdownRef.current && !notifDropdownRef.current.contains(e.target)) {
        setIsNotifOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleOpenNotif = async () => {
    setIsNotifOpen(!isNotifOpen);
    if (!isNotifOpen) {
      try {
        const res = await workspaceApi.getNotifications(20);
        if (res.success && res.data) {
          setNotifications(res.data);
        }
      } catch (err) {
        console.warn('Failed to load notifications', err);
      }
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await workspaceApi.markAllNotificationsAsRead();
      setUnreadCount(0);
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    } catch (err) {
      console.warn(err);
    }
  };

  const handleNotificationClick = async (notif) => {
    try {
      if (!notif.isRead) {
        await workspaceApi.markNotificationAsRead(notif.notificationId);
        setUnreadCount((c) => Math.max(0, c - 1));
        setNotifications((prev) =>
          prev.map((n) => (n.notificationId === notif.notificationId ? { ...n, isRead: true } : n))
        );
      }
      setIsNotifOpen(false);

      if (notif.relatedEntityId) {
        if (notif.notificationType === 'MESSAGE' || notif.notificationType === 'TASK' || notif.notificationType === 'FILE') {
          navigate(`/teams/${notif.relatedEntityId}/workspace`);
        } else if (notif.notificationType === 'INVITATION') {
          navigate('/invitations');
        }
      }
    } catch (e) {
      console.warn(e);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-40 bg-background/95 backdrop-blur-md border-b border-border transition-colors duration-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          
          {/* Logo & Toggle */}
          <div className="flex items-center space-x-3">
            {isAuthenticated && (
              <button
                onClick={onToggleSidebar}
                className="p-2 rounded-[var(--radius)] text-muted-foreground hover:text-foreground hover:bg-muted md:hidden transition"
                aria-label="Toggle navigation menu"
              >
                {isSidebarOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
              </button>
            )}
            <Link to="/" className="flex items-center space-x-1.5">
              <span className="text-xl font-black tracking-tight text-foreground">
                ACE <span className="text-primary font-normal">TeamUp</span>
              </span>
            </Link>
          </div>

          {/* Navigation Links */}
          <nav className="hidden md:flex items-center space-x-1 font-medium text-sm">
            <Link
              to="/events"
              className="px-3.5 py-2 rounded-[var(--radius)] text-muted-foreground hover:text-primary hover:bg-accent transition"
            >
              Events
            </Link>
            {isAuthenticated && (
              <>
                <Link
                  to="/recommendations"
                  className="px-3.5 py-2 rounded-[var(--radius)] text-muted-foreground hover:text-primary hover:bg-accent transition flex items-center space-x-1"
                >
                  <Sparkles className="w-4 h-4 text-primary" />
                  <span>AI Matching</span>
                </Link>
                <Link
                  to="/teams"
                  className="px-3.5 py-2 rounded-[var(--radius)] text-muted-foreground hover:text-primary hover:bg-accent transition"
                >
                  My Teams
                </Link>
                <Link
                  to="/invitations"
                  className="px-3.5 py-2 rounded-[var(--radius)] text-muted-foreground hover:text-primary hover:bg-accent transition"
                >
                  Invitations
                </Link>
              </>
            )}
          </nav>

          {/* Actions / Theme Toggle, Notifications & User Profile */}
          <div className="flex items-center space-x-2 sm:space-x-2.5">
            {/* Dark / Light Mode Toggle Button */}
            <button
              onClick={toggleTheme}
              className="p-2 rounded-[var(--radius)] text-muted-foreground hover:text-foreground hover:bg-accent border border-border/80 transition flex items-center justify-center shadow-xs"
              title={isDark ? "Switch to Light Mode" : "Switch to Dark Mode"}
              aria-label="Toggle Dark and Light Mode"
            >
              {isDark ? (
                <Sun className="w-4 h-4 text-amber-400 transition-transform rotate-0 scale-100" />
              ) : (
                <Moon className="w-4 h-4 text-slate-700 transition-transform rotate-0 scale-100" />
              )}
            </button>

            {isAuthenticated ? (
              <div className="flex items-center space-x-2 sm:space-x-2.5">
                {/* Notification Bell Dropdown */}
                <div className="relative" ref={notifDropdownRef}>
                  <button
                    onClick={handleOpenNotif}
                    className="p-2 rounded-[var(--radius)] text-muted-foreground hover:text-foreground hover:bg-accent border border-border/80 transition relative"
                    title="Notifications"
                  >
                    <Bell className="w-4 h-4" />
                    {unreadCount > 0 && (
                      <span className="absolute -top-1 -right-1 w-4 h-4 bg-primary text-primary-foreground font-black text-[9px] rounded-full flex items-center justify-center shadow-xs animate-bounce">
                        {unreadCount > 9 ? '9+' : unreadCount}
                      </span>
                    )}
                  </button>

                  {/* Dropdown Panel */}
                  {isNotifOpen && (
                    <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-card border border-border rounded-[var(--radius)] shadow-xl z-50 overflow-hidden text-xs">
                      <div className="p-3 border-b border-border flex items-center justify-between bg-card">
                        <div className="font-bold text-foreground flex items-center gap-1.5">
                          <Bell className="w-3.5 h-3.5 text-primary" />
                          <span>Notifications</span>
                          {unreadCount > 0 && (
                            <span className="px-1.5 py-0.2 rounded-full bg-primary/10 text-primary text-[10px] font-bold">
                              {unreadCount} new
                            </span>
                          )}
                        </div>
                        {unreadCount > 0 && (
                          <button
                            onClick={handleMarkAllRead}
                            className="text-[11px] font-bold text-primary hover:underline flex items-center gap-1"
                          >
                            <CheckCheck className="w-3 h-3" />
                            <span>Mark all read</span>
                          </button>
                        )}
                      </div>

                      <div className="max-h-80 overflow-y-auto divide-y divide-border">
                        {notifications.length === 0 ? (
                          <div className="p-6 text-center text-muted-foreground">
                            No notifications yet
                          </div>
                        ) : (
                          notifications.map((notif) => (
                            <div
                              key={notif.notificationId}
                              onClick={() => handleNotificationClick(notif)}
                              className={`p-3 cursor-pointer hover:bg-muted/70 transition space-y-1 ${
                                !notif.isRead ? 'bg-accent/40 font-semibold' : ''
                              }`}
                            >
                              <div className="flex items-center justify-between">
                                <span className="text-[11px] font-bold text-foreground truncate max-w-[200px]">
                                  {notif.title}
                                </span>
                                <span className="text-[10px] text-muted-foreground whitespace-nowrap">
                                  {new Date(notif.createdAt).toLocaleDateString()}
                                </span>
                              </div>
                              <p className="text-[11px] text-muted-foreground line-clamp-2 leading-relaxed">
                                {notif.message}
                              </p>
                            </div>
                          ))
                        )}
                      </div>
                    </div>
                  )}
                </div>

                <Link
                  to="/teams/create"
                  className="hidden sm:inline-flex items-center space-x-1.5 px-3.5 py-2 bg-primary hover:bg-[#1a8cd8] text-primary-foreground rounded-[var(--radius)] text-xs font-bold transition shadow-sm"
                >
                  <PlusCircle className="w-4 h-4" />
                  <span>Create Team</span>
                </Link>

                <Link
                  to="/profile"
                  className="flex items-center space-x-2 p-1.5 rounded-[var(--radius)] hover:bg-muted transition"
                >
                  <div className="w-8 h-8 rounded-[var(--radius)] bg-muted text-foreground font-bold flex items-center justify-center text-xs">
                    {user?.name?.charAt(0) || 'U'}
                  </div>
                  <span className="text-xs font-semibold text-foreground hidden lg:inline-block max-w-[120px] truncate">
                    {user?.name}
                  </span>
                </Link>

                <button
                  onClick={handleLogout}
                  title="Sign out"
                  className="p-2 text-muted-foreground hover:text-destructive hover:bg-red-500/10 rounded-[var(--radius)] transition"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <div className="flex items-center space-x-2">
                <Link
                  to="/login"
                  className="px-4 py-2 text-sm font-semibold text-foreground hover:text-primary transition"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 bg-primary hover:bg-[#1a8cd8] text-primary-foreground text-sm font-bold rounded-[var(--radius)] transition shadow-sm"
                >
                  Register
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
