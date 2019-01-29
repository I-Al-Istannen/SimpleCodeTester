import { MutationTree } from 'vuex';
import { MiscSettingsState, CheckCategory } from '../../types';

export const mutations: MutationTree<MiscSettingsState> = {
  setItemsPerPage(state: MiscSettingsState, items: number) {
    state.itemsPerPage = items;
  },
  setCategory(state: MiscSettingsState, category: CheckCategory | null) {
    state.category = category;
  }
};