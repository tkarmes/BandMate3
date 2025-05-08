<template>
  <div class="profile-display">
    <div class="hero">
      <img v-if="imageUrl" :src="imageUrl" :alt="displayName" class="profile-pic" @error="onImageError" />
      <img v-else src="/assets/musician-placeholder.png" alt="Default Musician" class="profile-pic" @error="onFallbackError" />
      <h1>{{ displayName || 'Loading...' }}</h1>
      <div class="hero-background" :style="heroBackgroundStyle"></div>
    </div>
    <div class="profile-details">
      <div v-if="!editing" class="info-section">
        <p><strong>Name:</strong> {{ displayName || 'Not set' }}</p>
        <p><strong>Bio:</strong> {{ profile?.bio || 'No bio yet' }}</p>
        <p><strong>Location:</strong> {{ profile?.location || 'Not specified' }}</p>
        <p><strong>Genres:</strong> {{ Array.isArray(profile?.genres) ? profile.genres.join(' ') : profile?.genres || 'None listed' }}</p>
        <p><strong>Instruments:</strong> {{ Array.isArray(profile?.instruments) ? profile.instruments.join(' ') : profile?.instruments || 'None listed' }}</p>
        <button v-if="isOwnProfile" @click="startEditing" class="edit-btn">Edit Profile</button>
        <div class="start-conversation" v-if="isOwnProfile">
          <h3>Start a New Conversation</h3>
          <input v-model="recipientUsername" type="text" placeholder="Enter username to message" />
          <input v-model="initialMessage" placeholder="Type your message" />
          <button @click="startConversation" :disabled="!recipientUsername || !initialMessage">Send</button>
          <p v-if="error" class="error">{{ error }}</p>
        </div>
      </div>
      <div v-else class="edit-section">
        <form @submit.prevent="saveProfile" enctype="multipart/form-data" class="edit-form">
          <div class="form-row">
            <label>Name:</label>
            <input v-model="editedProfile.name" type="text" />
          </div>
          <div class="form-row">
            <label>Bio:</label>
            <textarea v-model="editedProfile.bio"></textarea>
          </div>
          <div class="form-row">
            <label>Location:</label>
            <input v-model="editedProfile.location" type="text" />
          </div>
          <div class="form-row">
            <label>Genres (space-separated):</label>
            <input v-model="editedProfile.genres" type="text" />
          </div>
          <div class="form-row">
            <label>Instruments (space-separated):</label>
            <input v-model="editedProfile.instruments" type="text" />
          </div>
          <div class="form-row">
            <label>Profile Picture:</label>
            <input type="file" @change="onFileChange" accept="image/*" />
          </div>
          <div class="form-row preview-row" v-if="previewImage">
            <label>Preview:</label>
            <img :src="previewImage" alt="Preview" class="preview-pic" />
          </div>
          <div class="form-buttons">
            <button type="submit">Save</button>
            <button type="button" @click="cancelEditing">Cancel</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'MusicianProfile',
  props: {
    userId: { type: [String, Number], required: true }
  },
  data() {
    return {
      profile: null,
      editing: false,
      editedProfile: {
        name: '',
        bio: '',
        location: '',
        genres: '',
        instruments: '',
        profilePictureUrl: ''
      },
      previewImage: null,
      recipientUsername: '',
      initialMessage: '',
      error: '',
      loading: false
    };
  },
  computed: {
    imageUrl() {
      return this.profile?.profilePictureUrl
        ? `http://localhost:9000/users/uploads/${this.profile.profilePictureUrl}?t=${Date.now()}`
        : '';
    },
    heroBackgroundStyle() {
      return this.profile?.profilePictureUrl
        ? { backgroundImage: `url(${this.imageUrl})`, opacity: 0.2 }
        : { background: 'linear-gradient(to right, #1a1a1a, #333)' };
    },
    displayName() {
      return this.profile?.name || this.profile?.username || 'Musician Profile';
    },
    isOwnProfile() {
      return this.userId === localStorage.getItem('userId');
    }
  },
  created() {
    this.fetchProfile();
  },
  methods: {
    async fetchProfile() {
      const token = localStorage.getItem('token');
      if (!token || !this.userId) {
        this.error = 'Please log in or provide a valid user ID';
        return;
      }
      this.loading = true;
      try {
        const response = await axios.get(`http://localhost:9000/users/${this.userId}`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        this.profile = response.data;
        console.log('Profile fetched:', this.profile);
      } catch (err) {
        this.error = err.response?.data?.message || 'Failed to load profile';
        console.error('Musician profile fetch failed:', err.response?.data || err.message);
      } finally {
        this.loading = false;
      }
    },
    startEditing() {
      this.editing = true;
      this.editedProfile = {
        name: this.profile?.name || this.profile?.username || '',
        bio: this.profile?.bio || '',
        location: this.profile?.location || '',
        genres: Array.isArray(this.profile?.genres) ? this.profile.genres.join(' ') : this.profile?.genres || '',
        instruments: Array.isArray(this.profile?.instruments) ? this.profile.instruments.join(' ') : this.profile?.instruments || '',
        profilePictureUrl: this.profile?.profilePictureUrl || ''
      };
      this.previewImage = null;
    },
    async saveProfile() {
      const token = localStorage.getItem('token');
      if (!token || !this.userId) {
        this.error = 'Please log in';
        this.editing = false;
        return;
      }
      try {
        const formData = new FormData();
        formData.append('name', this.editedProfile.name || '');
        formData.append('bio', this.editedProfile.bio || '');
        formData.append('location', this.editedProfile.location || '');
        formData.append('genres', this.editedProfile.genres || '');
        formData.append('instruments', this.editedProfile.instruments || '');
        const fileInput = document.querySelector('input[type="file"]');
        if (fileInput && fileInput.files[0]) {
          formData.append('profilePicture', fileInput.files[0]);
        } else {
          formData.append('profilePictureUrl', this.profile?.profilePictureUrl || '');
        }
        const response = await axios.put(
          `http://localhost:9000/users/${this.userId}/musician-profile`,
          formData,
          { headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'multipart/form-data' } }
        );
        this.profile = response.data;
        this.editing = false;
        this.previewImage = null;
      } catch (err) {
        this.error = err.response?.data?.message || 'Failed to save profile';
        console.error('Musician profile update failed:', err.response?.data || err.message);
      }
    },
    cancelEditing() {
      this.editing = false;
      this.editedProfile = {
        name: '', bio: '', location: '', genres: '', instruments: '', profilePictureUrl: ''
      };
      this.previewImage = null;
    },
    onImageError() {
      console.log('Profile image failed to load:', this.imageUrl);
    },
    onFallbackError() {
      console.log('Fallback image failed to load: /assets/musician-placeholder.png');
    },
    onFileChange(event) {
      const file = event.target.files[0];
      this.previewImage = file ? URL.createObjectURL(file) : null;
    },
    async startConversation() {
      const token = localStorage.getItem('token');
      const loggedInUserId = localStorage.getItem('userId');
      if (!token || !loggedInUserId || !this.recipientUsername || !this.initialMessage) {
        this.error = 'Please enter a username and message';
        return;
      }
      this.error = '';
      try {
        const userResponse = await axios.get(
          `http://localhost:9000/users/by-username/${this.recipientUsername}`,
          { headers: { 'Authorization': `Bearer ${token}` } }
        );
        const recipientId = userResponse.data.userId;
        const convoPayload = {
          participants: [
            { userId: Number(loggedInUserId) },
            { userId: Number(recipientId) }
          ]
        };
        const convoResponse = await axios.post(
          'http://localhost:9000/messaging/conversations',
          convoPayload,
          { headers: { 'Authorization': `Bearer ${token}` } }
        );
        const conversationId = convoResponse.data.conversationId;
        const messagePayload = {
          conversationId: conversationId,
          senderId: Number(loggedInUserId),
          receiverId: Number(recipientId),
          content: this.initialMessage,
          sentAt: new Date().toISOString()
        };
        await axios.post(
          'http://localhost:9000/messaging/messages',
          messagePayload,
          { headers: { 'Authorization': `Bearer ${token}` } }
        );
        this.$router.push({ name: 'ConversationList' });
        this.recipientUsername = '';
        this.initialMessage = '';
      } catch (err) {
        this.error = err.response?.data?.message || `User '${this.recipientUsername}' not found`;
        console.error('Conversation failed:', err.response?.data || err.message);
      }
    }
  }
};
</script>

<style scoped>
/* Unchanged from your original */
</style>