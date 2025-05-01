<template>
  <div class="conversation">
    <h2>Your Conversations</h2>
    <ul v-if="conversations.length" class="convo-list">
      <li v-for="convo in conversations" :key="convo.conversationId" 
          @click="selectConversation(convo.conversationId)"
          :class="{ active: convo.conversationId === conversationId }">
        {{ getParticipantName(convo) }} - {{ formatTimestamp(convo.createdAt) }}
      </li>
    </ul>
    <p v-else>No conversations yet.</p>
    <h3 v-if="conversationId">Messages with {{ getParticipantName(conversations.find(c => c.conversationId === conversationId)) }}</h3>
    <ul v-if="messages.length && !loading" class="message-list">
      <li v-for="(message, index) in messages" :key="message.messageId" 
          :class="['message', isSent(message) ? 'sent' : 'received']">
        <img v-if="showAvatar(message, index)" :src="getAvatarUrl(message.senderId)" alt="Avatar" class="avatar" />
        <div v-else class="avatar-placeholder"></div>
        <div class="message-content">
          <strong v-if="showUsername(message, index)">{{ users[message.senderId]?.username || message.senderId }}</strong>
          {{ message.content }}
          <span class="timestamp">{{ formatTimestamp(message.sentAt) }}</span>
        </div>
      </li>
      <li v-if="isTyping" class="message received typing">
        <div class="avatar-placeholder"></div>
        <div class="message-content">
          <span class="typing-indicator">Typing...</span>
        </div>
      </li>
    </ul>
    <p v-else-if="!loading && conversationId">No messages yet.</p>
    <p v-if="loading">Loading...</p>
    <p v-if="error" class="error">{{ error }}</p>
    <div v-if="conversationId" class="send-message">
      <input v-model="newMessage" placeholder="Type a reply" class="text-input" @keyup.enter="sendMessage" @input="sendTypingEvent" />
      <button @click="sendMessage" :disabled="sending || !newMessage">Send</button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ConversationList',
  data() {
    return {
      conversations: [],
      conversationId: '',
      messages: [],
      loading: false,
      error: '',
      newMessage: '',
      receiverId: '',
      sending: false,
      userId: null,
      users: {},
      ws: null,
      isTyping: false
    };
  },
  created() {
    this.userId = Number(localStorage.getItem('userId'));
    this.fetchConversations();
    this.connectWebSocket();
  },
  beforeUnmount() {
    if (this.ws) {
      this.ws.close();
    }
  },
  methods: {
    connectWebSocket() {
      this.ws = new WebSocket('ws://localhost:9000/ws');
      this.ws.onopen = () => console.log('WebSocket connected');
      this.ws.onmessage = (event) => {
        const data = JSON.parse(event.data);
        console.log("WebSocket received:", data);
        if (data.type === 'TYPING' && data.conversationId === Number(this.conversationId)) {
          this.isTyping = true;
          setTimeout(() => this.isTyping = false, 3000);
        } else if (data.conversationId === Number(this.conversationId)) {
          this.messages.push(data);
          this.$nextTick(() => {
            const messageList = this.$el.querySelector('.message-list');
            if (messageList) messageList.scrollTop = messageList.scrollHeight;
          });
        }
      };
      this.ws.onerror = (err) => console.error('WebSocket error:', err);
      this.ws.onclose = () => console.log('WebSocket closed');
    },
    async fetchConversations() {
      const token = localStorage.getItem('token');
      if (!token) {
        this.error = 'Please log in first';
        return;
      }
      try {
        const response = await axios.get(
          'http://localhost:9000/messaging/conversations',
          { headers: { 'Authorization': `Bearer ${token}` } }
        );
        console.log('Conversations response:', response.data);
        this.conversations = response.data || [];
        // Fetch users for participants
        const participantIds = response.data
          .filter(c => c.participants)
          .flatMap(c => c.participants.map(p => p.userId))
          .filter(id => id !== this.userId && id != null);
        const uniqueIds = [...new Set(participantIds)];
        await this.fetchUsersByIds(uniqueIds);
      } catch (err) {
        this.error = err.response?.data?.message || 'Failed to load conversations';
        console.error('Fetch conversations failed:', err.response?.data || err.message);
        this.conversations = [];
      }
    },
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
          `http://localhost:9000/messaging/conversations/${this.conversationId}/messages`,
          { headers: { 'Authorization': `Bearer ${token}` } }
        );
        console.log('Messages response:', response.data);
        this.messages = response.data || [];
        if (response.data.length) {
          this.receiverId = response.data[0].senderId === this.userId 
            ? response.data[0].receiverId 
            : response.data[0].senderId;
          await this.fetchUsers(response.data);
        }
      } catch (err) {
        this.error = err.response?.data?.message || 'Failed to load messages';
        console.error('Fetch messages failed:', err.response?.data || err.message);
        this.messages = [];
      } finally {
        this.loading = false;
      }
    },
    async sendMessage() {
      const token = localStorage.getItem('token');
      if (!token || !this.userId || !this.newMessage || !this.conversationId) {
        this.error = 'Missing required fields';
        console.log("Missing fields:", { token, userId: this.userId, newMessage: this.newMessage, conversationId: this.conversationId });
        return;
      }
      this.sending = true;
      this.error = '';
      console.log("Sending message:", this.newMessage, "to convo ID:", this.conversationId);
      try {
        const message = {
          conversationId: Number(this.conversationId),
          senderId: this.userId,
          receiverId: Number(this.receiverId),
          content: this.newMessage,
          sentAt: new Date().toISOString()
        };
        console.log("Message payload:", message);
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
          console.log("Sending via WebSocket");
          this.ws.send(JSON.stringify(message));
          this.messages.push(message);
          this.newMessage = '';
        } else {
          console.log("WebSocket unavailable, using POST");
          const response = await axios.post(
            'http://localhost:9000/messaging/messages',
            message,
            { headers: { 'Authorization': `Bearer ${token}` } }
          );
          console.log("POST response:", response.data);
          this.messages.push(response.data);
          this.newMessage = '';
        }
        this.$nextTick(() => {
          const messageList = this.$el.querySelector('.message-list');
          if (messageList) messageList.scrollTop = messageList.scrollHeight;
        });
      } catch (err) {
        this.error = err.response?.data?.message || 'Failed to send message';
        console.error('Send failed:', err.response?.status, err.response?.data || err.message);
      } finally {
        this.sending = false;
      }
    },
    async createAndSendMessage(receiverId) {
      const token = localStorage.getItem('token');
      if (!token || !this.userId || !this.newMessage || !receiverId) {
        this.error = 'Missing required fields';
        return;
      }
      this.sending = true;
      this.error = '';
      try {
        const convoPayload = {
          participants: [
            { userId: this.userId },
            { userId: Number(receiverId) }
          ]
        };
        const convoResponse = await axios.post(
          'http://localhost:9000/messaging/conversations',
          convoPayload,
          { headers: { 'Authorization': `Bearer ${token}` } }
        );
        console.log('Conversation created:', convoResponse.data);
        this.conversationId = convoResponse.data.conversationId;
        this.receiverId = receiverId;
        await this.fetchConversations();
        await this.sendMessage();
      } catch (err) {
        this.error = err.response?.data?.message || 'Failed to create/send message';
        console.error('Create and send failed:', err.response?.data || err.message);
      } finally {
        this.sending = false;
      }
    },
    formatTimestamp(timestamp) {
      if (!timestamp) return 'Just now';
      const date = new Date(timestamp);
      if (isNaN(date.getTime())) return 'Just now';
      const now = new Date();
      const diffMs = now - date;
      const diffMin = Math.round(diffMs / 60000);
      if (diffMin < 60) {
        return diffMin <= 1 ? 'Just now' : `${diffMin} min ago`;
      }
      return date.toLocaleString([], { hour: 'numeric', minute: '2-digit', month: 'short', day: 'numeric' });
    },
    selectConversation(id) {
      this.conversationId = id;
      this.messages = [];
      this.isTyping = false;
      this.fetchMessages();
    },
    isSent(message) {
      return message.senderId === this.userId;
    },
    getAvatarUrl(senderId) {
      const user = this.users[senderId];
      if (user?.profilePictureUrl) {
        return `http://localhost:9000/users/uploads/${user.profilePictureUrl}`;
      }
      return senderId === this.userId ? '/assets/musician-placeholder.png' : '/assets/venue-placeholder.png';
    },
    getParticipantName(convo) {
      if (!convo || !convo.participants) return 'Unknown';
      const otherParticipant = convo.participants.find(p => p.userId !== this.userId);
      return otherParticipant ? (this.users[otherParticipant.userId]?.username || 'Unknown') : 'Unknown';
    },
    async fetchUsers(messages) {
      const token = localStorage.getItem('token');
      const uniqueSenderIds = [...new Set(messages.map(m => m.senderId))];
      for (const senderId of uniqueSenderIds) {
        if (!this.users[senderId]) {
          try {
            const response = await axios.get(
              `http://localhost:9000/users/${senderId}`,
              { headers: { 'Authorization': `Bearer ${token}` } }
            );
            this.users[senderId] = response.data;
            console.log(`Fetched user ${senderId}:`, response.data);
          } catch (err) {
            console.error(`Failed to fetch user ${senderId}:`, err);
          }
        }
      }
    },
    async fetchUsersByIds(userIds) {
      const token = localStorage.getItem('token');
      for (const userId of userIds) {
        if (!this.users[userId]) {
          try {
            const response = await axios.get(
              `http://localhost:9000/users/${userId}`,
              { headers: { 'Authorization': `Bearer ${token}` } }
            );
            this.users[userId] = response.data;
            console.log(`Fetched user ${userId}:`, response.data);
          } catch (err) {
            console.error(`Failed to fetch user ${userId}:`, err);
          }
        }
      }
    },
    showAvatar(message, index) {
      return index === 0 || this.messages[index - 1].senderId !== message.senderId;
    },
    showUsername(message, index) {
      return index === 0 || this.messages[index - 1].senderId !== message.senderId;
    },
    sendTypingEvent() {
      if (this.ws && this.ws.readyState === WebSocket.OPEN && this.newMessage) {
        this.ws.send(JSON.stringify({
          type: 'TYPING',
          conversationId: Number(this.conversationId),
          senderId: this.userId
        }));
      }
    }
  }
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap');

