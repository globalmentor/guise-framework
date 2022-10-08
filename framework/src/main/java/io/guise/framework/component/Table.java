/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.component;

import java.beans.*;
import java.util.*;
import java.util.concurrent.*;

import static java.util.Objects.*;

import static com.globalmentor.java.Classes.*;
import static io.guise.framework.theme.Theme.*;

import com.globalmentor.beans.*;
import com.globalmentor.collections.DecoratorReadWriteLockMap;
import com.globalmentor.collections.ReadWriteLockMap;
import com.globalmentor.util.*;

import io.guise.framework.GuiseSession;
import io.guise.framework.component.layout.Border;
import io.guise.framework.converter.*;
import io.guise.framework.event.*;
import io.guise.framework.geometry.Extent;
import io.guise.framework.model.*;
import io.guise.framework.model.ui.AbstractPresentationModel;
import io.guise.framework.model.ui.PresentationModel;
import io.guise.framework.prototype.AbstractActionPrototype;
import io.guise.framework.prototype.ActionPrototype;
import io.guise.framework.style.FontStyle;
import io.guise.framework.validator.*;

/**
 * A table component.
 * <p>
 * Property changes to a column's UI model are repeated with the component as the source and the column UI model as the target.
 * </p>
 * @author Garret Wilson
 */
public class Table extends AbstractCompositeStateControl<TableModel.Cell<?>, Table.CellComponentState> implements EditComponent, TableModel {

	/** The display row count bound property. */
	public static final String DISPLAY_ROW_COUNT_PROPERTY = getPropertyName(Table.class, "displayRowCount");
	/** The display row start index bound property. */
	public static final String DISPLAY_ROW_START_INDEX_PROPERTY = getPropertyName(Table.class, "displayRowStartIndex");

	/** The table model used by this component. */
	private final TableModel tableModel;

	/** @return The table model used by this component. */
	protected TableModel getTableModel() {
		return tableModel;
	}

	/** The prototype for the first action. */
	private final ActionPrototype firstActionPrototype;

	/** @return The prototype for the first action. */
	public ActionPrototype getFirstActionPrototype() {
		return firstActionPrototype;
	}

	/** The prototype for the previous action. */
	private final ActionPrototype previousActionPrototype;

	/** @return The prototype for the previous action. */
	public ActionPrototype getPreviousActionPrototype() {
		return previousActionPrototype;
	}

	/** The prototype for the next action. */
	private final ActionPrototype nextActionPrototype;

	/** @return The prototype for the next action. */
	public ActionPrototype getNextActionPrototype() {
		return nextActionPrototype;
	}

	/** The prototype for the last action. */
	private final ActionPrototype lastActionPrototype;

	/** @return The prototype for the last action. */
	public ActionPrototype getLastActionPrototype() {
		return lastActionPrototype;
	}

	/** The number of rows to display at one time, or -1 the row count is not restricted. */
	private int displayRowCount = -1;

	/** @return The number of rows to display at one time, or -1 the row count is not restricted. */
	public int getDisplayRowCount() {
		return displayRowCount;
	}

	/**
	 * Sets the number of rows to display at one time. This is a bound property of type <code>Integer</code>.
	 * @param newDisplayRowCount The number of rows to display at one time, or -1 the row count is not restricted.
	 * @see #DISPLAY_ROW_COUNT_PROPERTY
	 */
	public void setDisplayRowCount(final int newDisplayRowCount) {
		if(displayRowCount != newDisplayRowCount) { //if the value is really changing
			final int oldDisplayRowCount = displayRowCount; //get the old value
			displayRowCount = newDisplayRowCount; //actually change the value
			firePropertyChange(DISPLAY_ROW_COUNT_PROPERTY, oldDisplayRowCount, newDisplayRowCount); //indicate that the value changed
		}
	}

	/** The index of the first row to display. */
	private int displayRowStartIndex = 0;

	/** @return The index of the first row to display. */
	public int getDisplayRowStartIndex() {
		return displayRowStartIndex;
	}

	/**
	 * Sets the index of the first row to display. This is a bound property of type <code>Integer</code>.
	 * @param newDisplayRowStartIndex The index of the first row to display.
	 * @throws IndexOutOfBoundsException if the given index is less than zero.
	 * @see #DISPLAY_ROW_START_INDEX_PROPERTY
	 */
	public void setDisplayRowStartIndex(final int newDisplayRowStartIndex) {
		if(newDisplayRowStartIndex < 0) { //if the index is less than zero
			throw new IndexOutOfBoundsException("Display row index cannot be be less than zero: " + newDisplayRowStartIndex);
		}
		if(displayRowStartIndex != newDisplayRowStartIndex) { //if the value is really changing
			final int oldDisplayRowStartIndex = displayRowStartIndex; //get the old value
			displayRowStartIndex = newDisplayRowStartIndex; //actually change the value
			firePropertyChange(DISPLAY_ROW_START_INDEX_PROPERTY, oldDisplayRowStartIndex, newDisplayRowStartIndex); //indicate that the value changed
		}
	}

	/**
	 * Whether the table is editable and the cells will allow the the user to change their values, if their respective columns are designated as editable as well.
	 */
	private boolean editable = true;

	@Override
	public boolean isEditable() {
		return editable;
	}

	@Override
	public void setEditable(final boolean newEditable) {
		if(editable != newEditable) { //if the value is really changing
			final boolean oldEditable = editable; //get the old value
			editable = newEditable; //actually change the value
			firePropertyChange(EDITABLE_PROPERTY, Boolean.valueOf(oldEditable), Boolean.valueOf(newEditable)); //indicate that the value changed
		}
	}

