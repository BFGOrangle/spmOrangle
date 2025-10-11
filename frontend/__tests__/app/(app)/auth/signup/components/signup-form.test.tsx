import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import SignUpForm from '@/app/(app)/auth/signup/components/signup-form';
import { useRouter } from 'next/navigation';
import { userManagementService } from '@/services/user-management-service';
import { BaseApiError, BaseValidationError } from '@/services/authenticated-api-client';

// Mock external dependencies
jest.mock('next/navigation', () => ({
  useRouter: jest.fn(),
}));

jest.mock('next/link', () => {
  return ({ children, href }: { children: React.ReactNode; href: string }) => (
    <a href={href}>{children}</a>
  );
});

jest.mock('@/services/user-management-service', () => ({
  userManagementService: {
    signUp: jest.fn(),
  },
}));

jest.mock('sonner', () => ({
  toast: jest.fn(),
}));

// Mock UI components to match the actual structure
jest.mock('@/components/ui/button', () => ({
  Button: ({ children, className, ...props }: any) => (
    <button className={className} {...props}>
      {children}
    </button>
  ),
}));

jest.mock('@/components/ui/input', () => ({
  Input: (props: any) => <input {...props} />,
}));

jest.mock('@/components/ui/label', () => ({
  Label: ({ children, htmlFor, className }: any) => (
    <label htmlFor={htmlFor} className={className}>
      {children}
    </label>
  ),
}));

jest.mock('@/components/ui/card', () => ({
  Card: ({ children, className }: any) => <div className={className}>{children}</div>,
  CardContent: ({ children }: any) => <div>{children}</div>,
  CardFooter: ({ children, className }: any) => <div className={className}>{children}</div>,
  CardTitle: ({ children, className }: any) => <h1 className={className}>{children}</h1>,
}));

jest.mock('@/components/error-message-callout', () => ({
  ErrorMessageCallout: ({ errorMessage }: any) => (
    <div role="alert" data-testid="error-message">{errorMessage}</div>
  ),
}));

const mockRouter = {
  push: jest.fn(),
  replace: jest.fn(),
  prefetch: jest.fn(),
  back: jest.fn(),
};

