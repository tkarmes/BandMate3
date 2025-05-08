import { createRouter, createWebHistory } from 'vue-router';
import UserList from '@/components/UserList.vue';
import MusicianProfile from '@/components/MusicianProfile.vue';
import VenueProfile from '@/components/VenueProfile.vue';
import ConversationList from '@/components/ConversationList.vue';
import LoginForm from '@/components/LoginForm.vue';

const routes = [
  { path: '/', name: 'Login', component: LoginForm },
  { path: '/users', name: 'UserList', component: UserList, meta: { requiresAuth: true } },
  { path: '/musician/:userId', name: 'MusicianProfile', component: MusicianProfile, props: true, meta: { requiresAuth: true } },
  { path: '/venue/:userId', name: 'VenueProfile', component: VenueProfile, props: true, meta: { requiresAuth: true } },
  { path: '/conversations', name: 'ConversationList', component: ConversationList, meta: { requiresAuth: true } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login' });
  } else {
    next();
  }
});

export default router;