	/** The property change listener that updates prototype properties. */
	final protected PropertyChangeListener updatePrototypesPropertyChangeListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(final PropertyChangeEvent propertyChangeEvent) { //when the property changes
			updatePrototypes(); //update the prototypes
		}

	};

	/** The map of cell representation strategies for value classes. */
	private final Map<Class<?>, CellRepresentationStrategy<?>> valueClassCellRepresentationStrategyMap = new ConcurrentHashMap<Class<?>, CellRepresentationStrategy<?>>();

	/**
	 * Installs the given cell representation strategy to produce representation components for the given value class. The specified representation strategy will
	 * only be used if there is no representation strategy for a given column.
	 * @param <V> The type of value represented.
	 * @param valueClass The value class with which the strategy should be associated.
	 * @param cellRepresentationStrategy The strategy for generating components to represent values of the given type.
	 * @return The representation strategy previously associated with the given value class.
	 * @see #setCellRepresentationStrategy(TableColumnModel, CellRepresentationStrategy)
	 */
	@SuppressWarnings("unchecked")
	//we check the generic types before putting them in the map, so it's fine to cast the retrieved values
	public <V> CellRepresentationStrategy<? super V> setCellRepresentationStrategy(final Class<V> valueClass,
			CellRepresentationStrategy<V> cellRepresentationStrategy) {
		return (CellRepresentationStrategy<? super V>)valueClassCellRepresentationStrategyMap.put(valueClass, cellRepresentationStrategy); //associate the strategy with the value class in the map
	}

	/**
	 * Returns the given cell representation strategy assigned to produce representation components for the given value class. The returned representation
	 * strategy can be overridden by a representation strategy associated with a particular column.
	 * @param <V> The type of value represented.
	 * @param valueClass The value class with which the strategy should be associated.
	 * @return The strategy for generating components to represent values of the given type, or <code>null</code> if there is no associated representation
	 *         strategy.
	 * @see #getCellRepresentationStrategy(TableColumnModel)
	 */
	@SuppressWarnings("unchecked")
	//we check the generic types before putting them in the map, so it's fine to cast the retrieved values
	public <V> CellRepresentationStrategy<? super V> getCellRepresentationStrategy(final Class<V> valueClass) {
		return (CellRepresentationStrategy<? super V>)valueClassCellRepresentationStrategyMap.get(valueClass); //return the strategy linked to the value class in the map
	}

	/** The map of cell representation strategies for columns. */
	private final Map<TableColumnModel<?>, CellRepresentationStrategy<?>> columnCellRepresentationStrategyMap = new ConcurrentHashMap<TableColumnModel<?>, CellRepresentationStrategy<?>>();

	/**
	 * Installs the given cell representation strategy to produce representation components for the given column. A cell representation strategy for a particular
	 * column will override a cell representation strategy registered for a given type.
	 * @param <V> The type of value the column represents.
	 * @param column The column with which the strategy should be associated.
	 * @param cellRepresentationStrategy The strategy for generating components to represent values in the given column.
	 * @return The representation strategy previously associated with the given column.
	 * @see #setCellRepresentationStrategy(Class, CellRepresentationStrategy)
	 */
	@SuppressWarnings("unchecked")
	//we check the generic types before putting them in the map, so it's fine to cast the retrieved values
	public <V> CellRepresentationStrategy<? super V> setCellRepresentationStrategy(final TableColumnModel<V> column,
			CellRepresentationStrategy<V> cellRepresentationStrategy) {
		return (CellRepresentationStrategy<? super V>)columnCellRepresentationStrategyMap.put(column, cellRepresentationStrategy); //associate the strategy with the column in the map
	}

	/**
	 * Returns the given cell representation strategy assigned to produce representation components for the given column. A cell representation strategy for a
	 * particular column will override a cell representation strategy registered for a given type.
	 * @param <V> The type of value the column represents.
	 * @param column The column with which the strategy should be associated.
	 * @return The strategy for generating components to represent values in the given column, or <code>null</code> if there is no associated representation
	 *         strategy.
	 * @see #getCellRepresentationStrategy(Class)
	 */
	@SuppressWarnings("unchecked")
	//we check the generic types before putting them in the map, so it's fine to cast the retrieved values
	public <V> CellRepresentationStrategy<? super V> getCellRepresentationStrategy(final TableColumnModel<V> column) {
		return (CellRepresentationStrategy<? super V>)columnCellRepresentationStrategyMap.get(column); //return the strategy linked to the column in the map
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version is provided to allow public access.
	 * </p>
	 */
	@Override
	public Component getComponent(final TableModel.Cell<?> cell) {
		return super.getComponent(cell); //delegate to the parent version
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation delegates to {@link #createTypedComponentState(io.guise.framework.model.TableModel.Cell)}.
	 * </p>
	 */
	@Override
	protected CellComponentState createComponentState(final TableModel.Cell<?> cell) {
		return createTypedComponentState(cell); //delegate to the typed version
	}

	/**
	 * Creates a component state to represent the given object.
	 * @param <T> The type of value contained in the cell.
	 * @param cell The object with which the component state is to be associated.
	 * @return The component state to represent the given object.
	 * @throws IllegalArgumentException if the given object is not an appropriate object for a component state to be created.
	 * @throws IllegalStateException if there is no registered cell representation strategy appropriate for the cell.
	 */
	protected <T> CellComponentState createTypedComponentState(final TableModel.Cell<T> cell) {
		final TableModel tableModel = getTableModel(); //get the table model
		final int rowIndex = cell.getRowIndex(); //get the row index
		final TableColumnModel<T> column = cell.getColumn(); //get the cell column
		final boolean editable = isEditable() && column.isEditable(); //see if the cell is editable (a cell is only editable if both its table and column are editable)
		//TODO del		final TableModel.Cell<T> cell=new TableModel.Cell<T>(rowIndex, column);	//create a cell object representing this row and column
		//TODO fix editable		if(cellComponentState==null || cellComponentState.isEditable()!=editable)	//if there is no component for this cell, or the component has a different editable status
		CellRepresentationStrategy<? super T> cellRepresentationStrategy = getCellRepresentationStrategy(column); //see if there is a cell representation strategy registered for the column
		if(cellRepresentationStrategy == null) { //if there is no cell representation strategy for the column
			final Class<T> valueClass = column.getValueClass(); //get the value class of the column
			final Iterator<Class<?>> valueAncestorClassIterator = getAncestorClasses(valueClass).iterator(); //get all the ancestor classes of the value class, in increasing order of distance and abstractness
			while(cellRepresentationStrategy == null && valueAncestorClassIterator.hasNext()) { //keep iterating until we find a cell representation strategy
				final Class<? super T> valueAncestorClass = (Class<? super T>)valueAncestorClassIterator.next(); //get the next ancestor class
				cellRepresentationStrategy = getCellRepresentationStrategy(valueAncestorClass); //see if there is a cell representation strategy registered for the value class ancestor
			}
			if(cellRepresentationStrategy == null) { //if there is no cell representation strategy for the value class, either
				throw new IllegalStateException("No cell representation strategy registered for value class " + valueClass);
			}
		}
		final Component valueComponent = cellRepresentationStrategy.createComponent(this, tableModel, rowIndex, column, editable, false, false); //create a new component for the cell
		return new CellComponentState(valueComponent, editable); //create a new component state for the cell's component and metadata
	}

	/**
	 * Ensures the component for a particular row and column exists.
	 * @param <T> The type of value contained in the cells of the column.
	 * @param rowIndex The zero-based cell row index.
	 * @param column The cell column.
	 * @return The child component representing the given cell.
	 */
	/*TODO del when works
		public <T> Component verifyCellComponent(final int rowIndex, final TableColumnModel<T> column)
		{
			final TableModel tableModel=getTableModel();	//get the table model
			final boolean editable=isEditable() && column.isEditable();	//see if the cell is editable (a cell is only editable if both its table and column are editable)
			final TableModel.Cell<T> cell=new TableModel.Cell<T>(rowIndex, column);	//create a cell object representing this row and column
			CellComponentState cellComponentState=getComponentState(cell);	//get the component information for this cell
			if(cellComponentState==null || cellComponentState.isEditable()!=editable) {	//if there is no component for this cell, or the component has a different editable status
				final Component valueComponent=getCellRepresentationStrategy(column).createComponent(this, tableModel, rowIndex, column, editable, false, false);	//create a new component for the cell
	//TODO del			valueComponent.setParent(this);	//tell this component that this table component is its parent
				cellComponentState=new CellComponentState(valueComponent, editable);	//create a new component state for the cell's component and metadata
				putComponentState(cell, cellComponentState);	//store the component state in the map for next time
			}
			return cellComponentState.getComponent();	//return the representation component
		}
	*/

	/** The map of UI models for columns. */
	private final ReadWriteLockMap<TableColumnModel<?>, ColumnUIModel> columnUIModelMap = new DecoratorReadWriteLockMap<TableColumnModel<?>, ColumnUIModel>(
			new HashMap<TableColumnModel<?>, ColumnUIModel>());

	/**
	 * Retrieves the UI model for the given column. If no UI model yet exists for the given column, one will be created. There is normally no need for
	 * applications to call this method directly.
	 * @param column The column for which a UI model should be returned.
	 * @return The UI model for the given column.
	 * @throws NullPointerException if the given column is <code>null</code>.
	 */
	public ColumnUIModel getColumnUIModel(final TableColumnModel<?> column) { //if we ever allow columns to be removed, automatically remove the corresponding UI model and remove its repeat property change listener
		requireNonNull(column, "Column cannot be null.");
		ColumnUIModel columnUIModel; //we'll find a column UI model and store it here
		columnUIModelMap.readLock().lock(); //get a read lock to the column UI model map
		try {
			columnUIModel = columnUIModelMap.get(column); //try to get the column UI model
		} finally {
			columnUIModelMap.readLock().unlock(); //always release the read lock
		}
		if(columnUIModel == null) { //if there is as of yet no column UI model
			columnUIModelMap.writeLock().lock(); //get a write lock to the map
			try {
				columnUIModel = columnUIModelMap.get(column); //try to get the column UI model again
				if(columnUIModel == null) { //if there still is no column UI model
					columnUIModel = new ColumnUIModel(); //create a new column UI model
					columnUIModel.addPropertyChangeListener(getRepeatPropertyChangeListener()); //repeat constraints property change events
					columnUIModelMap.put(column, columnUIModel); //store the column UI model in the map
				}
			} finally {
				columnUIModelMap.writeLock().unlock(); //always release the write lock
			}
		}
		return columnUIModel; //return the column UI model we found
	}

	/**
	 * Returns the label font families for a given column.
	 * @param column The column for which the label font families should be returned.
	 * @return The prioritized list of label font family names, or <code>null</code> if no label font family names have been specified.
	 * @throws NullPointerException if the given column is <code>null</code>.
	 */
	public List<String> getColumnLabelFontFamilies(final TableColumnModel<?> column) {
		return getColumnUIModel(column).getLabelFontFamilies();
	}

	/**
	 * Sets the font families of the label for a given column. This is a bound property.
	 * @param column The column for which the label font families should be set.
	 * @param newLabelFontFamilies The new prioritized list of label font family names, or <code>null</code> if no label font family names are specified.
	 * @throws NullPointerException if the given column is <code>null</code>.
	 * @see ColumnUIModel#LABEL_FONT_FAMILIES_PROPERTY
	 */
	public void setColumnLabelFontFamilies(final TableColumnModel<?> column, final List<String> newLabelFontFamilies) {
		getColumnUIModel(column).setLabelFontFamilies(newLabelFontFamilies);
	}

	/**
	 * Sets the font families of the labels of all columns. This is a bound property for each column.
	 * @param newLabelFontFamilies The new prioritized list of label font family names, or <code>null</code> if no label font family names are specified.
	 * @see ColumnUIModel#LABEL_FONT_FAMILIES_PROPERTY
	 */
	public void setColumnLabelFontFamilies(final List<String> newLabelFontFamilies) {
		for(final TableColumnModel<?> column : getColumns()) { //for each column
			setColumnLabelFontFamilies(column, newLabelFontFamilies); //set the label font families
		}
	}

	/**
	 * Returns the label font size for a given column.
	 * @param column The column for which the label font size should be returned.
	 * @return The size of the label font from baseline to baseline, or <code>null</code> if no label font size has been specified.
	 * @throws NullPointerException if the given column is <code>null</code>.
	 */
	public Extent getColumnLabelFontSize(final TableColumnModel<?> column) {
		return getColumnUIModel(column).getLabelFontSize();
	}

	/**
	 * Sets the label font size of a given column. This is a bound property.
	 * @param column The column for which the label font size should be set.
	 * @param newLabelFontSize The new size of the label font from baseline to baseline, or <code>null</code> there is no label font size specified.
	 * @throws NullPointerException if the given column is <code>null</code>.
	 * @see ColumnUIModel#LABEL_FONT_SIZE_PROPERTY
	 */
	public void setColumnLabelFontSize(final TableColumnModel<?> column, final Extent newLabelFontSize) {
		getColumnUIModel(column).setLabelFontSize(newLabelFontSize);
	}

	/**
	 * Sets the label font size of all columns. This is a bound property for each column.
	 * @param newLabelFontSize The new size of the label font from baseline to baseline, or <code>null</code> there is no label font size specified.
	 * @see ColumnUIModel#LABEL_FONT_SIZE_PROPERTY
	 */
	public void setColumnLabelFontSize(final Extent newLabelFontSize) {
		for(final TableColumnModel<?> column : getColumns()) { //for each column
			setColumnLabelFontSize(column, newLabelFontSize); //set the label font size
		}
	}

	/**
	 * Returns the label font style for a given column.
	 * @param column The column for which the label font style should be returned.
	 * @return The style of the label font.
	 * @throws NullPointerException if the given column is <code>null</code>.
	 */
	public FontStyle getColumnLabelFontStyle(final TableColumnModel<?> column) {
		return getColumnUIModel(column).getLabelFontStyle();
	}

	/**
	 * Sets the style of the label font for a given column. This is a bound property.
	 * @param column The column for which the label font size should be set.
	 * @param newLabelFontStyle The style of the label font.
	 * @throws NullPointerException if the given column and/or label font style is <code>null</code>.
	 * @see ColumnUIModel#LABEL_FONT_STYLE_PROPERTY
	 */
	public void setColumnLabelFontStyle(final TableColumnModel<?> column, final FontStyle newLabelFontStyle) {
		getColumnUIModel(column).setLabelFontStyle(newLabelFontStyle);
	}

	/**
	 * Sets the style of the label font for all columns. This is a bound property of each column.
	 * @param newLabelFontStyle The style of the label font.
	 * @throws NullPointerException if the given label font style is <code>null</code>.
	 * @see ColumnUIModel#LABEL_FONT_STYLE_PROPERTY
	 */
	public void setColumnLabelFontStyle(final FontStyle newLabelFontStyle) {
		for(final TableColumnModel<?> column : getColumns()) { //for each column
			setColumnLabelFontStyle(column, newLabelFontStyle); //set the label font style
		}
	}

	/**
	 * Returns the label font weight for a given column.
	 * @param column The column for which the label font weight should be returned.
	 * @return The weight of the label font relative to a normal value of 0.5.
	 * @throws NullPointerException if the given column is <code>null</code>.
	 */
	public double getColumnLabelFontWeight(final TableColumnModel<?> column) {
		return getColumnUIModel(column).getLabelFontWeight();
	}

	/**
	 * Sets the weight of the label font of a given column. The weight of the label font relative to a normal value of 0.5. A font weight of 0.75 is equivalent to
	 * a bold font. It is recommended that the constant variables {@link PresentationModel#FONT_WEIGHT_NORMAL} and {@link PresentationModel#FONT_WEIGHT_BOLD} be
	 * used for the most compatibility across platforms. This is a bound property of type {@link Double}.
	 * @param column The column for which the label font weight should be set.
	 * @param newLabelFontWeight The weight of the label font relative to a normal value of 0.5.
	 * @throws NullPointerException if the given column is <code>null</code>.
	 * @see PresentationModel#FONT_WEIGHT_PROPERTY
	 * @see PresentationModel#FONT_WEIGHT_NORMAL
	 * @see PresentationModel#FONT_WEIGHT_BOLD
	 */
	public void setColumnLabelFontWeight(final TableColumnModel<?> column, final double newLabelFontWeight) {
		getColumnUIModel(column).setLabelFontWeight(newLabelFontWeight);
	}

	/**
	 * Sets the weight of the label font of all columns. The weight of the label font relative to a normal value of 0.5. A font weight of 0.75 is equivalent to a
	 * bold font. It is recommended that the constant variables {@link PresentationModel#FONT_WEIGHT_NORMAL} and {@link PresentationModel#FONT_WEIGHT_BOLD} be
	 * used for the most compatibility across platforms. This is a bound property of type {@link Double}.
	 * @param newLabelFontWeight The weight of the label font relative to a normal value of 0.5.
	 * @see PresentationModel#FONT_WEIGHT_PROPERTY
	 * @see PresentationModel#FONT_WEIGHT_NORMAL
	 * @see PresentationModel#FONT_WEIGHT_BOLD
	 */
	public void setColumnLabelFontWeight(final double newLabelFontWeight) {
		for(final TableColumnModel<?> column : getColumns()) { //for each column
			setColumnLabelFontWeight(column, newLabelFontWeight); //set the label font weight
		}
	}

	/**
	 * Returns the padding extent of the indicated column border.
	 * @param column The column for which a padding extent should be returned.
	 * @param border The border for which a padding extent should be returned.
	 * @return The padding extent of the given column border.
	 * @throws NullPointerException if the given column is <code>null</code>.
	 */
	public Extent getColumnPaddingExtent(final TableColumnModel<?> column, final Border border) {
		return getColumnUIModel(column).getPaddingExtent(border);
	}

	/**
	 * Returns the padding extent of the column line near page near border.
	 * @param column The column for which a padding extent should be returned.
	 * @return The padding extent of the given column border.
	 * @throws NullPointerException if the given column is <code>null</code>.
	 */
	public Extent getColumnPaddingLineNearExtent(final TableColumnModel<?> column) {
		return getColumnUIModel(column).getPaddingLineNearExtent();
	}

	/**
	 * Returns the padding extent of the column line far page near border.
	 * @param column The column for which a padding extent should be returned.
	 * @return The padding extent of the given column border.
	 * @throws NullPointerException if the given column is <code>null</code>.
	 */
	public Extent getColumnPaddingLineFarExtent(final TableColumnModel<?> column) {
		return getColumnUIModel(column).getPaddingLineFarExtent();
	}

	/**
	 * Returns the padding extent of the column line near page far border.
	 * @param column The column for which a padding extent should be returned.
	 * @return The padding extent of the given column border.
	 * @throws NullPointerException if the given column is <code>null</code>.
	 */
	public Extent getColumnPaddingPageNearExtent(final TableColumnModel<?> column) {
		return getColumnUIModel(column).getPaddingPageNearExtent();
	}

	/**
	 * Returns the padding extent of the column line far page far border.
	 * @param column The column for which a padding extent should be returned.
	 * @return The padding extent of the given column border.
	 * @throws NullPointerException if the given column is <code>null</code>.
	 */
	public Extent getColumnPaddingPageFarExtent(final TableColumnModel<?> column) {
		return getColumnUIModel(column).getPaddingPageFarExtent();
	}

	/**
	 * Sets the padding extent of a given column border. The padding extent of each column border represents a bound property.
	 * @param column The column for which the padding extent should be set.
	 * @param border The border for which the padding extent should be set.
	 * @param newPaddingExtent The padding extent.
	 * @throws NullPointerException if the given column, border and/or padding extent is <code>null</code>.
	 * @see ColumnUIModel#PADDING_LINE_NEAR_EXTENT_PROPERTY
	 * @see ColumnUIModel#PADDING_LINE_FAR_EXTENT_PROPERTY
	 * @see ColumnUIModel#PADDING_PAGE_NEAR_EXTENT_PROPERTY
	 * @see ColumnUIModel#PADDING_PAGE_FAR_EXTENT_PROPERTY
	 */
	public void setColumnPaddingExtent(final TableColumnModel<?> column, final Border border, final Extent newPaddingExtent) {
		getColumnUIModel(column).setPaddingExtent(border, newPaddingExtent);
	}

	/**
	 * Sets the padding extent of the column line near border. This is a bound property.
	 * @param column The column for which the padding extent should be set.
	 * @param newPaddingExtent The padding extent.
	 * @throws NullPointerException if the given column and/or padding extent is <code>null</code>.
	 * @see ColumnUIModel#PADDING_LINE_NEAR_EXTENT_PROPERTY
	 */
	public void setColumnPaddingLineNearExtent(final TableColumnModel<?> column, final Extent newPaddingExtent) {
		getColumnUIModel(column).setPaddingLineNearExtent(newPaddingExtent);
	}

	/**
	 * Sets the padding extent of the column line far border. This is a bound property.
	 * @param column The column for which the padding extent should be set.
	 * @param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
	 * @throws NullPointerException if the given column and/or padding extent is <code>null</code>.
	 * @see ColumnUIModel#PADDING_LINE_FAR_EXTENT_PROPERTY
	 */
	public void setColumnPaddingLineFarExtent(final TableColumnModel<?> column, final Extent newPaddingExtent) {
		getColumnUIModel(column).setPaddingLineFarExtent(newPaddingExtent);
	}

	/**
	 * Sets the padding extent of the column page near border. This is a bound property.
	 * @param column The column for which the padding extent should be set.
	 * @param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
	 * @throws NullPointerException if the given columna and/or padding extent is <code>null</code>.
	 * @see ColumnUIModel#PADDING_PAGE_NEAR_EXTENT_PROPERTY
	 */
	public void setColumnPaddingPageNearExtent(final TableColumnModel<?> column, final Extent newPaddingExtent) {
		getColumnUIModel(column).setPaddingPageNearExtent(newPaddingExtent);
	}

	/**
	 * Sets the padding extent of the column page far border. This is a bound property.
	 * @param column The column for which the padding extent should be set.
	 * @param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
	 * @throws NullPointerException if the given column and/or padding extent is <code>null</code>.
	 * @see ColumnUIModel#PADDING_PAGE_FAR_EXTENT_PROPERTY
	 */
	public void setColumnPaddingPageFarExtent(final TableColumnModel<?> column, final Extent newPaddingExtent) {
		getColumnUIModel(column).setPaddingPageFarExtent(newPaddingExtent);
	}

	/**
	 * Sets the padding extent of all borders of a column. The padding extent of each column border represents a bound property.
	 * @param column The column for which the padding extent should be set.
	 * @param newPaddingExtent The padding extent.
	 * @throws NullPointerException if the given column and/or padding extent is <code>null</code>.
	 * @see ColumnUIModel#PADDING_LINE_NEAR_EXTENT_PROPERTY
	 * @see ColumnUIModel#PADDING_LINE_FAR_EXTENT_PROPERTY
	 * @see ColumnUIModel#PADDING_PAGE_NEAR_EXTENT_PROPERTY
	 * @see ColumnUIModel#PADDING_PAGE_FAR_EXTENT_PROPERTY
	 */
	public void setColumnPaddingExtent(final TableColumnModel<?> column, final Extent newPaddingExtent) {
		getColumnUIModel(column).setPaddingExtent(newPaddingExtent);
	}

	/**
	 * Sets the padding extent of a all column borders of all columns. The padding extent of each column border represents a bound property.
	 * @param border The border for which the padding extent should be set.
	 * @param newPaddingExtent The padding extent.
	 * @throws NullPointerException if the border and/or padding extent is <code>null</code>.
	 * @see ColumnUIModel#PADDING_LINE_NEAR_EXTENT_PROPERTY
	 * @see ColumnUIModel#PADDING_LINE_FAR_EXTENT_PROPERTY
	 * @see ColumnUIModel#PADDING_PAGE_NEAR_EXTENT_PROPERTY
	 * @see ColumnUIModel#PADDING_PAGE_FAR_EXTENT_PROPERTY
	 */
	public void setColumnPaddingExtent(final Border border, final Extent newPaddingExtent) {
		for(TableColumnModel<?> column : getColumns()) { //for each column
			getColumnUIModel(column).setPaddingExtent(border, newPaddingExtent); //set the padding extent of the border for this column
		}
	}

	/**
	 * Sets the padding extent of the line near border of all columns. This is a bound property.
	 * @param newPaddingExtent The padding extent.
	 * @throws NullPointerException if the given padding extent is <code>null</code>.
	 * @see ColumnUIModel#PADDING_LINE_NEAR_EXTENT_PROPERTY
	 */
	public void setColumnPaddingLineNearExtent(final Extent newPaddingExtent) {
		setColumnPaddingExtent(Border.LINE_NEAR, newPaddingExtent);
	}

	/**
	 * Sets the padding extent of the line far border of all columns. This is a bound property.
	 * @param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
	 * @throws NullPointerException if the given padding extent is <code>null</code>.
	 * @see ColumnUIModel#PADDING_LINE_FAR_EXTENT_PROPERTY
	 */
	public void setColumnPaddingLineFarExtent(final Extent newPaddingExtent) {
		setColumnPaddingExtent(Border.LINE_FAR, newPaddingExtent);
	}

	/**
	 * Sets the padding extent of the page near border of all columns. This is a bound property.
	 * @param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
	 * @throws NullPointerException if the given padding extent is <code>null</code>.
	 * @see ColumnUIModel#PADDING_PAGE_NEAR_EXTENT_PROPERTY
	 */
	public void setColumnPaddingPageNearExtent(final Extent newPaddingExtent) {
		setColumnPaddingExtent(Border.PAGE_NEAR, newPaddingExtent);
	}

	/**
	 * Sets the padding extent of the page far border of all columns. This is a bound property.
	 * @param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
	 * @throws NullPointerException if the given padding extent is <code>null</code>.
	 * @see ColumnUIModel#PADDING_PAGE_FAR_EXTENT_PROPERTY
	 */
	public void setColumnPaddingPageFarExtent(final Extent newPaddingExtent) {
		setColumnPaddingExtent(Border.PAGE_FAR, newPaddingExtent);
	}

	/**
	 * Sets the padding extent of all borders of all columns. The padding extent of each border represents a bound property. This is a convenience method that
	 * calls {@link #setColumnPaddingExtent(Border, Extent)} for each border.
	 * @param newPaddingExtent The padding extent.
	 * @throws NullPointerException if the given padding extent is <code>null</code>.
	 * @see ColumnUIModel#PADDING_LINE_NEAR_EXTENT_PROPERTY
	 * @see ColumnUIModel#PADDING_LINE_FAR_EXTENT_PROPERTY
	 * @see ColumnUIModel#PADDING_PAGE_NEAR_EXTENT_PROPERTY
	 * @see ColumnUIModel#PADDING_PAGE_FAR_EXTENT_PROPERTY
	 */
	public void setColumnPaddingExtent(final Extent newPaddingExtent) {
		for(final Border border : Border.values()) { //for each border
			setColumnPaddingExtent(border, newPaddingExtent); //set this padding extent
		}
	}

	/**
	 * Value class and column names constructor with a default data model. Default cell representation strategies will be installed for the value classes of the
	 * indicated columns.
	 * @param <C> The type of values in all the cells in the table.
	 * @param valueClass The class indicating the type of values held in the model.
	 * @param columnNames The names to serve as label headers for the columns.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 */
	public <C> Table(final Class<C> valueClass, final String... columnNames) {
		this(new DefaultTableModel(valueClass, null, columnNames)); //construct the class with no default data
	}

	/**
	 * Columns constructor with a default data model. Default cell representation strategies will be installed for the value classes of the given columns.
	 * @param columns The models representing the table columns.
	 */
	public Table(final TableColumnModel<?>... columns) {
		this(new DefaultTableModel(null, columns)); //construct the class with no default data
	}

	/**
	 * Value class, table data, and column names constructor with a default data model. Default cell representation strategies will be installed for the value
	 * classes of the indicated columns.
	 * @param <C> The type of values in all the cells in the table.
	 * @param valueClass The class indicating the type of values held in the model.
	 * @param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if
	 *          no default values should be given.
	 * @param columnNames The names to serve as label headers for the columns.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 * @throws IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	 * @throws ClassCastException if one of the values in a row is not compatible with the type of its column.
	 */
	public <C> Table(final Class<C> valueClass, final C[][] rowValues, final String... columnNames) {
		this(new DefaultTableModel(valueClass, rowValues, columnNames)); //construct the class with a default model
	}

	/**
	 * Table data and columns constructor with a default data model. Default cell representation strategies will be installed for the value classes of the given
	 * columns.
	 * @param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if
	 *          no default values should be given.
	 * @param columns The models representing the table columns.
	 * @throws IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	 * @throws ClassCastException if one of the values in a row is not compatible with the type of its column.
	 */
	public Table(final Object[][] rowValues, final TableColumnModel<?>... columns) {
		this(new DefaultTableModel(rowValues, columns)); //construct the class with a default model
	}

	/**
	 * Table model constructor. Default cell representation strategies will be installed for the value classes of all the model's columns.
	 * @param tableModel The component data model.
	 * @throws NullPointerException if the given table model is <code>null</code>.
	 */
	public Table(final TableModel tableModel) {
		this.tableModel = requireNonNull(tableModel, "Table model cannot be null."); //save the table model
		this.tableModel.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen and repeat all property changes of the table model
		this.tableModel.addVetoableChangeListener(getRepeatVetoableChangeListener()); //listen and repeat all vetoable changes of the table model
		//TODO listen to and repeat table model events
		for(final TableColumnModel<?> column : tableModel.getColumns()) { //install a default cell representation strategy for each column's value class
			installDefaultCellRepresentationStrategy(column); //create and install a default representation strategy for this column's value class
		}
		getSession().addPropertyChangeListener(GuiseSession.LOCALE_PROPERTY, new AbstractGenericPropertyChangeListener<Locale>() { //listen for the session locale changing

			@Override
			public void propertyChange(GenericPropertyChangeEvent<Locale> propertyChangeEvent) { //if the locale changes
				clearComponentStates(); //clear all the components and component states in case they are locale-related TODO probably transfer this up to the abstract composite state class
			}

		});
		if(tableModel instanceof ListListenable) { //if this table model allows list listeners TODO improve this; create a table model listener---maybe that will implement ListListener
			final ListListenable<Object> listListenable = (ListListenable<Object>)tableModel; //get the list listenable
			listListenable.addListListener(new ListListener<Object>() { //listen for table modifications

				@Override
				public void listModified(final ListEvent<Object> listEvent) { //if the table list is modified
					clearComponentStates(); //clear all the components and component states TODO probably do this on a component-by-component basis
					getDepictor().setDepicted(false); //TODO fix hack; add a table listener and have the view listen to that
				};
			});
		}
		//first action prototype
		firstActionPrototype = new AbstractActionPrototype(LABEL_FIRST, GLYPH_FIRST) {

			@Override
			protected void action(final int force, final int option) {
				goFirst(); //go to the first set of rows
			};
		};
		//previous action prototype
		previousActionPrototype = new AbstractActionPrototype(LABEL_PREVIOUS, GLYPH_PREVIOUS) {

			@Override
			protected void action(final int force, final int option) {
				goPrevious(); //go to the previous set of rows
			};
		};
		//next action prototype
		nextActionPrototype = new AbstractActionPrototype(LABEL_NEXT, GLYPH_NEXT) {

			@Override
			protected void action(final int force, final int option) {
				goNext(); //go to the next set of rows
			};
		};
		//last action prototype
		lastActionPrototype = new AbstractActionPrototype(LABEL_LAST, GLYPH_LAST) {

			@Override
			protected void action(final int force, final int option) {
				goLast(); //go to the last set of rows
			};
		};
		addPropertyChangeListener(DISPLAY_ROW_COUNT_PROPERTY, updatePrototypesPropertyChangeListener); //update the prorotypes when the display row count changes
		addPropertyChangeListener(DISPLAY_ROW_START_INDEX_PROPERTY, updatePrototypesPropertyChangeListener); //update the prorotypes when the display row start index changes
		//TODO listen for the row count changing and update the prototypes in response
		updatePrototypes(); //update the prototypes
	}

	/** Updates the state of the prototypes, such as previous and next. */
	protected void updatePrototypes() {
		final int displayRowCount = getDisplayRowCount(); //get the rows to display
		if(displayRowCount > 0) { //if this table is paged
			final int displayRowStartIndex = getDisplayRowStartIndex(); //get the display row start index
			final boolean isFirstDisplayed = displayRowStartIndex == 0; //see if the first index is shown
			final boolean isLastDisplayed = displayRowStartIndex + displayRowCount >= getRowCount(); //see if the last index is shown
			firstActionPrototype.setEnabled(!isFirstDisplayed); //enable or disable the first action prototype
			previousActionPrototype.setEnabled(!isFirstDisplayed); //enable or disable the previous action prototype
			nextActionPrototype.setEnabled(!isLastDisplayed); //enable or disable the previous action prototype		
			lastActionPrototype.setEnabled(!isLastDisplayed); //enable or disable the last action prototype		
		} else { //if this table is not paged TODO decide if we later want to make these not displayed
			firstActionPrototype.setEnabled(false); //disable the first action prototype
			previousActionPrototype.setEnabled(false); //disable the previous action prototype
			nextActionPrototype.setEnabled(false); //disable the previous action prototype
			lastActionPrototype.setEnabled(false); //disable the last action prototype
		}
	}

	/** Goes to the first set of table rows. */
	public void goFirst() {
		setDisplayRowStartIndex(0); //go to the first index
	}

	/**
	 * Goes to the previous set of table rows if the display row count is restricted. If the display row count is not restricted and the row start index is
	 * greater than zero, it is reset to zero.
	 * @see #getDisplayRowStartIndex()
	 * @see #getDisplayRowCount()
	 */
	public void goPrevious() {
		final int displayRowStartIndex = getDisplayRowStartIndex(); //get the display row start index
		final int displayRowCount = getDisplayRowCount(); //get the rows to display
		if(displayRowCount > 0) { //if there is a valid display row count 
			setDisplayRowStartIndex(Math.max(displayRowStartIndex - displayRowCount, 0)); //go back a page, but not going below zero
		} else if(displayRowStartIndex > 0) { //if the display row count is not restricted, but the first index is not zero
			setDisplayRowStartIndex(0); //go to the first row
		}
	}

	/**
	 * Goes to the next set of table rows if the display row count is restricted. If the display row count is not restricted, or there are no rows, nothing
	 * occurs.
	 * @see #getDisplayRowStartIndex()
	 * @see #getDisplayRowCount()
	 */
	public void goNext() {
		if(getRowCount() > 0) { //if there are rows (this check prevents us setting the -1 row, below)
			final int displayRowStartIndex = getDisplayRowStartIndex(); //get the display row start index
			final int displayRowCount = getDisplayRowCount(); //get the rows to display
			final int nextDisplayRowStartIndex = displayRowStartIndex + displayRowCount; //calculate the next index
			if(displayRowCount > 0 && nextDisplayRowStartIndex < getRowCount() - 1) { //if there is a valid display row count and we can go forward
				setDisplayRowStartIndex(nextDisplayRowStartIndex); //go forward a page
			}
		}
	}

	/**
	 * Goes to the last set of table rows if the display row count is restricted. If the display row count is not restricted, nothing occurs.
	 * @see #getDisplayRowStartIndex()
	 * @see #getDisplayRowCount()
	 */
	public void goLast() {
		final int displayRowStartIndex = getDisplayRowStartIndex(); //get the display row start index
		final int displayRowCount = getDisplayRowCount(); //get the rows to display
		if(displayRowCount > 0) { //if there is a valid display row count
			final int rowCount = getRowCount(); //get the number of rows
			final int lastPageRowCount = rowCount % displayRowCount; //find out how many pages on the last page (which will be zero if the pages are evenly divisible by the display row count)
			setDisplayRowStartIndex(Math.max(rowCount - (lastPageRowCount > 0 ? lastPageRowCount : displayRowCount), 0)); //go to the last page, backing up a page if the pages are evenly divisible by the display row count, but not going below the first row
		}
	}

	//TableModel delegations

	@Override
	public int getColumnIndex(final TableColumnModel<?> column) {
		return getTableModel().getColumnIndex(column);
	}

	@Override
	public List<TableColumnModel<?>> getColumns() {
		return getTableModel().getColumns();
	}

	@Override
	public int getRowCount() {
		return getTableModel().getRowCount();
	}

	@Override
	public int getColumnCount() {
		return getTableModel().getColumnCount();
	}

	@Override
	public <C> C getCellValue(final Cell<C> cell) {
		return getTableModel().getCellValue(cell);
	}

	@Override
	public <C> C getCellValue(final int rowIndex, final TableColumnModel<C> column) {
		return getTableModel().getCellValue(rowIndex, column);
	}

	@Override
	public <C> void setCellValue(final Cell<C> cell, final C newCellValue) {
		getTableModel().setCellValue(cell, newCellValue);
	}

	@Override
	public <C> void setCellValue(final int rowIndex, final TableColumnModel<C> column, final C newCellValue) {
		getTableModel().setCellValue(rowIndex, column, newCellValue);
	}

	/**
	 * An encapsulation of a component for a cell along with other metadata, such as whether the component was editable when created.
	 * @author Garret Wilson
	 */
	protected static class CellComponentState extends AbstractCompositeStateComponent.ComponentState {

		/** Whether the component is for a cell that was editable when the component was created. */
		private final boolean editable;

		/** @return Whether the component is for a cell that was editable when the component was created. */
		public boolean isEditable() {
			return editable;
		}

		/**
		 * Constructor
		 * @param component The component for a cell.
		 * @param editable Whether the component is for a cell that was editable when the component was created.
		 * @throws NullPointerException if the given component is <code>null</code>.
		 */
		public CellComponentState(final Component component, final boolean editable) {
			super(component); //construct the parent class
			this.editable = editable;
		}
	}

	/**
	 * An encapsulation of the user interface-related model used for a column.
	 * @author Garret Wilson
	 */
	protected static class ColumnUIModel extends AbstractPresentationModel {
	}

	/**
	 * Installs a default cell representation strategy for the value class of the given column if no cell representation strategy is registered for that value
	 * class.
	 * @param <T> The type of value contained in the column.
	 * @param column The table column for the value class of which a default cell representation strategy should be installed.
	 */
	private <T> void installDefaultCellRepresentationStrategy(final TableColumnModel<T> column) {
		final Class<T> valueClass = column.getValueClass(); //get the column's value class
		if(getCellRepresentationStrategy(valueClass) == null) { //if there is not cell representation strategy installed for this value class
			setCellRepresentationStrategy(valueClass, new DefaultCellRepresentationStrategy<T>(AbstractStringLiteralConverter.getInstance(valueClass))); //create a default cell representation strategy for the value class and register it with the value class
		}
	}

	//TODO fix the edit event to actually be fired

	@Override
	public void addEditListener(final EditListener editListener) {
		getEventListenerManager().add(EditListener.class, editListener); //add the listener
	}

	@Override
	public void removeEditListener(final EditListener editListener) {
		getEventListenerManager().remove(EditListener.class, editListener); //remove the listener
	}

	/**
	 * Fires an edit event to all registered edit listeners. This method delegates to {@link #fireEdited(EditEvent)}.
	 * @see EditListener
	 * @see EditEvent
	 */
	protected void fireEdited() {
		if(getEventListenerManager().hasListeners(EditListener.class)) { //if there are edit listeners registered
			fireEdited(new EditEvent(this)); //create and fire a new edit event
		}
	}

	/**
	 * Fires a given edit event to all registered edit listeners.
	 * @param editEvent The edit event to fire.
	 */
	protected void fireEdited(final EditEvent editEvent) {
		for(final EditListener editListener : getEventListenerManager().getListeners(EditListener.class)) { //for each edit listener
			editListener.edited(editEvent); //dispatch the edit event to the listener
		}
	}

	/**
	 * A strategy for generating components to represent table cell model values.
	 * @param <V> The type of value the strategy is to represent.
	 * @author Garret Wilson
	 */
	public interface CellRepresentationStrategy<V> {

		/**
		 * Creates a component to represent the given cell.
		 * @param <C> The type of value contained in the column.
		 * @param table The component containing the model.
		 * @param model The model containing the value.
		 * @param rowIndex The zero-based row index of the value.
		 * @param column The column of the value.
		 * @param editable Whether values in this column are editable.
		 * @param selected <code>true</code> if the value is selected.
		 * @param focused <code>true</code> if the value has the focus.
		 * @return A new component to represent the given value.
		 */
		public <C extends V> Component createComponent(final Table table, final TableModel model, final int rowIndex, final TableColumnModel<C> column,
				final boolean editable, final boolean selected, final boolean focused);
	}

	/**
	 * A default table cell representation strategy. Component values will be represented as themselves. For non-editable cells, a message component will be
	 * generated using the cell's value as its message. Editable cells will be represented using a checkbox for boolean values and a text control for all other
	 * values.
	 * @param <V> The type of value the strategy is to represent.
	 * @see Message
	 * @see Converter
	 * @author Garret Wilson
	 */
	public static class DefaultCellRepresentationStrategy<V> implements CellRepresentationStrategy<V> {

		/** The converter to use for displaying the value as a string. */
		private final Converter<V, String> converter;

		/** @return The converter to use for displaying the value as a string. */
		public Converter<V, String> getConverter() {
			return converter;
		}

		/**
		 * Converter constructor.
		 * @param converter The converter to use for displaying the value as a string.
		 * @throws NullPointerException if the given converter is <code>null</code>.
		 */
		public DefaultCellRepresentationStrategy(final Converter<V, String> converter) {
			this.converter = requireNonNull(converter, "Converter cannot be null."); //save the converter
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * This implementation returns a message with string value of the given value using the object's <code>toString()</code> method.
		 * </p>
		 */
		@SuppressWarnings("unchecked")
		@Override
		//we check the type of the column value class, so the casts are safe
		public <C extends V> Component createComponent(final Table table, final TableModel model, final int rowIndex, final TableColumnModel<C> column,
				final boolean editable, final boolean selected, final boolean focused) {
			final TableModel.Cell<C> cell = new TableModel.Cell<C>(rowIndex, column); //create a cell to represent the row and column
			final Class<C> valueClass = column.getValueClass(); //get the value class of the column
			if(Component.class.isAssignableFrom(valueClass)) { //if a component is being represented
				return (Component)model.getCellValue(cell); //return the value as a component TODO find a way to update the cached component if it changes
			}
			final int columnIndex = model.getColumnIndex(column); //get the logical index of the given column
			if(editable) { //if the component should be editable
				final ValueModel<C> valueModel = new DefaultCellValueModel<C>(model, cell); //create a new value model for the cell
				if(Boolean.class.isAssignableFrom(valueClass)) { //if the value class is subclass of Boolean
					return new CheckControl((ValueModel<Boolean>)(Object)valueModel); //create a new check control for the Boolean value model	(intermediate cast needed for Sun JDK 1.6.0_03-b05, which complains of incompatible types; not required for Eclipse 3.4M3)
				} else { //for all other values
					return new TextControl<C>(valueModel); //generate a text input control for the value model
				}
			} else { //if the component should not be editable, return a message component
				return new DefaultCellMessage<C>(model, cell, getConverter()); //create a message component containing a message model representing the value's string value				
			}
		}
	}

	/**
	 * A message model that returns a default representation of the cell in a message.
	 * @param <C> The type of value in the cell.
	 * @author Garret Wilson
	 */
	public static class DefaultCellMessage<C> extends Message //TODO convert this to a DefaultCellText component
	{

		/** The table model of the cell. */
		private final TableModel tableModel;

		/** @return The table model of the cell. */
		protected TableModel getTableModel() {
			return tableModel;
		}

		/** The cell being represented */
		private TableModel.Cell<C> cell;

		/** @return The cell being represented */
		protected TableModel.Cell<C> getCell() {
			return cell;
		}

		/** The converter to use for displaying the value as a string. */
		private final Converter<? super C, String> converter;

		/** @return The converter to use for displaying the value as a string. */
		public Converter<? super C, String> getConverter() {
			return converter;
		}

		/**
		 * Constructs a default message for a cell.
		 * @param tableModel The table model of the cell.
		 * @param cell The cell being represented.
		 * @param converter The converter to use for displaying the value as a string.
		 * @throws NullPointerException if the given session, table model and/or cell is <code>null</code>.
		 */
		public DefaultCellMessage(final TableModel tableModel, final TableModel.Cell<C> cell, final Converter<? super C, String> converter) {
			this.tableModel = requireNonNull(tableModel, "Table model cannot be null.");
			this.cell = requireNonNull(cell, "Cell cannot be null.");
			this.converter = requireNonNull(converter, "Converter cannot be null.");
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * This implementation returns a message with a string value of the given value using the installed converter, if no message has been explicitly set.
		 * </p>
		 * @see #getConverter()
		 */
		@Override
		public String getMessage() {
			String message = super.getMessage(); //get the message explicitly set
			if(message == null) { //if no message has been explicitly set
				final TableModel.Cell<C> cell = getCell(); //get our current cell
				final C value = getTableModel().getCellValue(cell.getRowIndex(), cell.getColumn()); //get the value from the table model
				try {
					message = getConverter().convertValue(value); //return the literal value of the value
				} catch(final ConversionException conversionException) { //we don't expect a value-to-string conversion to result in an error
					throw new AssertionError(conversionException);
				}
			}
			return message; //return the message
		}

	}

	/**
	 * A value model that returns and updates a the value of the cell.
	 * @param <C> The type of value in the cell.
	 * @author Garret Wilson
	 */
	public static class DefaultCellValueModel<C> extends DefaultValueModel<C> {

		/** The table model of the cell. */
		private final TableModel model;

		/** @return The table model of the cell. */
		protected TableModel getModel() {
			return model;
		}

		/** The cell being represented */
		private TableModel.Cell<C> cell;

		/** @return The cell being represented */
		protected TableModel.Cell<C> getCell() {
			return cell;
		}

		/**
		 * Constructs a default value model for a cell.
		 * @param model The table model of the cell.
		 * @param cell The cell being represented.
		 * @throws NullPointerException if the given table model and/or cell is <code>null</code>.
		 */
		public DefaultCellValueModel(final TableModel model, final TableModel.Cell<C> cell) {
			super(requireNonNull(cell, "Cell cannot be null.").getColumn().getValueClass()); //construct the parent class
			this.model = requireNonNull(model, "Table model cannot be null.");
			this.cell = cell;
		}

		/*
		 * @return Whether the model's value is editable and the corresponding control will allow the the user to change the value. This version returns
		 *         <code>true</code> if the model and column are both editable.
		 */
		//TODO important fix		public boolean isEditable() {return getModel().isEditable() && getCell().getColumn().isEditable();}

		/*
		 * Sets whether the model's value is editable and the corresponding control will allow the the user to change the value. This version throws an exception,
		 * as the editable status is read-only.
		 * @param newEditable <code>true</code> if the corresponding control should allow the user to change the value.
		 */
		//TODO important fix		public void setEditable(final boolean newEditable) {throw new UnsupportedOperationException("Editable is read-only.");}

		/*
		 * @return Whether the model is enabled and and the corresponding control can receive user input. This version returns <code>true</code> if the model and
		 *         column are both enabled.
		 */
		//TODO update once enabled is moved		public boolean isEnabled() {return getModel().isEnabled() && getCell().getColumn().isEnabled();}

		/*
		 * Sets whether the model is enabled and and the corresponding control can receive user input. This version throws an exception, as the enabled status is
		 * read-only.
		 * @param newEnabled <code>true</code> if the corresponding control should indicate and accept user input.
		 */
		//TODO update		public void setEnabled(final boolean newEnabled) {throw new UnsupportedOperationException("Enabled is read-only.");}

		@Override
		public Validator<C> getValidator() {
			return getCell().getColumn().getValidator();
		} //return the validator from the column

		@Override
		public void setValidator(final Validator<C> newValidator) {
			getCell().getColumn().setValidator(newValidator);
		}

		@Override
		public C getValue() {
			return getModel().getCellValue(getCell());
		} //return the value from the table model

		@Override
		public void setValue(final C newValue) throws PropertyVetoException {
			final C oldValue = getValue(); //get the old value
			final Validator<C> validator = getValidator(); //get the currently installed validator, if there is one
			if(validator != null) { //if a validator is installed, always validate the value, even if it isn't changing, so that an initial value that may not be valid will throw an error when it's tried to be set to the same, but invalid, value
				try {
					validator.validate(newValue); //validate the new value, throwing an exception if anything is wrong
				} catch(final ValidationException validationException) { //if the new value doesn't pass validation
					throw createPropertyVetoException(this, validationException, VALUE_PROPERTY, oldValue, newValue); //throw a property veto exception representing the validation error
				}
			}
			getModel().setCellValue(getCell(), newValue); //set the value in the table model
		}

		@Override
		public void resetValue() {
			getModel().setCellValue(getCell(), null); //set a null value in the table model
		}

	}

}
