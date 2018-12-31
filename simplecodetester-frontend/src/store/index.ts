import Vue from 'vue'
import Vuex, { StoreOptions } from 'vuex'
import { RootState, UserInfo, UserState } from './types'
import { user } from './modules/user'
import { checkresult } from './modules/checkresult'
import createPersistedState from 'vuex-persistedstate'

const debug = process.env.NODE_ENV !== 'production'

Vue.use(Vuex)

const storeOptions: StoreOptions<RootState> = {
  state: {
    baseUrl: 'http://localhost:8080'
  } as RootState,
  modules: {
    user,
    checkresult
  },
  actions: {
    logout({commit}) {
      commit("user/logout");
      commit("checkresult/clear");
    }
  },
  strict: debug,
  plugins: [createPersistedState()]
}

export default new Vuex.Store(storeOptions);