<template>
  <div class="login-form">
    <h2>Login</h2>
    <form @submit.prevent="handleLogin">
      <input
        v-model="username"
        type="text"
        placeholder="Username"
        required
        :disabled="loading"
      />
      <input
        v-model="password"
        type="password"
        placeholder="Password"
        required
        :disabled="loading"
      />
      <button type="submit" :disabled="loading">
        {{ loading ? 'Logging in...' : 'Login' }}
      </button>
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
      error: '',
      loading: false
    };
  },
  methods: {
    async handleLogin() {
      if (this.loading) return;
      this.loading = true;
      this.error = '';
      try {
        const response = await axios.post('http://localhost:9000/auth/login', {
          username: this.username,
          password: this.password
        });
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('userId', response.data.user.userId);
        this.$emit('login-success');
        this.$router.push({ name: 'UserList' }); // Redirect to /users
        console.log('Login successful:', response.data);
      } catch (err) {
        this.error = err.response?.data?.message || 'Invalid username or password';
        console.error('Login error:', err.response?.data || err.message);
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.login-form {
  max-width: 400px;
  margin: 0 auto;
  padding: 20px;
  background: #2c2c2c;
  border-radius: 8px;
  color: var(--text);
}

h2 {
  color: var(--primary);
  margin-bottom: 20px;
  font-size: 1.8rem;
}

form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

input {
  padding: 10px;
  border: 1px solid #444;
  border-radius: 5px;
  font-size: 16px;
  background: #333;
  color: var(--text);
}

input::placeholder {
  color: #aaa;
}

input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

button {
  background: var(--primary);
  color: var(--text);
  padding: 10px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  transition: all 0.3s ease;
}

button:hover:not(:disabled) {
  background: var(--accent);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.7);
  transform: translateY(-2px);
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  color: var(--secondary);
  margin-top: 10px;
  font-size: 0.9rem;
}
</style>