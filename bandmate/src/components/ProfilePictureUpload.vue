<template>
  <div class="upload-container">
    <h2>Upload Profile Picture</h2>
    <form @submit.prevent="handleUpload">
      <input
        type="file"
        @change="onFileChange"
        accept="image/*"
        class="file-input"
      />
      <button type="submit" :disabled="!file">Upload</button>
    </form>
    <p v-if="message" :class="{ 'success': message.includes('successful'), 'error': !message.includes('successful') }">{{ message }}</p>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ProfilePictureUpload',
  data() {
    return {
      file: null,
      message: ''
    };
  },
  methods: {
    onFileChange(event) {
      this.file = event.target.files[0];
    },
    async handleUpload() {
      if (!this.file) {
        this.message = 'Please select a file.';
        return;
      }
      const token = localStorage.getItem('token');
      const userId = localStorage.getItem('userId');
      if (!token) {
        this.message = 'Please log in first.';
        return;
      }

      const formData = new FormData();
      formData.append('file', this.file);

      try {
        const response = await axios.post(
          `http://localhost:9000/users/${userId}/profile-picture`,
          formData,
          {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'multipart/form-data'
            }
          }
        );
        this.message = 'Upload successful!';
        console.log(response.data);
        this.$emit('upload-success'); // Emit event to refresh profile
      } catch (err) {
        this.message = 'Upload failed: ' + (err.response?.data?.message || err.message);
      }
    }
  }
};
</script>

<style scoped>
.upload-container {
  padding: 20px;
  border: 1px solid #444;
  border-radius: 8px;
  background-color: var(--card-bg);
  margin-top: 20px;
}

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

.file-input {
  padding: 10px;
  border: 1px solid #444;
  border-radius: 4px;
  font-size: 16px;
  background-color: #333;
  color: var(--text);
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

button:disabled {
  background-color: #555;
  cursor: not-allowed;
}

button:hover:not(:disabled) {
  background-color: var(--accent);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.7);
}

.success {
  color: var(--success);
  margin-top: 10px;
}

.error {
  color: var(--secondary);
  margin-top: 10px;
}
</style>