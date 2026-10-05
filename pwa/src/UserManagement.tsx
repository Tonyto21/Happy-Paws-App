import React, { useState, useEffect } from 'react';
import {
  UserPlus,
  Users,
  Shield,
  Key,
  Check,
  Copy,
  Lock,
  UserCheck,
  UserX,
  RefreshCw,
  AlertCircle
} from 'lucide-react';
import { UserRole } from './types';
import {
  AppUser,
  createNewUserByAdmin,
  resetUserPasswordByAdmin,
  toggleUserActiveStatus,
  generateStandardUsername
} from './authService';
import { collection, getDocs } from 'firebase/firestore';
import { db } from './firebase';

const ROLE_LABELS: Record<UserRole, string> = {
  super_admin: 'Super Admin',
  clinic_owner: 'Clinic Owner / Business Owner',
  veterinarian: 'Veterinarian',
  receptionist: 'Receptionist / Business Manager',
  pet_owner: 'Pet Owner'
};

const DEFAULT_USERS: AppUser[] = [
  {
    uid: 'usr_super_admin',
    email: 'super.admin@happypaws-liberia.org',
    fullName: 'Super Admin (Clinic Owner)',
    role: 'super_admin',
    active: true,
    createdAt: '2026-01-15T08:00:00Z'
  },
  {
    uid: 'usr_clinic_owner',
    email: 'clinic.owner@happypaws-liberia.org',
    fullName: 'Dr. David Kpadeh',
    role: 'clinic_owner',
    active: true,
    createdAt: '2026-01-16T09:00:00Z'
  },
  {
    uid: 'usr_vet',
    email: 'veterinarian@happypaws-liberia.org',
    fullName: 'Dr. Sarah Wilson',
    role: 'veterinarian',
    active: true,
    createdAt: '2026-01-18T10:00:00Z'
  },
  {
    uid: 'usr_receptionist',
    email: 'receptionist@happypaws-liberia.org',
    fullName: 'Marie Dennis',
    role: 'receptionist',
    active: true,
    createdAt: '2026-02-01T11:00:00Z'
  },
  {
    uid: 'usr_pet_owner',
    email: 'pet.owner@happypaws-liberia.org',
    fullName: 'Anthony Tolbert',
    role: 'pet_owner',
    active: true,
    createdAt: '2026-02-10T14:30:00Z'
  }
];

