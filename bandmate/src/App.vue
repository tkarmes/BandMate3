<template>
  <div id="app">
    <h1>Bandmate</h1>
    <button v-if="isLoggedIn" @click="logout">Logout</button>
    <LoginForm @login-success="onLoginSuccess" />
    <ProfilePictureUpload @upload-success="onUploadSuccess" />
    <ProfileDisplay ref="profileDisplay" />
  </div>
</template>

<script>
import LoginForm from './components/LoginForm.vue';
import ProfilePictureUpload from './components/ProfilePictureUpload.vue';
import ProfileDisplay from './components/ProfileDisplay.vue';

export default {
  name: 'App',
  components: {
    LoginForm,
    ProfilePictureUpload,
    ProfileDisplay
  },
  computed: {
    isLoggedIn() {
      return !!localStorage.getItem('token');
    }
  },
  methods: {
    onLoginSuccess() {
      console.log('Login successful event received');
      this.$refs.profileDisplay.fetchProfile();
    },
    onUploadSuccess() {
      console.log('Upload successful event received');
      this.$refs.profileDisplay.fetchProfile();
    },
    logout() {
      localStorage.clear();
      this.$refs.profileDisplay.profile = null;
    }
  }
};
</script>

<style>
:root {
  --primary: #457b9d; /* Blue buttons */
  --secondary: #e63946; /* Red logout/errors */
  --success: #2a9d8f; /* Green success */
  --background: #1a1a1a; /* Dark gray-black */
  --text: #f0f0f0; /* Off-white text */
  --accent: #a3bffa; /* Lighter blue hover */
  --card-bg: #2c2c2c; /* Darker card */
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
  text-transform: uppercase; /* Rock vibe */
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
  background-color: darken(var(--secondary), 10%);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.7);
  transform: translateY(-2px);
}
</style>