export class CheckBase {
  id: number;
  creator: string;
  name: string;
  approved: boolean;
  checkType: string;

  constructor(
    id: number,
    creator: string,
    name: string,
    approved: boolean,
    checkType: string
  ) {
    this.id = id;
    this.creator = creator;
    this.name = name;
    this.approved = approved;
    this.checkType = checkType;
  }
}

export class Check extends CheckBase {
  text: string;

  constructor(
    id: number,
    creator: string,
    name: string,
    approved: boolean,
    text: string,
    checkType: string
  ) {
    super(id, creator, name, approved, checkType);
    this.text = text;
  }
}