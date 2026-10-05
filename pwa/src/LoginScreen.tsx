import React, { useState } from 'react';
import { Lock, Mail, AlertCircle, ShieldCheck, CheckCircle2, Eye, EyeOff } from 'lucide-react';
import { authenticateWithFirebase, AppUser } from './authService';

interface LoginScreenProps {
  onLoginSuccess: (user: AppUser) => void;
}

export const LoginScreen: React.FC<LoginScreenProps> = ({ onLoginSuccess }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const user = await authenticateWithFirebase(email, password);
      onLoginSuccess(user);
    } catch (err: any) {
      setError(err.message || 'Authentication failed. Please verify credentials.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      backgroundColor: 'var(--color-warm-ivory)',
      padding: '20px',
      fontFamily: 'Inter, system-ui, sans-serif'
    }}>
      <div style={{
        maxWidth: '440px',
        width: '100%',
        backgroundColor: '#FFFFFF',
        borderRadius: '24px',
        padding: '36px 28px',
        boxShadow: 'var(--shadow-lg)',
        border: '1px solid var(--color-border-subtle)'
      }}>
        {/* Header with Logo */}
        <div style={{ textAlign: 'center', marginBottom: '28px' }}>
          <div style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '12px',
            backgroundColor: '#FAFAF9',
            borderRadius: '20px',
            border: '1px solid #E7E5E4',
            marginBottom: '14px',
            boxShadow: '0 2px 8px rgba(0,0,0,0.04)'
          }}>
            <img 
              src={`${import.meta.env.BASE_URL}Happy-paws-logo-transparent1.png`} 
              alt="Happy Paws Liberia Logo" 
              style={{ height: '78px', width: 'auto', display: 'block', objectFit: 'contain' }}
              onError={(e) => {
                // If base url failed, fallback to direct root
                const img = e.currentTarget;
                if (!img.dataset.fallback) {
                  img.dataset.fallback = 'true';
                  img.src = '/Happy-paws-logo-transparent1.png';
                }
              }}
            />
          </div>
          <h1 className="font-serif" style={{ fontSize: '24px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>
            Happy Paws Liberia
          </h1>
          <p style={{ fontSize: '13px', color: '#4B5563', fontWeight: 500 }}>
            Veterinary Clinic & Health Passport · Paynesville City, Liberia
          </p>
        </div>

        {/* Error Alert */}
        {error && (
          <div style={{
            display: 'flex',
            alignItems: 'flex-start',
            gap: '10px',
            backgroundColor: '#FEF2F2',
            border: '1px solid #FCA5A5',
            borderRadius: '12px',
            padding: '12px',
            marginBottom: '20px',
            color: '#B91C1C',
            fontSize: '13px'
          }}>
            <AlertCircle size={18} style={{ flexShrink: 0, marginTop: '2px' }} />
            <div>
              <strong>Login Failed:</strong> {error}
            </div>
          </div>
        )}

        {/* Real Firebase Login Form */}
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>
              Username / Email Address
            </label>
            <div style={{ position: 'relative' }}>
              <Mail size={16} style={{ position: 'absolute', left: '12px', top: '13px', color: '#6B7280' }} />
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="firstname.lastname@happypaws-liberia.org"
                required
                style={{
                  width: '100%',
                  padding: '10px 12px 10px 38px',
                  borderRadius: '10px',
                  border: '1px solid #D1D5DB',
                  fontSize: '14px',
                  color: '#111827',
                  fontWeight: 500,
                  outline: 'none',
                  backgroundColor: '#FFFFFF',
                  boxSizing: 'border-box'
                }}
              />
            </div>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>
              Password
            </label>
            <div style={{ position: 'relative' }}>
              <Lock size={16} style={{ position: 'absolute', left: '12px', top: '13px', color: '#6B7280' }} />
              <input
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter account password"
                required
                style={{
                  width: '100%',
                  padding: '10px 40px 10px 38px',
                  borderRadius: '10px',
                  border: '1px solid #D1D5DB',
                  fontSize: '14px',
                  color: '#111827',
                  fontWeight: 500,
                  outline: 'none',
                  backgroundColor: '#FFFFFF',
                  boxSizing: 'border-box'
                }}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                title={showPassword ? 'Hide password' : 'Show password'}
                style={{
                  position: 'absolute',
                  right: '12px',
                  top: '11px',
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  padding: '2px',
                  color: '#4B5563',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          <button
            type="submit"
            disabled={isLoading}
            style={{
              marginTop: '8px',
              padding: '12px',
              borderRadius: '12px',
              backgroundColor: 'var(--color-amber-terracotta)',
              color: '#FFFFFF',
              fontWeight: 700,
              fontSize: '15px',
              border: 'none',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '8px',
              opacity: isLoading ? 0.7 : 1
            }}
          >
            {isLoading ? 'Authenticating...' : (
              <>
                <ShieldCheck size={18} /> Sign In
              </>
            )}
          </button>
        </form>

        {/* Quick Test / Demo Accounts Helper */}
        <div style={{
          marginTop: '20px',
          padding: '12px 14px',
          backgroundColor: '#FFFBEB',
          borderRadius: '14px',
          border: '1px solid #FDE68A'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
            <span style={{ fontSize: '11px', fontWeight: 700, color: '#92400E', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Authorized Test Roles (1-Click Fill)
            </span>
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
            {[
              { label: '👑 Admin', email: 'super.admin@happypaws-liberia.org', pass: 'HP!Admin2026#' },
              { label: '🏥 Owner', email: 'clinic.owner@happypaws-liberia.org', pass: 'HP!Owner2026#' },
              { label: '🩺 Vet', email: 'veterinarian@happypaws-liberia.org', pass: 'HP!Vet2026#' },
              { label: '📋 Reception', email: 'receptionist@happypaws-liberia.org', pass: 'HP!Reception2026#' },
              { label: '🐾 Pet Owner', email: 'pet.owner@happypaws-liberia.org', pass: 'HP!Pet2026#' }
            ].map((acc) => (
              <button
                key={acc.email}
                type="button"
                onClick={() => {
                  setEmail(acc.email);
                  setPassword(acc.pass);
                  setError('');
                }}
                style={{
                  padding: '5px 10px',
                  backgroundColor: '#FFFFFF',
                  border: '1px solid #FCD34D',
                  borderRadius: '8px',
                  fontSize: '11px',
                  fontWeight: 600,
                  color: '#78350F',
                  cursor: 'pointer',
                  transition: 'all 0.15s ease'
                }}
              >
                {acc.label}
              </button>
            ))}
          </div>
        </div>

        {/* Security Notice */}
        <div style={{
          marginTop: '16px',
          padding: '14px',
          backgroundColor: '#F9FAFB',
          borderRadius: '12px',
          border: '1px solid #E5E7EB',
          fontSize: '11px',
          color: '#4B5563',
          lineHeight: 1.5
        }}>
          <div style={{ fontWeight: 700, color: '#111827', marginBottom: '4px', display: 'flex', alignItems: 'center', gap: '5px' }}>
            <CheckCircle2 size={13} color="var(--color-forest-sage)" /> Official Liberia Animal Welfare & Health System
          </div>
          Authorized staff and registered pet owners only. Secure authentication credentials strictly enforced.
        </div>
      </div>
    </div>
  );
};
