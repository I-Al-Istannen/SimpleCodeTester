import { ActionTree } from 'vuex';
import axios, { AxiosPromise } from 'axios';
import { UserState, UserLoginInfo, UserInfo } from '../../types';
import { RootState } from '../../types';

export const actions: ActionTree<UserState, RootState> = {
  login({ commit, state }, payload: UserLoginInfo): AxiosPromise<any> {
    const data: FormData = new FormData();
    data.append("username", payload.username);
    data.append("password", payload.password);


    return axios.post("/login", data).then((response) => {
      const result = response && response.data
      commit('loggedIn', new UserInfo(
        payload.username, result.token, result.displayName, result.roles
      ));

      return result
    });
  }
};