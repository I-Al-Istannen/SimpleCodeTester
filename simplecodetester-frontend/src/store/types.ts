export interface RootState {
  baseUrl: string;

  user: UserState;
  checkresult: CheckResultState;
}

export interface UserState {
  userName: string;
  displayName: string;
  roles: Array<string>;
  token: string | null;

  /**
   * Returns whether the current token is valid.
   */
  isTokenValid(): boolean;
}

export interface CheckResultState {
  checkResult: CheckResult | null;
}

export class Pair<K, V>{
  key: K;
  value: V;

  constructor(key: K, value: V) {
    this.key = key;
    this.value = value;
  }
}

export class CheckResult {
  results: Array<Pair<string, Array<FileCheckResult>>>

  constructor(results: Array<Pair<string, Array<FileCheckResult>>>) {
    this.results = results
  }
}

export class FileCheckResult {
  check: string;
  successful: boolean;
  message: string;
  errorOutput: string;

  constructor(check: string, successful: boolean, message: string, errorOutput: string) {
    this.check = check;
    this.successful = successful;
    this.message = message;
    this.errorOutput = errorOutput;
  }
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