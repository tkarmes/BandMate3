<template>
  <div class="profile-display">
    <div class="hero">
      <img v-if="imageUrl" :src="imageUrl" alt="Profile Picture" class="profile-pic" @error="onImageError" />
      <h1>{{ profile ? profile.username : 'Loading...' }}</h1>
      <div class="hero-background" :style="heroBackgroundStyle"></div>
    </div>
    <div class="profile-details">
      <div v-if="!editing" class="info-section">
        <p><strong>Bio:</strong> {{ profile?.bio || 'No bio yet' }}</p>
        <p><strong>Location:</strong> {{ profile?.location || 'Not specified' }}</p>
        <p><strong>Genres:</strong> {{ profile?.genres || 'None listed' }}</p>
        <p><strong>Instruments:</strong> {{ profile?.instruments || 'None listed' }}</p>
        <button @click="startEditing" class="edit-btn">Edit Profile</button>
      </div>
      <div v-else class="edit-section">
        <form @submit.prevent="saveProfile" enctype="multipart/form-data">
          <label>Bio:</label>
          <textarea v-model="editedProfile.bio"></textarea>
          <label>Location:</label>
          <input v-model="editedProfile.location" type="text" />
          <label>Genres (space-separated):</label>
          <input v-model="editedProfile.genres" type="text" />
          <label>Instruments (space-separated):</label>
          <input v-model="editedProfile.instruments" type="text" />
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
  name: 'ProfileDisplay',
  data() {
    return {
      profile: null,
      editing: false,
      editedProfile: {
        bio: '',
        location: '',
        genres: [],
        instruments: []
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
        const response = await axios.get(`http://localhost:9000/users/${userId}/musician-profile`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        this.profile = response.data;
      } catch (err) {
        console.error('Profile fetch failed:', err.response?.data || err.message);
        this.profile = null;
      }
    },
    startEditing() {
      this.editing = true;
      this.editedProfile = {
        bio: this.profile.bio || '',
        location: this.profile.location || '',
        genres: this.profile.genres ? this.profile.genres.split(' ') : [],
        instruments: this.profile.instruments ? this.profile.instruments.split(' ') : []
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
        formData.append('bio', this.editedProfile.bio || '');
        formData.append('location', this.editedProfile.location || '');
        formData.append('genres', this.editedProfile.genres.join(' '));
        formData.append('instruments', this.editedProfile.instruments.join(' '));
        const fileInput = document.querySelector('input[type="file"]');
        if (fileInput && fileInput.files[0]) {
          formData.append('profilePicture', fileInput.files[0]);
        } else {
          formData.append('profilePictureUrl', this.profile.profilePictureUrl || '');
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
      this.editedProfile = { bio: '', location: '', genres: [], instruments: [] };
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
.profile-display {
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