.conversation {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px;
  background: #1e1e1e;
  border-radius: 12px;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.4);
  color: #ffffff;
  font-family: 'Roboto', sans-serif;
}

h2, h3 {
  color: #6200ea;
  margin-bottom: 20px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
}

.convo-list {
  list-style: none;
  padding: 0;
  margin-bottom: 20px;
  max-height: 200px;
  overflow-y: auto;
  background: #2a2a2a;
  border-radius: 8px;
}

.convo-list::-webkit-scrollbar {
  width: 6px;
}

.convo-list::-webkit-scrollbar-track {
  background: #2a2a2a;
  border-radius: 10px;
}

.convo-list::-webkit-scrollbar-thumb {
  background: #6200ea;
  border-radius: 10px;
}

.convo-list li {
  padding: 12px 16px;
  border-bottom: 1px solid #444;
  color: #ffffff;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.convo-list li:hover {
  background-color: #b388ff;
}

.convo-list li.active {
  background: linear-gradient(45deg, #6200ea, #b388ff);
  color: #ffffff;
  font-weight: bold;
}

.message-list {
  list-style: none;
  padding: 0;
  margin: 20px 0;
  max-height: 400px;
  overflow-y: auto;
  background: #2a2a2a;
  border-radius: 8px;
  padding: 10px;
}

.message-list::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-track {
  background: #2a2a2a;
  border-radius: 10px;
}

.message-list::-webkit-scrollbar-thumb {
  background: #6200ea;
  border-radius: 10px;
}

.message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  margin: 8px 10px;
  max-width: 70%;
  border-radius: 15px;
  animation: fadeIn 0.3s ease-in;
}

