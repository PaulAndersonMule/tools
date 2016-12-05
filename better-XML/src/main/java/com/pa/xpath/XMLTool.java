package com.pa.xpath;

import com.pa.xml.AttributeObject;
import com.pa.xml.ElementObject;
import com.pa.xml.SAXHandler;
import com.pa.xml.TextNodeObject;
import com.pa.xml.TreeNodeObject;
import com.pa.xpath.data.DataService;
import com.pa.xpath.data.XPathLibrarySection;
import com.pa.xpath.data.XSLTItem;
import com.pa.xpath.data.XSLTLibrarySection;
import com.pa.xpath.data.XPathItem;
import com.pa.xpathutils.NamespaceResolverImpl;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bounce.text.xml.XMLEditorKit;
import org.bounce.text.xml.XMLStyleConstants;
import org.xml.sax.InputSource;

/**
 *
 * @author panderson
 */
public class XMLTool extends JFrame implements ItemListener, ActionListener {

//  private TreeNode rootXMLNode = new DefaultMutableTreeNode();
	private NamespaceResolverImpl nsResImpl;
	private String xpath;
	private String xslt;
	private String fileContents;
	private boolean autoXPath = true;
	private XPathLibrarySection selectedLibrarySection;
	private XSLTLibrarySection selectedXSLTLibrarySection;
	private XPathItem selectedXPath;
	private XSLTItem selectedXSLTItem;
	private static String prettyXSLT;
	private static String cleanNamespaceXSLT;
	private static String startupError;
	private static XMLEditorKit kit = new XMLEditorKit();

	static {
		try {
			prettyXSLT = readFromClasspath("/xslt/prettyPrint.xsl");
			cleanNamespaceXSLT = readFromClasspath("/xslt/cleanNamespaces.xsl");
		} catch (Exception ex) {
			startupError = ex.getMessage();
		}
	}
//  private ListModel<LibrarySection> librarySectionModel = new DefaultListModel<>();

	/**
	 * Creates new form XMLTool
	 */
	public XMLTool() {
		initComponents();

		kit.setStyle(XMLStyleConstants.ELEMENT_NAME, new Color(0, 0, 255), Font.BOLD);
		kit.setStyle(XMLStyleConstants.ELEMENT_VALUE, new Color(0, 0, 0), Font.BOLD);
		kit.setStyle(XMLStyleConstants.ELEMENT_PREFIX, new Color(128, 0, 0), Font.BOLD);

		kit.setStyle(XMLStyleConstants.ATTRIBUTE_NAME, new Color(255, 0, 0), Font.BOLD);
		kit.setStyle(XMLStyleConstants.ATTRIBUTE_VALUE, new Color(0, 0, 0), Font.BOLD);
		kit.setStyle(XMLStyleConstants.ATTRIBUTE_PREFIX, new Color(128, 0, 0), Font.BOLD);

		kit.setStyle(XMLStyleConstants.NAMESPACE_NAME, new Color(102, 102, 102), Font.PLAIN);
		kit.setStyle(XMLStyleConstants.NAMESPACE_VALUE, new Color(0, 51, 51), Font.PLAIN);
		kit.setStyle(XMLStyleConstants.NAMESPACE_PREFIX, new Color(102, 102, 102), Font.PLAIN);

		kit.setStyle(XMLStyleConstants.ENTITY, new Color(0, 0, 0), Font.PLAIN);
		kit.setStyle(XMLStyleConstants.COMMENT, new Color(153, 153, 153), Font.PLAIN);
		kit.setStyle(XMLStyleConstants.CDATA, new Color(0, 0, 0), Font.PLAIN);
		kit.setStyle(XMLStyleConstants.SPECIAL, new Color(0, 0, 0), Font.PLAIN);

		cbRealtimeXML.addItemListener(this);
		cbAutoXPath.addItemListener(this);
		jtreeSourceXML.setModel(new DefaultTreeModel(null));
		lstSections.setModel(new DefaultListModel());
		lstXPaths.setModel(new DefaultListModel());
		lstXSLTLibrary.setModel(new DefaultListModel());
		lstXSLTItems.setModel(new DefaultListModel());
		try {
			buildLibrarySectionsList();
			buildXSLTLibrarySectionsList();
		} catch (Exception x) {
			reportError(x.getMessage(), jeditorXSLT);
		}
		fileContents = jeditorPaneXMLText.getText();
		if (lstSections.getModel().getSize() > 0) {
			lstSections.setSelectedIndex(0);
		}
		if (lstXSLTLibrary.getModel().getSize() > 0) {
			lstXSLTLibrary.setSelectedIndex(0);
		}

		processChangedXML();
		if (startupError != null) {
			reportError("error reading prettyXSLT", txtErrorLog);
		}
//    jtreeSourceXML.setCellRenderer(new TreeCellRenderer());

//    try {
//      jframeSourceXML.setIcon(true);
//      File f = new File("C:\\projects\\Cisco\\development\\pams\\portalapplicationmanagementservice\\src\\test\\resources\\sampleCEPMResponse.xml");
//      readXML(f);
//    } catch (Exception ex) {
//
//    }
	}

