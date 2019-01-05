import { ActionTree } from 'vuex';
import axios, { AxiosPromise } from 'axios';
import { UserState, UserLoginInfo, UserInfo } from '../../types';
import { RootState } from '../../types';

export const actions: ActionTree<UserState, RootState> = {
  login({ commit, state, dispatch }, payload: UserLoginInfo): AxiosPromise<any> {
    const data: FormData = new FormData();
    data.append("username", payload.username);
    data.append("password", payload.password);

    return axios.post("/login", data).then((response) => {
      commit('setRefreshToken', response.data.token);

      return dispatch("fetchAccessToken")
    });
  },
  fetchAccessToken({ commit, state }): Promise<void> {
    const formData = new FormData()
    formData.append("refreshToken", state.refreshToken!)

    return axios.post("/login/get-access-token", formData)
      .then(response => {
        commit("setAccessToken", new UserInfo(
          response.data.userName,
          response.data.token,
          response.data.displayName,
          response.data.roles
        ));
      })
  }
};