import { Module } from 'vuex'
import { RootState, MiscSettingsState } from '../../types'
import { mutations } from './mutations'
import { actions } from './actions'
import { isJwtValid } from '@/util/requests';

export const state: MiscSettingsState = {
  itemsPerPage: 10,
  categoryId: 0
};

const namespaced: boolean = true;

export const miscsettings: Module<MiscSettingsState, RootState> = {
  namespaced,
  state,
  mutations,
  actions
};