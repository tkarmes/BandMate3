<template>
  <div v-if="profile" class="musician-profile">
    <!-- Hero Section -->
    <div class="hero" :style="heroBackgroundStyle">
      <div class="overlay"></div>
      <div class="hero-content">
        <div class="hero-info">
          <div class="profile-pic-container" v-if="profile.profilePictureUrl">
            <img
              :src="imageUrl"
              alt="Profile Picture"
              class="profile-pic"
              @error="onImageError"
            />
          </div>
          <h1>{{ profile.user?.username || 'Musician' }}</h1>
          <p class="location">{{ profile.location || 'Unknown Location' }}</p>
        </div>
        <button class="edit-btn" @click="startEditing" v-if="!editing">Edit Profile</button>
      </div>
    </div>
    <!-- Details Section -->
    <div class="details" v-if="!editing">
      <div class="bio-card">
        <h2>Bio</h2>
        <p>{{ profile.bio || 'Tell us about yourself!' }}</p>
      </div>
      <div class="genres-card">
        <h2>Genres</h2>
        <p>{{ profile.genres || 'No genres set' }}</p>
      </div>
      <div class="instruments-card">
        <h2>Instruments</h2>
        <p>{{ profile.instruments || 'No instruments set' }}</p>
      </div>
    </div>

    <!-- Edit Form -->
    <div class="edit-form" v-else>
      <form @submit.prevent="saveProfile">
        <div class="form-group">
          <label>Profile Picture</label>
          <input type="file" @change="onFileChange" accept="image/*" />
          <img
            :src="previewImage || imageUrl"
            alt="Preview"
            class="profile-pic-preview"
            v-if="previewImage || profile.profilePictureUrl"
          />
        </div>
        <div class="form-group">
          <label>Bio</label>
          <textarea v-model="editedProfile.bio" placeholder="Your bio" class="bio-input"></textarea>
        </div>
        <div class="form-group">
          <label>Location</label>
          <input v-model="editedProfile.location" placeholder="Your location" class="text-input" />
        </div>
        <div class="form-group">
          <label>Genres</label>
          <select v-model="editedProfile.genres" multiple class="select-input">
            <option value="Rock">Rock</option>
            <option value="Country">Country</option>
            <option value="Jazz">Jazz</option>
            <option value="Blues">Blues</option>
            <option value="Pop">Pop</option>
          </select>
        </div>
        <div class="form-group">
          <label>Instruments</label>
          <select v-model="editedProfile.instruments" multiple class="select-input">
            <option value="Guitar">Guitar</option>
            <option value="Bass">Bass</option>
            <option value="Drums">Drums</option>
            <option value="Vocals">Vocals</option>
            <option value="Piano">Piano</option>
          </select>
        </div>
        <div class="form-actions">
          <button type="submit" class="save-btn">Save</button>
          <button type="button" class="cancel-btn" @click="cancelEditing">Cancel</button>
        </div>
      </form>
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
      previewImage: null // For picture upload preview
    };
  },
  computed: {
    imageUrl() {
      const url = this.profile?.profilePictureUrl
        ? `http://localhost:9000/users/uploads/${this.profile.profilePictureUrl}?t=${Date.now()}` // Changed to /users/uploads/
        : '';
      return url;
    },
    heroBackgroundStyle() {
      return this.profile?.profilePictureUrl
        ? { backgroundImage: `url(${this.imageUrl})` }
        : { background: 'linear-gradient(to right, #00ffcc, #457b9d)' };
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
    startEditing() {
      this.editing = true;
      this.editedProfile = {
        bio: this.profile.bio || '',
        location: this.profile.location || '',
        genres: this.profile.genres ? this.profile.genres.split(' ') : [],
        instruments: this.profile.instruments ? this.profile.instruments.split(' ') : []
      };
      this.previewImage = null; // Reset preview
    },
    async saveProfile() {
      const token = localStorage.getItem('token');
      const userId = localStorage.getItem('userId');
      console.log('Saving profile with Token:', token, 'UserId:', userId);
      if (!token || !userId) {
        console.log('Missing token or userId - aborting save');
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
        console.log('Save response:', response.data);
        this.profile = response.data;
        this.editing = false;
        this.previewImage = null;
      } catch (err) {
        console.error('Profile update failed:', err.response?.status, err.response?.data || err.message);
        if (err.response?.status === 401) {
          console.log('Token expired - logging out');
          this.$parent.logout();
        } else {
          console.log('Save failed but staying logged in');
          this.editing = false;
        }
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
  }
};
</script>

<style scoped>
.musician-profile {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: linear-gradient(135deg, #1a1a1a 0%, #2a2a2a 100%);
  min-height: 100vh;
  color: #fff;
}

/* Hero Section */
.hero {
  position: relative;
  display: flex;
  align-items: center;
  padding: 40px 20px;
  border-radius: 15px;
  margin-bottom: 30px;
  background-size: cover;
  background-position: center;
  min-height: 300px;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  z-index: 1;
}

.hero-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  width: 100%;
}

.profile-pic-container {
  margin-right: 30px;
}

.profile-pic {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #fff;
  box-shadow: 0 0 20px rgba(0, 255, 204, 0.5);
  transition: transform 0.3s ease;
}

.profile-pic:hover {
  transform: scale(1.05);
}

.hero-info {
  flex: 1;
}

h1 {
  font-size: 2.5em;
  margin: 0;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.location {
  font-size: 1.2em;
  color: #e0e0e0;
  margin: 5px 0 0;
}

.edit-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  background: #fff;
  color: #1a1a1a;
  padding: 10px 20px;
  border: none;
  border-radius: 25px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 2;
}

.edit-btn:hover {
  background: #00ffcc;
  transform: scale(1.05);
}

/* Details Section */
.details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.bio-card, .genres-card, .instruments-card {
  background: #333;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
  transition: transform 0.2s ease;
}

.bio-card:hover, .genres-card:hover, .instruments-card:hover {
  transform: translateY(-5px);
}

h2 {
  font-size: 1.5em;
  margin: 0 0 10px;
  color: #00ffcc;
}

p {
  font-size: 1.1em;
  color: #ddd;
  margin: 0;
}

/* Edit Form */
.edit-form {
  background: #2a2a2a;
  padding: 30px;
  border-radius: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  font-size: 1.2em;
  color: #00ffcc;
  margin-bottom: 5px;
}

.bio-input {
  width: 100%;
  min-height: 120px;
  padding: 15px;
  background: #1a1a1a;
  border: 1px solid #444;
  border-radius: 8px;
  color: #fff;
  font-size: 1em;
  resize: vertical;
}

.text-input, .select-input {
  width: 100%;
  padding: 12px;
  background: #1a1a1a;
  border: 1px solid #444;
  border-radius: 8px;
  color: #fff;
  font-size: 1em;
}

.select-input {
  height: 120px;
}

.profile-pic-preview {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  margin-top: 10px;
  object-fit: cover;
}

.form-actions {
  display: flex;
  gap: 10px;
}

.save-btn, .cancel-btn {
  padding: 12px 25px;
  border: none;
  border-radius: 25px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}

.save-btn {
  background: #00ffcc;
  color: #1a1a1a;
}

.save-btn:hover {
  background: #00e6b3;
  transform: scale(1.05);
}

.cancel-btn {
  background: #666;
  color: #fff;
}

.cancel-btn:hover {
  background: #888;
  transform: scale(1.05);
}
</style>