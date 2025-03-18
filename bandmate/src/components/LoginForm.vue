<template>
  <div>
    <h2>Login</h2>
    <form @submit.prevent="handleLogin">
      <input v-model="username" type="text" placeholder="Username" />
      <input v-model="password" type="password" placeholder="Password" />
      <button type="submit">Login</button>
    </form>
    <p v-if="error" class="error">{{ error }}</p>
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
        const response = await axios.post('http://localhost:9000/auth/login', { // Changed to /auth/login
          username: this.username,
          password: this.password
        });
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('userId', response.data.user.userId);
        this.error = '';
        this.$emit('login-success'); // Must emit this
        console.log('Login successful:', response.data); // Optional: for debugging
      } catch (err) {
        this.error = err.response?.data || 'Login failed';
        console.log('Login error:', this.error);
      }
    }
  }
};
</script>

<style scoped>
h2 {
  color: var(--text);
  margin-bottom: 15px;
}

form {
  display: flex;
  flex-direction: column;
  gap: 15px;
  max-width: 300px;
  margin: 0 auto;
}

input {
  padding: 10px;
  border: 1px solid #444; /* Darker border */
  border-radius: 4px;
  font-size: 16px;
  background-color: #333; /* Dark input */
  color: var(--text);
}

input::placeholder {
  color: #aaa; /* Lighter placeholder */
}

button {
  background-color: var(--primary);
  color: var(--text);
  padding: 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  transition: all 0.3s ease;
}

button:hover {
  background-color: var(--accent);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.7);
}

.error {
  color: var(--secondary);
  margin-top: 10px;
}
</style>