	private static String readFromClasspath(String xsltRelativePath)
					throws Exception {
		InputStream is = null;
		try {
			is = Thread.currentThread().getClass().getResourceAsStream(xsltRelativePath);
			List<String> lines = IOUtils.readLines(is);
			StringBuilder sb = new StringBuilder();
			lines.stream().forEach((s) -> {
				sb.append(s);
			});
			return sb.toString();
		} catch (IOException x) {
			throw x;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
//  public List<LibrarySection> getLibSections(){
//    return libSections;
//  }

	private void buildLibrarySectionsList() {
		((DefaultListModel) lstSections.getModel()).removeAllElements();
		List<XPathLibrarySection> sections = DataService.getLibrarySections();
		for (XPathLibrarySection section : sections) {
			((DefaultListModel<XPathLibrarySection>) lstSections.getModel()).addElement(section);
		}
	}

	private void buildXSLTLibrarySectionsList() {
		((DefaultListModel) lstXSLTLibrary.getModel()).removeAllElements();
		List<XSLTLibrarySection> sections = DataService.getXSLTLibrarySections();
		for (XSLTLibrarySection section : sections) {
			((DefaultListModel<XSLTLibrarySection>) lstXSLTLibrary.getModel()).addElement(section);
		}
	}

	private void buildXSLTList() {
		((DefaultListModel) lstXSLTItems.getModel()).removeAllElements();
		if (selectedXSLTLibrarySection == null) {
			return;
		}
		List<XSLTItem> xsltItems = DataService.getXSLTLibraryItems(selectedXSLTLibrarySection.getSectionName());
		for (XSLTItem xsltEntry : xsltItems) {
			((DefaultListModel<XSLTItem>) lstXSLTItems.getModel()).addElement(xsltEntry);
		}
	}

	public String getXpath() {
		return xpath;
	}

	public String getFileContents() {
		return fileContents;
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
	 * Editor.
	 */
	@SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jScrollPane2 = new javax.swing.JScrollPane();
    jEditorPane2 = new javax.swing.JEditorPane();
    jFrameAbout = new javax.swing.JFrame();
    jLabel8 = new javax.swing.JLabel();
    jButton1 = new javax.swing.JButton();
    grpChildrenOrSiblings = new javax.swing.ButtonGroup();
    desktopPane = new javax.swing.JDesktopPane();
    jframeSourceXML = new javax.swing.JInternalFrame();
    jPanel8 = new javax.swing.JPanel();
    jSplitPane1 = new javax.swing.JSplitPane();
    jScrollPane8 = new javax.swing.JScrollPane();
    jtreeSourceXML = new javax.swing.JTree();
    jTabbedPane3 = new javax.swing.JTabbedPane();
    jScrollPane1 = new javax.swing.JScrollPane();
    jeditorPaneXMLText = new javax.swing.JEditorPane();
    jScrollPane11 = new javax.swing.JScrollPane();
    jeditorPaneSelectedNode = new javax.swing.JEditorPane();
    txtSelectedNodeXPath = new javax.swing.JTextField();
    jLabel6 = new javax.swing.JLabel();
    jPanel6 = new javax.swing.JPanel();
    txtTreeSearch = new javax.swing.JTextField();
    btnTreeSearch = new javax.swing.JButton();
    cbFullStringMatch = new javax.swing.JCheckBox();
    radioChildren = new javax.swing.JRadioButton();
    radioSiblings = new javax.swing.JRadioButton();
    btnProcessXMLEdits = new javax.swing.JButton();
    cbRealtimeXML = new javax.swing.JCheckBox();
    btnCleanNamespaces = new javax.swing.JButton();
    cbUseDefaultNameSpaceName = new javax.swing.JCheckBox();
    jframeXPath = new javax.swing.JInternalFrame();
    jTabbedPane1 = new javax.swing.JTabbedPane();
    jSplitPane2 = new javax.swing.JSplitPane();
    jScrollPane4 = new javax.swing.JScrollPane();
    jeditorXPathResults = new javax.swing.JEditorPane();
    jPanel1 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    jScrollPane5 = new javax.swing.JScrollPane();
    jeditorXPath = new javax.swing.JTextArea();
    cbAutoXPath = new javax.swing.JCheckBox();
    btnXPath = new javax.swing.JButton();
    jPanel5 = new javax.swing.JPanel();
    jSplitPane3 = new javax.swing.JSplitPane();
    jScrollPane9 = new javax.swing.JScrollPane();
    jeditorXSLT = new javax.swing.JEditorPane();
    jScrollPane10 = new javax.swing.JScrollPane();
    jeditorTransformedXML = new javax.swing.JEditorPane();
    jLabel10 = new javax.swing.JLabel();
    jLabel11 = new javax.swing.JLabel();
    btnLoadXSLT = new javax.swing.JButton();
    btnInsertNamespaces = new javax.swing.JButton();
    btnFormatXSLT = new javax.swing.JButton();
    jiframeConsole = new javax.swing.JInternalFrame();
    jScrollPane13 = new javax.swing.JScrollPane();
    txtErrorLog = new javax.swing.JEditorPane();
    btnClearConsole = new javax.swing.JButton();
    MainTools = new javax.swing.JInternalFrame();
    jTabbedPane2 = new javax.swing.JTabbedPane();
    jPanel9 = new javax.swing.JPanel();
    jPanelSelectedItemDetails = new javax.swing.JPanel();
    jLabel5 = new javax.swing.JLabel();
    txtSectionName = new javax.swing.JTextField();
    jLabel4 = new javax.swing.JLabel();
    txtXPathName = new javax.swing.JTextField();
    btnSaveEditedXPathItem = new javax.swing.JButton();
    jScrollPane7 = new javax.swing.JScrollPane();
    jeditorPaneSelectedXPath = new javax.swing.JEditorPane();
    btnRevertLibraryName = new javax.swing.JButton();
    btnRevertXPathItemName = new javax.swing.JButton();
    jPanel2 = new javax.swing.JPanel();
    jLabel2 = new javax.swing.JLabel();
    jScrollPane3 = new javax.swing.JScrollPane();
    lstSections = new javax.swing.JList();
    btnDeleteSection = new javax.swing.JButton();
    jPanel3 = new javax.swing.JPanel();
    jLabel3 = new javax.swing.JLabel();
    jScrollPane6 = new javax.swing.JScrollPane();
    lstXPaths = new javax.swing.JList();
    btnDeleteXPathItem = new javax.swing.JButton();
    jPanel11 = new javax.swing.JPanel();
    jPanel4 = new javax.swing.JPanel();
    jLabel7 = new javax.swing.JLabel();
    jScrollPane12 = new javax.swing.JScrollPane();
    lstXSLTLibrary = new javax.swing.JList<>();
    btnDeleteXSLTLibrary = new javax.swing.JButton();
    jPanel7 = new javax.swing.JPanel();
    jLabel9 = new javax.swing.JLabel();
    jScrollPane14 = new javax.swing.JScrollPane();
    lstXSLTItems = new javax.swing.JList<com.pa.xpath.data.XSLTItem>();
    btnDeleteXSLTItem = new javax.swing.JButton();
    jPanel10 = new javax.swing.JPanel();
    jLabel12 = new javax.swing.JLabel();
    txtXSLTSectionName = new javax.swing.JTextField();
    btnRevertXSLTLibraryName = new javax.swing.JButton();
    jLabel13 = new javax.swing.JLabel();
    txtXSLTName = new javax.swing.JTextField();
    btnSaveXSLTEdits = new javax.swing.JButton();
    btnRevertXSLTItemName = new javax.swing.JButton();
    jLabel14 = new javax.swing.JLabel();
    menuBar = new javax.swing.JMenuBar();
    fileMenu = new javax.swing.JMenu();
    openMenuItem = new javax.swing.JMenuItem();
    exitMenuItem = new javax.swing.JMenuItem();
    helpMenu = new javax.swing.JMenu();
    aboutMenuItem = new javax.swing.JMenuItem();

    jScrollPane2.setViewportView(jEditorPane2);

    jFrameAbout.setResizable(false);

    jLabel8.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
    jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel8.setText("by Paul Anderson");

    jButton1.setText("OK");
    jButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton1ActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jFrameAboutLayout = new javax.swing.GroupLayout(jFrameAbout.getContentPane());
    jFrameAbout.getContentPane().setLayout(jFrameAboutLayout);
    jFrameAboutLayout.setHorizontalGroup(
      jFrameAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jFrameAboutLayout.createSequentialGroup()
        .addGroup(jFrameAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jFrameAboutLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(jFrameAboutLayout.createSequentialGroup()
            .addGap(61, 61, 61)
            .addComponent(jButton1)))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    jFrameAboutLayout.setVerticalGroup(
      jFrameAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jFrameAboutLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jLabel8)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jButton1)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

    desktopPane.setLayout(null);

    jframeSourceXML.setIconifiable(true);
    jframeSourceXML.setMaximizable(true);
    jframeSourceXML.setResizable(true);
    jframeSourceXML.setTitle("source XML");
    jframeSourceXML.setVisible(true);

    jSplitPane1.setDividerLocation(400);

    jtreeSourceXML.setFont(new java.awt.Font("Courier New", 1, 14)); // NOI18N
    jtreeSourceXML.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
      public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
        jtreeSourceXMLValueChanged(evt);
      }
    });
    jScrollPane8.setViewportView(jtreeSourceXML);

    jSplitPane1.setRightComponent(jScrollPane8);

