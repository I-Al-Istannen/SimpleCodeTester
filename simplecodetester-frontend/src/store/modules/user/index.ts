import { Module } from "vuex";
import { RootState, UserState } from "../../types";
import { mutations } from "./mutations";
import { actions } from "./actions";
import { isJwtValid } from "@/util/requests";

export const state: UserState = {
  userName: "Unknown",
  roles: [],
  displayName: "Bobby Tables",
  token: null,
  refreshToken: null,
  _darkThemeSelected: undefined,
  isTokenValid(): boolean {
    return isJwtValid(this.refreshToken);
  },
  isAdmin: function() {
    return this.roles.includes("ROLE_ADMIN");
  },
  isEditor: function() {
    return this.roles.includes("ROLE_EDITOR");
  },
  darkThemeSelected: function() {
    if (this._darkThemeSelected !== undefined) {
      return this._darkThemeSelected;
    }
    return this.browserPrefersDarkTheme();
  },
  browserPrefersDarkTheme: function() {
    return window.matchMedia("(prefers-color-scheme: dark)").matches;
  },
  usesBrowsersThemePreferences: function() {
    return this._darkThemeSelected === undefined;
  },
};

const namespaced: boolean = true;

export const user: Module<UserState, RootState> = {
  namespaced,
  state,
  mutations,
  actions,
};
