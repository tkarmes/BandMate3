<template>
  <div id="app">
    <h1>Bandmate</h1>
    <nav v-if="loggedIn" class="nav">
      <router-link to="/users">Browse Users</router-link>
      <router-link to="/conversations">Conversations</router-link>
      <router-link :to="profileRoute">My Profile</router-link>
      <button @click="logout" class="logout-btn">Logout</button>
    </nav>
    <router-view
      @login-success="onLoginSuccess"
      :loggedIn="loggedIn"
      :userType="userType"
      @update:profile="updateProfile"
    />
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'App',
  data() {
    return {
      loggedIn: !!localStorage.getItem('token'),
      userType: null,
      userId: localStorage.getItem('userId')
    };
  },
  computed: {
    profileRoute() {
      if (!this.loggedIn || !this.userId || !this.userType) return '/';
      return this.userType === 'Musician'
        ? { name: 'MusicianProfile', params: { userId: this.userId } }
        : { name: 'VenueProfile', params: { userId: this.userId } };
    }
  },
  created() {
    if (this.loggedIn) {
      this.fetchUserType();
    }
  },
  methods: {
    async fetchUserType() {
      const token = localStorage.getItem('token');
      const userId = localStorage.getItem('userId');
      if (!token || !userId) return;
      try {
        const response = await axios.get(`http://localhost:9000/users/${userId}`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        this.userType = response.data.userType;
      } catch (err) {
        console.error('User type fetch failed:', err.response?.data || err.message);
        this.logout(); // Log out if token is invalid
      }
    },
    onLoginSuccess() {
      console.log('Login successful');
      this.loggedIn = true;
      this.userId = localStorage.getItem('userId');
      this.fetchUserType();
    },
    logout() {
      localStorage.clear();
      this.loggedIn = false;
      this.userType = null;
      this.userId = null;
      this.$router.push({ name: 'Login' });
    },
    updateProfile() {
      // Refresh profile if needed
      this.fetchUserType();
    }
  }
};
</script>

<style>
:root {
  --primary: #457b9d;
  --secondary: #e63946;
  --success: #2a9d8f;
  --background: #1a1a1a;
  --text: #f0f0f0;
  --accent: #a3bffa;
  --card-bg: #2c2c2c;
}

html, body {
  margin: 0;
  padding: 0;
  background-color: var(--background);
}

#app {
  width: 100%;
  margin: 0;
  padding: 20px;
  font-family: 'Roboto', Arial, sans-serif;
  text-align: center;
  background-color: var(--background);
  color: var(--text);
  min-height: 100vh;
}

h1 {
  color: var(--text);
  margin-bottom: 20px;
  font-size: 2.5em;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.nav {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-bottom: 20px;
}

.nav a {
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
}

.nav a:hover {
  color: var(--accent);
}

.logout-btn {
  background-color: var(--secondary);
  color: var(--text);
  padding: 8px 16px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background-color: var(--accent);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.7);
  transform: translateY(-2px);
}
</style>