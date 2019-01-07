import { Module } from 'vuex'
import { RootState, CheckCategoryState } from '../../types'
import { mutations } from './mutations'
import { actions } from './actions'

export const state: CheckCategoryState = {
  categories: []
};

const namespaced: boolean = true;

export const checkcategory: Module<CheckCategoryState, RootState> = {
  namespaced,
  state,
  mutations,
  actions
};