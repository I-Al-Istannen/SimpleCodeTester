export interface RootState {
  baseUrl: string;

  user: UserState;
}

export interface UserState {
  userName: string;
  displayName: string;
  roles: Array<string>;
  token: string | null;
}

export class UserInfo {
  username: string;
  token: string;
  displayName: string;
  roles: Array<string>;

  constructor(username: string, token: string, displayName: string, roles: Array<string>) {
    this.username = username;
    this.token = token;
    this.displayName = displayName;
    this.roles = roles;
  }
}

export class UserLoginInfo {
  username: string;
  password: string;

  constructor(username: string, password: string) {
    this.password = password;
    this.username = username;
  }
}