.message.sent {
  align-self: flex-end;
  flex-direction: row-reverse;
  background: linear-gradient(45deg, #6200ea, #b388ff);
  border-top-right-radius: 5px;
  color: #ffffff;
}

.message.received {
  align-self: flex-start;
  background: #333;
  border-top-left-radius: 5px;
  color: #ffffff;
}

.message.typing {
  opacity: 0.7;
}

.message:hover:not(.typing) {
  transform: scale(1.02);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #ffffff;
  background: #ffffff;
}

.avatar-placeholder {
  width: 36px;
  flex-shrink: 0;
}

.message-content {
  flex: 1;
}

.message-content strong {
  color: #b388ff;
  font-weight: 600;
  display: block;
  margin-bottom: 4px;
}

.message-content p {
  margin: 0;
  font-size: 1rem;
  line-height: 1.4;
}

.timestamp {
  display: block;
  margin-top: 6px;
  font-size: 0.85rem;
  color: #aaaaaa;
  text-align: right;
}

.typing-indicator {
  font-style: italic;
  color: #aaaaaa;
}

.typing-indicator::after {
  content: '...';
  display: inline-block;
  animation: blink 1s infinite;
}

.send-message {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.text-input {
  flex: 1;
  padding: 12px;
  border: 1px solid #555;
  border-radius: 8px;
  background: #333;
  color: #ffffff;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.text-input:focus {
  border-color: #b388ff;
  box-shadow: 0 0 5px rgba(179, 136, 255, 0.5);
  outline: none;
}

button {
  background: linear-gradient(45deg, #6200ea, #b388ff);
  color: #ffffff;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 600;
  transition: transform 0.2s, box-shadow 0.2s;
}

button:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

button:disabled {
  background: #555;
  cursor: not-allowed;
}

p {
  color: #ffffff;
  margin: 10px 0;
}

.error {
  color: #ff5252;
  margin-top: 10px;
  font-weight: 500;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes blink {
  0% { opacity: 1; }
  50% { opacity: 0; }
  100% { opacity: 1; }
}
</style>