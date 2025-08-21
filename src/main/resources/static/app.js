// BrainBuddy Frontend Application
class BrainBuddyApp {
    constructor() {
        this.currentUser = null;
        this.currentProfile = null;
        this.apiBase = 'http://localhost:8080/api';

        // Check authentication first
        if (!this.checkAuth()) {
            return;
        }

        this.initializeElements();
        this.attachEventListeners();
        this.initializeApp();
    }

    checkAuth() {
        const userData = localStorage.getItem('currentUser');
        if (!userData) {
            window.location.href = 'login.html';
            return false;
        }

        this.currentUser = JSON.parse(userData);
        return true;
    }

    initializeElements() {
        // Get DOM elements
        this.userInfo = document.getElementById('userInfo');
        this.userName = document.getElementById('userName');
        this.logoutBtn = document.getElementById('logoutBtn');
        this.swipeSection = document.getElementById('swipeSection');
        this.matchesSection = document.getElementById('matchesSection');
        this.profileCard = document.getElementById('profileCard');
        this.profileName = document.getElementById('profileName');
        this.profileBio = document.getElementById('profileBio');
        this.passBtn = document.getElementById('passBtn');
        this.likeBtn = document.getElementById('likeBtn');
        this.matchNotification = document.getElementById('matchNotification');
        this.matchedUserName = document.getElementById('matchedUserName');
        this.continueBtn = document.getElementById('continueBtn');
        this.noMoreProfiles = document.getElementById('noMoreProfiles');
        this.viewMatchesBtn = document.getElementById('viewMatchesBtn');
        this.matchesList = document.getElementById('matchesList');
        this.backToSwipeBtn = document.getElementById('backToSwipeBtn');
        this.loadingSpinner = document.getElementById('loadingSpinner');
        this.errorMessage = document.getElementById('errorMessage');
        this.errorText = document.getElementById('errorText');
        this.retryBtn = document.getElementById('retryBtn');
    }

    attachEventListeners() {
        this.logoutBtn.addEventListener('click', () => this.logout());
        this.passBtn.addEventListener('click', () => this.swipe(false));
        this.likeBtn.addEventListener('click', () => this.swipe(true));
        this.continueBtn.addEventListener('click', () => this.hideMatchNotification());
        this.viewMatchesBtn.addEventListener('click', () => this.showMatches());
        this.backToSwipeBtn.addEventListener('click', () => this.showSwipeSection());
        this.retryBtn.addEventListener('click', () => this.loadNextProfile());
    }

    initializeApp() {
        // Display user info
        this.userName.textContent = this.currentUser.name;

        // Start with swipe section
        this.showSwipeSection();
        this.loadNextProfile();
    }

    logout() {
        localStorage.removeItem('currentUser');
        localStorage.removeItem('authToken');
        window.location.href = 'login.html';
    }

    async loadNextProfile() {
        if (!this.currentUser) return;

        this.showLoading();

        try {
            const response = await fetch(`${this.apiBase}/swipes/potential/${this.currentUser.id}`);

            if (response.status === 404) {
                this.showNoMoreProfiles();
                return;
            }

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const profile = await response.json();
            this.currentProfile = profile;
            this.displayProfile(profile);
            this.hideLoading();

        } catch (error) {
            console.error('Error loading profile:', error);
            this.showError('Failed to load next profile. Please try again.');
        }
    }

    displayProfile(profile) {
        this.profileName.textContent = profile.name;
        this.profileBio.textContent = profile.bio || 'No bio available';
        this.profileCard.style.display = 'block';
    }

    async swipe(liked) {
        if (!this.currentProfile || !this.currentUser) return;

        this.showLoading();

        try {
            const response = await fetch(`${this.apiBase}/swipes`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    swiperId: this.currentUser.id,
                    targetId: this.currentProfile.id,
                    liked: liked
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();

            if (result.success) {
                if (result.isMatch) {
                    this.showMatchNotification(this.currentProfile.name);
                } else {
                    await this.loadNextProfile();
                }
            } else {
                this.showError(result.message || 'Failed to record swipe');
            }

        } catch (error) {
            console.error('Error recording swipe:', error);
            this.showError('Failed to record swipe. Please try again.');
        }
    }

    async showMatches() {
        if (!this.currentUser) return;

        this.hideAllSections();
        this.matchesSection.style.display = 'block';
        this.showLoading();

        try {
            const response = await fetch(`${this.apiBase}/matches/user/${this.currentUser.id}`);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const matches = await response.json();
            this.displayMatches(matches);
            this.hideLoading();

        } catch (error) {
            console.error('Error loading matches:', error);
            this.showError('Failed to load matches. Please try again.');
        }
    }

    displayMatches(matches) {
        this.matchesList.innerHTML = '';

        if (matches.length === 0) {
            this.matchesList.innerHTML = '<p style="text-align: center; color: #666;">No matches yet. Keep swiping!</p>';
            return;
        }

        matches.forEach(match => {
            const matchElement = document.createElement('div');
            matchElement.className = 'match-item';

            const matchDate = new Date(match.matchedAt).toLocaleDateString();

            matchElement.innerHTML = `
                <div>
                    <h3>${match.matchedUser.name}</h3>
                    <p>${match.matchedUser.bio || 'No bio available'}</p>
                </div>
                <div class="match-date">${matchDate}</div>
            `;

            this.matchesList.appendChild(matchElement);
        });
    }

    showMatchNotification(userName) {
        this.matchedUserName.textContent = userName;
        this.matchNotification.style.display = 'flex';
    }

    hideMatchNotification() {
        this.matchNotification.style.display = 'none';
        this.loadNextProfile();
    }

    showSwipeSection() {
        this.hideAllSections();
        this.swipeSection.style.display = 'block';
    }

    showNoMoreProfiles() {
        this.hideAllSections();
        this.noMoreProfiles.style.display = 'block';
    }

    showLoading() {
        this.loadingSpinner.style.display = 'block';
        this.profileCard.style.display = 'none';
        this.errorMessage.style.display = 'none';
    }

    hideLoading() {
        this.loadingSpinner.style.display = 'none';
    }

    showError(message) {
        this.hideLoading();
        this.profileCard.style.display = 'none';
        this.errorText.textContent = message;
        this.errorMessage.style.display = 'block';
    }

    hideAllSections() {
        this.swipeSection.style.display = 'none';
        this.matchesSection.style.display = 'none';
        this.noMoreProfiles.style.display = 'none';
        this.loadingSpinner.style.display = 'none';
        this.errorMessage.style.display = 'none';
        this.matchNotification.style.display = 'none';
    }
}

// Initialize the app when the page loads
document.addEventListener('DOMContentLoaded', () => {
    new BrainBuddyApp();
});

// Add some sample users data for testing
const SAMPLE_USERS = {
    1: { name: 'John Doe', bio: 'Love studying computer science!' },
    2: { name: 'Jane Smith', bio: 'Math enthusiast looking for study partners' },
    3: { name: 'Bob Wilson', bio: 'Engineering student, coffee addict' },
    4: { name: 'Alice Brown', bio: 'Psychology major, bookworm' }
};