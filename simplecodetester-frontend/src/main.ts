import Vue from 'vue'
import App from './App.vue'
import router from './router'
import Router from 'vue-router'
import Vuetify, { VLayout } from 'vuetify/lib'
import store from './store'
import Axios from 'axios';
import { isJwtValid } from './util/requests';
import 'vuetify/src/stylus/app.styl'

Vue.config.productionTip = false

Vue.use(Router)
Vue.use(Vuetify, {
  components: {
    VLayout
  },
  theme: {
    primary: '#4CAF50',
    accent: '#E040FB',
    red: '#FF6347'
  }
})

Axios.defaults.baseURL = "https://codetester.ialistannen.de"
//Axios.defaults.baseURL = "http://localhost:8081"

Axios.interceptors.request.use(
  async request => {
    if (!store.state.user.token) {
      return Promise.resolve(request);
    }

    if (!(request.url && request.url.indexOf("/login") >= 0) && !isJwtValid(store.state.user.token) && store.state.user.refreshToken) {
      await store.dispatch("user/fetchAccessToken");
    }

    request.headers['Authorization'] = 'Bearer ' + store.state.user.token;
    return Promise.resolve(request)
  }
)

new Vue({
  router,
  store,
  render: h => h(App),
}).$mount('#app')
