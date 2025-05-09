<template>
  <div class="venue-profile">
    <div class="hero">
      <img v-if="imageUrl" :src="imageUrl" :alt="displayName" class="profile-pic" @error="onImageError" />
      <img v-else src="/assets/venue-placeholder.png" alt="Default Venue" class="profile-pic" @error="onFallbackError" />
      <h1>{{ displayName || 'Loading...' }}</h1>
      <div class="hero-background" :style="heroBackgroundStyle"></div>
    </div>
    <div class="profile-details">
      <div v-if="!editing" class="info-section">
        <p v-if="hasValidValue(profile?.address, 'address')"><strong>Address:</strong> {{ profile?.address || 'Not specified' }}</p>
        <p v-if="hasValidValue(profile?.city, 'city')"><strong>City:</strong> {{ profile?.city || 'Not specified' }}</p>
        <p v-if="hasValidValue(profile?.state, 'state')"><strong>State:</strong> {{ profile?.state || 'Not specified' }}</p>
        <p v-if="hasValidValue(profile?.zipCode, 'zipCode')"><strong>Zip Code:</strong> {{ profile?.zipCode || 'Not specified' }}</p>
        <p v-if="hasValidValue(profile?.capacity, 'capacity')"><strong>Capacity:</strong> {{ profile?.capacity || 'Not specified' }}</p>
        <p v-if="hasValidValue(profile?.venueType, 'venueType')"><strong>Venue Type:</strong> {{ profile?.venueType || 'Not specified' }}</p>
        <p v-if="hasValidValue(profile?.genrePreferences, 'genrePreferences')"><strong>Genre Preferences:</strong> {{ profile?.genrePreferences?.join(' ') || 'None listed' }}</p>
        <p v-if="hasValidValue(profile?.phone, 'phone')"><strong>Phone:</strong> {{ profile?.phone || 'Not specified' }}</p>
        <p v-if="hasValidValue(profile?.email, 'email')"><strong>Email:</strong> {{ profile?.email || 'Not specified' }}</p>
        <p v-if="hasValidValue(profile?.websiteUrl, 'websiteUrl')"><strong>Website:</strong> {{ profile?.websiteUrl || 'Not specified' }}</p>
        <p v-if="hasValidValue(profile?.operatingHours, 'operatingHours')"><strong>Operating Hours:</strong> {{ profile?.operatingHours || 'Not specified' }}</p>
        <p v-if="hasValidValue(profile?.amenities, 'amenities')"><strong>Amenities:</strong> {{ profile?.amenities?.join(' ') || 'None listed' }}</p>
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
            <label>Venue Name:</label>
            <input v-model="editedProfile.name" type="text" />
          </div>
          <div class="form-row">
            <label>Address:</label>
            <input v-model="editedProfile.address" type="text" />
          </div>
          <div class="form-row">
            <label>City:</label>
            <input v-model="editedProfile.city" type="text" />
          </div>
          <div class="form-row">
            <label>State:</label>
            <input v-model="editedProfile.state" type="text" />
          </div>
          <div class="form-row">
            <label>Zip Code:</label>
            <input v-model="editedProfile.zipCode" type="text" />
          </div>
          <div class="form-row">
            <label>Capacity:</label>
            <input v-model="editedProfile.capacity" type="number" min="1" />
          </div>
          <div class="form-row">
            <label>Venue Type:</label>
            <input v-model="editedProfile.venueType" type="text" />
          </div>
          <div class="form-row">
            <label>Genre Preferences (space-separated):</label>
            <input v-model="editedProfile.genrePreferences" type="text" />
          </div>
          <div class="form-row">
            <label>Phone:</label>
            <input v-model="editedProfile.phone" type="text" />
          </div>
          <div class="form-row">
            <label>Email:</label>
            <input v-model="editedProfile.email" type="text" />
          </div>
          <div class="form-row">
            <label>Website URL:</label>
            <input v-model="editedProfile.websiteUrl" type="text" />
          </div>
          <div class="form-row">
            <label>Operating Hours:</label>
            <textarea v-model="editedProfile.operatingHours"></textarea>
          </div>
          <div class="form-row">
            <label>Amenities (space-separated):</label>
            <input v-model="editedProfile.amenities" type="text" />
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
  name: 'VenueProfile',
  props: {
    userId: { type: [String, Number], required: true }
  },
  data() {
    return {
      profile: null,
      editing: false,
      editedProfile: {
        name: '',
        address: '',
        city: '',
        state: '',
        zipCode: '',
        capacity: '',
        venueType: '',
        genrePreferences: '',
        phone: '',
        email: '',
        websiteUrl: '',
        operatingHours: '',
        amenities: '',
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
      return this.profile?.name || this.profile?.username || 'Venue Profile';
    },
    isOwnProfile() {
      return this.userId === localStorage.getItem('userId');
    }
  },
  created() {
    this.fetchProfile();
  },
  methods: {
    hasValidValue(value, fieldName) {
      if (Array.isArray(value)) {
        return value != null && value.length > 0 && value.every(item => item && item.trim() !== '');
      }
      if (fieldName === 'capacity') {
        const numValue = Number(value);
        return !isNaN(numValue) && numValue > 0;
      }
      return value != null && value !== '' && value !== 'Not specified' && value !== 'None listed';
    },
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
        console.error('Venue profile fetch failed:', err.response?.data || err.message);
      } finally {
        this.loading = false;
      }
    },
    startEditing() {
      this.editing = true;
      this.editedProfile = {
        name: this.profile?.name || this.profile?.username || '',
        address: this.profile?.address || '',
        city: this.profile?.city || '',
        state: this.profile?.state || '',
        zipCode: this.profile?.zipCode || '',
        capacity: this.profile?.capacity || '',
        venueType: this.profile?.venueType || '',
        genrePreferences: Array.isArray(this.profile?.genrePreferences) ? this.profile.genrePreferences.join(' ') : this.profile?.genrePreferences || '',
        phone: this.profile?.phone || '',
        email: this.profile?.email || this.profile?.username || '',
        websiteUrl: this.profile?.websiteUrl || '',
        operatingHours: this.profile?.operatingHours || '',
        amenities: Array.isArray(this.profile?.amenities) ? this.profile.amenities.join(' ') : this.profile?.amenities || '',
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
        formData.append('venueName', this.editedProfile.name || '');
        formData.append('address', this.editedProfile.address || '');
        formData.append('city', this.editedProfile.city || '');
        formData.append('state', this.editedProfile.state || '');
        formData.append('zipCode', this.editedProfile.zipCode || '');
        const capacity = Number(this.editedProfile.capacity);
        formData.append('capacity', isNaN(capacity) || capacity <= 0 ? '' : capacity.toString());
        formData.append('venueType', this.editedProfile.venueType || '');
        const genrePreferences = this.editedProfile.genrePreferences
          ? this.editedProfile.genrePreferences.split(' ').filter(item => item.trim() !== '')
          : [];
        formData.append('genrePreferences', genrePreferences.join(' '));
        formData.append('phone', this.editedProfile.phone || '');
        formData.append('email', this.editedProfile.email || '');
        formData.append('websiteUrl', this.editedProfile.websiteUrl || '');
        formData.append('operatingHours', this.editedProfile.operatingHours || '');
        const amenities = this.editedProfile.amenities
          ? this.editedProfile.amenities.split(' ').filter(item => item.trim() !== '')
          : [];
        formData.append('amenities', amenities.join(' '));
        const fileInput = document.querySelector('input[type="file"]');
        if (fileInput && fileInput.files[0]) {
          formData.append('profilePicture', fileInput.files[0]);
        } else {
          formData.append('profilePictureUrl', this.profile?.profilePictureUrl || '');
        }
        const response = await axios.put(
          `http://localhost:9000/users/${this.userId}/venue-profile`,
          formData,
          { headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'multipart/form-data' } }
        );
        this.profile = response.data;
        this.editing = false;
        this.previewImage = null;
      } catch (err) {
        this.error = err.response?.data?.message || 'Failed to save profile';
        console.error('Venue profile update failed:', err.response?.data || err.message);
      }
    },
    cancelEditing() {
      this.editing = false;
      this.editedProfile = {
        name: '', address: '', city: '', state: '', zipCode: '', capacity: '',
        venueType: '', genrePreferences: '', phone: '', email: '', websiteUrl: '',
        operatingHours: '', amenities: '', profilePictureUrl: ''
      };
      this.previewImage = null;
    },
    onImageError() {
      console.log('Profile image failed to load:', this.imageUrl);
    },
    onFallbackError() {
      console.log('Fallback image failed to load: /assets/venue-placeholder.png');
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

.venue-profile {
  max-width: 850px; /* Increased from 700px */
  margin: 0 auto;
  padding: 25px;
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
  width: 200px; /* Increased from 150px */
  height: 200px;
  border-radius: 50%;
  border: 4px solid #fff;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
  object-fit: cover;
  background: #ffffff;
}

.hero h1 {
  font-size: 2.2rem; /* Reduced from 2.5rem */
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
  padding: 20px; /* Reduced from 25px */
  border-radius: 12px;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.4);
  transition: transform 0.3s ease;
}

.profile-details:hover {
  transform: translateY(-5px);
}

.info-section p {
  margin: 10px 0; /* Reduced from 12px */
  font-size: 1rem; /* Reduced from 1.1rem */
}

.edit-btn {
  background: linear-gradient(45deg, var(--primary, #0288d1), var(--accent, #4fc3f7));
  color: white;
  padding: 10px 20px; /* Reduced from 12px 24px */
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
  width: 140px; /* Reduced from 150px */
  font-weight: bold;
  text-align: right;
  color: var(--primary, #0288d1);
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
  border-color: var(--accent, #4fc3f7);
  box-shadow: 0 0 5px rgba(79, 195, 247, 0.5);
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
  background: linear-gradient(45deg, var(--primary, #0288d1), var(--accent, #4fc3f7));
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
  color: var(--primary, #0288d1);
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
  border-color: var(--accent, #4fc3f7);
  outline: none;
}

.start-conversation button {
  background: linear-gradient(45deg, var(--primary, #0288d1), var(--accent, #4fc3f7));
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