describe('SignUpForm', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (useRouter as jest.Mock).mockReturnValue(mockRouter);
  });

  it('renders signup form with all elements', () => {
    render(<SignUpForm />);

    expect(screen.getByText('Please create an account.')).toBeInTheDocument();
    expect(screen.getByLabelText('username')).toBeInTheDocument();
    expect(screen.getByLabelText('Email')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByLabelText('Confirm Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();
    expect(screen.getByText(/already have an account/i)).toBeInTheDocument();
  });

  it('handles successful signup', async () => {
    (userManagementService.signUp as jest.Mock).mockResolvedValue({});

    render(<SignUpForm />);

    // Fill in all required fields
    fireEvent.change(screen.getByLabelText('username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'john@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'Password123!@#' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'Password123!@#' } });

    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(userManagementService.signUp).toHaveBeenCalledWith({
        username: 'testuser',
        email: 'john@example.com',
        password: 'Password123!@#',
      });
      expect(mockRouter.push).toHaveBeenCalledWith('/auth/signin');
    });
  });

  it('handles validation errors from backend', async () => {
    const validationError = new BaseValidationError([
      { field: 'email', message: 'Email already exists', timestamp: new Date().toISOString() }
    ]);
    (userManagementService.signUp as jest.Mock).mockRejectedValue(validationError);

    render(<SignUpForm />);

    // Fill in form fields
    fireEvent.change(screen.getByLabelText('username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'existing@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'Password123!@#' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'Password123!@#' } });

    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('email: Email already exists')).toBeInTheDocument();
    });
  });

  it('validates password confirmation', async () => {
    render(<SignUpForm />);

    // Fill in form with mismatched passwords
    fireEvent.change(screen.getByLabelText('username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'john@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'Password123!@#' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'DifferentPassword!' } });

    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Passwords do not match.')).toBeInTheDocument();
    });

    // userManagementService.signUp should not be called if validation fails
    expect(userManagementService.signUp).not.toHaveBeenCalled();
  });

  it('handles API errors from backend', async () => {
    const apiError = new BaseApiError(500, 'Internal Server Error', [
      { message: 'Server error occurred', timestamp: new Date().toISOString() }
    ]);
    (userManagementService.signUp as jest.Mock).mockRejectedValue(apiError);

    render(<SignUpForm />);

    // Fill in all required fields
    fireEvent.change(screen.getByLabelText('username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'john@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'Password123!@#' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'Password123!@#' } });

    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Server error occurred')).toBeInTheDocument();
    });
  });

  it('handles unknown errors', async () => {
    (userManagementService.signUp as jest.Mock).mockRejectedValue(new Error('Network error'));

    render(<SignUpForm />);

    // Fill in all required fields
    fireEvent.change(screen.getByLabelText('username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'john@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'Password123!@#' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'Password123!@#' } });

    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('An unknown error occurred. Please try again.')).toBeInTheDocument();
    });
  });

  it('validates password requirements', async () => {
    render(<SignUpForm />);

    // Fill in form with weak password
    fireEvent.change(screen.getByLabelText('username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'john@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'weak' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'weak' } });

    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    // Should show password validation error
    await waitFor(() => {
      expect(screen.getByText(/Password must be at least 8 characters long/)).toBeInTheDocument();
    });

    // userManagementService.signUp should not be called if password validation fails
    expect(userManagementService.signUp).not.toHaveBeenCalled();
  });

  it('has proper form structure and accessibility', () => {
    render(<SignUpForm />);

    const form = document.querySelector('form');
    expect(form).toBeInTheDocument();

    // Check all required fields
    const usernameInput = screen.getByLabelText('username');
    expect(usernameInput).toHaveAttribute('type', 'text');
    expect(usernameInput).toHaveAttribute('name', 'username');
    expect(usernameInput).toHaveAttribute('required');
    expect(usernameInput).toHaveAttribute('minLength', '4');

    const emailInput = screen.getByLabelText('Email');
    expect(emailInput).toHaveAttribute('type', 'email');
    expect(emailInput).toHaveAttribute('name', 'email');
    expect(emailInput).toHaveAttribute('required');

    const passwordInput = screen.getByLabelText('Password');
    expect(passwordInput).toHaveAttribute('type', 'password');
    expect(passwordInput).toHaveAttribute('name', 'password');
    expect(passwordInput).toHaveAttribute('required');

    const confirmPasswordInput = screen.getByLabelText('Confirm Password');
    expect(confirmPasswordInput).toHaveAttribute('type', 'password');
    expect(confirmPasswordInput).toHaveAttribute('name', 'confirmPassword');
    expect(confirmPasswordInput).toHaveAttribute('required');
  });

  it('has navigation link to signin', () => {
    render(<SignUpForm />);

    const signinLink = screen.getByRole('link', { name: /log in/i });
    expect(signinLink).toHaveAttribute('href', '/auth/signin');
  });

  it('includes all form data in submission', async () => {
    (userManagementService.signUp as jest.Mock).mockResolvedValue({});

    render(<SignUpForm />);

    fireEvent.change(screen.getByLabelText('username'), { target: { value: 'jane.smith' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'jane.smith@test.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'SecurePass123!@#' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'SecurePass123!@#' } });

    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(userManagementService.signUp).toHaveBeenCalledWith({
        username: 'jane.smith',
        email: 'jane.smith@test.com',
        password: 'SecurePass123!@#',
      });
    });
  });

  it('clears error when starting new submission', async () => {
    render(<SignUpForm />);

    // Fill in required fields first
    fireEvent.change(screen.getByLabelText('username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'john@example.com' } });
    
    // First, trigger a validation error
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'Password123!@#' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'Different!' } });

    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Passwords do not match.');
    });

    // Now fix the passwords and submit again
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'Password123!@#' } });
    fireEvent.change(screen.getByLabelText('username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'john@example.com' } });

    (userManagementService.signUp as jest.Mock).mockResolvedValueOnce({});

    fireEvent.click(submitButton);

    // Error should be cleared when new submission starts
    await waitFor(() => {
      expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    });

    await waitFor(() => {
      expect(mockRouter.push).toHaveBeenCalledWith('/auth/signin');
    });
  });

  it('renders with proper styling and layout', () => {
    render(<SignUpForm />);

    // Check for main card structure
    const titleElement = screen.getByText('Please create an account.');
    expect(titleElement).toBeInTheDocument();

    // Check for all form inputs
    expect(screen.getByLabelText('username')).toBeInTheDocument();
    expect(screen.getByLabelText('Email')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByLabelText('Confirm Password')).toBeInTheDocument();

    // Check for submit button
    const submitButton = screen.getByRole('button', { name: /create account/i });
    expect(submitButton).toBeInTheDocument();
  });
});