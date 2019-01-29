import { MutationTree } from 'vuex';
import { MiscSettingsState } from '../../types';

export const mutations: MutationTree<MiscSettingsState> = {
  setItemsPerPage(state: MiscSettingsState, items: number) {
    state.itemsPerPage = items;
  },
  setCategoryId(state: MiscSettingsState, categoryId: number) {
    state.categoryId = categoryId;
  }
};