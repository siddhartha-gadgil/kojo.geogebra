/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

package geogebra.gui.toolbar;
import geogebra.main.Application;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Toolbar configuration panel.
 *  
 * @author Markus Hohenwarter, based on a dialog from geonext.de
 *
 */
public class ToolbarConfigPanel extends javax.swing.JPanel implements java.awt.event.ActionListener, javax.swing.event.TreeExpansionListener {	
	
	private static final int SCROLL_PANEL_WIDTH = 300;
	private static final int SCROLL_PANEL_HEIGHT = 400;
	
	public JButton insertButton;
	public JButton moveUpButton;
	public JButton moveDownButton;
	public JButton deleteButton;
	public JTree tree;
	JScrollPane configScrollPane;
	JScrollPane modeScrollPane;
	JScrollPane iconScrollPane;
	JPanel selectionPanel;
	JList toolList;	
	int selectedRow;	
	Application app;	
	
	public ToolbarConfigPanel(Application app) {
		super();	
		this.app = app;			
		
		selectionPanel = new JPanel();
		selectionPanel.setLayout(new BorderLayout(5, 5));
		selectionPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(5, 5));
				
		tree = generateTree();
		setToolBarString(app.getGuiManager().getToolBarDefinition());	
		
		configScrollPane = new JScrollPane(tree);
		configScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		configScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		//configScrollPane.setSize(150, 150);		
		JPanel scrollSpacePanel = new JPanel();
		scrollSpacePanel.setLayout(new BorderLayout(0, 0));
		scrollSpacePanel.setBorder(new EmptyBorder(3, 5, 3, 5));
		scrollSpacePanel.add(configScrollPane, BorderLayout.CENTER); //
		JPanel scrollPanel = new JPanel();
		scrollPanel.setLayout(new BorderLayout(0, 0));
		scrollPanel.setBorder(new TitledBorder(app.getMenu("Toolbar")));
		scrollPanel.add(scrollSpacePanel, BorderLayout.CENTER);
				
		scrollPanel.setPreferredSize(new Dimension(SCROLL_PANEL_WIDTH, SCROLL_PANEL_HEIGHT));
		//
		selectionPanel.add(scrollPanel, BorderLayout.WEST);
		//
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(Box.createVerticalGlue());
		
		insertButton = new JButton("< " + app.getPlain("Insert"));		
		insertButton.addActionListener(this);
		insertButton.setAlignmentX(CENTER_ALIGNMENT);
		buttonPanel.add(insertButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		
		deleteButton = new javax.swing.JButton(app.getPlain("Remove") + " >");		
		deleteButton.addActionListener(this);
		deleteButton.setAlignmentX(CENTER_ALIGNMENT);
		buttonPanel.add(deleteButton);		
		
		buttonPanel.add(Box.createVerticalGlue());
		selectionPanel.add(buttonPanel, BorderLayout.CENTER);
		
		
		
		//		
		JPanel upDownPanel = new JPanel();
		moveUpButton = new javax.swing.JButton("\u25b2 " + app.getPlain("Up"));	
		moveUpButton.addActionListener(this);
		upDownPanel.add(moveUpButton);
		//
		moveDownButton = new javax.swing.JButton("\u25bc " + app.getPlain("Down"));		
		moveDownButton.addActionListener(this);
		upDownPanel.add(moveDownButton);
		
		scrollPanel.add(upDownPanel, BorderLayout.SOUTH);

		//

		//
		JPanel modePanel = new JPanel();
		modePanel.setLayout(new BorderLayout(0, 0));
		modePanel.setBorder(new TitledBorder(app.getMenu("Tools")));
		//modePanel.setBorder(new TitledBorder(new EmptyBorder(0, 0, 0, 0), " " + "W�hlbare Eintr�ge" + " "));
		//
		Vector modeVector = generateToolsVector();
		toolList = new JList(modeVector);		
		//modeList.setPreferredSize(new Dimension(150, 150));
		//
		//modeList.setSize(new Dimension(150, 150));
		//modeList.addListSelectionListener(this);
		//
		ListSelectionModel lsm = toolList.getSelectionModel();
		lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		toolList.setBackground(configScrollPane.getBackground());
		modeScrollPane = new JScrollPane(toolList);		
		modeScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		modeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		toolList.setCellRenderer(new ModeCellRenderer(app));
		toolList.setSelectedIndex(0);
		//
		//
		JPanel modeSpacePanel = new JPanel();
		modeSpacePanel.setLayout(new BorderLayout(0, 0));
		modeSpacePanel.setBorder(new EmptyBorder(3, 5, 3, 5));
		modeSpacePanel.add("Center", modeScrollPane);
		//
		//modeSpacePanel.setPreferredSize(new Dimension(175, 175));
		//
		//modeSpacePanel.setSize(new Dimension(175, 175));
		//
		modePanel.add("Center", modeSpacePanel);
		modePanel.setPreferredSize(new Dimension(SCROLL_PANEL_WIDTH, SCROLL_PANEL_HEIGHT));
		selectionPanel.add("East", modePanel);		
		add("Center", selectionPanel);
		//
		/*JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		okButton = new javax.swing.JButton("OK");
		okButton.setName("ok");
		okButton.addActionListener(this);
		controlPanel.add(okButton);
		cancelButton = new javax.swing.JButton("Abbrechen");
		cancelButton.setName("cancel");
		cancelButton.addActionListener(this);
		controlPanel.add(cancelButton);
		controlPanel.doLayout();*/
		//add(controlPanel, BorderLayout.SOUTH);
		//
		//doLayout();
		//this.validateTree();
		//modeScrollPane.setPreferredSize(new Dimension(Math.max(modeScrollPane.getWidth(), configScrollPane.getWidth()), (int) configScrollPane.getSize().getHeight()));
		//modeScrollPane.setSize(modeList.getPreferredSize());
		//modeScrollPane.setSize(modeScrollPane.getPreferredSize());
		//configScrollPane.setPreferredSize(new Dimension(Math.max(modeScrollPane.getWidth(), configScrollPane.getWidth()), (int) configScrollPane.getSize().getHeight()));
		
		
		try {
			tree.setSelectionRow(1);
		} catch (Exception exc) {
			tree.setSelectionRow(0);
		}				
	}
		
	
	
	
	/**
	 * Handles remove, add and up, down buttons.
	 */
	public void actionPerformed(ActionEvent e) {					
		// get selected node in tree
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();							
		TreePath selPath = tree.getSelectionPath();
		if (selPath == null) {			
		    tree.setSelectionRow(0); // take root if nothing is selected
		    selPath = tree.getSelectionPath();
		} 
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) selPath.getLastPathComponent();
		// remember row number
		int selRow = tree.getRowForPath(selPath);	
		
