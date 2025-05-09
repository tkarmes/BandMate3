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
@import url('https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap');

.profile-display {
  max-width: 850px; /* Increased from 700px, slightly less than original 900px */
  margin: 0 auto;
  padding: 25px; /* Balanced padding */
  color: var(--text, #ffffff);
  font-family: 'Roboto', sans-serif;
}

.hero {
  position: relative;
  text-align: center;
  margin-bottom: 25px;
  padding: 20px;
  border-radius: 10px;
  overflow: hidden;
  background: #2a2a2a;
}

.profile-pic {
  width: 200px; /* Increased from 150px, less than original 250px */
  height: 200px;
  border-radius: 50%;
  border: 4px solid #fff;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
  object-fit: cover;
  background: #ffffff;
}

.hero h1 {
  font-size: 2.2rem; /* Slightly smaller than original 2.5rem */
  margin: 10px 0;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  background-size: cover;
  background-position: center;
  opacity: 0.3;
  filter: blur(5px);
}

.hero-background::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.7));
}

.profile-details {
  background: #1e1e1e;
  padding: 20px; /* Slightly reduced from 25px */
  border-radius: 12px;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.4);
  transition: transform 0.3s ease;
}

.profile-details:hover {
  transform: translateY(-5px);
}

.info-section p {
  margin: 10px 0; /* Slightly reduced from 12px */
  font-size: 1rem; /* Slightly smaller than 1.1rem */
}

.edit-btn {
  background: linear-gradient(45deg, var(--primary, #6200ea), var(--accent, #b388ff));
  color: white;
  padding: 10px 20px; /* Slightly smaller than 12px 24px */
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-weight: bold;
  transition: transform 0.2s, box-shadow 0.2s;
}

.edit-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.edit-section {
  padding: 20px; /* Reduced from 25px */
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 15px; /* Reduced from 20px */
}

.form-row {
  display: flex;
  align-items: center;
  gap: 12px; /* Reduced from 15px */
}

.form-row label {
  width: 140px; /* Slightly reduced from 150px */
  font-weight: bold;
  text-align: right;
  color: var(--primary, #6200ea);
}

.form-row input,
.form-row textarea {
  flex: 1;
  padding: 8px; /* Reduced from 10px */
  border: 1px solid #555;
  border-radius: 6px;
  background-color: #2a2a2a;
  color: #fff;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.form-row input:focus,
.form-row textarea:focus {
  border-color: var(--accent, #b388ff);
  box-shadow: 0 0 5px rgba(179, 136, 255, 0.5);
  outline: none;
}

.form-row textarea {
  min-height: 70px; /* Reduced from 80px */
  resize: vertical;
}

.preview-row {
  align-items: flex-start;
}

.preview-pic {
  max-width: 100px; /* Reduced from 120px */
  margin-top: 8px;
  border-radius: 8px;
}

.form-buttons {
  display: flex;
  gap: 12px; /* Reduced from 15px */
  justify-content: flex-end;
}

.form-buttons button {
  background: linear-gradient(45deg, var(--primary, #6200ea), var(--accent, #b388ff));
  color: white;
  padding: 10px 20px; /* Reduced from 12px 24px */
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-weight: bold;
  transition: transform 0.2s, box-shadow 0.2s;
}

.form-buttons button:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.start-conversation {
  margin-top: 20px; /* Reduced from 25px */
  padding: 12px; /* Reduced from 15px */
  border: 1px solid #555;
  border-radius: 8px;
  background: #2a2a2a;
}

.start-conversation h3 {
  margin-bottom: 10px; /* Reduced from 12px */
  color: var(--primary, #6200ea);
}

.start-conversation input {
  display: block;
  width: 100%;
  margin-bottom: 10px; /* Reduced from 12px */
  padding: 8px;
  background-color: #333;
  color: #fff;
  border: 1px solid #555;
  border-radius: 6px;
  transition: border-color 0.3s;
}

.start-conversation input:focus {
  border-color: var(--accent, #b388ff);
  outline: none;
}

.start-conversation button {
  background: linear-gradient(45deg, var(--primary, #6200ea), var(--accent, #b388ff));
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 25px;
  transition: transform 0.2s;
}

.start-conversation button:hover {
  transform: scale(1.05);
}

.start-conversation button:disabled {
  background: #555;
  cursor: not-allowed;
}

.error {
  color: var(--secondary, #ff5252);
  margin-top: 8px;
}
</style>