<template>
  <div class="profile-display">
    <div class="hero">
      <img v-if="imageUrl" :src="imageUrl" alt="Profile Picture" class="profile-pic" @error="onImageError" />
      <h1>{{ profile ? profile.name : 'Loading...' }}</h1> <!-- Displays name -->
      <div class="hero-background" :style="heroBackgroundStyle"></div>
    </div>
    <div class="profile-details">
      <div v-if="!editing" class="info-section">
        <p><strong>Name:</strong> {{ profile?.name || 'Not set' }}</p> <!-- Added for display -->
        <p><strong>Bio:</strong> {{ profile?.bio || 'No bio yet' }}</p>
        <p><strong>Location:</strong> {{ profile?.location || 'Not specified' }}</p>
        <p><strong>Genres:</strong> {{ profile?.genres || 'None listed' }}</p>
        <p><strong>Instruments:</strong> {{ profile?.instruments || 'None listed' }}</p>
        <button @click="startEditing" class="edit-btn">Edit Profile</button>
        <div class="start-conversation">
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
            <input v-model="editedProfile.name" type="text" /> <!-- Added for editing -->
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
  data() {
    return {
      profile: null,
      editing: false,
      editedProfile: {
        name: '', // Added
        bio: '',
        location: '',
        genres: '',
        instruments: ''
      },
      previewImage: null,
      recipientUsername: '',
      initialMessage: '',
      error: ''
    };
  },
  computed: {
    imageUrl() {
      const url = this.profile?.profilePictureUrl
        ? `http://localhost:9000/users/uploads/${this.profile.profilePictureUrl}?t=${Date.now()}`
        : '';
      console.log('Generated imageUrl:', url);
      return url;
    },
    heroBackgroundStyle() {
      return this.profile?.profilePictureUrl
        ? { backgroundImage: `url(${this.imageUrl})`, opacity: 0.2 }
        : { background: 'linear-gradient(to right, #1a1a1a, #333)' };
    }
  },
  methods: {
    async fetchProfile() {
      const token = localStorage.getItem('token');
      const userId = localStorage.getItem('userId');
      if (!token || !userId) {
        this.profile = null;
        console.log('No token or userId, profile not fetched');
        return;
      }
      try {
        const response = await axios.get(`http://localhost:9000/users/${userId}/musician-profile`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        this.profile = response.data;
        console.log('Profile fetched:', this.profile);
      } catch (err) {
        console.error('Profile fetch failed:', err.response?.data || err.message);
        this.profile = null;
      }
    },
    startEditing() {
      this.editing = true;
      this.editedProfile = {
        name: this.profile?.name || '', // Added
        bio: this.profile?.bio || '',
        location: this.profile?.location || '',
        genres: this.profile?.genres || '',
        instruments: this.profile?.instruments || ''
      };
      this.previewImage = null;
    },
    async saveProfile() {
      const token = localStorage.getItem('token');
      const userId = localStorage.getItem('userId');
      if (!token || !userId) {
        this.editing = false;
        return;
      }
      try {
        const formData = new FormData();
        formData.append('name', this.editedProfile.name || ''); // Added
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
          `http://localhost:9000/users/${userId}/musician-profile`,
          formData,
          { headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'multipart/form-data' } }
        );
        this.profile = response.data;
        this.editing = false;
        this.previewImage = null;
      } catch (err) {
        console.error('Profile update failed:', err.response?.status, err.response?.data || err.message);
        this.editing = false;
      }
    },
    cancelEditing() {
      this.editing = false;
      this.editedProfile = { name: '', bio: '', location: '', genres: '', instruments: '' };
      this.previewImage = null;
    },
    onImageError() {
      console.log('Image failed to load:', this.imageUrl);
    },
    onFileChange(event) {
      const file = event.target.files[0];
      if (file) {
        this.previewImage = URL.createObjectURL(file);
      }
    },
    async startConversation() {
      const token = localStorage.getItem('token');
      const userId = localStorage.getItem('userId');
      if (!token || !userId || !this.recipientUsername || !this.initialMessage) {
        this.error = 'Please enter a username and message';
        return;
      }
      this.error = '';
      console.log("Sending request for username:", this.recipientUsername);
      try {
        const userResponse = await axios.get(
          `http://localhost:9000/users/by-username/${this.recipientUsername}`,
          { headers: { 'Authorization': `Bearer ${token}` } }
        );
        console.log("Response:", userResponse.data);
        const recipientId = userResponse.data.userId;

        const convoPayload = {
          participants: [
            { userId: Number(userId) },
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
          senderId: Number(userId),
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
        this.error = err.response?.data?.message || `User '${this.recipientUsername}' not found or failed to start conversation`;
        console.error('Failed:', err.response?.status, err.response?.data || err.message);
      }
    }
  },
  created() {
    this.fetchProfile();
  }
};
</script>

<!-- Keep your existing <style scoped> section -->
<style scoped>
.profile-display {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  color: var(--text);
}

.hero {
  position: relative;
  text-align: center;
  margin-bottom: 20px;
}

.profile-pic {
  max-width: 200px;
  border-radius: 50%;
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
}

.profile-details {
  background: #222;
  padding: 20px;
  border-radius: 5px;
}

.info-section p {
  margin: 10px 0;
}

.edit-btn {
  background-color: var(--primary);
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.edit-btn:hover {
  background-color: var(--accent);
}

.edit-section {
  padding: 20px;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-row label {
  width: 150px;
  font-weight: bold;
  text-align: right;
}

.form-row input,
.form-row textarea {
  flex: 1;
  padding: 8px;
  border: 1px solid #444;
  border-radius: 4px;
  background-color: #333;
  color: var(--text);
}

.form-row textarea {
  min-height: 60px;
  resize: vertical;
}

.preview-row {
  align-items: flex-start;
}

.preview-pic {
  max-width: 100px;
  margin-top: 5px;
}

.form-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.form-buttons button {
  background-color: var(--primary);
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.form-buttons button:hover {
  background-color: var(--accent);
}

.start-conversation {
  margin-top: 20px;
  padding: 10px;
  border: 1px solid #444;
  border-radius: 5px;
}

.start-conversation h3 {
  margin-bottom: 10px;
  color: var(--primary);
}

.start-conversation input {
  display: block;
  width: 100%;
  margin-bottom: 10px;
  padding: 5px;
  background-color: #333;
  color: #fff;
  border: 1px solid #444;
  border-radius: 4px;
}

.start-conversation button {
  background-color: var(--primary);
  color: white;
  padding: 5px 10px;
  border: none;
  border-radius: 5px;
}

.start-conversation button:disabled {
  background-color: #555;
}

.error {
  color: var(--secondary);
  margin-top: 5px;
}
</style>