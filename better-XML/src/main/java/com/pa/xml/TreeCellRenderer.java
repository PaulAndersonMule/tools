package com.pa.xml;

import javax.swing.Icon;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author panderson
 */
public class TreeCellRenderer extends DefaultTreeCellRenderer{
  private static final Icon elementIcon;
  private static final Icon attributeIcon;
  private static final Icon textIcon;
  
  static{
    elementIcon = new MetalIconFactory.TreeFolderIcon();
    attributeIcon = new MetalIconFactory.TreeFolderIcon();
    textIcon = new MetalIconFactory.TreeLeafIcon();
  }
  
  @Override
  public Icon getLeafIcon() {
    return textIcon;
  }

  @Override
  public Icon getClosedIcon() {
    return elementIcon;
  }

  @Override
  public Icon getOpenIcon() {
    return elementIcon;
  }
  
}
