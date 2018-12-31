import { Module } from 'vuex'
import { RootState, UserState } from '../../types'
import { mutations } from './mutations'
import { actions } from './actions'
import { isJwtValid } from '@/util/requests';

export const state: UserState = {
  userName: "Unknown",
  roles: [],
  displayName: "Bobby Tables",
  token: null,
  isTokenValid: function () {
    return isJwtValid(this.token)
  }
};

const namespaced: boolean = true;

export const user: Module<UserState, RootState> = {
  namespaced,
  state,
  mutations,
  actions
};