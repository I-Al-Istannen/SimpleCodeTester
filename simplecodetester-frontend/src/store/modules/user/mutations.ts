import { MutationTree } from "vuex";
import { UserState, UserInfo } from "../../types";

export const mutations: MutationTree<UserState> = {
  setRefreshToken(state: UserState, token: string) {
    state.refreshToken = token;
  },
  setAccessToken(state: UserState, payload: UserInfo) {
    state.token = payload.token;
    state.userName = payload.username;
    state.displayName = payload.displayName;
    state.roles = payload.roles;
  },
  logout(state: UserState) {
    state.userName = "";
    state.roles = [];
    state.token = "";
    state.refreshToken = "";
    state.displayName = "Bobby Tables";
  },
  darkThemeSelected(state: UserState, selected: boolean) {
    state._darkThemeSelected = selected;
  },
};
