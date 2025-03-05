<template>
  <div id="app">
    <h1>Bandmate</h1>
    <LoginForm @login-success="onLoginSuccess" />
    <ProfilePictureUpload />
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
  methods: {
    onLoginSuccess() {
      console.log('Login successful event received');
      this.$refs.profileDisplay.fetchProfile(); // Call fetchProfile directly
    }
  }
};
</script>

<style>
#app {
  text-align: center;
  font-family: Arial, sans-serif;
}
</style>