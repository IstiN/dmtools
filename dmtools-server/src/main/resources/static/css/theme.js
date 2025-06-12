/**
 * DMTools - Theme Switcher Script
 * Unifies theme switching functionality across all pages 
 */

// Function to toggle between light and dark themes
function toggleTheme() {
    const body = document.body;
    const isDarkTheme = body.classList.contains('dark-theme');
    const themeIcon = document.querySelector('.theme-switch i');
    const themeText = document.querySelector('.theme-text');
    
    // Toggle theme class
    body.classList.toggle('dark-theme');
    
    // Update icon and text
    if (isDarkTheme) {
        // Switching to light theme
        themeIcon.className = 'fas fa-moon';
        if (themeText) themeText.textContent = 'Dark mode';
        localStorage.setItem('dmtools-theme', 'light');
    } else {
        // Switching to dark theme
        themeIcon.className = 'fas fa-sun';
        if (themeText) themeText.textContent = 'Light mode';
        localStorage.setItem('dmtools-theme', 'dark');
    }
}

// Function to set theme based on user preference
function setThemeBasedOnPreference() {
    // Check for system preference
    const prefersDarkScheme = window.matchMedia('(prefers-color-scheme: dark)').matches;
    
    // Check for stored preference (this takes precedence)
    const savedTheme = localStorage.getItem('dmtools-theme');
    
    if (savedTheme === 'dark' || (savedTheme === null && prefersDarkScheme)) {
        document.body.classList.add('dark-theme');
        updateThemeUI(true);
    } else {
        document.body.classList.remove('dark-theme');
        updateThemeUI(false);
    }
}

// Update UI elements based on theme
function updateThemeUI(isDarkTheme) {
    const themeIcon = document.querySelector('.theme-switch i');
    const themeText = document.querySelector('.theme-text');
    
    if (isDarkTheme) {
        if (themeIcon) themeIcon.className = 'fas fa-sun';
        if (themeText) themeText.textContent = 'Light mode';
    } else {
        if (themeIcon) themeIcon.className = 'fas fa-moon';
        if (themeText) themeText.textContent = 'Dark mode';
    }
}

// Initialize theme on page load
document.addEventListener('DOMContentLoaded', function() {
    setThemeBasedOnPreference();
    
    // Add event listener to theme switch buttons
    const themeSwitch = document.getElementById('theme-toggle');
    if (themeSwitch) {
        themeSwitch.addEventListener('click', toggleTheme);
    }
}); 