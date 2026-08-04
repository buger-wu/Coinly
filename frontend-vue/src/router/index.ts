import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layout/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
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
          component: () => import('@/views/books/BooksView.vue')
        },
        {
          path: 'statistics',
          name: 'statistics',
          component: () => import('@/views/statistics/StatisticsView.vue')
        }
      ]
    }
  ]
})

export default router
