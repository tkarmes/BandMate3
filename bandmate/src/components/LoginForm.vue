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
        const response = await axios.post('http://localhost:9000/login', {
          username: this.username,
          password: this.password
        });
        const { token, user } = response.data;
        localStorage.setItem('token', token);
        localStorage.setItem('userId', user.userId);
        this.error = '';
        alert('Login successful! Token: ' + token);
        this.$emit('login-success');
      } catch (err) {
        console.error('Login error:', err.response || err.message);
        this.error = 'Login failed: ' + (err.response?.data?.message || err.message);
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