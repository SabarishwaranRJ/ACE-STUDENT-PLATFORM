import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import { studentsApi } from '../api/students';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import SkillBadge from '../components/common/SkillBadge';
import Modal from '../components/common/Modal';
import { Edit3, Plus, Trash2, Calendar, BookOpen, Building, Sparkles } from 'lucide-react';

const POPULAR_SKILLS = [
  'Python', 'React', 'Machine Learning', 'SQL', 'Java', 'Docker', 'UI/UX Design', 'Leadership',
  'JavaScript', 'TypeScript', 'Node.js', 'Express', 'Next.js', 'Vue.js', 'Angular', 'Tailwind CSS',
  'HTML/CSS', 'Spring Boot', 'C++', 'C#', '.NET', 'Go', 'Rust', 'PHP', 'Ruby on Rails',
  'PostgreSQL', 'MySQL', 'MongoDB', 'Redis', 'GraphQL', 'REST APIs', 'FastAPI', 'Django', 'Flask',
  'AWS', 'Google Cloud', 'Azure', 'Kubernetes', 'CI/CD', 'Git', 'Linux',
  'Deep Learning', 'PyTorch', 'TensorFlow', 'NLP', 'Computer Vision', 'Data Science', 'Pandas',
  'Flutter', 'React Native', 'Kotlin', 'Swift', 'Figma', 'System Design', 'Cybersecurity', 'Agile'
];

