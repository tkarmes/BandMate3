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
        </div>
        <div v-else class="edit-section">
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
        previewImage: null
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
          genrePreferences: this.profile?.genrePreferences || '',
          phone: this.profile?.phone || '',
          email: this.profile?.email || '',
          websiteUrl: this.profile?.websiteUrl || '',
          operatingHours: this.profile?.operatingHours || '',
          amenities: this.profile?.amenities || '',
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
          this.profile = response.data || this.profile; // Update if backend returns the profile
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
      }
    },
    created() {
      this.fetchProfile();
    }
  };
  </script>
  
  <style scoped>
  .venue-profile {
    width: 100%;
    min-height: 100vh;
    margin: 0;
    padding: 0;
    font-family: 'Arial', sans-serif;
    background-color: #1a1a1a;
    color: var(--text);
  }
  
  .hero {
    position: relative;
    width: 100%;
    height: 200px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    overflow: hidden;
  }
  
  .profile-pic {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    border: 3px solid #333;
    object-fit: cover;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.5);
    margin-bottom: 10px;
    z-index: 1;
  }
  
  h1 {
    color: #fff;
    font-size: 2em;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.7);
    margin: 0;
    z-index: 1;
  }
  
  .hero-background {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-size: cover;
    background-position: center;
    opacity: 0.2;
    filter: blur(5px);
    z-index: 0;
  }
  
  .profile-details {
    width: 100%;
    padding: 20px;
    background-color: #222;
  }
  
  .info-section {
    max-width: 800px;
    margin: 0 auto;
    padding: 0 20px;
  }
  
  .info-section p {
    margin: 15px 0;
    font-size: 1.2em;
  }
  
  .info-section strong {
    color: var(--primary);
    font-weight: 600;
  }
  
  .edit-btn {
    background-color: var(--primary);
    color: #fff;
    padding: 10px 20px;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    font-size: 16px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
    transition: all 0.3s ease;
  }
  
  .edit-btn:hover {
    background-color: var(--accent);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.7);
  }
  
  .edit-section {
    max-width: 800px;
    margin: 20px auto;
    padding: 20px;
    background-color: #2a2a2a;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.5);
  }
  
  form {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }
  
  label {
    color: var(--primary);
    font-weight: 600;
    margin-top: 10px;
  }
  
  input, textarea {
    padding: 10px;
    border: 1px solid #444;
    border-radius: 4px;
    background-color: #333;
    color: #fff;
    font-size: 16px;
  }
  
  textarea {
    resize: vertical;
    min-height: 100px;
  }
  
  .preview-pic {
    max-width: 200px;
    margin-top: 10px;
    border-radius: 8px;
  }
  
  .form-buttons {
    display: flex;
    gap: 10px;
  }
  
  button {
    background-color: var(--primary);
    color: #fff;
    padding: 10px 20px;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    font-size: 16px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
    transition: all 0.3s ease;
  }
  
  button:hover {
    background-color: var(--accent);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.7);
  }
  </style>