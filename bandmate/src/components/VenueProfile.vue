<template>
    <div class="venue-profile">
      <div class="hero">
        <img v-if="imageUrl" :src="imageUrl" alt="Venue Picture" class="profile-pic" @error="onImageError" />
        <h1>{{ profile ? profile.name : 'Loading...' }}</h1>
        <div class="hero-background" :style="heroBackgroundStyle"></div>
      </div>
      <div class="profile-details">
        <div v-if="!editing" class="info-section">
          <p><strong>Address:</strong> {{ profile?.address || 'Not specified' }}</p>
          <p><strong>City:</strong> {{ profile?.city || 'Not specified' }}</p>
          <p><strong>State:</strong> {{ profile?.state || 'Not specified' }}</p>
          <p><strong>Zip Code:</strong> {{ profile?.zipCode || 'Not specified' }}</p>
          <p><strong>Capacity:</strong> {{ profile?.capacity || 'Not specified' }}</p>
          <p><strong>Venue Type:</strong> {{ profile?.venueType || 'Not specified' }}</p>
          <p><strong>Genre Preferences:</strong> {{ profile?.genrePreferences?.join(' ') || 'None listed' }}</p>
          <p><strong>Phone:</strong> {{ profile?.phone || 'Not specified' }}</p>
          <p><strong>Email:</strong> {{ profile?.email || 'Not specified' }}</p>
          <p><strong>Website:</strong> {{ profile?.websiteUrl || 'Not specified' }}</p>
          <p><strong>Operating Hours:</strong> {{ profile?.operatingHours || 'Not specified' }}</p>
          <p><strong>Amenities:</strong> {{ profile?.amenities?.join(' ') || 'None listed' }}</p>
          <button @click="startEditing" class="edit-btn">Edit Profile</button>
          <!-- Start Conversation Section -->
          <div class="start-conversation">
            <h3>Start a New Conversation</h3>
            <input v-model="recipientUsername" type="text" placeholder="Enter username to message" />
            <input v-model="initialMessage" placeholder="Type your message" />
            <button @click="startConversation" :disabled="!recipientUsername || !initialMessage">Send</button>
            <p v-if="error" class="error">{{ error }}</p>
          </div>
        </div>
        <div v-else class="edit-section">
          <!-- Edit form unchanged -->
          <form @submit.prevent="saveProfile" enctype="multipart/form-data">
            <label>Venue Name:</label>
            <input v-model="editedProfile.name" type="text" />
            <label>Address:</label>
            <input v-model="editedProfile.address" type="text" />
            <label>City:</label>
            <input v-model="editedProfile.city" type="text" />
            <label>State:</label>
            <input v-model="editedProfile.state" type="text" />
            <label>Zip Code:</label>
            <input v-model="editedProfile.zipCode" type="text" />
            <label>Capacity:</label>
            <input v-model="editedProfile.capacity" type="number" />
            <label>Venue Type:</label>
            <input v-model="editedProfile.venueType" type="text" />
            <label>Genre Preferences (space-separated):</label>
            <input v-model="editedProfile.genrePreferences" type="text" />
            <label>Phone:</label>
            <input v-model="editedProfile.phone" type="text" />
            <label>Email:</label>
            <input v-model="editedProfile.email" type="text" />
            <label>Website URL:</label>
            <input v-model="editedProfile.websiteUrl" type="text" />
            <label>Operating Hours:</label>
            <textarea v-model="editedProfile.operatingHours"></textarea>
            <label>Amenities (space-separated):</label>
            <input v-model="editedProfile.amenities" type="text" />
            <label>Profile Picture:</label>
            <input type="file" @change="onFileChange" accept="image/*" />
            <img v-if="previewImage" :src="previewImage" alt="Preview" class="preview-pic" />
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
        recipientUsername: '', // Changed to username
        initialMessage: '',
        error: '' // For error messages
      };
    },
    computed: {
      imageUrl() {
        const url = this.profile?.profilePictureUrl
          ? `http://localhost:9000/users/uploads/${this.profile.profilePictureUrl}?t=${Date.now()}`
          : '';
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
          return;
        }
        try {
          const response = await axios.get(`http://localhost:9000/users/${userId}/venue-profile`, {
            headers: { 'Authorization': `Bearer ${token}` }
          });
          this.profile = response.data;
        } catch (err) {
          console.error('Venue profile fetch failed:', err.response?.data || err.message);
          this.profile = null;
        }
      },
      startEditing() {
        this.editing = true;
        this.editedProfile = {
          name: this.profile?.name || '',
          address: this.profile?.address || '',
          city: this.profile?.city || '',
          state: this.profile?.state || '',
          zipCode: this.profile?.zipCode || '',
          capacity: this.profile?.capacity || '',
          venueType: this.profile?.venueType || '',
          genrePreferences: this.profile?.genrePreferences?.join(' ') || '',
          phone: this.profile?.phone || '',
          email: this.profile?.email || '',
          websiteUrl: this.profile?.websiteUrl || '',
          operatingHours: this.profile?.operatingHours || '',
          amenities: this.profile?.amenities?.join(' ') || '',
          profilePictureUrl: this.profile?.profilePictureUrl || ''
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
          formData.append('venueName', this.editedProfile.name || '');
          formData.append('address', this.editedProfile.address || '');
          formData.append('city', this.editedProfile.city || '');
          formData.append('state', this.editedProfile.state || '');
          formData.append('zipCode', this.editedProfile.zipCode || '');
          formData.append('capacity', this.editedProfile.capacity || '');
          formData.append('venueType', this.editedProfile.venueType || '');
          formData.append('genrePreferences', this.editedProfile.genrePreferences || '');
          formData.append('phone', this.editedProfile.phone || '');
          formData.append('email', this.editedProfile.email || '');
          formData.append('websiteUrl', this.editedProfile.websiteUrl || '');
          formData.append('operatingHours', this.editedProfile.operatingHours || '');
          formData.append('amenities', this.editedProfile.amenities || '');
          const fileInput = document.querySelector('input[type="file"]');
          if (fileInput && fileInput.files[0]) {
            formData.append('profilePicture', fileInput.files[0]);
          } else {
            formData.append('profilePictureUrl', this.profile?.profilePictureUrl || '');
          }
  
          const response = await axios.put(
            `http://localhost:9000/users/${userId}/venue-profile`,
            formData,
            { headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'multipart/form-data' } }
          );
          this.profile = response.data || this.profile;
          this.editing = false;
          this.previewImage = null;
        } catch (err) {
          console.error('Venue profile update failed:', err.response?.status, err.response?.data || err.message);
          this.editing = false;
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
    const recipientId = userResponse.data.userId; // Changed from .id to .userId

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
  
  <style scoped>
  /* Existing styles */
  .start-conversation {
    margin-top: 20px;
    padding: 10px;
    border: 1px solid #444;
    border-radius: 5px;
  }
  .start-conversation h3 {
    margin-bottom: 10px;
  }
  .start-conversation input {
    display: block;
    width: 100%;
    margin-bottom: 10px;
    padding: 5px;
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