<template>
  <div id="app">
    <h1>Bandmate</h1>
    <LoginForm v-if="!loggedIn" @login-success="onLoginSuccess" />
    <div v-if="loggedIn" class="content">
      <ProfileDisplay v-if="userType === 'Musician'" ref="profileDisplay" />
      <VenueProfile v-if="userType === 'VenueOwner'" ref="venueProfile" />
      <ConversationList />
      <button v-if="loggedIn" @click="logout" class="logout-btn">Logout</button>
    </div>
  </div>
</template>

<script>
import LoginForm from './components/LoginForm.vue';
import ProfileDisplay from './components/ProfileDisplay.vue';
import VenueProfile from './components/VenueProfile.vue';
import ConversationList from './components/ConversationList.vue';
import axios from 'axios';

export default {
  name: 'App',
  components: {
    LoginForm,
    ProfileDisplay,
    VenueProfile,
    ConversationList
  },
  data() {
    return {
      loggedIn: false,
      userType: null
    };
  },
  created() {
    const token = localStorage.getItem('token');
    if (token && !window.performance.navigation.type === 0) {
      this.loggedIn = true;
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
        this.$nextTick(() => {
          if (this.userType === 'Musician' && this.$refs.profileDisplay) {
            this.$refs.profileDisplay.fetchProfile();
          } else if (this.userType === 'VenueOwner' && this.$refs.venueProfile) {
            this.$refs.venueProfile.fetchProfile();
          }
        });
      } catch (err) {
        console.error('User type fetch failed:', err.response?.data || err.message);
      }
    },
    onLoginSuccess() {
      console.log('Login successful event received');
      this.loggedIn = true;
      this.fetchUserType();
    },
    logout() {
      localStorage.clear();
      this.loggedIn = false;
      this.userType = null;
      if (this.$refs.profileDisplay) this.$refs.profileDisplay.profile = null;
      if (this.$refs.venueProfile) this.$refs.venueProfile.profile = null;
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

.content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
}

.logout-btn {
  background-color: var(--secondary);
  color: var(--text);
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  margin-top: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  transition: all 0.3s ease;
  align-self: center; /* Center it horizontally */
}

.logout-btn:hover {
  background-color: var(--accent);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.7);
  transform: translateY(-2px);
}
</style>