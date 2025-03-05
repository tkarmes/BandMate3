<template>
  <div>
    <h2>Login</h2>
    <form @submit.prevent="handleLogin">
      <input v-model="username" type="text" placeholder="Username" />
      <input v-model="password" type="password" placeholder="Password" />
      <button type="submit">Login</button>
    </form>
    <p v-if="error" style="color: red">{{ error }}</p>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'LoginForm',
  data() {
    return {
      username: '',
      password: '',
      error: ''
    };
  },
  methods: {
    async handleLogin() {
      try {
        const response = await axios.post('http://localhost:9000/login', {
          username: this.username,
          password: this.password
        });
        const { token, user } = response.data;
        localStorage.setItem('token', token);
        localStorage.setItem('userId', user.userId);
        this.error = '';
        alert('Login successful! Token: ' + token);
        this.$emit('login-success'); // Emit event
      } catch (err) {
        console.error('Login error:', err.response || err.message);
        this.error = 'Login failed: ' + (err.response?.data?.message || err.message);
      }
    }
  }
};
</script>

<style scoped>
form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 300px;
  margin: 20px auto;
}
input,
button {
  padding: 8px;
}
</style>