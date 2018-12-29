import { Module } from 'vuex'
import { RootState, UserState } from '../../types'
import { mutations } from './mutations'
import { actions } from './actions'

export const state: UserState = {
  userName: "Unknown",
  roles: [],
  displayName: "Bobby Tables",
  token: null
};

const namespaced: boolean = true;

export const user: Module<UserState, RootState> = {
  namespaced,
  state,
  mutations,
  actions
};