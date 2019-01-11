export interface CrudRepository<T> {

  /**
   * Updates an item.
   * 
   * @param id the id
   * @param item the new item
   */
  update(id: any, item: T): Promise<any>;

  /**
   * Deletes an item.
   * 
   * @param id the id
   */
  delete(id: any): Promise<any>;

  /**
   * Adds a new item.
   * 
   * @param id the id
   * @param item the item
   */
  add(id: any, item: T): Promise<any>

  /**
   * Returns all items.
   */
  fetchAll(): Promise<Array<T>>
}