const Profile = () => {
  const { user, updateUser } = useAuth();
  const [profile, setProfile] = useState(null);
  const [skills, setSkills] = useState([]);
  const [interests, setInterests] = useState([]);
  const [availability, setAvailability] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Modals
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isSkillModalOpen, setIsSkillModalOpen] = useState(false);
  const [isSlotModalOpen, setIsSlotModalOpen] = useState(false);

  // Edit Profile Form State
  const [editForm, setEditForm] = useState({
    name: '',
    department: '',
    academicYear: '3rd',
    preferredRole: '',
    experienceLevel: 'Intermediate',
    availability: 'Full-time',
    bio: '',
  });

  // Add Skill Form State
  const [skillInput, setSkillInput] = useState('');
  const [skillProficiency, setSkillProficiency] = useState('Intermediate');
  const [skillSuggestionsOpen, setSkillSuggestionsOpen] = useState(false);
  const [allPredefinedSkills, setAllPredefinedSkills] = useState(POPULAR_SKILLS);
  const skillDropdownRef = useRef(null);

  // Add Availability Form State
  const [newSlot, setNewSlot] = useState({
    dayOfWeek: 'Monday',
    startTime: '09:00',
    endTime: '17:00',
  });

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const [profRes, skillsRes, intRes, availRes] = await Promise.all([
        studentsApi.getMyProfile(),
        studentsApi.getMySkills(),
        studentsApi.getMyInterests(),
        studentsApi.getMyAvailability(),
      ]);

      if (profRes.success) {
        setProfile(profRes.data);
        setEditForm({
          name: profRes.data.name || '',
          department: profRes.data.department || '',
          academicYear: profRes.data.academicYear || '3rd',
          preferredRole: profRes.data.preferredRole || '',
          experienceLevel: profRes.data.experienceLevel || 'Intermediate',
          availability: profRes.data.availability || 'Full-time',
          bio: profRes.data.bio || '',
        });
      }
      if (skillsRes.success) setSkills(skillsRes.data || []);
      if (intRes.success) setInterests(intRes.data || []);
      if (availRes.success) setAvailability(availRes.data || []);
    } catch (err) {
      setError(err.message || 'Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    // Fetch system skills to merge with popular skills
    studentsApi.getAllSkills().then((res) => {
      if (res?.success && Array.isArray(res?.data)) {
        const backendSkillNames = res.data.map((s) => s.skillName);
        const merged = Array.from(new Set([...POPULAR_SKILLS, ...backendSkillNames]));
        setAllPredefinedSkills(merged);
      }
    }).catch(() => {});
  }, []);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (skillDropdownRef.current && !skillDropdownRef.current.contains(event.target)) {
        setSkillSuggestionsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    try {
      const res = await studentsApi.updateMyProfile(editForm);
      if (res.success) {
        setProfile(res.data);
        if (updateUser) {
          updateUser({ name: res.data.name });
        }
        setIsEditModalOpen(false);
      }
    } catch (err) {
      alert(err.message || 'Failed to update profile');
    }
  };

  const handleAddSkill = async (e) => {
    e.preventDefault();
    const trimmed = skillInput.trim();
    if (!trimmed) {
      alert('Please enter or select a skill');
      return;
    }
    try {
      await studentsApi.addSkill({
        skillName: trimmed,
        proficiencyLevel: skillProficiency,
        yearsOfExperience: 1.0,
      });
      setSkillInput('');
      setIsSkillModalOpen(false);
      setSkillSuggestionsOpen(false);
      loadData();
    } catch (err) {
      alert(err.message || 'Failed to add skill');
    }
  };

  const handleRemoveSkill = async (skillId) => {
    try {
      await studentsApi.removeSkill(skillId);
      loadData();
    } catch (err) {
      alert(err.message || 'Failed to remove skill');
    }
  };

  const handleAddSlot = async (e) => {
    e.preventDefault();
    try {
      await studentsApi.addAvailability(newSlot);
      setIsSlotModalOpen(false);
      loadData();
    } catch (err) {
      alert(err.message || 'Failed to add availability slot');
    }
  };

  const handleRemoveSlot = async (id) => {
    try {
      await studentsApi.removeAvailability(id);
      loadData();
    } catch (err) {
      alert(err.message || 'Failed to remove slot');
    }
  };

  const trimmedInput = skillInput.trim().toLowerCase();
  const filteredSuggestions = allPredefinedSkills.filter((s) => {
    const sLower = s.toLowerCase();
    if (!trimmedInput) return true;
    return sLower.includes(trimmedInput) && sLower !== trimmedInput;
  }).slice(0, 8);

  const isCustomSkill =
    skillInput.trim().length > 0 &&
    !allPredefinedSkills.some((s) => s.toLowerCase() === skillInput.trim().toLowerCase());

  if (loading) return <LoadingSpinner text="Loading student profile..." />;
  if (error) return <ErrorMessage message={error} onRetry={loadData} />;

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      {/* Profile Card Header */}
      <div className="bg-card rounded-[var(--radius)] border border-border p-6 sm:p-8 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
        <div className="flex items-center space-x-5">
          <div className="w-20 h-20 rounded-[var(--radius)] bg-primary text-primary-foreground font-black text-3xl flex items-center justify-center shadow-sm">
            {profile?.name?.charAt(0) || 'U'}
          </div>
          <div>
            <h2 className="text-2xl font-black text-foreground tracking-tight">{profile?.name}</h2>
            <p className="text-sm font-semibold text-primary mb-2">{profile?.preferredRole || 'Student Developer'}</p>
            <div className="flex flex-wrap items-center gap-3 text-xs text-muted-foreground font-medium">
              <span className="flex items-center space-x-1">
                <Building className="w-3.5 h-3.5 text-muted-foreground" />
                <span>{profile?.collegeName || 'Engineering College'}</span>
              </span>
              <span>•</span>
              <span className="flex items-center space-x-1">
                <BookOpen className="w-3.5 h-3.5 text-muted-foreground" />
                <span>{profile?.department} ({profile?.academicYear} Year)</span>
              </span>
              <span>•</span>
              <span className="px-2 py-0.5 rounded-full bg-secondary text-secondary-foreground font-semibold">
                {profile?.experienceLevel}
              </span>
            </div>
          </div>
        </div>

        <button
          onClick={() => setIsEditModalOpen(true)}
          className="flex items-center space-x-1.5 px-4 py-2.5 bg-secondary hover:opacity-90 text-secondary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold transition"
        >
          <Edit3 className="w-4 h-4" />
          <span>Edit Profile</span>
        </button>
      </div>

      {/* Bio */}
      <div className="bg-card rounded-[var(--radius)] border border-border p-6 shadow-xs">
        <h3 className="text-sm font-bold text-foreground uppercase tracking-wider mb-2">About Me</h3>
        <p className="text-sm text-muted-foreground leading-relaxed">
          {profile?.bio || 'No bio written yet. Click "Edit Profile" to add your introduction!'}
        </p>
      </div>

      {/* Grid: Skills & Schedule */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        
        {/* Skills Section */}
        <div className="bg-card rounded-[var(--radius)] border border-border p-6 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-bold text-foreground uppercase tracking-wider">
              Skills ({skills.length})
            </h3>
            <button
              onClick={() => setIsSkillModalOpen(true)}
              className="flex items-center space-x-1 text-xs font-bold text-primary hover:underline"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Add Skill</span>
            </button>
          </div>

          <div className="flex flex-wrap gap-2">
            {skills.length > 0 ? (
              skills.map((s) => (
                <div key={s.skillId} className="flex items-center space-x-1 bg-muted border border-border rounded-[calc(var(--radius)-0.25rem)] px-2.5 py-1">
                  <SkillBadge skillName={s.skillName} proficiency={s.proficiencyLevel} />
                  <button
                    onClick={() => handleRemoveSkill(s.skillId)}
                    className="p-1 text-muted-foreground hover:text-destructive rounded-md transition"
                  >
                    <Trash2 className="w-3 h-3" />
                  </button>
                </div>
              ))
            ) : (
              <span className="text-xs text-muted-foreground italic">No skills listed yet. Add your top skills to improve AI matching!</span>
            )}
          </div>
        </div>

        {/* Availability Slots */}
        <div className="bg-card rounded-[var(--radius)] border border-border p-6 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-bold text-foreground uppercase tracking-wider">
              Weekly Availability ({availability.length})
            </h3>
            <button
              onClick={() => setIsSlotModalOpen(true)}
              className="flex items-center space-x-1 text-xs font-bold text-primary hover:underline"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Add Slot</span>
            </button>
          </div>

          <div className="space-y-2">
            {availability.length > 0 ? (
              availability.map((slot) => (
                <div key={slot.availabilityId} className="flex items-center justify-between p-2.5 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-xs">
                  <div className="flex items-center space-x-2 font-semibold text-foreground">
                    <Calendar className="w-3.5 h-3.5 text-muted-foreground" />
                    <span>{slot.dayOfWeek}: {slot.startTime} - {slot.endTime}</span>
                  </div>
                  <button
                    onClick={() => handleRemoveSlot(slot.availabilityId)}
                    className="p-1 text-muted-foreground hover:text-destructive"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))
            ) : (
              <span className="text-xs text-muted-foreground italic">No availability slots set. Helps matching teams with overlapping schedules.</span>
            )}
          </div>
        </div>

      </div>

      {/* Edit Profile Modal */}
      <Modal isOpen={isEditModalOpen} onClose={() => setIsEditModalOpen(false)} title="Edit Profile">
        <form onSubmit={handleUpdateProfile} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-foreground mb-1">Full Name</label>
            <input
              type="text"
              required
              value={editForm.name}
              onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
              className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-foreground mb-1">Department</label>
              <input
                type="text"
                value={editForm.department}
                onChange={(e) => setEditForm({ ...editForm, department: e.target.value })}
                className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-foreground mb-1">Academic Year</label>
              <select
                value={editForm.academicYear}
                onChange={(e) => setEditForm({ ...editForm, academicYear: e.target.value })}
                className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              >
                <option value="1st" className="bg-card text-foreground">1st Year</option>
                <option value="2nd" className="bg-card text-foreground">2nd Year</option>
                <option value="3rd" className="bg-card text-foreground">3rd Year</option>
                <option value="4th" className="bg-card text-foreground">4th Year</option>
                <option value="Alumni" className="bg-card text-foreground">Alumni</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-foreground mb-1">Preferred Role</label>
              <input
                type="text"
                value={editForm.preferredRole}
                onChange={(e) => setEditForm({ ...editForm, preferredRole: e.target.value })}
                className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-foreground mb-1">Experience Level</label>
              <select
                value={editForm.experienceLevel}
                onChange={(e) => setEditForm({ ...editForm, experienceLevel: e.target.value })}
                className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              >
                <option value="Beginner" className="bg-card text-foreground">Beginner</option>
                <option value="Intermediate" className="bg-card text-foreground">Intermediate</option>
                <option value="Advanced" className="bg-card text-foreground">Advanced</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-foreground mb-1">Bio</label>
            <textarea
              rows={3}
              value={editForm.bio}
              onChange={(e) => setEditForm({ ...editForm, bio: e.target.value })}
              className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>

          <div className="flex justify-end space-x-2 pt-4 border-t border-border">
            <button
              type="button"
              onClick={() => setIsEditModalOpen(false)}
              className="px-4 py-2 border border-border rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold text-muted-foreground hover:bg-muted"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-primary hover:opacity-90 text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold"
            >
              Save Changes
            </button>
          </div>
        </form>
      </Modal>

      {/* Add Skill Modal */}
      <Modal
        isOpen={isSkillModalOpen}
        onClose={() => {
          setIsSkillModalOpen(false);
          setSkillSuggestionsOpen(false);
        }}
        title="Add Skill"
      >
        <form onSubmit={handleAddSkill} className="space-y-4">
          <div ref={skillDropdownRef} className="relative">
            <div className="flex items-center justify-between mb-1">
              <label className="block text-xs font-bold text-foreground">Skill Name</label>
              <span className="text-[11px] text-muted-foreground">Type any skill</span>
            </div>
            <div className="relative">
              <input
                type="text"
                value={skillInput}
                onChange={(e) => {
                  setSkillInput(e.target.value);
                  setSkillSuggestionsOpen(true);
                }}
                onFocus={() => setSkillSuggestionsOpen(true)}
                placeholder="Type a skill (e.g. Python, React, Solidity...)"
                className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                autoComplete="off"
              />
              {skillInput && (
                <button
                  type="button"
                  onClick={() => setSkillInput('')}
                  className="absolute right-2.5 top-1/2 -translate-y-1/2 text-xs text-muted-foreground hover:text-foreground"
                >
                  ✕
                </button>
              )}
            </div>

            {/* Recommendations Dropdown */}
            {skillSuggestionsOpen && (
              <div className="absolute z-50 left-0 right-0 mt-1 max-h-52 overflow-y-auto bg-card border border-border rounded-[calc(var(--radius)-0.125rem)] shadow-lg py-1">
                <div className="px-3 py-1 text-[10px] font-bold uppercase tracking-wider text-muted-foreground flex items-center justify-between">
                  <span className="flex items-center gap-1">
                    <Sparkles className="w-3 h-3 text-primary" />
                    Recommended Suggestions
                  </span>
                  <span>Click to pick</span>
                </div>

                {filteredSuggestions.length > 0 ? (
                  filteredSuggestions.map((item) => (
                    <button
                      key={item}
                      type="button"
                      onMouseDown={(e) => {
                        e.preventDefault();
                        setSkillInput(item);
                        setSkillSuggestionsOpen(false);
                      }}
                      className="w-full text-left px-3.5 py-2 text-xs font-medium text-foreground hover:bg-accent hover:text-accent-foreground flex items-center justify-between transition cursor-pointer"
                    >
                      <span>{item}</span>
                      <span className="text-[10px] text-muted-foreground">Select</span>
                    </button>
                  ))
                ) : (
                  <div className="px-3.5 py-2 text-xs text-muted-foreground italic">
                    No matching suggestions from database
                  </div>
                )}

                {isCustomSkill && (
                  <div className="border-t border-border mt-1 pt-1 px-3.5 py-2 bg-accent/30 text-xs text-foreground flex items-center justify-between">
                    <span className="font-semibold text-primary">
                      + Add custom skill: &ldquo;{skillInput.trim()}&rdquo;
                    </span>
                    <span className="text-[10px] text-muted-foreground">Will be auto-created</span>
                  </div>
                )}
              </div>
            )}
          </div>

          <div>
            <label className="block text-xs font-bold text-foreground mb-1">Proficiency Level</label>
            <select
              value={skillProficiency}
              onChange={(e) => setSkillProficiency(e.target.value)}
              className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
            >
              <option value="Beginner" className="bg-card text-foreground">Beginner</option>
              <option value="Intermediate" className="bg-card text-foreground">Intermediate</option>
              <option value="Advanced" className="bg-card text-foreground">Advanced</option>
              <option value="Expert" className="bg-card text-foreground">Expert</option>
            </select>
          </div>

          <div className="flex justify-end space-x-2 pt-4 border-t border-border">
            <button
              type="button"
              onClick={() => {
                setIsSkillModalOpen(false);
                setSkillSuggestionsOpen(false);
              }}
              className="px-4 py-2 border border-border rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold text-muted-foreground hover:bg-muted"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-primary hover:opacity-90 text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold"
            >
              Add Skill
            </button>
          </div>
        </form>
      </Modal>

      {/* Add Availability Modal */}
      <Modal isOpen={isSlotModalOpen} onClose={() => setIsSlotModalOpen(false)} title="Add Weekly Availability Slot">
        <form onSubmit={handleAddSlot} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-foreground mb-1">Day of Week</label>
            <select
              value={newSlot.dayOfWeek}
              onChange={(e) => setNewSlot({ ...newSlot, dayOfWeek: e.target.value })}
              className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
            >
              <option value="Monday" className="bg-card text-foreground">Monday</option>
              <option value="Tuesday" className="bg-card text-foreground">Tuesday</option>
              <option value="Wednesday" className="bg-card text-foreground">Wednesday</option>
              <option value="Thursday" className="bg-card text-foreground">Thursday</option>
              <option value="Friday" className="bg-card text-foreground">Friday</option>
              <option value="Saturday" className="bg-card text-foreground">Saturday</option>
              <option value="Sunday" className="bg-card text-foreground">Sunday</option>
            </select>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-foreground mb-1">Start Time</label>
              <input
                type="time"
                value={newSlot.startTime}
                onChange={(e) => setNewSlot({ ...newSlot, startTime: e.target.value })}
                className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-foreground mb-1">End Time</label>
              <input
                type="time"
                value={newSlot.endTime}
                onChange={(e) => setNewSlot({ ...newSlot, endTime: e.target.value })}
                className="w-full px-3.5 py-2 bg-input border border-border rounded-[calc(var(--radius)-0.125rem)] text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              />
            </div>
          </div>

          <div className="flex justify-end space-x-2 pt-4 border-t border-border">
            <button
              type="button"
              onClick={() => setIsSlotModalOpen(false)}
              className="px-4 py-2 border border-border rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold text-muted-foreground hover:bg-muted"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-primary hover:opacity-90 text-primary-foreground rounded-[calc(var(--radius)-0.125rem)] text-xs font-bold"
            >
              Add Slot
            </button>
          </div>
        </form>
      </Modal>

    </div>
  );
};

export default Profile;
