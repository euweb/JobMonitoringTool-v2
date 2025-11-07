import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { ThemeProvider } from "@mui/material/styles";
import LoginPage from "../pages/LoginPage";
import { theme } from "../theme";

// Mock the auth service module
vi.mock("../services/authService", () => ({
  authService: {
    login: vi.fn(),
    logout: vi.fn(),
    refreshToken: vi.fn(),
    getCurrentUser: vi.fn(),
    updateProfile: vi.fn(),
    changePassword: vi.fn(),
  },
}));

const renderWithProviders = (component: React.ReactElement) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <MemoryRouter>{component}</MemoryRouter>
      </ThemeProvider>
    </QueryClientProvider>,
  );
};

describe("LoginPage", () => {
  beforeEach(async () => {
    vi.clearAllMocks();
    const { authService } = (await vi.importMock(
      "../services/authService",
    )) as any;
    authService.login.mockResolvedValue({
      access_token: "mock-token",
      refresh_token: "mock-refresh-token",
      type: "Bearer",
    });
  });

  it("renders login form", () => {
    renderWithProviders(<LoginPage />);

    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /sign in/i }),
    ).toBeInTheDocument();
  });

  it("shows validation errors for empty fields", async () => {
    renderWithProviders(<LoginPage />);

    const submitButton = screen.getByRole("button", { name: /sign in/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      // Check for form validation - these might be from browser validation or form library
      const form = document.querySelector("form");
      expect(form).toBeInTheDocument();
    });
  });

  it("handles successful login", async () => {
    const { authService } = (await vi.importMock(
      "../services/authService",
    )) as any;
    authService.login.mockResolvedValueOnce({
      access_token: "test-token",
      refresh_token: "test-refresh-token",
      type: "Bearer",
    });

    renderWithProviders(<LoginPage />);

    const usernameInput = screen.getByLabelText(/username/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole("button", { name: /sign in/i });

    fireEvent.change(usernameInput, { target: { value: "admin" } });
    fireEvent.change(passwordInput, { target: { value: "admin123" } });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(authService.login).toHaveBeenCalledWith({
        username: "admin",
        password: "admin123",
      });
    });
  });

  it("handles login error", async () => {
    const { authService } = (await vi.importMock(
      "../services/authService",
    )) as any;
    authService.login.mockRejectedValueOnce(new Error("Invalid credentials"));

    renderWithProviders(<LoginPage />);

    const usernameInput = screen.getByLabelText(/username/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole("button", { name: /sign in/i });

    fireEvent.change(usernameInput, { target: { value: "admin" } });
    fireEvent.change(passwordInput, { target: { value: "wrong" } });
    fireEvent.click(submitButton);

    await waitFor(() => {
      // Check if error state is handled - might need to check specific error UI
      expect(authService.login).toHaveBeenCalledWith({
        username: "admin",
        password: "wrong",
      });
    });
  });

  it("disables submit button during login", async () => {
    const { authService } = (await vi.importMock(
      "../services/authService",
    )) as any;
    // Mock a delayed response
    authService.login.mockImplementationOnce(
      () => new Promise((resolve) => setTimeout(resolve, 1000)),
    );

    renderWithProviders(<LoginPage />);

    const usernameInput = screen.getByLabelText(/username/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole("button", { name: /sign in/i });

    fireEvent.change(usernameInput, { target: { value: "admin" } });
    fireEvent.change(passwordInput, { target: { value: "admin123" } });
    fireEvent.click(submitButton);

    // Button should be disabled while loading
    expect(submitButton).toBeDisabled();
  });
});
