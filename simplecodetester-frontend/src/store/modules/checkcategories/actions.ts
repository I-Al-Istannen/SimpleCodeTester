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
  },
  async addNew({ commit }, checkName: string) {
    const formData = new FormData()
    formData.append("name", checkName)
    const response = await axios.post("/check-category/add-new", formData);
    const category = new CheckCategory(response.data.name, response.data.id);

    commit("addCategory", category)

    return category
  },
  async deleteCheck({ commit }, category: CheckCategory) {
    const response = await axios.delete(`/check-category/delete/${category.id}`)

    commit("removeCategory", category)

    return response
  }
};