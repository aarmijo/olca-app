package org.openlca.core.application.wizards;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.openlca.core.application.Messages;
import org.openlca.core.database.IDatabaseServer;
import org.openlca.core.resources.ImageType;
import org.openlca.ui.UI;

class DatabaseWizardPage extends WizardPage {

	private Text nameText;
	private Button[] contentRadios;
	private String[] existingNames;
	private int[] contentTypes;

	public DatabaseWizardPage(String[] existingNames) {
		super("database-wizard-page", Messages.NewDatabase,
				ImageType.NEW_WIZ_DATABASE.getDescriptor());
		setDescription(Messages.NewDatabase_Description);
		this.existingNames = existingNames;
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);
		UI.gridLayout(composite, 2);
		nameText = UI.formText(composite, Messages.NewDatabase_Name);
		nameText.addModifyListener(new TextListener());
		UI.formLabel(composite, Messages.NewDatabase_RefData);
		createContentRadios(composite);
	}

	private void createContentRadios(Composite composite) {
		Composite radioGroup = new Composite(composite, SWT.NONE);
		radioGroup.setLayout(new RowLayout(SWT.VERTICAL));
		String[] labels = { Messages.EMPTY_DATABASE,
				Messages.UNITS_AND_FLOW_PROPS, Messages.COMPLETE_REF_DATA };
		contentTypes = new int[] { IDatabaseServer.CONTENT_TYPE_EMPTY,
				IDatabaseServer.CONTENT_TYPE_UNITS,
				IDatabaseServer.CONTENT_TYPE_ALL_REF };
		contentRadios = new Button[3];
		for (int i = 0; i < 3; i++) {
			contentRadios[i] = new Button(radioGroup, SWT.RADIO);
			contentRadios[i].setText(labels[i]);
		}
		contentRadios[2].setSelection(true);
	}

	private class TextListener implements ModifyListener {
		@Override
		public void modifyText(ModifyEvent e) {
			String text = nameText.getText();
			validateName(text.toLowerCase());
		}
	}

	private void validateName(String name) {
		if (name == null || name.length() < 4)
			error(Messages.NewDatabase_NameToShort);
		else if (name.equals("test") || name.equals("mysql"))
			error(name + " " + Messages.NewDatabase_ReservedName);
		else if (!isIdentifier(name))
			error(Messages.NewDatabase_InvalidName);
		else if (exists(name))
			error(Messages.NewDatabase_AlreadyExists);
		else {
			setMessage(null);
			setPageComplete(true);
		}
	}

	private void error(String string) {
		this.setMessage(string, DialogPage.ERROR);
		setPageComplete(false);

	}

	private boolean isIdentifier(String s) {
		if (s.length() == 0 || !Character.isJavaIdentifierStart(s.charAt(0)))
			return false;
		for (int i = 1; i < s.length(); i++)
			if (!Character.isJavaIdentifierPart(s.charAt(i)))
				return false;
		return true;
	}

	private boolean exists(String name) {
		if (existingNames == null)
			return false;
		for (int i = 0; i < existingNames.length; i++) {
			String existingName = existingNames[i];
			if (existingName != null && existingName.equalsIgnoreCase(name))
				return true;
		}
		return false;
	}

	PageData getPageData() {
		PageData data = new PageData();
		for (int i = 0; i < contentRadios.length; i++) {
			if (contentRadios[i].getSelection()) {
				data.contentType = contentTypes[i];
				break;
			}
		}
		data.databaseName = nameText.getText().trim().toLowerCase();
		return data;
	}

	class PageData {
		int contentType = IDatabaseServer.CONTENT_TYPE_EMPTY;
		String databaseName;
	}

}