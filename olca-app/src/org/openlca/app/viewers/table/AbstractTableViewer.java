package org.openlca.app.viewers.table;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.app.Messages;
import org.openlca.app.components.ModelTransfer;
import org.openlca.app.resources.ImageManager;
import org.openlca.app.resources.ImageType;
import org.openlca.app.util.UI;
import org.openlca.app.util.Viewers;
import org.openlca.app.viewers.AbstractViewer;
import org.openlca.app.viewers.table.modify.CellModifySupport;
import org.openlca.app.viewers.table.modify.IModelChangedListener;
import org.openlca.app.viewers.table.modify.IModelChangedListener.ModelChangeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of AbstractViewer for SWT table viewer.
 * 
 * There are three extensions that can be implemented by annotating the methods
 * of impelementing classes. To enable creation and removal actions use
 * annotations {@link OnCreate} and {@link OnRemove}. The run methods of each
 * action will call all annotated methods. Implementations are responsible to
 * update the input. To enable drop feature use {@link OnDrop} and specify the
 * type of accepted elements by the input parameter of the annotated method.
 */
public class AbstractTableViewer<T> extends AbstractViewer<T, TableViewer> {

	private Logger log = LoggerFactory.getLogger(getClass());
	private List<IModelChangedListener<T>> changeListener = new ArrayList<>();
	private List<Action> actions;
	private CellModifySupport<T> cellModifySupport;

	protected AbstractTableViewer(Composite parent) {
		super(parent);
	}

	@Override
	protected TableViewer createViewer(Composite parent) {
		TableViewer viewer = new TableViewer(parent, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.MULTI);

		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(getLabelProvider());
		viewer.setSorter(getSorter());

		Table table = viewer.getTable();
		String[] columnHeaders = getColumnHeaders();
		if (!useColumnHeaders()) {
			table.setLinesVisible(false);
			table.setHeaderVisible(false);
		} else {
			table.setLinesVisible(true);
			table.setHeaderVisible(true);
			for (String p : columnHeaders)
				new TableColumn(table, SWT.NULL).setText(p);
			for (TableColumn c : table.getColumns())
				c.pack();
		}
		if (useColumnHeaders())
			viewer.setColumnProperties(columnHeaders);
		UI.gridData(table, true, true);

		actions = new ArrayList<>();
		if (supports(OnCreate.class))
			actions.add(new CreateAction());
		if (supports(OnRemove.class))
			actions.add(new RemoveAction());
		UI.bindActions(viewer, actions.toArray(new Action[actions.size()]));

		if (supports(OnDrop.class))
			addDropSupport(viewer);

		cellModifySupport = new CellModifySupport<>(viewer);

		return viewer;
	}

	private void addDropSupport(TableViewer viewer) {
		final Transfer transferType = ModelTransfer.getInstance();
		DropTarget dropTarget = new DropTarget(viewer.getTable(), DND.DROP_COPY
				| DND.DROP_MOVE | DND.DROP_DEFAULT);
		dropTarget.setTransfer(new Transfer[] { transferType });
		final AbstractTableViewer<T> thisObject = this;
		dropTarget.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				if (transferType.isSupportedType(event.currentDataType))
					if (event.data != null)
						for (Method method : getMethods(OnDrop.class))
							tryInvoke(method, event.data);
			}

			private void tryInvoke(Method method, Object value) {
				Class<?> parameterType = method.getParameterTypes().length > 0 ? method
						.getParameterTypes()[0] : null;
				Class<?> dataType = value.getClass();
				if (dataType.isArray()) {
					for (Object object : (Object[]) value)
						if (parameterType == object.getClass()) {
							try {
								method.invoke(thisObject, object);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
				} else {
					if (parameterType == dataType)
						try {
							method.invoke(thisObject, value);
						} catch (Exception e) {
							e.printStackTrace();
						}
				}
			}
		});
	}

	protected CellModifySupport<T> getCellModifySupport() {
		return cellModifySupport;
	}

	/**
	 * Subclasses may override this for support of column headers for the table
	 * combo, if null or empty array is returned, the headers are not visible
	 * and the combo behaves like a standard combo
	 */
	protected String[] getColumnHeaders() {
		return null;
	}

	private boolean useColumnHeaders() {
		return getColumnHeaders() != null && getColumnHeaders().length > 0;
	}

	/**
	 * Binds the create and remove actions of the table viewer to the given
	 * section.
	 */
	public void bindTo(Section section) {
		UI.bindActions(section, actions.toArray(new Action[actions.size()]));
	}

	@SuppressWarnings("unchecked")
	public List<T> getAllSelected() {
		List<Object> list = Viewers.getAllSelected(getViewer());
		List<T> result = new ArrayList<>();
		for (Object value : list)
			if (!(value instanceof AbstractViewer.Null))
				result.add((T) value);
		return result;
	}

	public void addDoubleClickListener(IDoubleClickListener listener) {
		getViewer().addDoubleClickListener(listener);
	}

	public void removeDoubleClickListener(IDoubleClickListener listener) {
		getViewer().removeDoubleClickListener(listener);
	}

	public void addModelChangedListener(IModelChangedListener<T> listener) {
		if (!changeListener.contains(listener))
			changeListener.add(listener);
	}

	public void removeModelChangedListener(IModelChangedListener<T> listener) {
		if (changeListener.contains(listener))
			changeListener.remove(listener);
	}

	protected void fireModelChanged(ModelChangeType type, T element) {
		for (IModelChangedListener<T> listener : changeListener)
			listener.modelChanged(type, element);
	}

	private boolean supports(Class<? extends Annotation> clazz) {
		for (Method method : this.getClass().getDeclaredMethods())
			if (method.isAnnotationPresent(clazz))
				return true;
		return false;
	}

	private void call(Class<? extends Annotation> clazz) {
		for (Method method : getMethods(clazz))
			try {
				method.invoke(this);
			} catch (Exception e) {
				log.error("Cannot call onAdd method", e);
			}
	}

	private List<Method> getMethods(Class<? extends Annotation> clazz) {
		List<Method> methods = new ArrayList<>();
		for (Method method : this.getClass().getDeclaredMethods())
			if (method.isAnnotationPresent(clazz))
				methods.add(method);
		return methods;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	protected @interface OnCreate {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	protected @interface OnRemove {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	protected @interface OnDrop {
	}

	private class CreateAction extends Action {

		private CreateAction() {
			setText(Messages.AddAction_Text);
			setImageDescriptor(ImageManager
					.getImageDescriptor(ImageType.ADD_ICON));
			setDisabledImageDescriptor(ImageManager
					.getImageDescriptor(ImageType.ADD_ICON_DISABLED));
		}

		@Override
		public void run() {
			call(OnCreate.class);
		}

	}

	private class RemoveAction extends Action {

		private RemoveAction() {
			setText(Messages.RemoveAction_Text);
			setImageDescriptor(ImageManager
					.getImageDescriptor(ImageType.DELETE_ICON));
			setDisabledImageDescriptor(ImageManager
					.getImageDescriptor(ImageType.DELETE_ICON_DISABLED));
		}

		@Override
		public void run() {
			call(OnRemove.class);
		}

	}

}
