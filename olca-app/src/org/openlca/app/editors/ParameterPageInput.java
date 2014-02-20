package org.openlca.app.editors;

import java.util.List;

import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.expressions.FormulaInterpreter;

/**
 * The input of a parameter section.
 */
public class ParameterPageInput {

	private ModelEditor<?> editor;
	private List<Parameter> parameters;
	private FormulaInterpreter interpreter;
	private ParameterScope scope;

	public ModelEditor<?> getEditor() {
		return editor;
	}

	public void setEditor(ModelEditor<?> editor) {
		this.editor = editor;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public FormulaInterpreter getInterpreter() {
		return interpreter;
	}

	public void setInterpreter(FormulaInterpreter interpreter) {
		this.interpreter = interpreter;
	}

	public ParameterScope getScope() {
		return scope;
	}

	public void setScope(ParameterScope scope) {
		this.scope = scope;
	}

}