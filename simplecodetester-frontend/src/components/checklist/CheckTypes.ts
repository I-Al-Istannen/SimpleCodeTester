import Axios, { AxiosPromise } from 'axios';
import { CheckCategory } from '@/store/types';

/**
 * The base of a check, containing all metadata but no content.
 */
export class CheckBase {
  id: number;
  creator: string;
  name: string;
  approved: boolean;
  checkType: string;
  category: CheckCategory;

  constructor(
    id: number,
    creator: string,
    name: string,
    approved: boolean,
    checkType: string,
    category: CheckCategory
  ) {
    this.id = id;
    this.creator = creator;
    this.name = name;
    this.approved = approved;
    this.checkType = checkType;
    this.category = category;
  }
}

export class IOCheck {
  input: string;
  output: string;
  name: string;

  constructor(input: string, output: string, name: string) {
    this.input = input;
    this.output = output;
    this.name = name;
  }
}

/**
 * A collection of CheckBases and their content, lazily fetched.
 */
export class CheckCollection {
  private checkBases: Array<CheckBase> = []
  private checkContents: any = {}

  /**
   * Fetches the content for a single check. The promise resolves when the content
   * is saved in this collection.
   * 
   * @param check the check to fetch the content for
   */
  fetchContent(check: CheckBase): Promise<any> {
    if (this.checkContents[check.id]) {
      return Promise.resolve(this.checkContents[check.id])
    }
    return Axios.get("/checks/get", {
      params: {
        id: check.id
      }
    })
      .then(response => {
        this.checkContents[check.id] = response.data.text;

        return this.checkContents[check.id]
      })
  }

  /**
   * Deletes a single check. The promise resolves if it was successfully deted.
   * 
   * @param check the check to delete
   */
  deleteCheck(check: CheckBase): Promise<void> {
    return Axios.delete("/checks/remove/" + check.id)
      .then(_ => {
        const index = this.checkBases.indexOf(check);
        if (index >= 0) {
          this.checkBases.splice(index, 1);
          delete this.checkContents[check.id];
        }
      });
  }

  /**
   * Sets the approval status for a check. The promise resolves after the status was set.
   * 
   * @param check the check to change the approval status for
   * @param approved whether the check is approved
   */
  setCheckApproval(check: CheckBase, approved: boolean): Promise<void> {
    const formData = new FormData();
    formData.append("id", check.id.toString());
    formData.append("approved", approved ? "true" : "false");

    return Axios.post("/checks/approve", formData)
      .then(response => {
        check.approved = approved;
      })
  }

  /**
   * Fetches all checks. The promise resolves after they have been fetched.
   */
  fetchAll(): Promise<void> {
    return Axios.get("/checks/get-all")
      .then(response => {
        this.checkBases = response.data as Array<CheckBase>;
        const scratchObject = {} as any;
        this.checkBases.forEach(it => (scratchObject[it.id] = undefined));
        // This is needed as vue can not observe property addition/deletion
        // So we just build the full object and then assign to to vue (and making it reactive)
        this.checkContents = scratchObject;
      });
  }

  /**
   * Updates an IO check.
   * 
   * @param check the check data
   * @param id the check id
   */
  updateIoCheck(check: IOCheck, id: number): Promise<void> {
    const formData = new FormData();
    formData.append("input", check.input)
    formData.append("output", check.output)
    formData.append("name", check.name)
    formData.append("checkId", "" + id)

    return Axios.post("/checks/update-io", formData)
      .then(response => {
        this.checkContents[id] = response.data.text
      })
  }
}