		DefaultMutableTreeNode parentNode;		
		if (selNode == root) { // root is selected
			parentNode = selNode;
		} else {
			parentNode = (DefaultMutableTreeNode) selNode.getParent();							
		}	
		int childIndex = parentNode.getIndex(selNode);
		
		Object src = e.getSource();	
		
		// DELETE
		if (src == deleteButton) {									
			if (selRow > 0) { // not root					
				// delete node unless move node				
				Object userOb = selNode.getUserObject();
				if (userOb == null)
					userOb = ((DefaultMutableTreeNode) selNode.getFirstChild()).getUserObject();				
				if (userOb instanceof Integer && ((Integer) userOb).intValue() == 0) 
					return;
				
				// not move mode: delete node
				model.removeNodeFromParent(selNode);				
				// remove empty menu too	
				if (parentNode.getChildCount() == 0) {
					if (!parentNode.isRoot()) { 					
						model.removeNodeFromParent(parentNode);
						selRow--;
					}
				}
				
				// select node at same row or above
				if (selRow >= tree.getRowCount())
					selRow--;
				tree.setSelectionRow(selRow);
			}						
		} 
		// INSERT
		else if (src == insertButton) {		
			childIndex++;
			
			boolean didInsert = false;
			Object [] tools = toolList.getSelectedValues();						
			for (int i=0; i < tools.length; i++) {
				// check if too is already there
				Integer modeInt = (Integer)tools[i];
				if (modeInt.intValue() > -1 && containsTool(root, (Integer)tools[i]))
					continue;
				
				DefaultMutableTreeNode newNode;
				if (parentNode == root && modeInt.intValue() > -1) {
					// parent is root: create new submenu
					newNode = new DefaultMutableTreeNode();			
					newNode.add(new DefaultMutableTreeNode(modeInt));
				}
				else {
					// parent is submenu
					newNode = new DefaultMutableTreeNode(modeInt);						
				}											
				model.insertNodeInto(newNode, parentNode, childIndex++);				
				didInsert = true;				
			}
			
			if (didInsert) {
				// make sure that root is expanded
				tree.expandPath(new TreePath(model.getRoot()));
				
				// select first inserted node
				tree.setSelectionRow(++selRow);
				tree.scrollRowToVisible(selRow);
				configScrollPane.getHorizontalScrollBar().setValue(0); // scroll to left
			}
		}
		
		// UP
		else if (src == moveUpButton) {
			if (selNode == root)
				return;
						
			if (parentNode.getChildBefore(selNode) != null) {							
				model.removeNodeFromParent(selNode);
				model.insertNodeInto(selNode, parentNode, --childIndex);
				tree.setSelectionRow(--selRow);
			}			
		}
		
