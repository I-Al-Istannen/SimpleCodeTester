import { MutationTree } from 'vuex';
import { CheckCategoryState, CheckCategory } from '../../types';

export const mutations: MutationTree<CheckCategoryState> = {
  setCategories(state: CheckCategoryState, payload: Array<CheckCategory>) {
    state.categories = payload;
  },
  clear(state: CheckCategoryState) {
    state.categories = []
  }
};