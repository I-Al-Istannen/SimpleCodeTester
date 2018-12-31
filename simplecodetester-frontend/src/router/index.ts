import Router, { Route } from 'vue-router'
import Vue from 'vue'
import Login from '@/components/Login.vue'
import Profile from '@/components/Profile.vue'
import CheckCode from '@/components/CheckCode.vue'
import ViewCheckResult from '@/components/ViewCheckResult.vue'
import UploadCheck from '@/components/checksubmit/UploadCheck.vue'
import CheckList from '@/components/CheckList.vue'
import store from '@/store';

let router = new Router({
  routes: [
    {
      path: '/',
      redirect: '/profile'
    },
    {
      path: '/login',
      name: 'login',
      component: Login,
      meta: {
        title: function () {
          return 'Login'
        }
      }
    },
    {
      path: '/profile',
      name: 'profile',
      component: Profile,
      meta: {
        title: function () {
          return 'Profile'
        }
      }
    },
    {
      path: '/check-code',
      name: 'checkCode',
      component: CheckCode,
      meta: {
        title: function () {
          return 'Check code'
        }
      }
    },
    {
      path: '/view-check-result',
      name: 'viewCheckResult',
      component: ViewCheckResult,
      meta: {
        title: function () {
          return 'View check result'
        }
      }
    },
    {
      path: '/submit-check',
      name: 'submitCheck',
      component: UploadCheck,
      meta: {
        title: function () {
          return 'Submit a new check'
        }
      }
    },
    {
      path: '/view-checks',
      name: 'viewChecks',
      component: CheckList,
      meta: {
        title: function () {
          return 'View all checks'
        }
      }
    }
  ]
})

router.beforeEach((to, from, next) => {
  // Do not require auth for login
  if (to.path.startsWith('login') || to.path.startsWith('/login')) {
    next()
    return
  }

  if (!store.state.user.isTokenValid()) {
    next({
      name: 'login',
      query: {
        redirect: to.path
      }
    })
  } else {
    next();
  }
})

router.afterEach((to, from) => {
  if (to && to.meta && to.meta.title) {
    Vue.nextTick(() => {
      document.title = to.meta.title(to)
    })
  }
})

export default router