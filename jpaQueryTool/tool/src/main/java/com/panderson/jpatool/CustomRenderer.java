package com.panderson.jpatool;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

class CustomRenderer extends DefaultTreeCellRenderer {

	private final Icon objectIcon;

	public CustomRenderer(Icon icon) {
		objectIcon = icon;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if (leaf && isObject(value)) {
				setIcon(objectIcon);
		} else {
			setToolTipText(null); //no tool tip
		}

		return this;
	}

	protected boolean isObject(Object value) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object o = ((NodeObject)node.getUserObject()).getValue();
		if (o == null){
			return false;
		}
		return JPAWindow.isComplexObject(o.getClass());
	}
}