export const UserManagement: React.FC = () => {
  const [users, setUsers] = useState<AppUser[]>(DEFAULT_USERS);
  const [showCreateModal, setShowCreateModal] = useState<boolean>(false);
  const [newFullName, setNewFullName] = useState<string>('');
  const [newRole, setNewRole] = useState<UserRole>('veterinarian');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [oneTimeCredential, setOneTimeCredential] = useState<{
    fullName: string;
    role: string;
    username: string;
    temporaryPassword: string;
    isReset?: boolean;
  } | null>(null);
  const [copied, setCopied] = useState<boolean>(false);

  // Load registered users from Firestore
  useEffect(() => {
    async function loadUsers() {
      try {
        const querySnapshot = await getDocs(collection(db, 'users'));
        const loaded: AppUser[] = [];
        querySnapshot.forEach(docSnap => {
          const d = docSnap.data();
          loaded.push({
            uid: docSnap.id,
            email: d.email,
            fullName: d.fullName || d.email.split('@')[0],
            role: d.role as UserRole,
            active: d.active !== false,
            createdAt: d.createdAt || new Date().toISOString()
          });
        });
        if (loaded.length > 0) {
          // Merge with default initial test accounts to ensure all 5 are present
          const existingEmails = new Set(loaded.map(u => u.email.toLowerCase()));
          const missingDefaults = DEFAULT_USERS.filter(d => !existingEmails.has(d.email.toLowerCase()));
          setUsers([...loaded, ...missingDefaults]);
        }
      } catch (err) {
        console.warn('Could not fetch Firestore users:', err);
      }
    }
    loadUsers();
  }, []);

  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newFullName.trim()) return;
    setIsSubmitting(true);

    try {
      const result = await createNewUserByAdmin(newFullName.trim(), newRole);
      setUsers(prev => [result.user, ...prev]);
      setShowCreateModal(false);
      setNewFullName('');
      setOneTimeCredential({
        fullName: result.user.fullName,
        role: ROLE_LABELS[result.user.role],
        username: result.user.email,
        temporaryPassword: result.temporaryPassword,
        isReset: false
      });
    } catch (err: any) {
      alert('Error creating user: ' + err.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleResetPassword = async (user: AppUser) => {
    if (!window.confirm(`Generate a new temporary password for ${user.fullName} (${user.email})?`)) {
      return;
    }
    try {
      const tempPass = await resetUserPasswordByAdmin(user);
      setOneTimeCredential({
        fullName: user.fullName,
        role: ROLE_LABELS[user.role],
        username: user.email,
        temporaryPassword: tempPass,
        isReset: true
      });
    } catch (err: any) {
      alert('Error resetting password: ' + err.message);
    }
  };

  const handleToggleActive = async (user: AppUser) => {
    const nextState = !user.active;
    try {
      await toggleUserActiveStatus(user.uid, nextState);
      setUsers(prev => prev.map(u => u.uid === user.uid ? { ...u, active: nextState } : u));
    } catch (err: any) {
      alert('Error toggling status: ' + err.message);
    }
  };

  const copyToClipboard = () => {
    if (!oneTimeCredential) return;
    const text = `Happy Paws Liberia - Login Credentials\n\nFull Name: ${oneTimeCredential.fullName}\nRole: ${oneTimeCredential.role}\nUsername: ${oneTimeCredential.username}\nTemporary Password: ${oneTimeCredential.temporaryPassword}\n\nNotice: This temporary password must be changed upon first login.`;
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 3000);
  };

  // Preview generated username
  const existingEmails = users.map(u => u.email);
  const previewUsername = newFullName.trim() ? generateStandardUsername(newFullName, existingEmails) : 'firstname.lastname@happypaws-liberia.org';

  return (
    <div style={{ padding: '4px 0' }}>
      {/* Header & Create Button */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '12px',
        marginBottom: '20px'
      }}>
        <div>
          <h2 className="font-serif" style={{ fontSize: '20px', fontWeight: 700, color: '#111827', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Users size={22} color="var(--color-forest-sage)" /> Super Admin User & Staff Management
          </h2>
          <p style={{ fontSize: '12px', color: '#4B5563', fontWeight: 500, marginTop: '2px' }}>
            Create accounts, manage the 5 roles, activate/deactivate accounts, and generate temporary passwords.
          </p>
        </div>

        <button
          onClick={() => setShowCreateModal(true)}
          className="btn-primary"
          style={{ padding: '8px 16px', fontWeight: 700, fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px' }}
        >
          <UserPlus size={16} /> Create New User
        </button>
      </div>

      {/* Users Table */}
      <div style={{
        backgroundColor: '#FFFFFF',
        borderRadius: '16px',
        border: '1px solid var(--color-border-subtle)',
        overflow: 'hidden',
        boxShadow: 'var(--shadow-sm)'
      }}>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
            <thead>
              <tr style={{ backgroundColor: '#F9FAFB', borderBottom: '1px solid #E5E7EB', color: '#374151', fontWeight: 700 }}>
                <th style={{ padding: '12px 16px' }}>User Details</th>
                <th style={{ padding: '12px 16px' }}>Assigned Role</th>
                <th style={{ padding: '12px 16px' }}>Status</th>
                <th style={{ padding: '12px 16px', textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.uid} style={{ borderBottom: '1px solid #F3F4F6', opacity: user.active ? 1 : 0.6 }}>
                  <td style={{ padding: '12px 16px' }}>
                    <div style={{ fontWeight: 700, color: '#111827' }}>{user.fullName}</div>
                    <div style={{ fontSize: '12px', color: '#4B5563', fontWeight: 500 }}>{user.email}</div>
                  </td>
                  <td style={{ padding: '12px 16px' }}>
                    <span style={{
                      display: 'inline-block',
                      padding: '3px 8px',
                      borderRadius: '6px',
                      fontSize: '11px',
                      fontWeight: 700,
                      backgroundColor: user.role === 'super_admin' ? '#FEF3C7' : user.role === 'veterinarian' ? '#E0F2FE' : '#F3F4F6',
                      color: user.role === 'super_admin' ? '#92400E' : user.role === 'veterinarian' ? '#0369A1' : '#374151'
                    }}>
                      {ROLE_LABELS[user.role]}
                    </span>
                  </td>
                  <td style={{ padding: '12px 16px' }}>
                    {user.active ? (
                      <span style={{ color: '#047857', fontWeight: 700, display: 'inline-flex', alignItems: 'center', gap: '4px', fontSize: '12px' }}>
                        <UserCheck size={14} /> Active
                      </span>
                    ) : (
                      <span style={{ color: '#DC2626', fontWeight: 700, display: 'inline-flex', alignItems: 'center', gap: '4px', fontSize: '12px' }}>
                        <UserX size={14} /> Deactivated
                      </span>
                    )}
                  </td>
                  <td style={{ padding: '12px 16px', textAlign: 'right' }}>
                    <div style={{ display: 'flex', gap: '6px', justifyContent: 'flex-end' }}>
                      <button
                        onClick={() => handleResetPassword(user)}
                        title="Generate New Temporary Password"
                        className="btn-secondary"
                        style={{ padding: '5px 10px', fontSize: '11px', fontWeight: 700, color: '#111827' }}
                      >
                        <RefreshCw size={12} style={{ marginRight: '4px' }} /> Reset Password
                      </button>
                      <button
                        onClick={() => handleToggleActive(user)}
                        title={user.active ? 'Deactivate Account' : 'Activate Account'}
                        className="btn-secondary"
                        style={{
                          padding: '5px 10px',
                          fontSize: '11px',
                          fontWeight: 700,
                          color: user.active ? '#B91C1C' : '#047857'
                        }}
                      >
                        {user.active ? 'Deactivate' : 'Activate'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* CREATE NEW USER MODAL */}
      {showCreateModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.65)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: '20px'
        }}>
          <div style={{
            backgroundColor: '#FFFFFF',
            borderRadius: '20px',
            maxWidth: '480px',
            width: '100%',
            padding: '28px',
            boxShadow: 'var(--shadow-lg)'
          }}>
            <h3 className="font-serif" style={{ fontSize: '19px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>
              Create New Happy Paws User
            </h3>
            <p style={{ fontSize: '12px', color: '#4B5563', fontWeight: 500, marginBottom: '20px' }}>
              The system will automatically generate a standard username and strong temporary password.
            </p>

            <form onSubmit={handleCreateUser} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>
                  Full Name
                </label>
                <input
                  type="text"
                  value={newFullName}
                  onChange={(e) => setNewFullName(e.target.value)}
                  placeholder="e.g. James Brown"
                  required
                  style={{
                    width: '100%',
                    padding: '9px 12px',
                    borderRadius: '8px',
                    border: '1px solid #D1D5DB',
                    fontSize: '13px',
                    color: '#111827',
                    fontWeight: 600,
                    boxSizing: 'border-box'
                  }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>
                  Assigned Role
                </label>
                <select
                  value={newRole}
                  onChange={(e) => setNewRole(e.target.value as UserRole)}
                  style={{
                    width: '100%',
                    padding: '9px 12px',
                    borderRadius: '8px',
                    border: '1px solid #D1D5DB',
                    fontSize: '13px',
                    color: '#111827',
                    fontWeight: 600,
                    boxSizing: 'border-box'
                  }}
                >
                  <option value="super_admin">⚡ Super Admin</option>
                  <option value="clinic_owner">💼 Clinic Owner / Business Owner</option>
                  <option value="veterinarian">🩺 Veterinarian</option>
                  <option value="receptionist">📋 Receptionist / Business Manager</option>
                  <option value="pet_owner">🐶 Pet Owner</option>
                </select>
              </div>

              {/* Live Preview Box */}
              <div style={{
                backgroundColor: '#F3F4F6',
                borderRadius: '10px',
                padding: '12px',
                fontSize: '12px',
                border: '1px solid #E5E7EB'
              }}>
                <div style={{ color: '#4B5563', fontWeight: 600, marginBottom: '4px' }}>
                  Generated Login Username:
                </div>
                <div style={{ fontWeight: 700, color: 'var(--color-amber-terracotta)', wordBreak: 'break-all' }}>
                  {previewUsername}
                </div>
              </div>

              <div style={{ backgroundColor: '#EFF6FF', borderRadius: '10px', padding: '10px', fontSize: '11px', color: '#1E40AF', border: '1px solid #BFDBFE' }}>
                ℹ️ Staff record and role permissions are registered in Firestore. Production live authentication is managed through Firebase Authentication.
              </div>

              <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '10px' }}>
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="btn-secondary"
                  style={{ padding: '8px 16px', color: '#111827', fontWeight: 700 }}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="btn-primary"
                  style={{ padding: '8px 18px', fontWeight: 700 }}
                >
                  {isSubmitting ? 'Creating...' : 'Create Account'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ONE-TIME CREDENTIAL MODAL */}
      {oneTimeCredential && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.7)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 110,
          padding: '20px'
        }}>
          <div style={{
            backgroundColor: '#FFFFFF',
            borderRadius: '24px',
            maxWidth: '460px',
            width: '100%',
            padding: '28px',
            boxShadow: 'var(--shadow-lg)',
            border: '2px solid var(--color-forest-sage)'
          }}>
            <div style={{ textAlign: 'center', marginBottom: '20px' }}>
              <div style={{
                width: '48px',
                height: '48px',
                borderRadius: '50%',
                backgroundColor: '#ECFDF5',
                color: 'var(--color-forest-sage)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                margin: '0 auto 10px'
              }}>
                <Key size={26} />
              </div>
              <h3 className="font-serif" style={{ fontSize: '20px', fontWeight: 700, color: '#111827' }}>
                {oneTimeCredential.isReset ? 'Password Reset Successfully' : 'User Created Successfully'}
              </h3>
              <p style={{ fontSize: '12px', color: '#4B5563', fontWeight: 500, marginTop: '4px' }}>
                Provide these credentials to the user. The temporary password will NOT be shown again.
              </p>
            </div>

            {/* Credential Card */}
            <div style={{
              backgroundColor: '#F9FAFB',
              borderRadius: '14px',
              border: '1px solid #E5E7EB',
              padding: '16px',
              display: 'flex',
              flexDirection: 'column',
              gap: '12px',
              marginBottom: '20px'
            }}>
              <div>
                <span style={{ fontSize: '11px', color: '#6B7280', fontWeight: 600, display: 'block' }}>Full Name</span>
                <span style={{ fontSize: '14px', fontWeight: 700, color: '#111827' }}>{oneTimeCredential.fullName}</span>
              </div>
              <div>
                <span style={{ fontSize: '11px', color: '#6B7280', fontWeight: 600, display: 'block' }}>Role</span>
                <span style={{ fontSize: '14px', fontWeight: 700, color: '#111827' }}>{oneTimeCredential.role}</span>
              </div>
              <div>
                <span style={{ fontSize: '11px', color: '#6B7280', fontWeight: 600, display: 'block' }}>Username (Firebase Login)</span>
                <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--color-amber-terracotta)', wordBreak: 'break-all' }}>
                  {oneTimeCredential.username}
                </span>
              </div>
              <div style={{
                backgroundColor: '#FEF2F2',
                border: '1px solid #FCA5A5',
                borderRadius: '10px',
                padding: '10px 12px'
              }}>
                <span style={{ fontSize: '11px', color: '#B91C1C', fontWeight: 700, display: 'block', textTransform: 'uppercase' }}>
                  Temporary Password (One-Time)
                </span>
                <span style={{ fontSize: '16px', fontWeight: 800, color: '#991B1B', letterSpacing: '1px', fontFamily: 'monospace' }}>
                  {oneTimeCredential.temporaryPassword}
                </span>
              </div>
            </div>

            <div style={{ display: 'flex', gap: '10px' }}>
              <button
                onClick={copyToClipboard}
                className="btn-primary"
                style={{
                  flex: 1,
                  padding: '10px',
                  fontWeight: 700,
                  fontSize: '13px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '6px',
                  backgroundColor: copied ? '#059669' : 'var(--color-amber-terracotta)'
                }}
              >
                {copied ? <Check size={16} /> : <Copy size={16} />}
                {copied ? 'Copied to Clipboard!' : 'Copy Credentials'}
              </button>
              <button
                onClick={() => setOneTimeCredential(null)}
                className="btn-secondary"
                style={{ padding: '10px 18px', color: '#111827', fontWeight: 700, fontSize: '13px' }}
              >
                Done
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
