<template>
  <div v-if="profile" class="profile" :class="{ 'fade-in': profile }">
    <h2>Your Profile</h2>
    <div v-if="!editing">
      <p class="bio">{{ profile.bio }}</p>
      <p><strong>Location:</strong> {{ profile.location || 'Not set' }}</p>
      <p><strong>Genres:</strong> {{ profile.genres || 'Not set' }}</p>
      <p><strong>Instruments:</strong> {{ profile.instruments || 'Not set' }}</p>
      <div class="pic-container">
        <span>Picture:</span>
        <img
          :src="imageUrl"
          alt="Profile Picture"
          class="profile-pic"
          @error="onImageError"
          v-if="profile.profilePictureUrl"
        />
        <span v-else>No picture available</span>
      </div>
      <button @click="startEditing">Edit Profile</button>
    </div>
    <div v-else>
      <form @submit.prevent="saveProfile">
        <label>Bio:</label>
        <textarea v-model="editedProfile.bio" placeholder="Your bio" class="bio-input"></textarea>
        <label>Location:</label>
        <input v-model="editedProfile.location" placeholder="Your location" class="text-input" />
        <label>Genres:</label>
        <select v-model="editedProfile.genres" multiple class="select-input">
          <option value="Rock">Rock</option>
          <option value="Country">Country</option>
          <option value="Jazz">Jazz</option>
          <option value="Blues">Blues</option>
          <option value="Pop">Pop</option>
          <!-- Add more as needed -->
        </select>
        <label>Instruments:</label>
        <select v-model="editedProfile.instruments" multiple class="select-input">
          <option value="Guitar">Guitar</option>
          <option value="Bass">Bass</option>
          <option value="Drums">Drums</option>
          <option value="Vocals">Vocals</option>
          <option value="Piano">Piano</option>
          <!-- Add more as needed -->
        </select>
        <button type="submit">Save</button>
        <button type="button" @click="cancelEditing">Cancel</button>
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
      }
    };
  },
  computed: {
    imageUrl() {
      const url = this.profile?.profilePictureUrl
        ? `http://localhost:9000/uploads/${this.profile.profilePictureUrl}?t=${Date.now()}`
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
    startEditing() {
      this.editing = true;
      this.editedProfile = {
        bio: this.profile.bio || '',
        location: this.profile.location || '',
        genres: this.profile.genres ? this.profile.genres.split(' ') : [],
        instruments: this.profile.instruments ? this.profile.instruments.split(' ') : []
      };
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
        const payload = {
          ...this.editedProfile,
          genres: this.editedProfile.genres.join(' '),
          instruments: this.editedProfile.instruments.join(' '),
          profilePictureUrl: this.profile.profilePictureUrl
        };
        const response = await axios.put(
          `http://localhost:9000/users/${userId}/musician-profile`,
          payload,
          { headers: { 'Authorization': `Bearer ${token}` } }
        );
        console.log('Save response:', response.data);
        this.profile = response.data;
        this.editing = false;
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
    },
    onImageError() {
      console.log('Image failed to load:', this.imageUrl);
    }
  }
};
</script>

<style scoped>
.profile {
  padding: 20px;
  border: 1px solid #444;
  border-radius: 8px;
  background-color: var(--card-bg);
  margin-top: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.5);
  transition: transform 0.2s ease;
}

.profile:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.7);
}

h2 {
  color: var(--text);
  margin-bottom: 15px;
}

.bio, p {
  font-size: 18px;
  color: var(--text);
  line-height: 1.5;
  margin-bottom: 20px;
}

.pic-container {
  margin: 20px 0;
}

.pic-container span {
  display: block;
  color: var(--text);
  margin-bottom: 10px;
}

.profile-pic {
  width: 200px;
  height: 200px;
  object-fit: cover;
  border-radius: 50%;
  border: 3px solid var(--primary);
  box-shadow: 0 0 10px rgba(69, 123, 157, 0.5);
}

form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

label {
  color: var(--text);
  font-size: 16px;
  margin-bottom: 5px;
}

.bio-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #444;
  border-radius: 4px;
  background-color: #333;
  color: var(--text);
  font-size: 16px;
  min-height: 100px;
  margin-bottom: 15px;
}

.text-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #444;
  border-radius: 4px;
  background-color: #333;
  color: var(--text);
  font-size: 16px;
}

.select-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #444;
  border-radius: 4px;
  background-color: #333;
  color: var(--text);
  font-size: 16px;
  height: 100px; /* Fixed height for multi-select */
}

button {
  background-color: var(--primary);
  color: var(--text);
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  margin-top: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  transition: all 0.3s ease;
}

button:hover {
  background-color: var(--accent);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.7);
}

.fade-in {
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>