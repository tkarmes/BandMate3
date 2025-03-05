<template>
  <div v-if="profile">
    <h2>Your Profile</h2>
    <p>Bio: {{ profile.bio }}</p>
    <p>Picture:</p>
    <img
      :src="imageUrl"
      alt="Profile Picture"
      style="max-width: 200px;"
      @error="onImageError"
      v-if="profile.profilePictureUrl"
    />
    <p v-else>No picture available</p>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ProfileDisplay',
  data() {
    return {
      profile: null
    };
  },
  computed: {
    imageUrl() {
      const url = this.profile?.profilePictureUrl
        ? `http://localhost:9000/uploads/${this.profile.profilePictureUrl}`
        : '';
      console.log('Image URL:', url);
      return url;
    }
  },
  methods: {
    async fetchProfile() {
      const token = localStorage.getItem('token');
      const userId = localStorage.getItem('userId');
      console.log('Fetching profile with Token:', token, 'UserId:', userId);
      if (!token || !userId) {
        console.log('Missing token or userId');
        this.profile = null;
        return;
      }

      try {
        const response = await axios.get(`http://localhost:9000/users/${userId}/musician-profile`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        console.log('Profile response:', response.data);
        this.profile = response.data;
      } catch (err) {
        console.error('Profile fetch failed:', err.response?.data || err.message);
        this.profile = null;
      }
    },
    onImageError() {
      console.log('Image failed to load:', this.imageUrl);
    }
  }
};
</script>