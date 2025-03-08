<template>
  <div class="upload-container">
    <h2>Upload Profile Picture</h2>
    <form @submit.prevent="handleUpload">
      <input
        type="file"
        @change="onFileChange"
        accept="image/*"
        class="file-input"
      />
      <button type="submit" :disabled="!file">Upload</button>
    </form>
    <p v-if="message" :class="{ 'success': message.includes('successful'), 'error': !message.includes('successful') }">{{ message }}</p>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ConversationList',
  data() {
    return {
      conversationId: '',
      messages: [],
      loading: false,
      error: '',
      newMessage: '',
      receiverId: '',
      sending: false
    };
  },
  methods: {
    async fetchMessages() {
      const token = localStorage.getItem('token');
      if (!token || !this.conversationId) {
        this.error = 'Missing token or conversation ID';
        return;
      }
      this.loading = true;
      this.error = '';
      try {
        const response = await axios.get(
          `http://localhost:9000/conversations/${this.conversationId}/messages`,
          { headers: { 'Authorization': `Bearer ${token}` } }
        );
        console.log('Messages response:', response.data);
        this.messages = response.data;
      } catch (err) {
        this.error = err.response?.data || 'Failed to load messages';
        console.error('Fetch messages failed:', err.response?.data || err.message);
        this.messages = [];
      } finally {
        this.loading = false;
      }
    },
    async createAndSendMessage() {
      const token = localStorage.getItem('token');
      const userId = localStorage.getItem('userId');
      if (!token || !userId || !this.newMessage || !this.receiverId) {
        this.error = 'Missing required fields';
        return;
      }
      this.sending = true;
      this.error = '';
      try {
        if (!this.conversationId) {
          const convoPayload = {
            participants: [
              { userId: Number(userId) },
              { userId: Number(this.receiverId) }
            ]
          };
          const convoResponse = await axios.post(
            'http://localhost:9000/conversations',
            convoPayload,
            { headers: { 'Authorization': `Bearer ${token}` } }
          );
          console.log('Conversation created:', convoResponse.data);
          this.conversationId = convoResponse.data.conversationId;
        }
        const messagePayload = {
          conversationId: Number(this.conversationId),
          receiverId: Number(this.receiverId),
          content: this.newMessage
        };
        const messageResponse = await axios.post(
          'http://localhost:9000/messages',
          messagePayload,
          { headers: { 'Authorization': `Bearer ${token}` } }
        );
        console.log('Send response:', messageResponse.data);
        this.messages.push(messageResponse.data);
        this.newMessage = '';
      } catch (err) {
        this.error = err.response?.data?.message || 'Failed to send message';
        console.error('Send message failed:', err.response?.data || err.message);
      } finally {
        this.sending = false;
      }
    },
    formatTimestamp(timestamp) {
      if (!timestamp) return 'Just now';
      const date = new Date(timestamp);
      return isNaN(date.getTime()) ? 'Just now' : date.toLocaleString();
    }
  }
};
</script>

<style scoped>
.upload-container {
  padding: 20px;
  border: 1px solid #444;
  border-radius: 8px;
  background-color: var(--card-bg);
  margin-top: 20px;
}

h2 {
  color: var(--text);
  margin-bottom: 15px;
}

form {
  display: flex;
  flex-direction: column;
  gap: 15px;
  max-width: 300px;
  margin: 0 auto;
}

.file-input {
  padding: 10px;
  border: 1px solid #444;
  border-radius: 4px;
  font-size: 16px;
  background-color: #333;
  color: var(--text);
}

button {
  background-color: var(--primary);
  color: var(--text);
  padding: 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  transition: all 0.3s ease;
}

button:disabled {
  background-color: #555;
  cursor: not-allowed;
}

button:hover:not(:disabled) {
  background-color: var(--accent);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.7);
}

.success {
  color: var(--success);
  margin-top: 10px;
}

.error {
  color: var(--secondary);
  margin-top: 10px;
}
</style>