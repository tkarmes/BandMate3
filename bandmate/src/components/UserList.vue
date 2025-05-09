<template>
    <div class="user-list">
      <h2>{{ listType === 'artists' ? 'Artists' : 'Venues' }}</h2>
      <div class="list-toggle">
        <button :class="{ active: listType === 'artists' }" @click="switchList('artists')">Artists</button>
        <button :class="{ active: listType === 'venues' }" @click="switchList('venues')">Venues</button>
      </div>
      <div v-if="loading && users.length === 0" class="loading">Loading...</div>
      <div v-else-if="error" class="error">{{ error }}</div>
      <ul v-else class="user-list-container">
        <li v-for="user in users" :key="user.userId" class="user-item" @click="viewProfile(user)">
          <img :src="getAvatarUrl(user)" :alt="user.username" class="avatar" @error="onImageError" />
          <div class="user-info">
            <strong>{{ user.username }}</strong>
            <span>{{ user.userType }}</span>
          </div>
        </li>
        <li v-if="loading" class="loading">Loading more...</li>
        <li v-if="!hasMore && users.length > 0" class="no-more">No more {{ listType }} to load</li>
      </ul>
      <div ref="sentinel" class="sentinel"></div>
    </div>
  </template>
  
  <script>
  import axios from 'axios';
  
  export default {
    name: 'UserList',
    data() {
      return {
        users: [],
        listType: 'artists',
        page: 0,
        pageSize: 10,
        loading: false,
        error: '',
        hasMore: true,
        token: localStorage.getItem('token')
      };
    },
    created() {
      this.fetchUsers();
      this.setupIntersectionObserver();
    },
    beforeUnmount() {
      if (this.observer) {
        this.observer.disconnect();
      }
    },
    methods: {
      async fetchUsers() {
        if (!this.token) {
          this.error = 'Please log in to view the list';
          return;
        }
        if (this.loading || !this.hasMore) return;
        this.loading = true;
        this.error = '';
        try {
          const userType = this.listType === 'artists' ? 'Musician' : 'VenueOwner';
          const response = await axios.get('http://localhost:9000/users/list', {
            headers: { 'Authorization': `Bearer ${this.token}` },
            params: { userType, page: this.page, size: this.pageSize }
          });
          const newUsers = response.data || [];
          console.log(`Fetched ${this.listType}:`, newUsers);
          this.users = [...this.users, ...newUsers];
          this.page++;
          this.hasMore = newUsers.length === this.pageSize;
        } catch (err) {
          this.error = err.response?.data?.message || `Failed to load ${this.listType}`;
          console.error(`Fetch ${this.listType} failed:`, err.response?.data || err.message);
        } finally {
          this.loading = false;
        }
      },
      switchList(type) {
        if (type !== this.listType) {
          this.listType = type;
          this.users = [];
          this.page = 0;
          this.hasMore = true;
          this.fetchUsers();
        }
      },
      setupIntersectionObserver() {
        this.observer = new IntersectionObserver(
          (entries) => {
            if (entries[0].isIntersecting && this.hasMore && !this.loading) {
              this.fetchUsers();
            }
          },
          { threshold: 0.1 }
        );
        this.$nextTick(() => {
          if (this.$refs.sentinel) {
            this.observer.observe(this.$refs.sentinel);
          }
        });
      },
      getAvatarUrl(user) {
        return user.profilePictureUrl
          ? `http://localhost:9000/users/uploads/${user.profilePictureUrl}?t=${Date.now()}`
          : this.listType === 'artists' ? '/assets/musician-placeholder.png' : '/assets/venue-placeholder.png';
      },
      onImageError(event) {
        event.target.src = this.listType === 'artists' ? '/assets/musician-placeholder.png' : '/assets/venue-placeholder.png';
      },
      viewProfile(user) {
        const routeName = user.userType === 'Musician' ? 'MusicianProfile' : 'VenueProfile';
        this.$router.push({ name: routeName, params: { userId: user.userId } });
      }
    }
  };
  </script>
  
  <style scoped>
  @import url('https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap');
  
  .user-list {
  max-width: 850px; /* Increased from 700px */
  margin: 0 auto;
  padding: 20px;
  color: var(--text);
  font-family: 'Roboto', sans-serif;
}

.user-list-container {
  max-height: 450px; /* Slightly reduced from 500px */
  overflow-y: auto;
  padding: 15px;
  background: var(--card-bg);
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.avatar {
  width: 35px; /* Increased from 30px */
  height: 35px;
  border-radius: 50%;
  margin-right: 10px;
  object-fit: cover;
}

h2 {
  font-size: 1.6rem; /* Increased from 1.5rem */
  color: var(--primary);
  margin-bottom: 15px;
}
  
  .list-toggle {
    display: flex;
    gap: 10px;
    margin-bottom: 15px;
  }
  
  .list-toggle button {
    background: #2a2a2a;
    color: #ffffff;
    padding: 8px 16px;
    border: 1px solid #555;
    border-radius: 6px;
    cursor: pointer;
    transition: background 0.3s;
  }
  
  .list-toggle button.active,
  .list-toggle button:hover {
    background: linear-gradient(45deg, #0288d1, #4fc3f7);
  }
  
  .user-list-container {
    list-style: none;
    padding: 0;
    max-height: 500px;
    overflow-y: auto;
    background: #2a2a2a;
    border-radius: 6px;
  }
  
  .user-list-container::-webkit-scrollbar {
    width: 6px;
  }
  
  .user-list-container::-webkit-scrollbar-track {
    background: #2a2a2a;
  }
  
  .user-list-container::-webkit-scrollbar-thumb {
    background: #0288d1;
    border-radius: 6px;
  }
  
  .user-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px;
    border-bottom: 1px solid #444;
    cursor: pointer;
  }
  
  .user-item:hover {
    background: #333;
  }
  
  .avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    object-fit: cover;
    border: 2px solid #ffffff;
  }
  
  .user-info strong {
    color: #4fc3f7;
    font-weight: 600;
  }
  
  .user-info span {
    font-size: 0.9rem;
    color: #aaaaaa;
  }
  
  .loading, .error, .no-more {
    text-align: center;
    padding: 15px;
    color: #ffffff;
  }
  
  .error {
    color: #ff5252;
  }
  
  .sentinel {
    height: 10px;
  }
  </style>