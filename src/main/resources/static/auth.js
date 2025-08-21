// Authentication Manager
class AuthManager {
    constructor(mode) {
        this.mode = mode; // 'login' or 'register'
        this.apiBase = 'http://localhost:8080/api';
        
        this.initializeElements();
        this.attachEventListeners();
        
        // Check if user is already logged in
        this.checkAuthStatus();
    }

    initializeElements() {
        if (this.mode === 'login') {
            this.form = document.getElementById('loginForm');
            this.emailInput = document.getElementById('email');
            this.passwordInput = document.getElementById('password');
            this.submitBtn = document.getElementById('loginBtn');
        } else {
            this.form = document.getElementById('registerForm');
            this.nameInput = document.getElementById('name');
            this.emailInput = document.getElementById('email');
            this.passwordInput = document.getElementById('password');
            this.bioInput = document.getElementById('bio');
            this.submitBtn = document.getElementById('registerBtn');
            this.charCount = document.querySelector('.char-count');
        }
        
        this.btnText = this.submitBtn.querySelector('.btn-text');
        this.btnSpinner = this.submitBtn.querySelector('.btn-spinner');
        this.generalError = document.getElementById('generalError');
    }

    attachEventListeners() {
        this.form.addEventListener('submit', (e) => this.handleSubmit(e));
        
        // Real-time validation
        this.emailInput.addEventListener('blur', () => this.validateEmail());
        this.passwordInput.addEventListener('input', () => this.validatePassword());
        
        if (this.mode === 'register') {
            this.nameInput.addEventListener('blur', () => this.validateName());
            this.bioInput.addEventListener('input', () => this.updateCharCount());
            this.emailInput.addEventListener('blur', () => this.checkEmailExists());
        }
    }

    async handleSubmit(e) {
        e.preventDefault();
        
        if (!this.validateForm()) {
            return;
        }
        
        this.setLoading(true);
        
        try {
            if (this.mode === 'login') {
                await this.handleLogin();
            } else {
                await this.handleRegister();
            }
        } catch (error) {
            console.error('Auth error:', error);
            this.showError('An unexpected error occurred. Please try again.');
        } finally {
            this.setLoading(false);
        }
    }

    async handleLogin() {
        const loginData = {
            email: this.emailInput.value.trim(),
            password: this.passwordInput.value
        };

        const response = await fetch(`${this.apiBase}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(loginData)
        });

        const result = await response.json();

        if (result.success) {
            // Store user data
            localStorage.setItem('currentUser', JSON.stringify(result.user));
            localStorage.setItem('authToken', result.token);
            
            // Redirect to main app
            window.location.href = 'index.html';
        } else {
            this.showError(result.message || 'Login failed');
        }
    }

    async handleRegister() {
        const registerData = {
            name: this.nameInput.value.trim(),
            email: this.emailInput.value.trim(),
            password: this.passwordInput.value,
            bio: this.bioInput.value.trim()
        };

        const response = await fetch(`${this.apiBase}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(registerData)
        });

        const result = await response.json();

        if (result.success) {
            // Store user data
            localStorage.setItem('currentUser', JSON.stringify(result.user));
            localStorage.setItem('authToken', result.token);
            
            // Show success message and redirect
            this.showSuccess('Account created successfully! Redirecting...');
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 2000);
        } else {
            this.showError(result.message || 'Registration failed');
        }
    }

    validateForm() {
        let isValid = true;
        
        if (this.mode === 'register') {
            isValid = this.validateName() && isValid;
        }
        
        isValid = this.validateEmail() && isValid;
        isValid = this.validatePassword() && isValid;
        
        return isValid;
    }

    validateName() {
        const name = this.nameInput.value.trim();
        const errorElement = document.getElementById('nameError');
        
        if (name.length < 2) {
            this.showFieldError('nameError', 'Name must be at least 2 characters long');
            this.nameInput.classList.add('invalid');
            return false;
        }
        
        if (name.length > 50) {
            this.showFieldError('nameError', 'Name cannot exceed 50 characters');
            this.nameInput.classList.add('invalid');
            return false;
        }
        
        this.clearFieldError('nameError');
        this.nameInput.classList.remove('invalid');
        this.nameInput.classList.add('valid');
        return true;
    }

    validateEmail() {
        const email = this.emailInput.value.trim();
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        
        if (!emailRegex.test(email)) {
            this.showFieldError('emailError', 'Please enter a valid email address');
            this.emailInput.classList.add('invalid');
            return false;
        }
        
        this.clearFieldError('emailError');
        this.emailInput.classList.remove('invalid');
        this.emailInput.classList.add('valid');
        return true;
    }

    validatePassword() {
        const password = this.passwordInput.value;
        
        if (password.length < 6) {
            this.showFieldError('passwordError', 'Password must be at least 6 characters long');
            this.passwordInput.classList.add('invalid');
            return false;
        }
        
        this.clearFieldError('passwordError');
        this.passwordInput.classList.remove('invalid');
        this.passwordInput.classList.add('valid');
        return true;
    }

    async checkEmailExists() {
        if (!this.validateEmail()) return;
        
        const email = this.emailInput.value.trim();
        
        try {
            const response = await fetch(`${this.apiBase}/auth/check-email?email=${encodeURIComponent(email)}`);
            const exists = await response.json();
            
            if (exists) {
                this.showFieldError('emailError', 'This email is already registered');
                this.emailInput.classList.add('invalid');
                return false;
            }
        } catch (error) {
            console.error('Error checking email:', error);
        }
        
        return true;
    }

    updateCharCount() {
        const bioLength = this.bioInput.value.length;
        this.charCount.textContent = `${bioLength}/500 characters`;
        
        if (bioLength > 500) {
            this.charCount.style.color = '#e74c3c';
            this.showFieldError('bioError', 'Bio cannot exceed 500 characters');
            return false;
        } else {
            this.charCount.style.color = '#999';
            this.clearFieldError('bioError');
            return true;
        }
    }

    showFieldError(fieldId, message) {
        const errorElement = document.getElementById(fieldId);
        if (errorElement) {
            errorElement.textContent = message;
        }
    }

    clearFieldError(fieldId) {
        const errorElement = document.getElementById(fieldId);
        if (errorElement) {
            errorElement.textContent = '';
        }
    }

    showError(message) {
        this.generalError.textContent = message;
        this.generalError.style.display = 'block';
    }

    showSuccess(message) {
        // Create success message element
        const successDiv = document.createElement('div');
        successDiv.className = 'success-message';
        successDiv.textContent = message;
        
        // Insert before the form
        this.form.parentNode.insertBefore(successDiv, this.form);
        
        // Hide form
        this.form.style.display = 'none';
    }

    setLoading(loading) {
        if (loading) {
            this.submitBtn.disabled = true;
            this.btnText.style.opacity = '0';
            this.btnSpinner.style.display = 'block';
            this.form.classList.add('form-loading');
        } else {
            this.submitBtn.disabled = false;
            this.btnText.style.opacity = '1';
            this.btnSpinner.style.display = 'none';
            this.form.classList.remove('form-loading');
        }
    }

    checkAuthStatus() {
        const currentUser = localStorage.getItem('currentUser');
        if (currentUser && window.location.pathname.includes('login.html')) {
            // User is already logged in, redirect to main app
            window.location.href = 'index.html';
        }
    }
}

// Utility function to logout
function logout() {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('authToken');
    window.location.href = 'login.html';
}