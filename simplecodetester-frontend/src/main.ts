import Vue from 'vue'
import App from './App.vue'
import router from './router'
import Router from 'vue-router'
import Vuetify from 'vuetify'
import store from './store'
import Axios from 'axios';

Vue.config.productionTip = false

Vue.use(Router)
Vue.use(Vuetify, {
  theme: {
    primary: '#4CAF50',
    accent: '#E040FB'
  }
})

Axios.defaults.baseURL = "https://codetester.ialistannen.de"
// Axios.defaults.baseURL = "http://localhost:8081"

Axios.interceptors.request.use(
  it => {
    if (store.state.user.token) {
      it.headers['Authorization'] = 'Bearer ' + store.state.user.token;
    }
    return Promise.resolve(it)
  }
)


new Vue({
  router,
  store,
  render: h => h(App),
}).$mount('#app')
