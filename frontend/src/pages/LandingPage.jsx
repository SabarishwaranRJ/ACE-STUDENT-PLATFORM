import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Sparkles,
  ArrowRight,
  Users,
  Calendar,
  Layers,
  CheckCircle2,
  Cpu,
  MessageSquare,
  FileText,
  ShieldCheck,
  ChevronDown,
  Star,
  Zap,
  Code2,
  Kanban,
  ExternalLink,
  Lock,
  Trophy,
  GraduationCap
} from 'lucide-react';

const LandingPage = () => {
  const { isAuthenticated, user } = useAuth();
  const [activeTab, setActiveTab] = useState('ai');
  const [openFaq, setOpenFaq] = useState(null);

  const toggleFaq = (index) => {
    setOpenFaq(openFaq === index ? null : index);
  };

  const faqs = [
    {
      q: 'How does the AI Teammate Finder evaluate compatibility?',
      a: 'We leverage Sentence-BERT semantic embeddings to analyze technical skill complementarity, experience levels, availability windows, and project interests. Rather than simple keyword matching, our model understands domain synergies (e.g. pairing an NLP ML engineer with a React frontend architect).'
    },
    {
      q: 'What is the AI Team Builder (Integer Linear Programming)?',
      a: 'Powered by Google OR-Tools, the Team Builder takes event constraints (e.g., team size of 4, required skills in Python, React, and Design) and mathematically optimizes group formation across an entire cohort to maximize total synergy and minimize skill gaps.'
    },
    {
      q: 'What features are included in the Team Workspace?',
      a: 'Each team gets a dedicated, secure real-time collaboration hub featuring WebSocket group chat, direct messaging, interactive Kanban task boards with assignment workflows, permanent cloud file sharing with Aiven MySQL BLOB storage, announcements, and meeting coordination.'
    },
    {
      q: 'Can students from different colleges collaborate?',
      a: 'Yes! Events specify whether they are open to all undergraduate institutions or restricted to specific cohorts. Profile visibility can be set to Public, College-Only, or Private to give you full control over who can discover and invite you.'
    },
    {
      q: 'Is ACE TeamUp free for students and hackathon organizers?',
      a: 'ACE TeamUp is 100% free and open-source for hackathon participants, collegiate clubs, and student developers.'
    }
  ];

  return (
    <div className="w-full -mt-4 sm:-mt-6 lg:-mt-8 -mx-4 sm:-mx-6 lg:-mx-8">
      
      {/* ========================================================================= */}
      {/* 1. HERO SECTION (ChatDeck Style) */}
      {/* ========================================================================= */}
      <section className="relative overflow-hidden pt-16 pb-20 md:pt-24 md:pb-32 bg-gradient-to-b from-primary/10 via-background to-background border-b border-border/40">
        
        {/* Ambient background glow circles */}
        <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[350px] bg-primary/20 blur-[130px] rounded-full pointer-events-none -z-10" />
        <div className="absolute top-1/3 left-1/4 w-[300px] h-[200px] bg-accent/30 blur-[100px] rounded-full pointer-events-none -z-10" />

        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          
          {/* Announcement pill */}
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full border border-primary/30 bg-primary/10 text-primary text-xs font-semibold mb-8 shadow-xs animate-fade-in hover:bg-primary/15 transition cursor-pointer">
            <span className="flex h-2 w-2 rounded-full bg-primary animate-pulse" />
            <span>ACE TeamUp 2.0 is Live</span>
            <span className="text-muted-foreground">•</span>
            <span className="text-foreground/80 font-normal">Real-Time Workspaces & AI Matching</span>
            <ArrowRight className="w-3.5 h-3.5 ml-0.5" />
          </div>

          {/* Headline */}
          <h1 className="text-4xl sm:text-5xl md:text-6xl lg:text-7xl font-black text-foreground tracking-tight max-w-4xl mx-auto leading-[1.1] mb-6">
            Assemble Winning Hackathon Teams with{' '}
            <span className="bg-gradient-to-r from-primary via-indigo-500 to-purple-600 bg-clip-text text-transparent">
              AI Synergy
            </span>
          </h1>

          {/* Subtitle */}
          <p className="text-lg sm:text-xl text-muted-foreground max-w-2xl mx-auto mb-10 leading-relaxed font-normal">
            Discover complementary teammates using Sentence-BERT embeddings, optimize rosters with OR-Tools, and collaborate in real-time workspaces with live chat, Kanban boards, and persistent files.
          </p>

          {/* CTA Buttons */}
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-16">
            {isAuthenticated ? (
              <>
                <Link
                  to="/recommendations"
                  className="w-full sm:w-auto px-7 py-3.5 rounded-[var(--radius)] bg-primary text-primary-foreground font-bold text-sm hover:opacity-95 shadow-lg shadow-primary/25 transition flex items-center justify-center gap-2 group"
                >
                  <Sparkles className="w-4 h-4 text-primary-foreground" />
                  <span>Launch AI Teammate Finder</span>
                  <ArrowRight className="w-4 h-4 group-hover:translate-x-0.5 transition-transform" />
                </Link>
                <Link
                  to="/teams"
                  className="w-full sm:w-auto px-7 py-3.5 rounded-[var(--radius)] bg-card border border-border text-foreground font-semibold text-sm hover:bg-muted transition flex items-center justify-center gap-2 shadow-xs"
                >
                  <Users className="w-4 h-4 text-muted-foreground" />
                  <span>My Active Workspaces</span>
                </Link>
              </>
            ) : (
              <>
                <Link
                  to="/register"
                  className="w-full sm:w-auto px-8 py-3.5 rounded-[var(--radius)] bg-primary text-primary-foreground font-bold text-sm hover:opacity-95 shadow-lg shadow-primary/25 transition flex items-center justify-center gap-2 group"
                >
                  <span>Get Started Free</span>
                  <ArrowRight className="w-4 h-4 group-hover:translate-x-0.5 transition-transform" />
                </Link>
                <Link
                  to="/events"
                  className="w-full sm:w-auto px-8 py-3.5 rounded-[var(--radius)] bg-card border border-border text-foreground font-semibold text-sm hover:bg-muted transition flex items-center justify-center gap-2 shadow-xs"
                >
                  <Calendar className="w-4 h-4 text-muted-foreground" />
                  <span>Explore Active Hackathons</span>
                </Link>
              </>
            )}
          </div>

          {/* Trust stats pill row */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 max-w-3xl mx-auto pt-6 border-t border-border/60">
            <div className="text-center p-3">
              <div className="text-2xl font-black text-foreground">94%</div>
              <div className="text-xs text-muted-foreground mt-0.5 font-medium">Synergy Score Match</div>
            </div>
            <div className="text-center p-3">
              <div className="text-2xl font-black text-foreground">&lt; 30s</div>
              <div className="text-xs text-muted-foreground mt-0.5 font-medium">Team Formation Speed</div>
            </div>
            <div className="text-center p-3">
              <div className="text-2xl font-black text-foreground">100%</div>
              <div className="text-xs text-muted-foreground mt-0.5 font-medium">Real-Time STOMP Sync</div>
            </div>
            <div className="text-center p-3">
              <div className="text-2xl font-black text-foreground">Free</div>
              <div className="text-xs text-muted-foreground mt-0.5 font-medium">For Student Builders</div>
            </div>
          </div>

        </div>
      </section>

      {/* ========================================================================= */}
      {/* 2. INTERACTIVE PRODUCT SHOWCASE (ChatDeck App Window Mockup) */}
      {/* ========================================================================= */}
      <section className="py-16 md:py-24 max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        
        <div className="text-center max-w-2xl mx-auto mb-10">
          <div className="text-xs font-bold uppercase tracking-wider text-primary mb-2">
            Inside the Platform
          </div>
          <h2 className="text-3xl font-black tracking-tight text-foreground">
            Built for High-Velocity Hackathon Teams
          </h2>
          <p className="text-sm text-muted-foreground mt-2">
            Switch between the matching engine, live workspace, and mathematical optimizer.
          </p>
        </div>

        {/* Tab Controls */}
        <div className="flex justify-center mb-8">
          <div className="inline-flex p-1.5 rounded-[var(--radius)] bg-muted/80 border border-border">
            <button
              onClick={() => setActiveTab('ai')}
              className={`px-4 py-2 rounded-[calc(var(--radius)-0.2rem)] text-xs font-bold transition flex items-center gap-2 ${
                activeTab === 'ai'
                  ? 'bg-card text-foreground shadow-xs'
                  : 'text-muted-foreground hover:text-foreground'
              }`}
            >
              <Sparkles className="w-3.5 h-3.5 text-primary" />
              <span>AI Teammate Finder</span>
            </button>
            <button
              onClick={() => setActiveTab('workspace')}
              className={`px-4 py-2 rounded-[calc(var(--radius)-0.2rem)] text-xs font-bold transition flex items-center gap-2 ${
                activeTab === 'workspace'
                  ? 'bg-card text-foreground shadow-xs'
                  : 'text-muted-foreground hover:text-foreground'
              }`}
            >
              <Kanban className="w-3.5 h-3.5 text-primary" />
              <span>Team Workspace</span>
            </button>
            <button
              onClick={() => setActiveTab('builder')}
              className={`px-4 py-2 rounded-[calc(var(--radius)-0.2rem)] text-xs font-bold transition flex items-center gap-2 ${
                activeTab === 'builder'
                  ? 'bg-card text-foreground shadow-xs'
                  : 'text-muted-foreground hover:text-foreground'
              }`}
            >
              <Cpu className="w-3.5 h-3.5 text-primary" />
              <span>ILP Roster Optimizer</span>
            </button>
          </div>
        </div>

        {/* Browser Mockup Window */}
        <div className="rounded-2xl border border-border bg-card shadow-2xl overflow-hidden transition-all duration-300">
          
          {/* Chrome title bar */}
          <div className="h-10 bg-muted/60 border-b border-border flex items-center px-4 justify-between select-none">
            <div className="flex items-center space-x-2">
              <span className="w-3 h-3 rounded-full bg-red-500/80 inline-block" />
              <span className="w-3 h-3 rounded-full bg-yellow-500/80 inline-block" />
              <span className="w-3 h-3 rounded-full bg-green-500/80 inline-block" />
            </div>
            <div className="text-[11px] font-mono text-muted-foreground bg-background px-4 py-0.5 rounded border border-border">
              ace-teamup.platform/workspace
            </div>
            <div className="w-10" />
          </div>

          {/* Window Body */}
          <div className="p-6 md:p-8 bg-card min-h-[380px]">
            {activeTab === 'ai' && (
              <div className="grid md:grid-cols-2 gap-6 animate-fade-in">
                <div className="space-y-4">
                  <div className="flex items-center justify-between border-b border-border pb-3">
                    <div>
                      <h4 className="font-bold text-foreground text-sm">Target Event: KIT HackFest 2026</h4>
                      <p className="text-xs text-muted-foreground">Seeking: NLP & Full-stack synergy</p>
                    </div>
                    <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-primary/10 text-primary border border-primary/20">
                      S-BERT Online
                    </span>
                  </div>

                  {/* Candidate Match Card 1 */}
                  <div className="p-4 rounded-xl border border-border bg-accent/30 space-y-2">
                    <div className="flex items-center justify-between">
                      <div className="font-bold text-sm text-foreground">Anjali Sharma</div>
                      <span className="text-xs font-black px-2 py-0.5 rounded bg-primary text-primary-foreground">
                        95% Match
                      </span>
                    </div>
                    <p className="text-xs text-muted-foreground">
                      Brings complementary leadership, communication, and UI/UX design skills.
                    </p>
                    <div className="flex flex-wrap gap-1.5 pt-1">
                      <span className="text-[10px] font-medium px-2 py-0.5 rounded bg-card border border-border text-foreground">
                        UI/UX Design
                      </span>
                      <span className="text-[10px] font-medium px-2 py-0.5 rounded bg-card border border-border text-foreground">
                        Figma
                      </span>
                      <span className="text-[10px] font-medium px-2 py-0.5 rounded bg-card border border-border text-foreground">
                        Product Pitching
                      </span>
                    </div>
                  </div>

                  {/* Candidate Match Card 2 */}
                  <div className="p-4 rounded-xl border border-border bg-accent/30 space-y-2">
                    <div className="flex items-center justify-between">
                      <div className="font-bold text-sm text-foreground">Rahul Verma</div>
                      <span className="text-xs font-black px-2 py-0.5 rounded bg-primary/90 text-primary-foreground">
                        80% Match
                      </span>
                    </div>
                    <p className="text-xs text-muted-foreground">
                      Brings strong backend API and cloud infrastructure expertise.
                    </p>
                    <div className="flex flex-wrap gap-1.5 pt-1">
                      <span className="text-[10px] font-medium px-2 py-0.5 rounded bg-card border border-border text-foreground">
                        Java / Spring
                      </span>
                      <span className="text-[10px] font-medium px-2 py-0.5 rounded bg-card border border-border text-foreground">
                        Docker
                      </span>
                    </div>
                  </div>
                </div>

                <div className="p-5 rounded-xl border border-border bg-muted/40 flex flex-col justify-between">
                  <div>
                    <div className="text-xs font-bold uppercase tracking-wider text-primary mb-2">
                      Synergy Breakdown
                    </div>
                    <h4 className="font-bold text-foreground text-base mb-3">Multi-Dimensional Scoring</h4>
                    <p className="text-xs text-muted-foreground leading-relaxed mb-4">
                      Our semantic embedding model scores compatibility by weighting technical skill complementarity (35%), project interest overlap (25%), experience level balance (15%), and schedule availability (15%).
                    </p>
                    <div className="space-y-2.5">
                      <div>
                        <div className="flex justify-between text-xs font-semibold mb-1">
                          <span>Skill Complementarity</span>
                          <span className="text-primary font-bold">96%</span>
                        </div>
                        <div className="h-1.5 w-full bg-border rounded-full overflow-hidden">
                          <div className="h-full bg-primary rounded-full w-[96%]" />
                        </div>
                      </div>
                      <div>
                        <div className="flex justify-between text-xs font-semibold mb-1">
                          <span>Availability Overlap</span>
                          <span className="text-primary font-bold">88%</span>
                        </div>
                        <div className="h-1.5 w-full bg-border rounded-full overflow-hidden">
                          <div className="h-full bg-primary rounded-full w-[88%]" />
                        </div>
                      </div>
                      <div>
                        <div className="flex justify-between text-xs font-semibold mb-1">
                          <span>Interest Alignment</span>
                          <span className="text-primary font-bold">92%</span>
                        </div>
                        <div className="h-1.5 w-full bg-border rounded-full overflow-hidden">
                          <div className="h-full bg-primary rounded-full w-[92%]" />
                        </div>
                      </div>
                    </div>
                  </div>

                  <Link
                    to="/recommendations"
                    className="mt-6 w-full py-2.5 rounded-[var(--radius)] bg-primary text-primary-foreground font-bold text-xs text-center hover:opacity-95 transition"
                  >
                    Try AI Teammate Finder &rarr;
                  </Link>
                </div>
              </div>
            )}

            {activeTab === 'workspace' && (
              <div className="grid md:grid-cols-3 gap-4 animate-fade-in">
                {/* Kanban Column 1 */}
                <div className="p-3.5 rounded-xl border border-border bg-muted/30">
                  <div className="flex items-center justify-between text-xs font-bold text-muted-foreground uppercase tracking-wider mb-3">
                    <span>Todo (2)</span>
                    <span className="w-2 h-2 rounded-full bg-blue-500" />
                  </div>
                  <div className="space-y-2.5">
                    <div className="p-3 bg-card border border-border rounded-lg text-xs shadow-2xs">
                      <div className="font-bold text-foreground">Draft Architecture Deck</div>
                      <div className="text-[11px] text-muted-foreground mt-1">Due in 2 days • High</div>
                    </div>
                    <div className="p-3 bg-card border border-border rounded-lg text-xs shadow-2xs">
                      <div className="font-bold text-foreground">Setup MySQL & Docker</div>
                      <div className="text-[11px] text-muted-foreground mt-1">Assigned to Sabarish</div>
                    </div>
                  </div>
                </div>

                {/* Kanban Column 2 */}
                <div className="p-3.5 rounded-xl border border-border bg-muted/30">
                  <div className="flex items-center justify-between text-xs font-bold text-muted-foreground uppercase tracking-wider mb-3">
                    <span>In Progress (1)</span>
                    <span className="w-2 h-2 rounded-full bg-yellow-500" />
                  </div>
                  <div className="space-y-2.5">
                    <div className="p-3 bg-card border border-border rounded-lg text-xs shadow-2xs">
                      <div className="font-bold text-foreground">Build STOMP WebSocket Chat</div>
                      <div className="text-[11px] text-muted-foreground mt-1">Live streaming channel</div>
                    </div>
                  </div>
                </div>

                {/* Kanban Column 3 */}
                <div className="p-3.5 rounded-xl border border-border bg-muted/30">
                  <div className="flex items-center justify-between text-xs font-bold text-muted-foreground uppercase tracking-wider mb-3">
                    <span>Completed (3)</span>
                    <span className="w-2 h-2 rounded-full bg-green-500" />
                  </div>
                  <div className="space-y-2.5">
                    <div className="p-3 bg-card border border-border rounded-lg text-xs shadow-2xs opacity-80">
                      <div className="font-bold text-foreground line-through">Team Ideation & Roster</div>
                      <div className="text-[11px] text-muted-foreground mt-1">Complete</div>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'builder' && (
              <div className="p-6 rounded-xl border border-border bg-accent/20 animate-fade-in text-center max-w-xl mx-auto">
                <Cpu className="w-10 h-10 text-primary mx-auto mb-3" />
                <h4 className="text-base font-black text-foreground mb-1">
                  Google OR-Tools Integer Linear Programming
                </h4>
                <p className="text-xs text-muted-foreground mb-4">
                  Construct balanced 3-to-5 person teams across a pool of applicants while satisfying constraints on required tech stacks, leadership presence, and schedule overlap.
                </p>
                <div className="flex items-center justify-center gap-3 text-xs font-semibold">
                  <span className="px-3 py-1 bg-card border border-border rounded-md text-foreground">
                    Constraint Satisfaction: 100%
                  </span>
                  <span className="px-3 py-1 bg-card border border-border rounded-md text-foreground">
                    Avg Solve Time: 120ms
                  </span>
                </div>
              </div>
            )}
          </div>

        </div>

      </section>

      {/* ========================================================================= */}
      {/* 3. BENTO GRID FEATURES (ChatDeck Style) */}
      {/* ========================================================================= */}
      <section id="features" className="py-16 md:py-24 bg-muted/40 border-y border-border/60 scroll-mt-16">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          
          <div className="text-center max-w-2xl mx-auto mb-16">
            <div className="text-xs font-bold uppercase tracking-wider text-primary mb-2">
              Core Capabilities
            </div>
            <h2 className="text-3xl sm:text-4xl font-black tracking-tight text-foreground">
              Everything Your Hackathon Squad Needs
            </h2>
            <p className="text-sm text-muted-foreground mt-2">
              From teammate discovery to project submission, engineered for hackathon speed.
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-6">
            
            {/* Feature 1 (Large 2-col) */}
            <div className="md:col-span-2 p-8 rounded-2xl bg-card border border-border shadow-xs hover:border-primary/50 transition duration-300 relative overflow-hidden group">
              <div className="w-10 h-10 rounded-xl bg-primary/10 border border-primary/20 text-primary flex items-center justify-center mb-5">
                <Sparkles className="w-5 h-5" />
              </div>
              <h3 className="text-xl font-bold text-foreground mb-2">
                Sentence-BERT Semantic Matching
              </h3>
              <p className="text-sm text-muted-foreground leading-relaxed max-w-xl">
                Keywords fail when evaluating talent. Our Sentence-BERT model maps project descriptions, bios, and specific framework experience into high-dimensional embedding vectors, calculating cosine similarity and skill gaps in real time.
              </p>
              <div className="mt-6 flex flex-wrap gap-2 text-xs font-semibold">
                <span className="px-2.5 py-1 rounded-md bg-accent text-accent-foreground border border-border">
                  Cosine Similarity
                </span>
                <span className="px-2.5 py-1 rounded-md bg-accent text-accent-foreground border border-border">
                  Skill Gap Detection
                </span>
                <span className="px-2.5 py-1 rounded-md bg-accent text-accent-foreground border border-border">
                  Adaptive Feedback Loop
                </span>
              </div>
            </div>

            {/* Feature 2 */}
            <div className="p-8 rounded-2xl bg-card border border-border shadow-xs hover:border-primary/50 transition duration-300">
              <div className="w-10 h-10 rounded-xl bg-primary/10 border border-primary/20 text-primary flex items-center justify-center mb-5">
                <Kanban className="w-5 h-5" />
              </div>
              <h3 className="text-xl font-bold text-foreground mb-2">
                Interactive Kanban Board
              </h3>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Organize hackathon deliverables with Todo, In Progress, In Review, and Done columns. Assign tasks to teammates with live status updates.
              </p>
            </div>

            {/* Feature 3 */}
            <div className="p-8 rounded-2xl bg-card border border-border shadow-xs hover:border-primary/50 transition duration-300">
              <div className="w-10 h-10 rounded-xl bg-primary/10 border border-primary/20 text-primary flex items-center justify-center mb-5">
                <MessageSquare className="w-5 h-5" />
              </div>
              <h3 className="text-xl font-bold text-foreground mb-2">
                Real-Time STOMP Chat
              </h3>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Low-latency team group chat and 1-on-1 direct messaging over Spring Boot WebSockets with instant unread badges.
              </p>
            </div>

            {/* Feature 4 */}
            <div className="p-8 rounded-2xl bg-card border border-border shadow-xs hover:border-primary/50 transition duration-300">
              <div className="w-10 h-10 rounded-xl bg-primary/10 border border-primary/20 text-primary flex items-center justify-center mb-5">
                <FileText className="w-5 h-5" />
              </div>
              <h3 className="text-xl font-bold text-foreground mb-2">
                Permanent Cloud Storage
              </h3>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Upload slide decks, architecture diagrams, and code archives with resilient Aiven MySQL BLOB backups that persist permanently.
              </p>
            </div>

            {/* Feature 5 */}
            <div className="p-8 rounded-2xl bg-card border border-border shadow-xs hover:border-primary/50 transition duration-300">
              <div className="w-10 h-10 rounded-xl bg-primary/10 border border-primary/20 text-primary flex items-center justify-center mb-5">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <h3 className="text-xl font-bold text-foreground mb-2">
                Verified Student Badges
              </h3>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Connect with authentic students from verified collegiate institutions like Kongu Engineering, PSG Tech, and SVCE.
              </p>
            </div>

          </div>

        </div>
      </section>

      {/* ========================================================================= */}
      {/* 4. ACTIVE HACKATHONS TEASER */}
      {/* ========================================================================= */}
      <section className="py-16 md:py-24 max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col md:flex-row md:items-end justify-between mb-10 gap-4">
          <div>
            <div className="text-xs font-bold uppercase tracking-wider text-primary mb-2">
              Active Competitions
            </div>
            <h2 className="text-3xl font-black tracking-tight text-foreground">
              Form Teams for Upcoming Hackathons
            </h2>
          </div>
          <Link
            to="/events"
            className="text-xs font-bold text-primary hover:underline flex items-center gap-1"
          >
            <span>View All Competitions</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        <div className="grid md:grid-cols-2 gap-6">
          <div className="p-6 rounded-2xl border border-border bg-card shadow-xs hover:border-primary/40 transition">
            <div className="flex items-center justify-between text-xs text-muted-foreground mb-3">
              <span className="font-bold px-2.5 py-1 rounded bg-accent text-accent-foreground border border-border">
                Hackathon
              </span>
              <span>Deadline: Nov 30, 2026</span>
            </div>
            <h3 className="text-xl font-bold text-foreground mb-2">KIT HackFest 2026</h3>
            <p className="text-xs text-muted-foreground mb-5 leading-relaxed">
              A 36-hour inter-college hackathon focused on AI and social impact solutions. Form 4-person teams to prototype and pitch.
            </p>
            <Link
              to="/recommendations?eventId=1"
              className="inline-flex items-center gap-2 text-xs font-bold text-primary hover:underline"
            >
              <span>Find KIT HackFest Teammates</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          <div className="p-6 rounded-2xl border border-border bg-card shadow-xs hover:border-primary/40 transition">
            <div className="flex items-center justify-between text-xs text-muted-foreground mb-3">
              <span className="font-bold px-2.5 py-1 rounded bg-accent text-accent-foreground border border-border">
                Datathon
              </span>
              <span>Deadline: Dec 05, 2026</span>
            </div>
            <h3 className="text-xl font-bold text-foreground mb-2">DataStorm 2026</h3>
            <p className="text-xs text-muted-foreground mb-5 leading-relaxed">
              Data science competition analyzing real-world datasets with Python, SQL, and ML to build predictive models and dashboards.
            </p>
            <Link
              to="/recommendations?eventId=2"
              className="inline-flex items-center gap-2 text-xs font-bold text-primary hover:underline"
            >
              <span>Find DataStorm Teammates</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        </div>
      </section>

      {/* ========================================================================= */}
      {/* 5. FAQ ACCORDION (ChatDeck Style) */}
      {/* ========================================================================= */}
      <section id="faq" className="py-16 md:py-24 bg-muted/30 border-t border-border/60 scroll-mt-16">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          
          <div className="text-center mb-12">
            <div className="text-xs font-bold uppercase tracking-wider text-primary mb-2">
              Got Questions?
            </div>
            <h2 className="text-3xl font-black tracking-tight text-foreground">
              Frequently Asked Questions
            </h2>
          </div>

          <div className="space-y-3">
            {faqs.map((faq, idx) => (
              <div
                key={idx}
                className="rounded-xl border border-border bg-card overflow-hidden shadow-2xs transition"
              >
                <button
                  type="button"
                  onClick={() => toggleFaq(idx)}
                  className="w-full p-4 sm:p-5 text-left font-bold text-sm text-foreground flex items-center justify-between gap-4 hover:bg-muted/40 transition"
                >
                  <span>{faq.q}</span>
                  <ChevronDown
                    className={`w-4 h-4 text-muted-foreground transition-transform duration-200 flex-shrink-0 ${
                      openFaq === idx ? 'rotate-180 text-primary' : ''
                    }`}
                  />
                </button>
                {openFaq === idx && (
                  <div className="px-4 sm:px-5 pb-5 text-xs text-muted-foreground leading-relaxed border-t border-border/50 pt-3">
                    {faq.a}
                  </div>
                )}
              </div>
            ))}
          </div>

        </div>
      </section>

      {/* ========================================================================= */}
      {/* 6. BOTTOM CALL-TO-ACTION BANNER (ChatDeck Style) */}
      {/* ========================================================================= */}
      <section className="py-20 md:py-28 relative overflow-hidden bg-gradient-to-b from-card to-background border-t border-border">
        
        {/* Glow */}
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[300px] bg-primary/15 blur-[120px] rounded-full pointer-events-none -z-10" />

        <div className="max-w-4xl mx-auto px-4 text-center">
          <h2 className="text-3xl sm:text-4xl md:text-5xl font-black text-foreground tracking-tight mb-4">
            Ready to Build Your Winning Team?
          </h2>
          <p className="text-sm sm:text-base text-muted-foreground max-w-xl mx-auto mb-8">
            Join hundreds of collegiate builders, find teammates whose skills complement your own, and submit your best work.
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-3">
            {isAuthenticated ? (
              <Link
                to="/teams/create"
                className="px-8 py-3.5 rounded-[var(--radius)] bg-primary text-primary-foreground font-bold text-sm hover:opacity-90 shadow-lg shadow-primary/25 transition"
              >
                Create a New Team &rarr;
              </Link>
            ) : (
              <>
                <Link
                  to="/register"
                  className="px-8 py-3.5 rounded-[var(--radius)] bg-primary text-primary-foreground font-bold text-sm hover:opacity-90 shadow-lg shadow-primary/25 transition"
                >
                  Create Student Account
                </Link>
                <Link
                  to="/login"
                  className="px-8 py-3.5 rounded-[var(--radius)] border border-border bg-card text-foreground font-semibold text-sm hover:bg-muted transition"
                >
                  Sign In
                </Link>
              </>
            )}
          </div>
        </div>
      </section>

      {/* ========================================================================= */}
      {/* 7. FOOTER */}
      {/* ========================================================================= */}
      <footer className="py-12 border-t border-border bg-card/60">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-muted-foreground">
          <div className="flex items-center gap-2">
            <span className="font-black text-foreground text-sm tracking-tight">
              ACE <span className="text-primary font-normal">TeamUp</span>
            </span>
            <span>•</span>
            <span>AI-Powered Teammate Discovery & Collaboration</span>
          </div>

          <div className="flex items-center gap-6">
            <Link to="/events" className="hover:text-foreground transition">
              Hackathons
            </Link>
            <Link to="/recommendations" className="hover:text-foreground transition">
              AI Matching
            </Link>
            <Link to="/teams" className="hover:text-foreground transition">
              Workspaces
            </Link>
            <a
              href="https://github.com/SabarishwaranRJ/ACE-STUDENT-PLATFORM"
              target="_blank"
              rel="noreferrer"
              className="hover:text-foreground transition flex items-center gap-1"
            >
              <span>GitHub</span>
              <ExternalLink className="w-3 h-3" />
            </a>
          </div>
        </div>
      </footer>

    </div>
  );
};

export default LandingPage;