		// DOWN
		else if (src == moveDownButton) {
			if (selNode == root)
				return;
						
			if (parentNode.getChildAfter(selNode) != null) {							
				model.removeNodeFromParent(selNode);
				model.insertNodeInto(selNode, parentNode, ++childIndex);
				tree.setSelectionRow(++selRow);
			}			
		}
	}	
	
	private boolean containsTool(DefaultMutableTreeNode node, Integer mode) {
        // compare modes
		Object ob = node.getUserObject();
        if (ob != null && mode.compareTo((Integer)ob) == 0) {           	
        	return true;
        }
    
        if (node.getChildCount() >= 0) {
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
            	DefaultMutableTreeNode n = (DefaultMutableTreeNode)e.nextElement();
            	if (containsTool(n, mode))
            		return true;
            }
        }
        return false;
    }
	
	/**
	 * Inits the toolbar tree in this panel to show the given toolbar definition string.
	 */
	public void setToolBarString(String toolbarDefinition) {				
		// create new tree model
		Vector toolVec = MyToolbar.createToolBarVec(toolbarDefinition);
		DefaultTreeModel model = new DefaultTreeModel(generateRootNode(toolVec));
		tree.setModel(model);		
		collapseAllRows();	
		tree.setRowHeight(-1);				
	}
	
	/**
	 * Returns the custom toolbar created with this panel as a String.
	 * Separator ("||" between menus, "," in menu), New menu starts with "|"
	 */
	public String getToolBarString() {								
		StringBuffer sb = new StringBuffer();
		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();		            
        for (int i=0; i < root.getChildCount(); i++) {
        	DefaultMutableTreeNode menu = (DefaultMutableTreeNode) root.getChildAt(i);
        	
        	if (menu.getChildCount() == 0) { // new menu with separator
        		sb.append("|| ");
        	} 
        	else if (i > 0 && !sb.toString().endsWith("|| ")) // new menu
        		sb.append("| ");
        	
        	for (int j=0; j < menu.getChildCount(); j++) {
            	DefaultMutableTreeNode node = (DefaultMutableTreeNode) menu.getChildAt(j);
            	int mode = ((Integer) node.getUserObject()).intValue();
            	            	
            	if (mode < 0) // separator
            		sb.append(", ");
            	else { // mode number            		
            		sb.append(mode);
            		sb.append(" ");
            	}            	
            }        	        	
        }
                
        return sb.toString().trim();    
	}		
	
	public void collapseAllRows() {
		int z = tree.getRowCount();
		for (int i = z; i > 0; i--) {
			tree.collapseRow(i);
		}
	}
	
		
	/**
	 * 
	 */
	public Vector generateToolsVector() {				
		Vector vector = new Vector();		
		// separator
		vector.add(MyToolbar.TOOLBAR_SEPARATOR);
				
		// get default toolbar as nested vectors
		Vector defTools = MyToolbar.createToolBarVec(app.getGuiManager().getDefaultToolbarString());				
		for (int i=0; i < defTools.size(); i++) {
			Object element = defTools.get(i);
			
			if (element instanceof Vector) {
				Vector menu = (Vector) element;
				for (int j=0; j < menu.size(); j++) {
					Integer modeInt = (Integer) menu.get(j);
					int mode = modeInt.intValue();
					if (mode != -1)
						vector.add(modeInt);
				}
			} else {
				Integer modeInt = (Integer) element;
				int mode = modeInt.intValue();
				if (mode != -1)
					vector.add(modeInt);
			}			
		}				
		return vector;
	}
	/**
	 *
	 */
	private JTree generateTree() {			
		final JTree jTree = new JTree() {
	        protected void setExpandedState(TreePath path, boolean state) {
	            // Ignore all collapse requests of root        	
	            if (path != getPathForRow(0)) {
	                super.setExpandedState(path, state);
	            }
	        }
	    };	    	    	   
		
		jTree.setCellRenderer(new ModeCellRenderer(app));
		jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		jTree.putClientProperty("JTree.lineStyle", "Angled");
		jTree.addTreeExpansionListener(this);						
		
		return jTree;
	}
	/**
	 * 
	 */
	public DefaultMutableTreeNode generateRootNode(Vector toolbarModes) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode();
		
		for (int i = 0; i < toolbarModes.size(); i++) {
			Object ob = toolbarModes.get(i);
			if (ob instanceof Vector) {
				Vector menu = (Vector) ob;  
				DefaultMutableTreeNode sub = new DefaultMutableTreeNode();
				for (int j = 0; j < menu.size(); j++) {
					sub.add(new DefaultMutableTreeNode(menu.get(j)));
				}
				node.add(sub);
			}
			else
				node.add(new DefaultMutableTreeNode(ob));
		}
		return node;
	}
		
	/**
	 * 
	 */
	public void treeCollapsed(javax.swing.event.TreeExpansionEvent event) {}
	/**
	 * 
	 */
	public void treeExpanded(javax.swing.event.TreeExpansionEvent event) {
		/*tabbed.invalidate();
		tabbed.validateTree();*/
	}
	
	/**
	 * 
	 */
	public void valueChanged(javax.swing.event.ListSelectionEvent e) {}
	
	
}