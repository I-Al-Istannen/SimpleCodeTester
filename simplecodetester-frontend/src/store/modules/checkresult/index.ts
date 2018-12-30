import { Module } from 'vuex'
import { RootState, CheckResultState } from '../../types'
import { mutations } from './mutations'
import { actions } from './actions'

export const state: CheckResultState = {
  checkResult: null
};

const namespaced: boolean = true;

export const checkresult: Module<CheckResultState, RootState> = {
  namespaced,
  state,
  mutations,
  actions
};