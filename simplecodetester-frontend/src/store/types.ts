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

  /**
   * Whether the current user is an admin.
   */
  isAdmin(): boolean;
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
  result: CheckResultType;
  message: string;
  errorOutput: string;

  constructor(check: string, result: CheckResultType, message: string, errorOutput: string) {
    this.check = check;
    this.result = result;
    this.message = message;
    this.errorOutput = errorOutput;
  }

  /**
   * Returns whether the result is FAILED.
   */
  failed(): boolean {
    return this.result === CheckResultType.FAILED;
  }
}

export enum CheckResultType {
  SUCCESSFUL = "SUCCESSFUL",
  FAILED = "FAILED",
  NOT_APPLICABLE = "NOT_APPLICABLE"
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