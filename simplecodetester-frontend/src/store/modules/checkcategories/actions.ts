import { ActionTree } from 'vuex';
import axios, { AxiosPromise } from 'axios';
import { CheckCategory, CheckCategoryState } from '../../types';
import { RootState } from '../../types';

export const actions: ActionTree<CheckCategoryState, RootState> = {
  async fetchAll({ commit, state }): Promise<Array<CheckCategory>> {
    if (state.categories.length > 0) {
      return state.categories;
    }
    const response = await axios.get("/check-category/get-all");
    const data = (response.data as Array<any>).map(it => new CheckCategory(it.name, it.id));
    commit("setCategories", data);
    return data;
  }
};