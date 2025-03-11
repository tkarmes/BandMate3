<template>
  <div id="app">
    <h1>Bandmate</h1>
    <button v-if="loggedIn" @click="logout">Logout</button>
    <LoginForm v-if="!loggedIn" @login-success="onLoginSuccess" />
    <div v-if="loggedIn" class="content">
      <ProfilePictureUpload @upload-success="onUploadSuccess" />
      <ProfileDisplay ref="profileDisplay" />
      <ConversationList /> <!-- No onUploadSuccess here -->
    </div>
  </div>
</template>

<script>
import LoginForm from './components/LoginForm.vue';
import ProfilePictureUpload from './components/ProfilePictureUpload.vue';
import ProfileDisplay from './components/ProfileDisplay.vue';
import ConversationList from './components/ConversationList.vue';

export default {
  name: 'App',
  components: {
    LoginForm,
    ProfilePictureUpload,
    ProfileDisplay,
    ConversationList
  },
  data() {
    return {
      loggedIn: false // Always start false
    };
  },
  created() {
    // Only stay logged in if token exists AND we’re not reloading fresh
    const token = localStorage.getItem('token');
    if (token && !window.performance.navigation.type === 0) { // Not a full page load
      this.loggedIn = true;
      this.$nextTick(() => {
        if (this.$refs.profileDisplay) {
          this.$refs.profileDisplay.fetchProfile();
        }
      });
    }
  },
  methods: {
    onLoginSuccess() {
      console.log('Login successful event received');
      this.loggedIn = true;
      this.$nextTick(() => {
        if (this.$refs.profileDisplay) {
          this.$refs.profileDisplay.fetchProfile();
        } else {
          console.log('ProfileDisplay ref still not ready');
        }
      });
    },
    onUploadSuccess() {
      console.log('Upload successful event received');
      if (this.$refs.profileDisplay) {
        this.$refs.profileDisplay.fetchProfile();
      }
    },
    logout() {
      localStorage.clear();
      this.loggedIn = false;
      if (this.$refs.profileDisplay) {
        this.$refs.profileDisplay.profile = null;
      }
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

#app {
  max-width: 600px;
  margin: 0 auto;
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

button {
  background-color: var(--secondary);
  color: var(--text);
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  transition: all 0.3s ease;
}

button:hover {
  background-color: var(--accent);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.7);
  transform: translateY(-2px);
}

.content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
</style>