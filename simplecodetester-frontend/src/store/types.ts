export interface RootState {
  baseUrl: string;

  user: UserState;
  checkresult: CheckResultState;
  checkcategory: CheckCategoryState;
  miscsettings: MiscSettingsState;
}

export interface UserState {
  userName: string;
  displayName: string;
  roles: Array<string>;
  token: string | null;
  refreshToken: string | null;

  /**
   * Returns whether the current refresh token is valid.
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

export interface MiscSettingsState {
  itemsPerPage: number;
  category: CheckCategory | null;
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
  output: Array<IoLine>

  constructor(check: string, result: CheckResultType, message: string, errorOutput: string, output: Array<IoLine>) {
    this.check = check;
    this.result = result;
    this.message = message;
    this.errorOutput = errorOutput;
    this.output = output;
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


export class IoLine {
  lineType: IoLineType;
  content: string;

  constructor(lineType: IoLineType, content: string) {
    this.lineType = lineType;
    this.content = content;
  }
}

export enum IoLineType {
  ERROR = "error",
  INPUT = "input",
  OUTPUT = "output"
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

export class CheckCategoryState {
  categories: Array<CheckCategory>

  constructor(categories: Array<CheckCategory>) {
    this.categories = categories;
  }
}

export class CheckCategory {
  name: string;
  id: number;

  constructor(name: string, id: number) {
    this.name = name;
    this.id = id;
  }
}