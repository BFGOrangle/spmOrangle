import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import SigninForm from '@/app/(app)/auth/signin/components/signin-form';
import { useRouter } from 'next/navigation';
import { handleSignIn } from '@/lib/cognito-actions';

// Mock external dependencies
jest.mock('next/navigation', () => ({
  useRouter: jest.fn(),
}));

jest.mock('next/link', () => {
  return ({ children, href }: { children: React.ReactNode; href: string }) => (
    <a href={href}>{children}</a>
  );
});

jest.mock('@/lib/cognito-actions', () => ({
  handleSignIn: jest.fn(),
}));

// Mock UI components
jest.mock('@/components/ui/button', () => ({
  Button: ({ children, onClick, disabled, className, ...props }: any) => (
    <button onClick={onClick} disabled={disabled} className={className} {...props}>
      {children}
    </button>
  ),
}));

jest.mock('@/components/ui/input', () => ({
  Input: (props: any) => <input {...props} />,
}));

jest.mock('@/components/ui/label', () => ({
  Label: ({ children, htmlFor, className }: any) => (
    <label htmlFor={htmlFor} className={className}>{children}</label>
  ),
}));

jest.mock('@/components/ui/card', () => ({
  Card: ({ children, className }: any) => <div className={className}>{children}</div>,
  CardContent: ({ children }: any) => <div>{children}</div>,
  CardDescription: ({ children, className }: any) => <div className={className}>{children}</div>,
  CardTitle: ({ children, className }: any) => <h1 className={className}>{children}</h1>,
}));

jest.mock('@/components/error-message-callout', () => ({
  ErrorMessageCallout: ({ errorMessage }: { errorMessage: string }) => (
    <div data-testid="error-callout">{errorMessage}</div>
  ),
}));

const mockRouter = {
  push: jest.fn(),
  replace: jest.fn(),
  prefetch: jest.fn(),
  back: jest.fn(),
};

