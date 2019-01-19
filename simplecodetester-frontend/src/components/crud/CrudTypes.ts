export interface Identifiable {

  /**
   * The id of the object.
   */
  id: any
}

export interface CrudRepository<T extends Identifiable, A extends Identifiable> {

  /**
   * Updates an item.
   * 
   * @param item the new item
   */
  updateItem(item: A): Promise<any>;

  /**
   * Deletes an item.
   * 
   * @param id the id
   */
  deleteItem(id: any): Promise<any>;

  /**
   * Adds a new item.
   * 
   * @param item the item
   */
  addItem(item: A): Promise<any>

  /**
   * Returns all items.
   */
  fetchAll(): Promise<Array<T>>
}