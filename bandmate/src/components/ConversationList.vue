<template>
  <div class="conversation">
    <h2>Your Conversations</h2>
    <ul v-if="conversations.length" class="convo-list">
      <li v-for="convo in conversations" :key="convo.conversationId" 
          @click="selectConversation(convo.conversationId)"
          :class="{ active: convo.conversationId === conversationId }">
        Conversation {{ convo.conversationId }} - {{ formatTimestamp(convo.createdAt) }}
      </li>
    </ul>
    <p v-else>No conversations yet.</p>
    <h3 v-if="conversationId">Messages for Convo {{ conversationId }}</h3>
    <ul v-if="messages.length && !loading" class="message-list">
      <li v-for="message in messages" :key="message.messageId" 
          :class="['message', isSent(message) ? 'sent' : 'received']">
          <img v-if="users[message.senderId]?.profilePictureUrl" 
               :src="`http://localhost:9000/users/uploads/${users[message.senderId].profilePictureUrl}`" 
               class="avatar" />
        <div class="message-content">
          <strong>{{ users[message.senderId]?.username || message.senderId }}:</strong> {{ message.content }}
          <span class="timestamp">{{ formatTimestamp(message.sentAt) }}</span>
        </div>
      </li>
    </ul>
    <p v-else-if="!loading && conversationId">No messages yet.</p>
    <p v-if="loading">Loading...</p>
    <p v-if="error" class="error">{{ error }}</p>
    <div v-if="conversationId" class="send-message">
      <input v-model="newMessage" placeholder="Type a reply" class="text-input" @keyup.enter="sendMessage" />
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
      ws: null
    };
  },
  created() {
    this.userId = Number(localStorage.getItem('userId'));
    this.fetchConversations();
    this.connectWebSocket();
  },
  beforeUnmount() { // Changed from beforeDestroy
    if (this.ws) {
      this.ws.close();
    }
  },
  methods: {
    connectWebSocket() {
  this.ws = new WebSocket('ws://localhost:9000/ws');
  this.ws.onopen = () => console.log('WebSocket connected');
  this.ws.onmessage = (event) => {
    const message = JSON.parse(event.data);
    console.log("WebSocket received:", message);
    if (message.conversationId === Number(this.conversationId)) {
      console.log("Adding to messages for convo:", this.conversationId);
      this.messages.push(message);
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
        this.conversations = response.data;
      } catch (err) {
        this.error = err.response?.data || 'Failed to load conversations';
        console.error('Fetch conversations failed:', err.response?.data || err.message);
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
        this.messages = response.data;
        if (response.data.length) {
          this.receiverId = response.data[0].senderId === this.userId 
            ? response.data[0].receiverId 
            : response.data[0].senderId;
          await this.fetchUsers(response.data);
        }
      } catch (err) {
        this.error = err.response?.data || 'Failed to load messages';
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
      this.messages.push(message); // Add locally for immediate display
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
      return isNaN(date.getTime()) ? 'Just now' : date.toLocaleString();
    },
    selectConversation(id) {
      this.conversationId = id;
      this.messages = [];
      this.fetchMessages();
    },
    isSent(message) {
      return message.senderId === this.userId;
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
    }
  }
};
</script>

<style scoped>
.conversation {
  padding: 20px;
  border: 1px solid #444;
  border-radius: 8px;
  background-color: var(--card-bg);
  margin-top: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.5);
}

h2, h3 {
  color: var(--text);
  margin-bottom: 15px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.convo-list {
  list-style: none;
  padding: 0;
  margin-bottom: 20px;
  max-height: 200px;
  overflow-y: auto;
}

.convo-list li {
  padding: 12px;
  border-bottom: 1px solid #444;
  color: var(--text);
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.convo-list li:hover {
  background-color: var(--accent);
}

.convo-list li.active {
  background-color: var(--primary);
  color: #fff;
  font-weight: bold;
}

.message-list {
  list-style: none;
  padding: 0;
  margin-top: 20px;
  max-height: 300px;
  overflow-y: auto;
}

.message {
  display: flex;
  align-items: flex-start;
  padding: 10px 15px;
  margin: 5px 0;
  border-radius: 8px;
  color: var(--text);
  background-color: #333;
  max-width: 80%;
}

.message.sent {
  background-color: var(--success);
  margin-left: auto;
  flex-direction: row-reverse;
}

.message.received {
  background-color: #444;
  margin-right: auto;
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  margin: 0 10px;
  border: 1px solid #444;
}

.message-content {
  flex: 1;
}

.message strong {
  color: var(--primary);
  font-weight: 600;
}

.timestamp {
  font-size: 10px;
  color: #aaa;
  display: block;
  margin-top: 5px;
}

.text-input {
  width: 300px;
  padding: 10px;
  border: 1px solid #444;
  border-radius: 4px;
  background-color: #333;
  color: var(--text);
  font-size: 16px;
  margin-right: 10px;
  transition: border-color 0.3s ease;
}

.text-input:focus {
  border-color: var(--accent);
  outline: none;
}

button {
  background-color: var(--primary);
  color: var(--text);
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
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

p {
  color: var(--text);
  margin-top: 20px;
}

.error {
  color: var(--secondary);
  margin-top: 10px;
}

.send-message {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}
</style>