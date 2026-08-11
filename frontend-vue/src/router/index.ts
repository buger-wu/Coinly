import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layout/MainLayout.vue'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { public: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: { public: true }
    },
    {
      path: '/',
      component: MainLayout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue')
        },
        {
          path: 'books',
          name: 'books',
          component: () => import('@/views/book/BookListView.vue')
        },
        {
          path: 'books/:bookId/transactions',
          name: 'transactions',
          component: () => import('@/views/transaction/TransactionListView.vue')
        },
        {
          path: 'categories',
          name: 'categories',
          component: () => import('@/views/category/CategoryView.vue')
        },
        {
          path: 'budget',
          name: 'budget',
          component: () => import('@/views/budget/BudgetView.vue')
        },
        {
          path: 'statistics',
          name: 'statistics',
          component: () => import('@/views/statistics/StatisticsView.vue')
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/user/ProfileView.vue')
        },
        {
          path: 'password',
          name: 'password',
          component: () => import('@/views/user/PasswordView.vue')
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
      meta: { public: true }
    }
  ]
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (!to.meta.public && !userStore.isLoggedIn()) {
    return '/login'
  }
  if (to.meta.public && userStore.isLoggedIn() && (to.path === '/login' || to.path === '/register')) {
    return '/dashboard'
  }
})

export default router
