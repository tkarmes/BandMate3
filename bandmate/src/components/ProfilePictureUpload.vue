<template>
    <div>
      <h2>Upload Profile Picture</h2>
      <form @submit.prevent="handleUpload">
        <input
          type="file"
          @change="onFileChange"
          accept="image/*"
        />
        <button type="submit" :disabled="!file">Upload</button>
      </form>
      <p v-if="message">{{ message }}</p>
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
        if (!token) {
          this.message = 'Please log in first.';
          return;
        }
  
        const formData = new FormData();
        formData.append('file', this.file);
  
        try {
          const userId = localStorage.getItem('userId');
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
          console.log(response.data); // Updated profile
        } catch (err) {
          this.message = 'Upload failed: ' + (err.response?.data?.message || err.message);
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
  input, button {
    padding: 8px;
  }
  button:disabled {
    opacity: 0.5;
  }
  </style>