    jeditorPaneXMLText.setEditorKit(kit);
    jeditorPaneXMLText.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
    jeditorPaneXMLText.setText("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<about>      \n   <instructions value=\"File...Open to choose file containing XML (file-types are not filtered) Alternatively, paste your XML in the text-pane on the 'full XML' tab of the 'source XML' main-window\"/>      \n   <XPath>           \n      <item value=\"type a valid XPath expression in the XPath tab (see Real-time XPath and XSLT frame).\"/>           \n      <item value=\"have the 'auto?' box in the checked state to see XPath results as you type.\"/>           \n      <item value=\"Sometimes it's not helpful to have 'auto?' turned on because with each error due to partial  XPath-expressions you lose sight of any results thus far.\"/>           \n      <library>                \n         <item value=\"Often-used XPath expressions can be saved in the library - create a new library just by typing in a new name (not already used), name the expression, type the expression in the text-area below the 'save...' button, then click the 'save...' button iteself.\"/>                \n         <item value=\"To apply a library XPath expression to the XML in the 'full XML' window, just click on the library, then on the expression name. XPath results show in the XPath tab of the 'Real-time XPath and XSLT' frame.\"/>                \n         <item value=\"To see the XPath of a node, click on it in the XML-tree; to see the source-XML for the selected-node, view the 'selected node' tab.\"/>           \n      </library>      \n   </XPath>      \n   <XSLT>           \n      <item value=\"XSLT processes in real-time - operates on the XML that is in the text-pane of the 'full XML' tab. Either type in or paste XSLT into the XSLT pane, or load XSLT from a file. If any XSLT expressions use namespaces (other than 'xsl'), those namespaces need to be declared in the XSLT to prevent errors. To discover and insert the namespaces, click an appropriate location in the XSLT 'xsl:stylesheet' element to locate the cursor there, then click 'insert namespaces' button.\"/>           \n      <item value=\"'format' cleans up the XSLT format, but will fail if the XSLT is not well-formed.\"/>      \n   </XSLT> \n</about>");
    jeditorPaneXMLText.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyReleased(java.awt.event.KeyEvent evt) {
        jeditorPaneXMLTextKeyReleased(evt);
      }
    });
    jScrollPane1.setViewportView(jeditorPaneXMLText);

    jTabbedPane3.addTab("all", jScrollPane1);

    jeditorPaneSelectedNode.setEditorKit(new XMLEditorKit());
    jeditorPaneSelectedNode.setEditable(false);
    jeditorPaneSelectedNode.setEditorKit(kit);
    jeditorPaneSelectedNode.setFont(new java.awt.Font("Courier New", 0, 13)); // NOI18N
    jScrollPane11.setViewportView(jeditorPaneSelectedNode);

    jTabbedPane3.addTab("selected", jScrollPane11);

    jSplitPane1.setLeftComponent(jTabbedPane3);

    txtSelectedNodeXPath.setEditable(false);
    txtSelectedNodeXPath.setBackground(new java.awt.Color(238, 238, 238));
    txtSelectedNodeXPath.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N

    jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel6.setText("XPath");

    jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    txtTreeSearch.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N

    btnTreeSearch.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
    btnTreeSearch.setText("search");
    btnTreeSearch.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnTreeSearchActionPerformed(evt);
      }
    });

    cbFullStringMatch.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
    cbFullStringMatch.setText("whole string?");

    grpChildrenOrSiblings.add(radioChildren);
    radioChildren.setSelected(true);
    radioChildren.setText("children?");

    grpChildrenOrSiblings.add(radioSiblings);
    radioSiblings.setText("siblings?");

    javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
    jPanel6.setLayout(jPanel6Layout);
    jPanel6Layout.setHorizontalGroup(
      jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel6Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(txtTreeSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btnTreeSearch)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(cbFullStringMatch)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(radioChildren)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(radioSiblings)
        .addContainerGap())
    );
    jPanel6Layout.setVerticalGroup(
      jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel6Layout.createSequentialGroup()
        .addGap(6, 6, 6)
        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtTreeSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(btnTreeSearch)
          .addComponent(cbFullStringMatch)
          .addComponent(radioChildren)
          .addComponent(radioSiblings))
        .addGap(6, 6, 6))
    );

    btnProcessXMLEdits.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
    btnProcessXMLEdits.setText("process changed XML");
    btnProcessXMLEdits.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnProcessXMLEditsActionPerformed(evt);
      }
    });

    cbRealtimeXML.setText("real-time XML processing");

    btnCleanNamespaces.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
    btnCleanNamespaces.setText("clean namespaces");
    btnCleanNamespaces.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseReleased(java.awt.event.MouseEvent evt) {
        btnCleanNamespacesMouseReleased(evt);
      }
    });

    cbUseDefaultNameSpaceName.setText("use default NS name in XPath?");

    javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
    jPanel8.setLayout(jPanel8Layout);
    jPanel8Layout.setHorizontalGroup(
      jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel8Layout.createSequentialGroup()
        .addGap(3, 3, 3)
        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(jPanel8Layout.createSequentialGroup()
            .addComponent(jLabel6)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(txtSelectedNodeXPath))
          .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(jPanel8Layout.createSequentialGroup()
            .addComponent(btnProcessXMLEdits)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btnCleanNamespaces)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(cbRealtimeXML)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(cbUseDefaultNameSpaceName)
            .addGap(0, 0, Short.MAX_VALUE)))
        .addGap(3, 3, 3))
    );
    jPanel8Layout.setVerticalGroup(
      jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel8Layout.createSequentialGroup()
        .addGap(3, 3, 3)
        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtSelectedNodeXPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel6))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(5, 5, 5)
        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(btnProcessXMLEdits)
          .addComponent(cbRealtimeXML)
          .addComponent(btnCleanNamespaces)
          .addComponent(cbUseDefaultNameSpaceName))
        .addGap(6, 6, 6)
        .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
        .addGap(3, 3, 3))
    );

    javax.swing.GroupLayout jframeSourceXMLLayout = new javax.swing.GroupLayout(jframeSourceXML.getContentPane());
    jframeSourceXML.getContentPane().setLayout(jframeSourceXMLLayout);
    jframeSourceXMLLayout.setHorizontalGroup(
      jframeSourceXMLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jframeSourceXMLLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addContainerGap())
    );
    jframeSourceXMLLayout.setVerticalGroup(
      jframeSourceXMLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jframeSourceXMLLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addContainerGap())
    );

    desktopPane.add(jframeSourceXML);
    jframeSourceXML.setBounds(0, 0, 950, 580);

    jframeXPath.setIconifiable(true);
    jframeXPath.setMaximizable(true);
    jframeXPath.setResizable(true);
    jframeXPath.setTitle("Real-time XPath and XSLT");
    jframeXPath.setVisible(true);

    jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

    jeditorXPathResults.setEditable(false);
    jeditorXPathResults.setEditorKit(kit);
    jeditorXPathResults.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
    jScrollPane4.setViewportView(jeditorXPathResults);

    jSplitPane2.setBottomComponent(jScrollPane4);

    jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel1.setText("XPath");

    jeditorXPath.setColumns(20);
    jeditorXPath.setFont(new java.awt.Font("Courier New", 1, 14)); // NOI18N
    jeditorXPath.setLineWrap(true);
    jeditorXPath.setRows(5);
    jeditorXPath.setText("/");
    jeditorXPath.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyReleased(java.awt.event.KeyEvent evt) {
        jeditorXPathKeyReleased(evt);
      }
    });
    jScrollPane5.setViewportView(jeditorXPath);

    cbAutoXPath.setSelected(true);
    cbAutoXPath.setText("auto?");
    cbAutoXPath.setToolTipText("check this to get real-time XPath2.0 results"); // NOI18N
    cbAutoXPath.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cbAutoXPathActionPerformed(evt);
      }
    });

    btnXPath.setText("XPath");
    btnXPath.setEnabled(false);
    btnXPath.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnXPathActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(cbAutoXPath)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(8, 8, 8)
            .addComponent(jLabel1))
          .addComponent(btnXPath))
        .addGap(10, 10, 10)
        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
        .addContainerGap())
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addGap(22, 22, 22)
        .addComponent(jLabel1)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(cbAutoXPath)
        .addGap(11, 11, 11)
        .addComponent(btnXPath)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jScrollPane5))
    );

    jSplitPane2.setLeftComponent(jPanel1);

    jTabbedPane1.addTab("XPath", jSplitPane2);

    jSplitPane3.setDividerLocation(300);

    jeditorXSLT.setEditorKit(kit);
    jeditorXSLT.setFont(new java.awt.Font("Courier New", 0, 13)); // NOI18N
    jeditorXSLT.setText("<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n<xsl:output method=\"xml\" indent=\"yes\"/>\n\n<xsl:template match=\"/\">\n  <xsl:copy-of select=\".\"/>\n</xsl:template>\n\n</xsl:stylesheet>");
    jeditorXSLT.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyReleased(java.awt.event.KeyEvent evt) {
        jeditorXSLTKeyReleased(evt);
      }
    });
    jScrollPane9.setViewportView(jeditorXSLT);

    jSplitPane3.setLeftComponent(jScrollPane9);

    jeditorTransformedXML.setEditable(false);
    jeditorTransformedXML.setEditorKit(kit);
    jeditorTransformedXML.setFont(new java.awt.Font("Courier New", 0, 13)); // NOI18N
    jScrollPane10.setViewportView(jeditorTransformedXML);

    jSplitPane3.setRightComponent(jScrollPane10);

    jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
    jLabel10.setText("XSLT");

    jLabel11.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
    jLabel11.setText("Transformed XML   ");

    btnLoadXSLT.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
    btnLoadXSLT.setText("load XSLT file");
    btnLoadXSLT.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnLoadXSLTActionPerformed(evt);
      }
    });

    btnInsertNamespaces.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
    btnInsertNamespaces.setText("insert namespaces");
    btnInsertNamespaces.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnInsertNamespacesActionPerformed(evt);
      }
    });

    btnFormatXSLT.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
    btnFormatXSLT.setText("format");
    btnFormatXSLT.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnFormatXSLTActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
    jPanel5.setLayout(jPanel5Layout);
    jPanel5Layout.setHorizontalGroup(
      jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel5Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jLabel10)
        .addGap(18, 18, 18)
        .addComponent(btnLoadXSLT, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(18, 18, 18)
        .addComponent(btnFormatXSLT, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btnInsertNamespaces, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 257, Short.MAX_VALUE)
        .addComponent(jLabel11)
        .addContainerGap())
      .addComponent(jSplitPane3, javax.swing.GroupLayout.Alignment.TRAILING)
    );
    jPanel5Layout.setVerticalGroup(
      jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
        .addGap(6, 6, 6)
        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(btnLoadXSLT)
          .addComponent(jLabel10)
          .addComponent(jLabel11)
          .addComponent(btnInsertNamespaces)
          .addComponent(btnFormatXSLT))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jSplitPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
        .addContainerGap())
    );

    jTabbedPane1.addTab("XSLT", jPanel5);

    javax.swing.GroupLayout jframeXPathLayout = new javax.swing.GroupLayout(jframeXPath.getContentPane());
    jframeXPath.getContentPane().setLayout(jframeXPathLayout);
    jframeXPathLayout.setHorizontalGroup(
      jframeXPathLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jframeXPathLayout.createSequentialGroup()
        .addComponent(jTabbedPane1)
        .addContainerGap())
    );
    jframeXPathLayout.setVerticalGroup(
      jframeXPathLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jframeXPathLayout.createSequentialGroup()
        .addComponent(jTabbedPane1)
        .addContainerGap())
    );

    desktopPane.add(jframeXPath);
    jframeXPath.setBounds(760, 0, 800, 480);

    jiframeConsole.setIconifiable(true);
    jiframeConsole.setMaximizable(true);
    jiframeConsole.setResizable(true);
    jiframeConsole.setTitle("console");
    jiframeConsole.setVisible(true);

    txtErrorLog.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
    txtErrorLog.setForeground(new java.awt.Color(255, 51, 51));
    jScrollPane13.setViewportView(txtErrorLog);

    btnClearConsole.setText("clear");
    btnClearConsole.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnClearConsoleActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jiframeConsoleLayout = new javax.swing.GroupLayout(jiframeConsole.getContentPane());
    jiframeConsole.getContentPane().setLayout(jiframeConsoleLayout);
    jiframeConsoleLayout.setHorizontalGroup(
      jiframeConsoleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 736, Short.MAX_VALUE)
      .addGroup(jiframeConsoleLayout.createSequentialGroup()
        .addGap(6, 6, 6)
        .addComponent(btnClearConsole)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    jiframeConsoleLayout.setVerticalGroup(
      jiframeConsoleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jiframeConsoleLayout.createSequentialGroup()
        .addGap(6, 6, 6)
        .addComponent(btnClearConsole)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
        .addContainerGap())
    );

    desktopPane.add(jiframeConsole);
    jiframeConsole.setBounds(0, 630, 760, 150);

    MainTools.setVisible(true);

    jPanelSelectedItemDetails.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
    jLabel5.setText("library name");

    txtSectionName.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyReleased(java.awt.event.KeyEvent evt) {
        txtSectionNameKeyReleased(evt);
      }
    });

    jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
    jLabel4.setText("expression  name");

    txtXPathName.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyReleased(java.awt.event.KeyEvent evt) {
        txtXPathNameKeyReleased(evt);
      }
    });

    btnSaveEditedXPathItem.setText("save edits");
    btnSaveEditedXPathItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnSaveEditedXPathItemActionPerformed(evt);
      }
    });

    jeditorPaneSelectedXPath.setFont(new java.awt.Font("Courier New", 0, 13)); // NOI18N
    jScrollPane7.setViewportView(jeditorPaneSelectedXPath);

    btnRevertLibraryName.setText("revert");
    btnRevertLibraryName.setEnabled(false);
    btnRevertLibraryName.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnRevertLibraryNameActionPerformed(evt);
      }
    });

    btnRevertXPathItemName.setText("revert");
    btnRevertXPathItemName.setEnabled(false);
    btnRevertXPathItemName.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnRevertXPathItemNameActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanelSelectedItemDetailsLayout = new javax.swing.GroupLayout(jPanelSelectedItemDetails);
    jPanelSelectedItemDetails.setLayout(jPanelSelectedItemDetailsLayout);
    jPanelSelectedItemDetailsLayout.setHorizontalGroup(
      jPanelSelectedItemDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanelSelectedItemDetailsLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanelSelectedItemDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
          .addGroup(jPanelSelectedItemDetailsLayout.createSequentialGroup()
            .addComponent(btnSaveEditedXPathItem)
            .addGap(0, 0, Short.MAX_VALUE))
          .addGroup(jPanelSelectedItemDetailsLayout.createSequentialGroup()
            .addGroup(jPanelSelectedItemDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(jPanelSelectedItemDetailsLayout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtXPathName))
              .addGroup(jPanelSelectedItemDetailsLayout.createSequentialGroup()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtSectionName)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanelSelectedItemDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(btnRevertLibraryName, javax.swing.GroupLayout.Alignment.TRAILING)
              .addComponent(btnRevertXPathItemName, javax.swing.GroupLayout.Alignment.TRAILING))))
        .addContainerGap())
    );
    jPanelSelectedItemDetailsLayout.setVerticalGroup(
      jPanelSelectedItemDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanelSelectedItemDetailsLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanelSelectedItemDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtSectionName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel5)
          .addComponent(btnRevertLibraryName))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanelSelectedItemDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtXPathName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel4)
          .addComponent(btnRevertXPathItemName))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btnSaveEditedXPathItem)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane7)
        .addContainerGap())
    );

    jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
    jLabel2.setText("library name");

    lstSections.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
      public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
        lstSectionsValueChanged(evt);
      }
    });
    jScrollPane3.setViewportView(lstSections);

    btnDeleteSection.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
    btnDeleteSection.setText("delete selected");
    btnDeleteSection.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnDeleteSectionActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
            .addGap(28, 28, 28)
            .addComponent(jLabel2)
            .addGap(0, 0, Short.MAX_VALUE))
          .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
              .addComponent(btnDeleteSection, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))))
        .addContainerGap())
    );
    jPanel2Layout.setVerticalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addGap(6, 6, 6)
        .addComponent(jLabel2)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btnDeleteSection)
        .addContainerGap())
    );

    jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
    jLabel3.setText("  xpath expressions in library");

    lstXPaths.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    lstXPaths.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
      public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
        lstXPathsValueChanged(evt);
      }
    });
    jScrollPane6.setViewportView(lstXPaths);

    btnDeleteXPathItem.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
    btnDeleteXPathItem.setText("delete selected");
    btnDeleteXPathItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnDeleteXPathItemActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
          .addGroup(jPanel3Layout.createSequentialGroup()
            .addComponent(jLabel3)
            .addGap(0, 27, Short.MAX_VALUE))
          .addComponent(btnDeleteXPathItem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap())
    );
    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addGap(6, 6, 6)
        .addComponent(jLabel3)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btnDeleteXPathItem)
        .addContainerGap())
    );

    javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
    jPanel9.setLayout(jPanel9Layout);
    jPanel9Layout.setHorizontalGroup(
      jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel9Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jPanelSelectedItemDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addContainerGap())
    );
    jPanel9Layout.setVerticalGroup(
      jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel9Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanelSelectedItemDetails, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
    );

    jTabbedPane2.addTab("XPath", jPanel9);

    jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    jLabel7.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
    jLabel7.setText("  library name");

    lstXSLTLibrary.setModel(new DefaultListModel<XSLTLibrarySection>());
    lstXSLTLibrary.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
      public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
        lstXSLTLibraryValueChanged(evt);
      }
    });
    jScrollPane12.setViewportView(lstXSLTLibrary);

    btnDeleteXSLTLibrary.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
    btnDeleteXSLTLibrary.setText("delete selected");
    btnDeleteXSLTLibrary.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnDeleteXSLTLibraryActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
    jPanel4.setLayout(jPanel4Layout);
    jPanel4Layout.setHorizontalGroup(
      jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel4Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel4Layout.createSequentialGroup()
            .addComponent(jLabel7)
            .addGap(139, 139, 139))
          .addGroup(jPanel4Layout.createSequentialGroup()
            .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
            .addContainerGap())
          .addComponent(btnDeleteXSLTLibrary, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
    );
    jPanel4Layout.setVerticalGroup(
      jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel4Layout.createSequentialGroup()
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(jLabel7)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btnDeleteXSLTLibrary)
        .addGap(6, 6, 6))
    );

    jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    jLabel9.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
    jLabel9.setText("  XSLT in library");

    lstXSLTItems.setModel(new DefaultListModel<com.pa.xpath.data.XSLTItem>());
    lstXSLTItems.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
      public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
        lstXSLTItemsValueChanged(evt);
      }
    });
    jScrollPane14.setViewportView(lstXSLTItems);

    btnDeleteXSLTItem.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
    btnDeleteXSLTItem.setText("delete selected");
    btnDeleteXSLTItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnDeleteXSLTItemActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
    jPanel7.setLayout(jPanel7Layout);
    jPanel7Layout.setHorizontalGroup(
      jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel7Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
          .addGroup(jPanel7Layout.createSequentialGroup()
            .addComponent(jLabel9)
            .addGap(0, 0, Short.MAX_VALUE))
          .addComponent(btnDeleteXSLTItem, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE))
        .addContainerGap())
    );
    jPanel7Layout.setVerticalGroup(
      jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel7Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jLabel9)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btnDeleteXSLTItem)
        .addGap(6, 6, 6))
    );

    jPanel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    jLabel12.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
    jLabel12.setText("library name");

    txtXSLTSectionName.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyReleased(java.awt.event.KeyEvent evt) {
        txtXSLTSectionNameKeyReleased(evt);
      }
    });

    btnRevertXSLTLibraryName.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
    btnRevertXSLTLibraryName.setText("revert");
    btnRevertXSLTLibraryName.setEnabled(false);

    jLabel13.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
    jLabel13.setText("XSLT name");

    txtXSLTName.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyReleased(java.awt.event.KeyEvent evt) {
        txtXSLTNameKeyReleased(evt);
      }
    });

    btnSaveXSLTEdits.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
    btnSaveXSLTEdits.setText("save edits");
    btnSaveXSLTEdits.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnSaveXSLTEditsActionPerformed(evt);
      }
    });

    btnRevertXSLTItemName.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
    btnRevertXSLTItemName.setText("revert");
    btnRevertXSLTItemName.setEnabled(false);

    jLabel14.setText("use XSLT window  to view and edit XSLT");

    javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
    jPanel10.setLayout(jPanel10Layout);
    jPanel10Layout.setHorizontalGroup(
      jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel10Layout.createSequentialGroup()
        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel10Layout.createSequentialGroup()
            .addGap(21, 21, 21)
            .addComponent(jLabel14))
          .addGroup(jPanel10Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(btnSaveXSLTEdits)
              .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                  .addGroup(jPanel10Layout.createSequentialGroup()
                    .addComponent(jLabel12)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(txtXSLTSectionName, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                  .addGroup(jPanel10Layout.createSequentialGroup()
                    .addComponent(jLabel13)
                    .addGap(18, 18, 18)
                    .addComponent(txtXSLTName)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(btnRevertXSLTItemName, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(btnRevertXSLTLibraryName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))))))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    jPanel10Layout.setVerticalGroup(
      jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel10Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel12)
          .addComponent(txtXSLTSectionName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(btnRevertXSLTLibraryName))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel13)
          .addComponent(txtXSLTName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(btnRevertXSLTItemName))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btnSaveXSLTEdits)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel14)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
    jPanel11.setLayout(jPanel11Layout);
    jPanel11Layout.setHorizontalGroup(
      jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel11Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );
    jPanel11Layout.setVerticalGroup(
      jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel11Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
            .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap())
    );

    jTabbedPane2.addTab("XSLT", jPanel11);

    javax.swing.GroupLayout MainToolsLayout = new javax.swing.GroupLayout(MainTools.getContentPane());
    MainTools.getContentPane().setLayout(MainToolsLayout);
    MainToolsLayout.setHorizontalGroup(
      MainToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(MainToolsLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jTabbedPane2)
        .addContainerGap())
    );
    MainToolsLayout.setVerticalGroup(
      MainToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(MainToolsLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jTabbedPane2))
    );

    desktopPane.add(MainTools);
    MainTools.setBounds(750, 490, 910, 360);

    fileMenu.setMnemonic('f');
    fileMenu.setText("File");

    openMenuItem.setMnemonic('o');
    openMenuItem.setText("Open XML");
    openMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        openMenuItemMouseClicked(evt);
      }
    });
    openMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        openMenuItemActionPerformed(evt);
      }
    });
    fileMenu.add(openMenuItem);

    exitMenuItem.setMnemonic('x');
    exitMenuItem.setText("Exit");
    exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exitMenuItemActionPerformed(evt);
      }
    });
    fileMenu.add(exitMenuItem);

    menuBar.add(fileMenu);

    helpMenu.setMnemonic('h');
    helpMenu.setText("Help");

    aboutMenuItem.setMnemonic('a');
    aboutMenuItem.setText("About");
    aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        aboutMenuItemActionPerformed(evt);
      }
    });
    helpMenu.add(aboutMenuItem);

    menuBar.add(helpMenu);

    setJMenuBar(menuBar);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(desktopPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1602, Short.MAX_VALUE)
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(desktopPane, javax.swing.GroupLayout.DEFAULT_SIZE, 954, Short.MAX_VALUE)
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void jeditorXPathKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jeditorXPathKeyReleased
		if (!autoXPath) {
			return;
		}
		doXPath();
		jeditorXPathResults.setCaretPosition(0);
  }//GEN-LAST:event_jeditorXPathKeyReleased

	private void doXPath() {
		xpath = jeditorXPath.getText();
		if (xpath == null || xpath.trim().equals("")) {
			return;
		}
		doXPath(jeditorXPathResults, jeditorXPath.getText());
//    String xpresults = XMLHelper.getXPathResults(nsResImpl, xpath, fileContents);
//    jeditorXPathResults.setText(xpresults);
		jeditorXPathResults.setCaretPosition(0);
	}

	private void qik(Map<String, Object> arg) {
		arg.entrySet().stream().forEach((entry) -> {
			entry.setValue("" + entry.getValue());
		});
	}

	private void doXPath(JEditorPane pane, String xpath) {

		String xpresults = XMLHelper.getXPathResults(nsResImpl, xpath, fileContents);
		try {
			pane.setText(XSLTHelper.transformXML(xpresults, prettyXSLT));
			pane.setCaretPosition(0);
		} catch (Exception ex) {
			pane.setText(xpresults);
			pane.setCaretPosition(0);
			reportError(ex.getMessage(), pane);
		}
	}


  private void cbAutoXPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAutoXPathActionPerformed
		autoXPath = cbAutoXPath.isSelected();
  }//GEN-LAST:event_cbAutoXPathActionPerformed

  private void btnXPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXPathActionPerformed
		doXPath();
  }//GEN-LAST:event_btnXPathActionPerformed

  private void jeditorPaneXMLTextKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jeditorPaneXMLTextKeyReleased
		fileContents = jeditorPaneXMLText.getText();
		if (cbRealtimeXML.isSelected()) {
			processChangedXML();
		}
  }//GEN-LAST:event_jeditorPaneXMLTextKeyReleased

	private void prettyXML() {
		try {
			String prettyXML = XSLTHelper.transformXML(jeditorPaneXMLText.getText(), prettyXSLT);
			jeditorPaneXMLText.setText(prettyXML);
		} catch (Exception ex) {
			String message = String.format("XML is invalid - it is failing prettyprint XSLT with messatge '%s'", ex.getMessage());
			reportError(message, jeditorPaneXMLText);
		}
	}

	private void reportError(String message, JEditorPane alternateDest) {
		txtErrorLog.setText(message);
		try {
			jiframeConsole.setIcon(false);
		} catch (PropertyVetoException ex1) {
			alternateDest.setText(message);
		}
	}

	private void clearSelectedDataNotSection() {
//    txtSectionName.setText("");
		txtXPathName.setText("");
		jeditorXPath.setText("");
//    this.selectedLibrarySection = null;
		this.selectedXPath = null;
		jeditorPaneSelectedXPath.setText("");
		jeditorXPathResults.setText("");
	}

	private void setSectionNameRevertButtonStatus() {
		if (!txtSectionName.getText().equals("") && selectedLibrarySection == null) {
			btnRevertLibraryName.setEnabled(true);
			return;
		}
		if (!txtSectionName.getText().equals("") && selectedLibrarySection == null) {
			btnRevertLibraryName.setEnabled(false);
			return;
		}
		btnRevertLibraryName.setEnabled(!txtSectionName.getText().equals(selectedLibrarySection.getSectionName()));
	}

	private void setXSLTSectionNameRevertButtonStatus() {
		if (!txtXSLTSectionName.getText().equals("") && selectedXSLTLibrarySection == null) {
			btnRevertXSLTLibraryName.setEnabled(true);
			return;
		}
		if (!txtXSLTSectionName.getText().equals("") && selectedXSLTLibrarySection == null) {
			btnRevertXSLTLibraryName.setEnabled(false);
			return;
		}
		btnRevertXSLTLibraryName.setEnabled(!txtXSLTSectionName.getText().equals(selectedXSLTLibrarySection.getSectionName()));
	}

	private void setXPathNameRevertButtonStatus() {
		if (txtSectionName == null || selectedXPath == null) {
			btnRevertXPathItemName.setEnabled(false);
			return;
		}
		btnRevertXPathItemName.setEnabled(!txtXPathName.getText().equals(selectedXPath.getXpathLibraryPK().getItemName()));
	}

	private void setXSLTNameRevertButtonStatus() {
		if (txtXSLTSectionName == null || selectedXSLTItem == null) {
			btnRevertXSLTItemName.setEnabled(false);
			return;
		}
		btnRevertXSLTItemName.setEnabled(!txtXSLTName.getText().equals(selectedXSLTItem.getXSLTLibraryPK().getItemName()));
	}

  private void jtreeSourceXMLValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jtreeSourceXMLValueChanged
		clearErrorLog();
		TreePath path = evt.getPath();
		String xpathCalc = buildXPath(path);
		txtSelectedNodeXPath.setText(xpathCalc);
		doXPath(jeditorPaneSelectedNode, txtSelectedNodeXPath.getText());

  }//GEN-LAST:event_jtreeSourceXMLValueChanged

  private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
		jFrameAbout.setSize(173, 95);
		jFrameAbout.setVisible(true);
  }//GEN-LAST:event_aboutMenuItemActionPerformed

  private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		jFrameAbout.setVisible(false);
  }//GEN-LAST:event_jButton1ActionPerformed

  private void jeditorXSLTKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jeditorXSLTKeyReleased
		txtErrorLog.setText("");
		doXSLT();
  }//GEN-LAST:event_jeditorXSLTKeyReleased

	private void doXSLT() {
		if (jeditorXSLT.getText() == null || jeditorXSLT.getText().trim().equals("")) {
			return;
		}
		try {
			String transformed = XSLTHelper.transformXML(fileContents, jeditorXSLT.getText());
			jeditorTransformedXML.setText(transformed);
			jeditorTransformedXML.setCaretPosition(0);
		} catch (Throwable t) {
			reportError(t.getMessage(), jeditorTransformedXML);
		}
	}

  private void btnLoadXSLTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadXSLTActionPerformed
		JFileChooser chooser = new JFileChooser(new File("/"));
		chooser.showOpenDialog(this);
		File xlstFile = chooser.getSelectedFile();
		readXSLT(xlstFile);
  }//GEN-LAST:event_btnLoadXSLTActionPerformed

  private void btnInsertNamespacesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInsertNamespacesActionPerformed
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : nsResImpl.getNamespacePairs().entrySet()) {
			sb.append(String.format(" xmlns:%s=\"%s\" ", entry.getKey(), entry.getValue()));
		}
		sb.append(" ");
		try {
			jeditorXSLT.getDocument().insertString(jeditorXSLT.getCaretPosition(), sb.toString(), null);
		} catch (BadLocationException x) {
			reportError(x.getMessage(), jeditorXSLT);
		}
  }//GEN-LAST:event_btnInsertNamespacesActionPerformed

	private void searchTree() {
		TreeModel treeModel = jtreeSourceXML.getModel();
		Object start = jtreeSourceXML.getSelectionPath();
		if (start == null) {
			start = (DefaultMutableTreeNode) treeModel.getRoot();
		} else {
			start = (DefaultMutableTreeNode) jtreeSourceXML.getSelectionPath().getLastPathComponent();
		}

		DefaultMutableTreeNode startTreeNode = (DefaultMutableTreeNode) start;

		boolean fullStringSearch = cbFullStringMatch.isSelected();

		DefaultMutableTreeNode foundItem = null;

		if (radioChildren.isSelected()) {
			foundItem = searchChildren(fullStringSearch, treeModel, (DefaultMutableTreeNode) start, txtTreeSearch.getText().toLowerCase());
		} else {

//      DefaultMutableTreeNode foundNode = searchChildren(fullStringSearch, treeModel, startTreeNode, txtTreeSearch.getText().toLowerCase());
			foundItem = searchChildren(fullStringSearch, treeModel, startTreeNode, txtTreeSearch.getText().toLowerCase());

			DefaultMutableTreeNode thisSibling = startTreeNode;
			int nSiblingsThisNode = startTreeNode.getSiblingCount();

			if (foundItem == null) {
				for (int k = 0; k < nSiblingsThisNode; k++) {
					thisSibling = (DefaultMutableTreeNode) thisSibling.getNextSibling();
					foundItem = searchChildren(fullStringSearch, treeModel, thisSibling, txtTreeSearch.getText().toLowerCase());
					if (foundItem != null) {
						break;
					}
				}
			}
		}

		if (foundItem != null) {
			jtreeSourceXML.setExpandsSelectedPaths(true);
			TreeNode[] nodePath = foundItem.getPath();
			TreePath path = new TreePath(foundItem.getPath());
			jtreeSourceXML.setSelectionPath(path);
			jtreeSourceXML.scrollPathToVisible(path);
		}
	}

  private void btnTreeSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTreeSearchActionPerformed
		searchTree();
  }//GEN-LAST:event_btnTreeSearchActionPerformed

  private void btnProcessXMLEditsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProcessXMLEditsActionPerformed
		clearErrorLog();
		processChangedXML();
  }//GEN-LAST:event_btnProcessXMLEditsActionPerformed

  private void btnDeleteXPathItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteXPathItemActionPerformed
		XPathItem xpathLibraryItem = (XPathItem) lstXPaths.getSelectedValue();
		if (xpathLibraryItem == null) {
			return;
		}
		DataService.deleteXPathItem(xpathLibraryItem.getXpathLibraryPK().getLibrarySection(), xpathLibraryItem.getXpathLibraryPK().getItemName());
		buildXPathList();
		lstXPaths.clearSelection();
		jeditorPaneSelectedXPath.setText("");
		txtXPathName.setText("");
  }//GEN-LAST:event_btnDeleteXPathItemActionPerformed

  private void lstXPathsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstXPathsValueChanged
		selectedXPath = (XPathItem) lstXPaths.getSelectedValue();
		if (selectedXPath == null) {
			return;
		}
		xpath = selectedXPath.getXpath();
		txtXPathName.setText(selectedXPath.getXpathLibraryPK().getItemName());
		jeditorPaneSelectedXPath.setText(xpath);
		jeditorXPath.setText(xpath);
		if (fileContents == null || fileContents.trim().equals("")) {
			return;
		}
		doXPath();
		buildSaveButtonText();
  }//GEN-LAST:event_lstXPathsValueChanged

  private void btnDeleteSectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteSectionActionPerformed
		XPathLibrarySection section = (XPathLibrarySection) lstSections.getSelectedValue();
		DataService.deleteXPathLibrarySection(section.getSectionName());
		try {
			clearSelectedDataNotSection();
			txtSectionName.setText("");
			buildLibrarySectionsList();
			((DefaultListModel<XPathItem>) lstXPaths.getModel()).removeAllElements();
		} catch (Throwable x) {
			reportError(x.getMessage(), txtErrorLog);
		}
  }//GEN-LAST:event_btnDeleteSectionActionPerformed

  private void lstSectionsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstSectionsValueChanged
		selectedLibrarySection = (XPathLibrarySection) lstSections.getSelectedValue();
		if (selectedLibrarySection == null) {
			return;
		}
		txtSectionName.setText(selectedLibrarySection.getSectionName());
		buildXPathList();
		clearSelectedDataNotSection();
		buildSaveButtonText();
  }//GEN-LAST:event_lstSectionsValueChanged

  private void btnRevertXPathItemNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRevertXPathItemNameActionPerformed
		txtXPathName.setText(selectedXPath.getXpathLibraryPK().getItemName());
		btnRevertXPathItemName.setEnabled(false);
		setSaveButtonText();
  }//GEN-LAST:event_btnRevertXPathItemNameActionPerformed

  private void btnRevertLibraryNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRevertLibraryNameActionPerformed
		if (selectedLibrarySection == null) {
			btnRevertLibraryName.setEnabled(false);
			txtSectionName.setText("");
			txtSectionName.setCaretPosition(0);
			return;
		}
		txtSectionName.setText(selectedLibrarySection.getSectionName());
		btnRevertLibraryName.setEnabled(false);
		setSaveButtonText();
  }//GEN-LAST:event_btnRevertLibraryNameActionPerformed

  private void btnSaveEditedXPathItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveEditedXPathItemActionPerformed
		String sectionName = txtSectionName.getText();
		boolean sectionExists = sectionExistsInModel(sectionName);
		if (sectionExists) {
			if (!xpathNameExistsInSection(txtXPathName.getText())) {
				DataService.createXPathEntry(sectionName, txtXPathName.getText(), jeditorPaneSelectedXPath.getText());
				buildXPathList();
			} else if (xpathHasChanged()) {
				DataService.updateXPathItem(sectionName, txtXPathName.getText(), jeditorPaneSelectedXPath.getText());
			}
		} else {
			XPathLibrarySection section = new XPathLibrarySection(sectionName);
			XPathItem xpLibrary;
			String xpathName = txtXPathName.getText();
			if (!xpathName.trim().equals("")) {
				xpLibrary = DataService.createXPathEntryLazy(sectionName, xpathName, jeditorPaneSelectedXPath.getText());
				buildXPathList();
				lstXPaths.setSelectedValue(xpLibrary, true);
			} else {
				DataService.createXSLTLibrarySection(sectionName);
			}
			buildLibrarySectionsList();
			lstSections.setSelectedValue(section, true);
		}
		setSectionNameRevertButtonStatus();
		setXPathNameRevertButtonStatus();
		setSaveButtonText();
  }//GEN-LAST:event_btnSaveEditedXPathItemActionPerformed

  private void txtXPathNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtXPathNameKeyReleased
		setXPathNameRevertButtonStatus();
		setSaveButtonText();
  }//GEN-LAST:event_txtXPathNameKeyReleased

  private void txtSectionNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSectionNameKeyReleased
		setSectionNameRevertButtonStatus();
		setSaveButtonText();
  }//GEN-LAST:event_txtSectionNameKeyReleased

  private void btnFormatXSLTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFormatXSLTActionPerformed
		try {
			jeditorXSLT.setText(XSLTHelper.transformXML(jeditorXSLT.getText(), prettyXSLT));
		} catch (Exception ex) {
			reportError(ex.getMessage(), jeditorXSLT);
		}
  }//GEN-LAST:event_btnFormatXSLTActionPerformed

  private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
		System.exit(0);
  }//GEN-LAST:event_exitMenuItemActionPerformed

  private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
		if (evt.getSource() == openMenuItem) {
			String filePath = Preferences.userRoot().get("xpFilePath", null);

			if (filePath == null) {
				filePath = "/";
			}
			JFileChooser chooser = new JFileChooser(new File(filePath));
			chooser.showOpenDialog(this);

			File xmlFile = chooser.getSelectedFile();
			if (xmlFile != null) {
				Preferences.userRoot().put("xpFilePath", xmlFile.getAbsolutePath());
				readXML(xmlFile);
			}
		}
  }//GEN-LAST:event_openMenuItemActionPerformed

  private void openMenuItemMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openMenuItemMouseClicked

  }//GEN-LAST:event_openMenuItemMouseClicked

  private void btnClearConsoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearConsoleActionPerformed
		clearErrorLog();
  }//GEN-LAST:event_btnClearConsoleActionPerformed

	private void clearErrorLog() {
		txtErrorLog.setText("");
	}

  private void btnCleanNamespacesMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCleanNamespacesMouseReleased
		try {
			String cleaned = XSLTHelper.transformXML(jeditorPaneXMLText.getText(), cleanNamespaceXSLT);
			cleaned = XSLTHelper.transformXML(cleaned, prettyXSLT);
			jeditorPaneXMLText.setText(cleaned);
		} catch (Exception ex) {
			Logger.getLogger(XMLTool.class.getName()).log(Level.SEVERE, null, ex);
		}
  }//GEN-LAST:event_btnCleanNamespacesMouseReleased

  private void lstXSLTLibraryValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstXSLTLibraryValueChanged
		selectedXSLTLibrarySection = (XSLTLibrarySection) lstXSLTLibrary.getSelectedValue();
		if (selectedXSLTLibrarySection == null) {
			return;
		}
		txtXSLTSectionName.setText(selectedXSLTLibrarySection.getSectionName());
		buildXSLTList();
		clearSelectedDataNotSection();
		buildSaveButtonText();
  }//GEN-LAST:event_lstXSLTLibraryValueChanged

  private void lstXSLTItemsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstXSLTItemsValueChanged
		if (lstXSLTItems.getSelectedValue() == null) {
			return;
		}
		jeditorXSLT.setText(lstXSLTItems.getSelectedValue().getXSLT());

		selectedXSLTItem = lstXSLTItems.getSelectedValue();
		if (selectedXSLTItem == null) {
			return;
		}
		xslt = selectedXSLTItem.getXSLT();
		txtXSLTName.setText(selectedXSLTItem.getXSLTLibraryPK().getItemName());
		jeditorXSLT.setText(xslt);
		jeditorXSLT.setCaretPosition(0);
