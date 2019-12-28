import { MutationTree } from 'vuex';
import { CheckCategoryState, CheckCategory } from '../../types';

export const mutations: MutationTree<CheckCategoryState> = {
  setCategories(state: CheckCategoryState, payload: Array<CheckCategory>) {
    state.categories = payload;
  },
  addCategory(state: CheckCategoryState, category: CheckCategory) {
    state.categories.push(category)
  },
  removeCategory(state: CheckCategoryState, category: CheckCategory) {
    const index = state.categories.findIndex(it => it.id === category.id)
    if (index >= 0) {
      state.categories.splice(index, 1)
    }
  },
  clear(state: CheckCategoryState) {
    state.categories = []
  },
  renameCategory(state: CheckCategoryState, { id, newName }) {
    state.categories.filter(it => it.id == id).forEach(it => it.name = newName)
  }
};