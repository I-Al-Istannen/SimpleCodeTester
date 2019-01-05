import Axios from 'axios';

/**
 * A single user.
 */
export class User {
  id: string;
  displayName: string;
  enabled: boolean;
  roles: Array<string>;

  constructor(id: string, displayName: string, enabled: boolean, roles: Array<string>) {
    this.id = id;
    this.displayName = displayName;
    this.enabled = enabled;
    this.roles = roles;
  }
}

/**
 * A user that should be added.
 */
export class UserToAdd extends User {
  password: string;

  constructor(id: string, displayName: string, roles: Array<string>, password: string) {
    super(id, displayName, true, roles);
    this.password = password;
  }
}

/**
 * Contains all users.
 */
export class Users {
  users: Array<User> = []

  /**
   * Fetches all users.
   */
  async fetchAll(): Promise<void> {
    const response = await Axios.get("/admin/get-users");
    this.users = (response.data as Array<any>)
      .map(json => new User(json.id, json.name, json.enabled, json.authorities));
  }

  /**
   * Deletes a user.
   * 
   * @param user the user to delete
   */
  async deleteUser(user: User): Promise<void> {
    const response = await Axios.delete(`/admin/delete-user/${user.id}`);
    const index = this.users.indexOf(user);
    if (index < 0) {
      return;
    }
    this.users.splice(index, 1);
  }

  /**
   * Adds a user.
   * 
   * @param user the user to add
   */
  async addUser(user: UserToAdd): Promise<void> {
    const response = await Axios.post(`/admin/add-user`, user);
    this.users.push(user)
  }

  /**
   * Sets the enabled status for a user.
   * 
   * @param user the user to change
   * @param enabled whether the user should be enabled
   */
  async setEnabled(user: User, enabled: boolean): Promise<void> {
    const formData = new FormData()
    formData.append("userId", user.id)
    formData.append("enabled", enabled ? "true" : "false")

    const response = await Axios.post("/admin/set-enabled", formData);
    user.enabled = enabled;
  }
}