//    if (fileContents == null || fileContents.trim().equals("")) {
//      return;
//    }
		doXSLT();
		jeditorTransformedXML.setCaretPosition(0);
		buildSaveButtonText();
		doXSLT();
  }//GEN-LAST:event_lstXSLTItemsValueChanged

  private void btnSaveXSLTEditsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveXSLTEditsActionPerformed

		String sectionName = txtSectionName.getText();
		boolean sectionExists = sectionExistsInModel(sectionName);
		if (sectionExists) {
			if (!xpathNameExistsInSection(txtXPathName.getText())) {
				DataService.createXPathEntry(sectionName, txtXPathName.getText(), jeditorPaneSelectedXPath.getText());
				buildXPathList();
			} else if (xpathHasChanged()) {
				DataService.updateXPathItem(sectionName, txtXPathName.getText(), jeditorPaneSelectedXPath.getText());
			}
		} else {
			XPathLibrarySection section = new XPathLibrarySection(sectionName);
			XPathItem xpLibrary;
			String xpathName = txtXPathName.getText();
			if (!xpathName.trim().equals("")) {
				xpLibrary = DataService.createXPathEntryLazy(sectionName, xpathName, jeditorPaneSelectedXPath.getText());
				buildXPathList();
				lstXPaths.setSelectedValue(xpLibrary, true);
			} else {
				DataService.createXSLTLibrarySection(sectionName);
			}
			buildLibrarySectionsList();
			lstSections.setSelectedValue(section, true);
		}
		setSectionNameRevertButtonStatus();
		setXPathNameRevertButtonStatus();
		setSaveButtonText();

		String xsltSectionName = txtXSLTSectionName.getText();
		boolean xsltSectionExists = xsltSectionExistsInModel(xsltSectionName);
		if (xsltSectionExists) {
			if (!xsltNameExistsInSection(txtXSLTName.getText())) {
				DataService.createXSLTEntry(xsltSectionName, txtXSLTName.getText(), jeditorXSLT.getText());
				buildXSLTList();
			}
			DataService.updateXSLTItem(xsltSectionName, txtXSLTName.getText(), jeditorXSLT.getText());

		} else {
			XSLTLibrarySection section = new XSLTLibrarySection(xsltSectionName);
			XSLTItem xsltItem;
			String xsltName = txtXSLTName.getText();

			if (!xsltName.trim().equals("")) {
				xsltItem = DataService.createXSLTEntryLazy(xsltSectionName, xsltName, jeditorXSLT.getText());

				lstXSLTItems.setSelectedValue(xsltItem, true);
				buildXSLTList();
			} else {
				DataService.createXSLTLibrarySection(xsltSectionName);
			}

			buildXSLTLibrarySectionsList();
			lstXSLTLibrary.setSelectedValue(section, true);
		}
		setXSLTSectionNameRevertButtonStatus();
		setXSLTNameRevertButtonStatus();
		setXSLTSaveButtonText();
  }//GEN-LAST:event_btnSaveXSLTEditsActionPerformed

  private void txtXSLTSectionNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtXSLTSectionNameKeyReleased
		setXSLTSectionNameRevertButtonStatus();
		setXSLTSaveButtonText();
  }//GEN-LAST:event_txtXSLTSectionNameKeyReleased

  private void txtXSLTNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtXSLTNameKeyReleased
		setXSLTNameRevertButtonStatus();
		setXSLTSaveButtonText();    // TODO add your handling code here:
  }//GEN-LAST:event_txtXSLTNameKeyReleased

  private void btnDeleteXSLTLibraryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteXSLTLibraryActionPerformed
		DataService.deleteXSLTLibrarySection(lstXSLTLibrary.getSelectedValue().getSectionName());
		buildXSLTLibrarySectionsList();
		buildXSLTList();
  }//GEN-LAST:event_btnDeleteXSLTLibraryActionPerformed

  private void btnDeleteXSLTItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteXSLTItemActionPerformed
		DataService.deleteXSLTItem(lstXSLTItems.getSelectedValue().getXSLTLibraryPK().getXsltLibrarySection(), lstXSLTItems.getSelectedValue().getXSLTLibraryPK().getItemName());
		buildXSLTList();
  }//GEN-LAST:event_btnDeleteXSLTItemActionPerformed

	private DefaultMutableTreeNode searchChildren(boolean fullStringSearch, TreeModel treeModel, DefaultMutableTreeNode parent, String searchString) {

		int nChildren = treeModel.getChildCount(parent);
		if (nChildren == 0) {
			TreeNodeObject thisNode = (TreeNodeObject) parent.getUserObject();
			if (fullStringSearch) {
				if (thisNode.getName().equalsIgnoreCase(searchString) || thisNode.getValue().equalsIgnoreCase(searchString)) {
					return parent;
				}
			} else if (thisNode.getName().toLowerCase().contains(searchString) || thisNode.getValue().toLowerCase().contains(searchString)) {
				return parent;
			}
		}
		for (int i = 0; i < nChildren; i++) {
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) treeModel.getChild(parent, i);
			TreeNodeObject nodeObj = (TreeNodeObject) treeNode.getUserObject();

			switch (nodeObj.getNodeKind()) {

				case ELEMENT:
					if (((ElementObject) nodeObj).getName().toLowerCase().contains(searchString)) {
						return treeNode;
					}
					int childCountHere = treeModel.getChildCount(treeNode);
					if (childCountHere > 0) {

						for (int j = 0; j < childCountHere; j++) {
							DefaultMutableTreeNode thisFind = searchChildren(fullStringSearch, treeModel, (DefaultMutableTreeNode) treeModel.getChild(treeNode, j), searchString);
							if (thisFind != null) {
								return thisFind;
							}
						}

//            return searchChildren(treeModel, treeNode, searchString);
					}
					break;

				case ATTRIBUTE:
					AttributeObject attrObj = (AttributeObject) nodeObj;
					if (fullStringSearch) {
						if (attrObj.getName().equalsIgnoreCase(searchString) || attrObj.getValue().equalsIgnoreCase(searchString)) {
							return treeNode;
						}
					} else if (attrObj.getName().toLowerCase().contains(searchString) || attrObj.getValue().toLowerCase().contains(searchString)) {
						return treeNode;
					}
					break;
				case TEXT:
					TextNodeObject txtObj = (TextNodeObject) nodeObj;
					if (fullStringSearch) {
						if (txtObj.getValue().equalsIgnoreCase(searchString)) {
							return treeNode;
						}
					} else if (txtObj.getValue().toLowerCase().contains(searchString)) {
						return treeNode;
					}
					break;
			}
		}
		return null;
	}

	private String buildXPath(TreePath path) {
		Object[] objects = path.getPath();
		int nObjects = objects.length;

		DefaultMutableTreeNode root = (DefaultMutableTreeNode) objects[0];

		boolean useExplicitDefaultNamespace = this.nsResImpl.getNamespacePairs().size() > 1 && cbUseDefaultNameSpaceName.isSelected();
		String rootName = ((TreeNodeObject) root.getUserObject()).buildPath(useExplicitDefaultNamespace, 1);
		StringBuilder sb = new StringBuilder(String.format("/%s", rootName));

		for (int i = 1; i < nObjects; i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) objects[i];
			String thisName = ((TreeNodeObject) node.getUserObject()).getName();
			TreeNode parent = node.getParent();

			int nChildren = parent.getChildCount();
			int counter = 0;
			for (int c = 0; c < nChildren; c++) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) parent.getChildAt(c);
				TreeNodeObject userObjectInChild = (TreeNodeObject) ((DefaultMutableTreeNode) childNode).getUserObject();
				if (userObjectInChild.getNodeKind().equals(TreeNodeObject.E_NODE_KIND.ELEMENT) && thisName.equals(((TreeNodeObject) childNode.getUserObject()).getName())) {
					counter++;
				}
				if (node.equals(parent.getChildAt(c))) {
					break;
				}
			}

			sb.append("/").append(((TreeNodeObject) node.getUserObject()).buildPath(useExplicitDefaultNamespace, counter));
		}
		return sb.toString();
	}

	private void buildXPathList() {
		((DefaultListModel) lstXPaths.getModel()).removeAllElements();
		if (selectedLibrarySection == null) {
			return;
		}
		List<XPathItem> xpaths = DataService.getXpathLibraryItems(selectedLibrarySection.getSectionName());
		for (XPathItem xpathEntry : xpaths) {
			((DefaultListModel<XPathItem>) lstXPaths.getModel()).addElement(xpathEntry);
		}
	}

	private void setSaveButtonText() {
		String btnText = buildSaveButtonText();
		btnSaveEditedXPathItem.setText(btnText);
	}

	private void setXSLTSaveButtonText() {
		String btnText = buildXSLTSaveButtonText();
		btnSaveXSLTEdits.setText(btnText);
	}

	private String buildSaveButtonText() {
		String btnText;
		if (btnRevertLibraryName.isEnabled() && btnRevertXPathItemName.isEnabled()) {
			return String.format("save new library  '%s' with new xpath-item '%s'", txtSectionName.getText(), txtXPathName.getText());
		}
		if (btnRevertLibraryName.isEnabled()) {
			if (txtXPathName.getText().equals("")) {
				return String.format("save new empty library '%s'", txtSectionName.getText());
			} else {
				return String.format("save new library  '%s' xpath-item '%s'", txtSectionName.getText(), txtXPathName.getText());
			}
		}
		if (btnRevertXPathItemName.isEnabled()) {
			return String.format("save new xpath-item '%s'", txtXPathName.getText());
		}
		return "save edited xpath";
	}

	private String buildXSLTSaveButtonText() {
		if (btnRevertXSLTLibraryName.isEnabled() && btnRevertXSLTItemName.isEnabled()) {
			return String.format("save new library  '%s' with new xslt-item '%s'", txtXSLTSectionName.getText(), txtXSLTName.getText());
		}
		if (btnRevertXSLTLibraryName.isEnabled()) {
			if (txtXSLTName.getText().equals("")) {
				return String.format("save new empty library '%s'", txtXSLTSectionName.getText());
			} else {
				return String.format("save new library  '%s' xslt-item '%s'", txtXSLTSectionName.getText(), txtXSLTName.getText());
			}
		}
		if (btnRevertXSLTItemName.isEnabled()) {
			return String.format("save new xslt-item '%s'", txtXSLTName.getText());
		}
		return "save edited XSLT";
	}

	private boolean sectionExistsInModel(String sectioName) {
		Enumeration<XPathLibrarySection> sections = ((DefaultListModel<XPathLibrarySection>) lstSections.getModel()).elements();
		DefaultListModel x;

		while (sections.hasMoreElements()) {
			XPathLibrarySection section = sections.nextElement();
			if (section.getSectionName().equals(sectioName)) {
				return true;
			}
		}
		return false;
	}

	private boolean xsltSectionExistsInModel(String sectioName) {
		Enumeration<XSLTLibrarySection> sections = ((DefaultListModel<XSLTLibrarySection>) lstXSLTLibrary.getModel()).elements();

		while (sections.hasMoreElements()) {
			XSLTLibrarySection section = sections.nextElement();
			if (section.getSectionName().equals(sectioName)) {
				return true;
			}
		}
		return false;
	}

	private boolean xpathNameExistsInSection(String xpathName) {
		Enumeration<XPathItem> xpathItems = ((DefaultListModel<XPathItem>) lstXPaths.getModel()).elements();
		DefaultListModel x;

		while (xpathItems.hasMoreElements()) {
			XPathItem item = xpathItems.nextElement();
			if (item.getXpathLibraryPK().getItemName().equals(xpathName)) {
				return true;
			}
		}
		return false;
	}

	private boolean xsltNameExistsInSection(String xsltName) {
		Enumeration<XSLTItem> xsltItems = ((DefaultListModel<XSLTItem>) lstXSLTItems.getModel()).elements();
		DefaultListModel x;

		while (xsltItems.hasMoreElements()) {
			XSLTItem item = xsltItems.nextElement();
			if (item.getXSLTLibraryPK().getItemName().equals(xsltName)) {
				return true;
			}
		}
		return false;
	}

	private boolean xpathHasChanged() {
		boolean res = jeditorPaneSelectedXPath.getText().equals(jeditorXPath.getText());
		return !res;
	}

	private void readXML(File file) {
		if (file == null) {
			return;
		}
		try {
			fileContents = FileUtils.readFileToString(file);
			jeditorPaneXMLText.setCaretPosition(0);
			this.jeditorPaneXMLText.setText(fileContents);
		} catch (IOException ex) {
			reportError(ex.getMessage(), jeditorPaneXMLText);
		}
		processChangedXML();
	}

	private void readXSLT(File file) {
		try {
			String xslt = FileUtils.readFileToString(file);
			try {
				xslt = XSLTHelper.transformXML(xslt, prettyXSLT);
			} catch (Exception ex) {
				reportError(String.format("error reading file %s: %s", file.getAbsoluteFile(), ex.getMessage()), jeditorXSLT);
			}
			this.jeditorXSLT.setText(xslt);
		} catch (IOException ex) {
			reportError(ex.getMessage(), jeditorXSLT);
		}
	}

	private void processChangedXML() {
		try {
			nsResImpl = new NamespaceResolverImpl(fileContents);
			buildTree(fileContents);
			jeditorXPathResults.setText("");
			prettyXML();
			doXPath();
			doXSLT();
			jeditorPaneXMLText.setCaretPosition(0);
		} catch (XPathExpressionException x) {
			reportError(x.getMessage(), txtErrorLog);
		}
	}

	private void buildTree(String fileContents) {
		SAXHandler h = new SAXHandler();
		InputSource is = new InputSource(new StringReader(fileContents));
		try {
			SAXParserFactory.newInstance().newSAXParser().parse(is, h);
		} catch (Exception ex) {
			reportError(ex.getMessage(), txtErrorLog);
		}
		jtreeSourceXML.setModel(h.getTreeModel());
		TreePath p = new TreePath(jtreeSourceXML.getModel().getRoot());
		jtreeSourceXML.scrollRowToVisible(0);
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
     * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Windows".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(XMLTool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		//</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new XMLTool().setVisible(true);
			}
		});
	}

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JInternalFrame MainTools;
  private javax.swing.JMenuItem aboutMenuItem;
  private javax.swing.JButton btnCleanNamespaces;
  private javax.swing.JButton btnClearConsole;
  private javax.swing.JButton btnDeleteSection;
  private javax.swing.JButton btnDeleteXPathItem;
  private javax.swing.JButton btnDeleteXSLTItem;
  private javax.swing.JButton btnDeleteXSLTLibrary;
  private javax.swing.JButton btnFormatXSLT;
  private javax.swing.JButton btnInsertNamespaces;
  private javax.swing.JButton btnLoadXSLT;
  private javax.swing.JButton btnProcessXMLEdits;
  private javax.swing.JButton btnRevertLibraryName;
  private javax.swing.JButton btnRevertXPathItemName;
  private javax.swing.JButton btnRevertXSLTItemName;
  private javax.swing.JButton btnRevertXSLTLibraryName;
  private javax.swing.JButton btnSaveEditedXPathItem;
  private javax.swing.JButton btnSaveXSLTEdits;
  private javax.swing.JButton btnTreeSearch;
  private javax.swing.JButton btnXPath;
  private javax.swing.JCheckBox cbAutoXPath;
  private javax.swing.JCheckBox cbFullStringMatch;
  private javax.swing.JCheckBox cbRealtimeXML;
  private javax.swing.JCheckBox cbUseDefaultNameSpaceName;
  private javax.swing.JDesktopPane desktopPane;
  private javax.swing.JMenuItem exitMenuItem;
  private javax.swing.JMenu fileMenu;
  private javax.swing.ButtonGroup grpChildrenOrSiblings;
  private javax.swing.JMenu helpMenu;
  private javax.swing.JButton jButton1;
  private javax.swing.JEditorPane jEditorPane2;
  private javax.swing.JFrame jFrameAbout;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel10;
  private javax.swing.JLabel jLabel11;
  private javax.swing.JLabel jLabel12;
  private javax.swing.JLabel jLabel13;
  private javax.swing.JLabel jLabel14;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JLabel jLabel7;
  private javax.swing.JLabel jLabel8;
  private javax.swing.JLabel jLabel9;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel10;
  private javax.swing.JPanel jPanel11;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JPanel jPanel5;
  private javax.swing.JPanel jPanel6;
  private javax.swing.JPanel jPanel7;
  private javax.swing.JPanel jPanel8;
  private javax.swing.JPanel jPanel9;
  private javax.swing.JPanel jPanelSelectedItemDetails;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane10;
  private javax.swing.JScrollPane jScrollPane11;
  private javax.swing.JScrollPane jScrollPane12;
  private javax.swing.JScrollPane jScrollPane13;
  private javax.swing.JScrollPane jScrollPane14;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JScrollPane jScrollPane4;
  private javax.swing.JScrollPane jScrollPane5;
  private javax.swing.JScrollPane jScrollPane6;
  private javax.swing.JScrollPane jScrollPane7;
  private javax.swing.JScrollPane jScrollPane8;
  private javax.swing.JScrollPane jScrollPane9;
  private javax.swing.JSplitPane jSplitPane1;
  private javax.swing.JSplitPane jSplitPane2;
  private javax.swing.JSplitPane jSplitPane3;
  private javax.swing.JTabbedPane jTabbedPane1;
  private javax.swing.JTabbedPane jTabbedPane2;
  private javax.swing.JTabbedPane jTabbedPane3;
  private javax.swing.JEditorPane jeditorPaneSelectedNode;
  private javax.swing.JEditorPane jeditorPaneSelectedXPath;
  private javax.swing.JEditorPane jeditorPaneXMLText;
  private javax.swing.JEditorPane jeditorTransformedXML;
  private javax.swing.JTextArea jeditorXPath;
  private javax.swing.JEditorPane jeditorXPathResults;
  private javax.swing.JEditorPane jeditorXSLT;
  private javax.swing.JInternalFrame jframeSourceXML;
  private javax.swing.JInternalFrame jframeXPath;
  private javax.swing.JInternalFrame jiframeConsole;
  private javax.swing.JTree jtreeSourceXML;
  private javax.swing.JList lstSections;
  private javax.swing.JList lstXPaths;
  private javax.swing.JList<com.pa.xpath.data.XSLTItem> lstXSLTItems;
  private javax.swing.JList<XSLTLibrarySection> lstXSLTLibrary;
  private javax.swing.JMenuBar menuBar;
  private javax.swing.JMenuItem openMenuItem;
  private javax.swing.JRadioButton radioChildren;
  private javax.swing.JRadioButton radioSiblings;
  private javax.swing.JEditorPane txtErrorLog;
  private javax.swing.JTextField txtSectionName;
  private javax.swing.JTextField txtSelectedNodeXPath;
  private javax.swing.JTextField txtTreeSearch;
  private javax.swing.JTextField txtXPathName;
  private javax.swing.JTextField txtXSLTName;
  private javax.swing.JTextField txtXSLTSectionName;
  // End of variables declaration//GEN-END:variables

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getSource() instanceof JCheckBox) {

			if (e.getSource() == cbRealtimeXML) {
				btnProcessXMLEdits.setEnabled(!((JCheckBox) e.getSource()).isSelected());
			}
			if (e.getSource() == cbAutoXPath) {
				btnXPath.setEnabled(!((JCheckBox) e.getSource()).isSelected());
			}

		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == txtTreeSearch) {
			searchTree();
		}
	}

}
