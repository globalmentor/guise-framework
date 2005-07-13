package com.javaguise.model;

import java.util.*;

import com.garretwilson.lang.ObjectUtilities;

import static com.garretwilson.lang.ObjectUtilities.*;

/**A model of a table.
The model is thread-safe, synchronized on itself. Any iteration over values should include synchronization on the instance of this class. 
@param <C> The type of values contained in all the cells.
@author Garret Wilson
*/
public interface TableModel<C> extends LabelModel
{

	/**Determines the logical index of the given table column.
	@param column One of the table columns.
	@return The zero-based logical index of the column within the table, or -1 if the column is not one of the model's columns.
	*/
	public int getColumnIndex(final TableColumnModel<? extends C> column);

	/**@return A read-only list of table columns in physical order.*/ 
	public List<TableColumnModel<? extends C>> getColumns();

	/**@return The number of rows in this table.*/
	public int getRowCount();

	/**@return The number of columns in this table.*/
	public int getColumnCount();

	/**Returns the cell value at the given row and column.
	@param rowIndex The zero-based row index.
	@param column The column for which a value should be returned.
	@return The value in the cell at the given row and column, or <code>null</code> if there is no value in that cell.
	@exception IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	@exception IllegalArgumentException if the given column is not one of this table's columns.
	*/
	public <T extends C> T getCellValue(final int rowIndex, final TableColumnModel<T> column);

	/**Sets the cell value at the given row and column.
	@param rowIndex The zero-based row index.
	@param column The column for which a value should be returned.
	@param newCellValue The value to place in the cell at the given row and column, or <code>null</code> if there should be no value in that cell.
	@return The value previously in the given cell.
	@exception IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	@exception IllegalArgumentException if the given column is not one of this table's columns.
	*/
	public <T extends C> T setCellValue(final int rowIndex, final TableColumnModel<T> column, final T newCellValue);


	/**A lightweight class representing a row and column in a table.
	This class is useful as a map key, for instance.
	@param <V> The type of value contained in the cell.
	@author Garret Wilson
	*/
	public static class Cell<V>
	{
	
		/**The zero-based row index.*/		
		private final int rowIndex;
		
			/**@return The zero-based row index.*/		
			public int getRowIndex() {return rowIndex;}

		/**The column.*/		
		private final TableColumnModel<V> column;
		
			/**@return The column.*/		
			public TableColumnModel<V> getColumn() {return column;}

		/**Row and column constructor
		@param rowIndex The zero-based cell row index.
		@param column The cell column.
		@exception NullPointerException if the given column is null.
		*/
		public Cell(final int rowIndex, final TableColumnModel<V> column)
		{
			this.rowIndex=rowIndex;
			this.column=checkNull(column, "Column cannot be null.");
		}

    /**Determines whether the given object is equal to this object.
		@param object The object to compare to this object.
		@return <code>true</code> if the given object is another cell with the same row index and column.
		*/
		public boolean equals(final Object object)
		{
			if(object instanceof Cell)	//if the object is a cell
			{
				final Cell cell=(Cell)object;	//cast the object to a cell
				return getRowIndex()==cell.getRowIndex() && getColumn().equals(cell.getColumn());	//compare row index and column
			}
			else	//if the object is not a cell
			{
				return false;	//the objects aren't equal
			}			
		}

		/**@return A hash code for the cell.*/
    public int hashCode()
    {
    	return ObjectUtilities.hashCode(rowIndex, column);	//generate a hash code
    }

	}

}