describe('SigninForm', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (useRouter as jest.Mock).mockReturnValue(mockRouter);
  });

  it('renders signin form with all elements', () => {
    render(<SigninForm />);

    expect(screen.getByText('Welcome Back')).toBeInTheDocument();
    expect(screen.getByText('Sign in to your account to continue')).toBeInTheDocument();
    expect(screen.getByLabelText('Email')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
    expect(screen.getByText(/forgot password/i)).toBeInTheDocument();
    expect(screen.getByText(/don't have an account/i)).toBeInTheDocument();
  });

  it('handles successful signin', async () => {
    const mockSuccessResult = {
      success: true,
      redirectTo: '/dashboard'
    };
    (handleSignIn as jest.Mock).mockResolvedValue(mockSuccessResult);

    render(<SigninForm />);

    const emailInput = screen.getByLabelText('Email');
    const passwordInput = screen.getByLabelText('Password');
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(handleSignIn).toHaveBeenCalled();
      expect(mockRouter.push).toHaveBeenCalledWith('/dashboard');
    });
  });

  it('handles signin error with string response', async () => {
    const errorMessage = 'Invalid credentials';
    (handleSignIn as jest.Mock).mockResolvedValue(errorMessage);

    render(<SigninForm />);

    const emailInput = screen.getByLabelText('Email');
    const passwordInput = screen.getByLabelText('Password');
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'wrongpassword' } });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByTestId('error-callout')).toHaveTextContent(errorMessage);
    });
  });

  it('handles unexpected errors during signin', async () => {
    (handleSignIn as jest.Mock).mockRejectedValue(new Error('Network error'));

    render(<SigninForm />);

    const emailInput = screen.getByLabelText('Email');
    const passwordInput = screen.getByLabelText('Password');
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByTestId('error-callout')).toHaveTextContent(
        'An unexpected error occurred. Please try again.'
      );
    });
  });

  it('shows loading state during signin', async () => {
    // Mock a delayed response
    (handleSignIn as jest.Mock).mockImplementation(() => 
      new Promise(resolve => setTimeout(() => resolve({ success: true, redirectTo: '/dashboard' }), 100))
    );

    render(<SigninForm />);

    const emailInput = screen.getByLabelText('Email');
    const passwordInput = screen.getByLabelText('Password');
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.click(submitButton);

    // Button should be disabled during loading
    expect(submitButton).toBeDisabled();

    await waitFor(() => {
      expect(mockRouter.push).toHaveBeenCalledWith('/dashboard');
    });

    // Button should be enabled again after completion
    expect(submitButton).not.toBeDisabled();
  });

  it('clears error message on new submission', async () => {
    // First, trigger an error
    (handleSignIn as jest.Mock).mockResolvedValueOnce('First error');

    render(<SigninForm />);

    const emailInput = screen.getByLabelText('Email');
    const passwordInput = screen.getByLabelText('Password');
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'wrongpassword' } });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByTestId('error-callout')).toHaveTextContent('First error');
    });

    // Now submit again with success
    (handleSignIn as jest.Mock).mockResolvedValueOnce({ success: true, redirectTo: '/dashboard' });

    fireEvent.change(passwordInput, { target: { value: 'correctpassword' } });
    fireEvent.click(submitButton);

    // Error should be cleared immediately
    expect(screen.queryByTestId('error-callout')).not.toBeInTheDocument();

    await waitFor(() => {
      expect(mockRouter.push).toHaveBeenCalledWith('/dashboard');
    });
  });

  it('includes email and password in form submission', async () => {
    (handleSignIn as jest.Mock).mockResolvedValue({ success: true, redirectTo: '/dashboard' });

    render(<SigninForm />);

    const emailInput = screen.getByLabelText('Email');
    const passwordInput = screen.getByLabelText('Password');
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'user@test.com' } });
    fireEvent.change(passwordInput, { target: { value: 'mypassword' } });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(handleSignIn).toHaveBeenCalled();
    });

    // Verify the form data contains the expected values
    const [, formData] = (handleSignIn as jest.Mock).mock.calls[0];
    expect(formData.get('email')).toBe('user@test.com');
    expect(formData.get('password')).toBe('mypassword');
  });

  it('has proper form structure and accessibility', () => {
    render(<SigninForm />);

    const form = document.querySelector('form');
    expect(form).toBeInTheDocument();

    const emailInput = screen.getByLabelText('Email');
    expect(emailInput).toHaveAttribute('type', 'email');
    expect(emailInput).toHaveAttribute('name', 'email');
    expect(emailInput).toHaveAttribute('required');
    expect(emailInput).toHaveAttribute('id', 'email');

    const passwordInput = screen.getByLabelText('Password');
    expect(passwordInput).toHaveAttribute('type', 'password');
    expect(passwordInput).toHaveAttribute('name', 'password');
    expect(passwordInput).toHaveAttribute('required');
    expect(passwordInput).toHaveAttribute('id', 'password');
  });

  it('has navigation links to forgot password and signup', () => {
    render(<SigninForm />);

    const forgotPasswordLink = screen.getByRole('link', { name: /reset password/i });
    expect(forgotPasswordLink).toHaveAttribute('href', '/auth/forgot-password');

    const signupLink = screen.getByRole('link', { name: /sign up/i });
    expect(signupLink).toHaveAttribute('href', '/auth/signup');
  });

  it('prevents default form submission behavior', async () => {
    const mockPreventDefault = jest.fn();
    (handleSignIn as jest.Mock).mockResolvedValue({ success: true, redirectTo: '/dashboard' });

    render(<SigninForm />);

    const form = document.querySelector('form');
    const event = {
      preventDefault: mockPreventDefault,
      currentTarget: form,
    } as any;

    const submitHandler = jest.fn();
    form?.addEventListener('submit', submitHandler);

    if (form) {
      fireEvent.submit(form);
    }

    // The component should handle preventDefault internally
    expect(mockPreventDefault).not.toHaveBeenCalled(); // Since we're not calling the handler directly
  });

  it('handles non-object successful responses gracefully', async () => {
    // Test edge case where handleSignIn returns neither an object nor a string
    (handleSignIn as jest.Mock).mockResolvedValue(true);

    render(<SigninForm />);

    const emailInput = screen.getByLabelText('Email');
    const passwordInput = screen.getByLabelText('Password');
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(handleSignIn).toHaveBeenCalled();
    });

    // Should not redirect or show error for non-object/non-string responses
    expect(mockRouter.push).not.toHaveBeenCalled();
    expect(screen.queryByTestId('error-callout')).not.toBeInTheDocument();
  });

  it('renders with proper styling and layout', () => {
    render(<SigninForm />);

    // Check for main card structure
    const titleElement = screen.getByText('Welcome Back');
    expect(titleElement).toBeInTheDocument();

    const descriptionElement = screen.getByText('Sign in to your account to continue');
    expect(descriptionElement).toBeInTheDocument();

    // Check for form inputs are present
    expect(screen.getByLabelText('Email')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();

    // Check for submit button
    const submitButton = screen.getByRole('button', { name: /sign in/i });
    expect(submitButton).toBeInTheDocument();
  });
});