import { MutationTree } from 'vuex';
import { UserState, UserInfo } from '../../types';

export const mutations: MutationTree<UserState> = {
  loggedIn(state: UserState, payload: UserInfo) {
    state.token = payload.token;
    state.userName = payload.username;
    state.displayName = payload.displayName;
    state.roles = payload.roles;
  }
};