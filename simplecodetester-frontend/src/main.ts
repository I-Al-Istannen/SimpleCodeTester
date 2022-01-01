import Vue from 'vue'
import App from './App.vue'
import router from './router'
import Router from 'vue-router'
import store from './store'
import Axios from 'axios';
import {isJwtValid} from './util/requests';
import vuetify from './plugins/vuetify';

Vue.config.productionTip = false

Vue.use(Router)

Axios.defaults.baseURL = process.env.VUE_APP_BASE_URL

Axios.interceptors.request.use(
  async request => {
    if (!store.state.user.token) {
      return Promise.resolve(request);
    }

    if (!(request.url && request.url.indexOf("/login") >= 0) && !isJwtValid(store.state.user.token) && store.state.user.refreshToken) {
      await store.dispatch("user/fetchAccessToken");
    }

    if (!request.headers) {
      request.headers = {}
    }

    // eslint-disable-next-line require-atomic-updates
    request.headers['Authorization'] = 'Bearer ' + store.state.user.token;
    return Promise.resolve(request)
  }
)

new Vue({
  router,
  store,
  vuetify,
  render: h => h(App),
}).$mount('#app')
