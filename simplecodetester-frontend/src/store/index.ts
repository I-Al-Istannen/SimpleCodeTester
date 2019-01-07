import Vue from 'vue'
import Vuex, { StoreOptions } from 'vuex'
import { RootState } from './types'
import { user } from './modules/user'
import { checkresult } from './modules/checkresult'
import { checkcategory } from './modules/checkcategories'
import createPersistedState from 'vuex-persistedstate'

const debug = process.env.NODE_ENV !== 'production'

Vue.use(Vuex)

const storeOptions: StoreOptions<RootState> = {
  state: {
    baseUrl: 'http://localhost:8080'
  } as RootState,
  modules: {
    user,
    checkresult,
    checkcategory
  },
  actions: {
    logout({ commit }) {
      commit("user/logout");
      commit("checkresult/clear");
      commit("checkcategory/clear")
    }
  },
  strict: debug,
  plugins: [createPersistedState({
    paths: ["user"]
  })]
}

export default new Vuex.Store(storeOptions);