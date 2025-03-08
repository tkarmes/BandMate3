<template>
    <div class="conversation">
      <h2>Your Conversations</h2>
      <ul v-if="conversations.length" class="convo-list">
        <li v-for="convo in conversations" :key="convo.conversationId" @click="selectConversation(convo.conversationId)">
          Conversation {{ convo.conversationId }} - {{ formatTimestamp(convo.createdAt) }}
        </li>
      </ul>
      <p v-else>No conversations yet.</p>
      <h3>Messages</h3>
      <input v-model="conversationId" placeholder="Enter Conversation ID" class="text-input" />
      <button @click="fetchMessages" :disabled="loading">Load Messages</button>
      <p v-if="loading">Loading...</p>
      <p v-if="error" class="error">{{ error }}</p>
      <ul v-if="messages.length && !loading">
        <li v-for="message in messages" :key="message.messageId" class="message">
          <strong>{{ message.senderId }}:</strong> {{ message.content }}
          <span class="timestamp">{{ formatTimestamp(message.sentAt) }}</span>
        </li>
      </ul>
      <p v-else-if="!loading">No messages yet.</p>
      <div class="send-message">
        <input v-model="newMessage" placeholder="Type a message" class="text-input" />
        <input v-model="receiverId" placeholder="Receiver ID" class="text-input" />
        <button @click="createAndSendMessage" :disabled="sending || !newMessage || !receiverId">Send</button>
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
        sending: false
      };
    },
    created() {
      this.fetchConversations();
    },
    methods: {
      async fetchConversations() {
        const token = localStorage.getItem('token');
        if (!token) {
          this.error = 'Please log in first';
          return;
        }
        try {
          const response = await axios.get(
            'http://localhost:9000/conversations',
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
            this.fetchConversations(); // Refresh convo list
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
      },
      selectConversation(id) {
        this.conversationId = id;
        this.fetchMessages();
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
  }
  
  .convo-list {
    list-style: none;
    padding: 0;
    margin-bottom: 20px;
  }
  
  .convo-list li {
    padding: 10px;
    border-bottom: 1px solid #444;
    color: var(--text);
    cursor: pointer;
    transition: background-color 0.3s ease;
  }
  
  .convo-list li:hover {
    background-color: var(--accent);
  }
  
  .text-input {
    width: 200px;
    padding: 10px;
    border: 1px solid #444;
    border-radius: 4px;
    background-color: #333;
    color: var(--text);
    font-size: 16px;
    margin-right: 10px;
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
  
  ul {
    list-style: none;
    padding: 0;
    margin-top: 20px;
  }
  
  .message {
    padding: 10px;
    border-bottom: 1px solid #444;
    color: var(--text);
  }
  
  .timestamp {
    font-size: 12px;
    color: #aaa;
    margin-left: 10px;
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