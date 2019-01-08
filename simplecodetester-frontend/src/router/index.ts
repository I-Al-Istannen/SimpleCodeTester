import Router, { Route } from 'vue-router'
import Vue from 'vue'
import Login from '@/components/Login.vue'
import Profile from '@/components/Profile.vue'
import CheckCode from '@/components/CheckCode.vue'
import ViewCheckResult from '@/components/ViewCheckResult.vue'
import UploadCheck from '@/components/checksubmit/UploadCheck.vue'
import CheckList from '@/components/checklist/CheckList.vue'
import UserList from '@/components/users/UserList.vue'
import CheckCategoryList from '@/components/checkcategory/CheckCategoryList.vue'
import ChangeOwnPassword from '@/components/users/ChangeOwnPassword.vue'
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
    },
    {
      path: '/view-users',
      name: 'viewUsers',
      component: UserList,
      meta: {
        title: function () {
          return 'Manage Users'
        }
      }
    },
    {
      path: '/change-own-password',
      name: 'changePassword',
      component: ChangeOwnPassword,
      meta: {
        title: function () {
          return 'Change your password'
        }
      }
    },
    {
      path: '/view-check-categories',
      name: 'viewCheckCategories',
      component: CheckCategoryList,
      meta: {
        title: function () {
          return 'View all check categories'
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