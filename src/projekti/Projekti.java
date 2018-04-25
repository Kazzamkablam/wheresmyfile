package projekti;

import java.awt.Dimension;
import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;

import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;  
import javax.swing.JFileChooser;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.event.ListSelectionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

final class varDefinitions {
	
	public static String[] listChoices = {"Everything", "My Images", "My Documents", "My Videos"}; //choices for drop down, if I wanted to go fancy I could make these user created.
	public static int itemEverything = 0;
	public static int itemImages = 1;
	public static int itemDocuments = 2;
	public static int itemVideos = 3;
}

class dataBaseObj { //database object for file information
	public Integer itemID; //for database compatibility
	public String fileData;
	public String pathData;
	public Integer itemType;
	
	dataBaseObj(Integer gID, String file, String path, Integer item) { //init new database object
		this.itemID = gID;
		this.fileData = file;
		this.pathData = path;
		this.itemType = item;		
	}
	
	public String toString() {  //simple tostring function
	       String val = fileData;
			return val;
	}
	
	public int getType() { //get item type			
		return this.itemType;
	}
		

}
class fileDataBase { //actual database

	private static ArrayList<dataBaseObj> dataObj = new ArrayList<dataBaseObj>();

	
	public static  Object[] filesData() {	  // puts shit to array		
		return dataObj.toArray();
	}
	
	public static boolean addFile(int gID, String name, String path, int index) { //add an entry

		dataObj.add(new dataBaseObj(gID, path,name,index));
		return true;
	}

	public static boolean updateFile(int gID, int itemindex, String name, String path, int index) { //update entry
		dataBaseObj Obj = new dataBaseObj(gID, name, path, index);		
		dataObj.set(itemindex, Obj);
		return true;
	}
	
	public static dataBaseObj getFile(int index) { //get an entry
		dataBaseObj Obj;
		Obj = dataObj.get(index);
		return Obj;
	}
	
	public static int getSize() {  //get size
		return dataObj.size();
	}
	
	public static int getItemID(int index) {  //get size
		return dataObj.get(index).itemID;
	}
	
	public static boolean deleteFile(int index) { //delete file
		try {
		dataObj.remove(index);
		return true; //we were successful, return true
		}
		catch(ArrayIndexOutOfBoundsException err) {		 //caught index out of  bounds, return false	
		return false;	
		}
	}
	public static String getIndexName(int index) { //retrieve data name
		return dataObj.get(index).fileData;
	}
	
	public static String getIndexPath(int index) { //retrieve data path
		return dataObj.get(index).pathData;
	}
	public static Integer getIndexItem(int index) { //retrieve data category
		return dataObj.get(index).itemType;
	}

}

class SQLQueries { //sql queries class
	private static Connection conn;
	private static final String URL ="jdbc:mysql://penttinen-juha.mysql.database.azure.com:3306/myfiles?useSSL=true&requireSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC"; //need timezone stuff to get connection to work properly 
	private static String database = "myfiles";

	private static final String USERNAME ="JuhPen12@penttinen-juha";
	private static final String PASSWORD="Murio4Supper";
	private static int connectedUser = -1; //who is connected, used for sql queries
	public static boolean OpenConnection() {
		try {

		conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
		System.out.println("Connection to database successful");
		return true;
		}
		catch (SQLException err)
		{
			System.err.println("Caught SQLException: " + err.getMessage());
			return false;
		}
	}
	
	public static int GetConnectedUser() { //return connected user
	return connectedUser;
	}
	
	public static void SetConnectedUser(Integer User) { //set connected user
	connectedUser = User;
	}	
	
	public static boolean GetUserData(int UserID) {
	
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT FileID, FileName, FileDesc, FileType, UserID FROM files WHERE UserID ='"+ UserID+"'"); 
			while (rs.next()) {
				  int fID = rs.getInt("FileID");
				  String fName = rs.getString("FileName");
				  String fDesc = rs.getString("FileDesc");
				  int fType = rs.getInt("FileType");
				  fileDataBase.addFile(fID,fName,fDesc,fType);	
//					public static boolean addFile(int gID, String name, String path, int index) { //add an entry
			}
			 return true;	
			}
			catch (SQLException err)
			{
				System.err.println("Caught SQLException: " + err.getMessage());
				return false;			
			}		
		
	}
	
	public static int GetUser(String Username, String Password) { //get user ,return false if wrong

		try {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT UserID, UserName, PassWord FROM users WHERE userName='"+ Username+"' AND Password='"+ Password+ "'"); 
		while (rs.next()) {
			  String uName = rs.getString("UserName");
			  String pWord = rs.getString("PassWord");
			  int uID = rs.getInt("UserID");
			  if (uName.equals(Username)) {
			//	  System.out.println("got here");
			  return uID;	
			  }
		}

		}
		catch (SQLException err)
		{
			System.err.println("Caught SQLException: " + err.getMessage());
			return -1;			
		}		
		return -1;
	}
	
	public static boolean CreateUser(String Username, String Password) { //create new user
		try {

			PreparedStatement ps = conn.prepareStatement(
				      "INSERT INTO users SET UserID = ?, UserName = ?, PassWord = ?");
		    ps.setInt(1,0);
			ps.setString(2,Username);
		    ps.setString(3,Password);
		    ps.executeUpdate();
		    ps.close();
			return true;
			}
			catch (SQLException err)
			{
				System.err.println("Caught SQLException: " + err.getMessage());
				return false;			
			}		

	}
	
	public static int CreateEntry(int UserID, String filename, String filedesc, int filetype) { //create new entry
		try {

			PreparedStatement ps = conn.prepareStatement(
				      "INSERT INTO files SET FileID = ?, FileName = ?, FileDesc = ?, FileType = ?, UserID = ?", Statement.RETURN_GENERATED_KEYS);
		    ps.setInt(1,0);
			ps.setString(2,filename);
		    ps.setString(3,filedesc);
		    ps.setInt(4,filetype);
		    ps.setInt(5,UserID);
		    ps.executeUpdate();
		    
		    	   ResultSet rs = ps.getGeneratedKeys();
		    	    rs.next();
		    	   int auto_id = rs.getInt(1);
		
		    
		    ps.close();
			return auto_id;
			}
			catch (SQLException err)
			{
				System.err.println("Caught SQLException: " + err.getMessage());
				return -1;			
			}		

	}
	
	public static boolean UpdateEntry(int FileID, int UserID, String filename, String filedesc, int filetype) { //update entry
		try {

			PreparedStatement ps = conn.prepareStatement(
				      "UPDATE files SET FileName = ?, FileDesc = ?, FileType = ?, UserID = ? WHERE FileID = ?");
		    ps.setInt(5,FileID);
			ps.setString(1,filename);
		    ps.setString(2,filedesc);
		    ps.setInt(3,filetype);
		    ps.setInt(4,UserID);
		    ps.executeUpdate();
		    		    
		    ps.close();
			return true;
			}
			catch (SQLException err)
			{
				System.err.println("Caught SQLException: " + err.getMessage());
				return false;			
			}		

	}
	
	public static boolean DeleteEntry(int FileID) { //delete entry
		try {

			PreparedStatement ps = conn.prepareStatement(
				      "DELETE FROM files WHERE FileID = ?");
		    ps.setInt(1,FileID);
		    ps.executeUpdate();
		    		    
		    ps.close();
			return true;
			}
			catch (SQLException err)
			{
				System.err.println("Caught SQLException: " + err.getMessage());
				return false;			
			}		

	}	
}



	
class Projekti {
//    public static JList<Object> fileNames = new JList<Object>();
	public static JList<Object> fileNames = new JList<Object>();
    public static JLabel lblFileInformation = new JLabel("File Information");
    public static JComboBox itemListing = new JComboBox(varDefinitions.listChoices);
    public static ArrayList<Integer> referenceIndex = new ArrayList<Integer>();
    
	private static void refreshList(JList<Object> listItem) { //refresh contents of list item
		listItem.removeAll();
		
        if (itemListing.getSelectedIndex() == varDefinitions.itemEverything) {
        	listItem.setListData(fileDataBase.filesData()); //update everything
        	referenceIndex.clear();
        } else
        {
        	referenceIndex.clear();
        	DefaultListModel<Object> dlm = new DefaultListModel<Object>();
        	int numbering = 0; //for reference index
        	for(int i=0; i<fileDataBase.getSize(); i++){
        		if (fileDataBase.getIndexItem(i) == itemListing.getSelectedIndex()) { //only display selected item category.
//      		dlm.addElement(fileDataBase.getIndexName(i));	
        		dlm.addElement(fileDataBase.getFile(i));
            	referenceIndex.add(i);
            	System.out.println(i);
            	numbering++;
            }
        	listItem.setModel(dlm); //update everything

            }
        }
        
	}
	
	private static int cIndex() { //convert index to get proper file id.
		int convertIndex;
		if (itemListing.getSelectedIndex() == varDefinitions.itemEverything) {
			convertIndex= fileNames.getSelectedIndex();
		}else
		{
			convertIndex = referenceIndex.get(fileNames.getSelectedIndex()); //convert to right index to get right file.
		}
		return convertIndex;
	};
	
	private static void deleteFile() {  //delete file button code

			int convertIndex = cIndex();
		try {
			int itemID = fileDataBase.getItemID(convertIndex);
			SQLQueries.DeleteEntry(itemID); //delete from database
			fileDataBase.deleteFile(convertIndex); //delete this file
			refreshList(fileNames); //refresh the listbox
		} catch (ArrayIndexOutOfBoundsException err) {
			 System.err.println("Caught ArrayIndexOutOfBoundsException: " + err.getMessage());
		}
		
	}
	
	private static void openFile() { //open file

		int convertIndex = cIndex();
		try {

		Desktop.getDesktop().open(new File(fileDataBase.getIndexPath(convertIndex)));
		} catch (IOException err) {
			 System.err.println("Caught IOException: " + err.getMessage());
		}
		
	}
	private static void createAndShowGUI() { //Main UI

        //Create and set up the window.
        JFrame frame = new JFrame("Dude, where's my file?");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        

        JPanel topContent = new JPanel();

        frame.getContentPane().add(topContent, BorderLayout.NORTH);
        FlowLayout fl_topContent = new FlowLayout(FlowLayout.CENTER, 5, 5);
        topContent.setLayout(fl_topContent);
        
        JButton btnNewFile = new JButton("New File");
        btnNewFile.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) { //I don't know why but this triggers twice?
        		showNewMenu();
        	}
        });
        
     	JLabel lblShow = new JLabel("Show:");
        topContent.add(lblShow);
        
      //  JComboBox itemListing = new JComboBox(varDefinitions.listChoices);
        itemListing.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent arg0) {
//        		System.out.println(itemListing.getSelectedIndex());
    	    	refreshList(fileNames); //refresh the list
        	}
        });

        

        
        
        
        topContent.add(itemListing);

        topContent.add(btnNewFile);
        
        JButton btnUpdateFile = new JButton("Update File");
        btnUpdateFile.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) { //update file
        		if (fileNames.getSelectedIndex() != -1) displayFileAdd(1);	
        	}
        });
        
        JButton btnOpenFile = new JButton("Open File");
        btnOpenFile.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) { //open file
        		openFile(); 
        	}
        });
        topContent.add(btnOpenFile);
        topContent.add(btnUpdateFile);
        
        JButton btnDeleteFile = new JButton("Delete File");
        btnDeleteFile.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) { //delete file
        		deleteFile();
        	}
        });
        topContent.add(btnDeleteFile);
        
        JButton btnExit = new JButton("Exit");
        topContent.add(btnExit);
        btnExit.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		System.exit(0);
        	}
        });
        
        JPanel bottomContent = new JPanel();
        FlowLayout flowLayout = (FlowLayout) bottomContent.getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        bottomContent.setBorder(new SoftBevelBorder(BevelBorder.RAISED, null, null, null, null));
        frame.getContentPane().add(bottomContent, BorderLayout.SOUTH);
        
        //JLabel lblFileInformation = new JLabel("File Information");
        lblFileInformation.setHorizontalAlignment(SwingConstants.CENTER);
        bottomContent.add(lblFileInformation);
        
        JPanel westContent = new JPanel(new BorderLayout());
        frame.getContentPane().add(westContent, BorderLayout.WEST);
        westContent.setLayout(new BoxLayout(westContent, BoxLayout.X_AXIS));
        
        JPanel eastContent = new JPanel(new BorderLayout());
        frame.getContentPane().add(eastContent, BorderLayout.EAST);
        eastContent.setLayout(new BoxLayout(eastContent, BoxLayout.X_AXIS)); 
        


        fileNames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        fileNames.addListSelectionListener(new ListSelectionListener() {
        	public void valueChanged(ListSelectionEvent arg0) {
        		if (fileDataBase.getSize() > fileNames.getSelectedIndex() && fileNames.getSelectedIndex() != -1) lblFileInformation.setText(fileDataBase.getIndexPath(fileNames.getSelectedIndex())); //index is not out of bounds
       		
        	}
        });

        fileNames.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		if (e.getClickCount() == 2) openFile();

        	}
        });
        


        JPanel centerContent = new JPanel();       
        fileNames.setBorder(new LineBorder(new Color(0, 0, 0)));
        fileNames.setVisibleRowCount(2);

        JScrollPane listScroller = new JScrollPane();

        listScroller.setViewportView(fileNames);
        fileNames.setLayoutOrientation(JList.VERTICAL);
        centerContent.setLayout(new BorderLayout()); 
        centerContent.add(listScroller);



        //Display the window.
        frame.getContentPane().add(centerContent);
        frame.setPreferredSize(new Dimension(600, 400));
        frame.pack();  

        
        frame.getContentPane().add(centerContent);
        frame.setVisible(true);
    }
	
	private static boolean displayLogin() { //code for login function
		JPanel menuPanel = new JPanel();
        BoxLayout newMenuLayout = new BoxLayout(menuPanel, BoxLayout.PAGE_AXIS);
        menuPanel.setLayout(newMenuLayout);
		JPanel browsePanel = new JPanel(); //panels and panels of stuff
		JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));	
		JTextField userName = new JTextField(50);		
		userPanel.add(new JLabel("Username:"));
		userPanel.add(userName);

		browsePanel.add(new JLabel("Password:"));
		JPasswordField passwordField = new JPasswordField(50);	
		browsePanel.add(passwordField);		
		
		menuPanel.add(userPanel);	 //phew	
		menuPanel.add(browsePanel);		

		String[] options = new String[] {"Login", "Register", "Exit"};
				int rows = 0; //default result rows
				int result = JOptionPane.showOptionDialog(null, menuPanel, "Please log in or register",
				
	            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
	            null, options, options[0]);
	    
		    if (result == 0) {
		    	//System.out.println(userName.getText());
		    	SQLQueries.OpenConnection();
		    	String myPass=String.valueOf(passwordField.getPassword());
		    	int connected = SQLQueries.GetUser(userName.getText(), myPass); //try user connection
		    	
		    	
		    	if (connected != -1) {
		    		infoBox("Login successful","Success");
		    		SQLQueries.SetConnectedUser(connected); //set connected user
		    		return true; 
		    	}
		    	else
		    	{
		    		infoBox("Invalid username or password","Error");
		    		displayLogin();
		    		return false; 	    		
		    	}
		    }
		    if (result == 1) { //chose register
		    	String myPass=String.valueOf(passwordField.getPassword());
		    	if (userName.getText().equals("") || myPass.equals("")) {
		    		infoBox("Username or password cannot be empty","Failed");	
		    		displayLogin();
		    		return false;
		    	}
		    	SQLQueries.OpenConnection();
		    	boolean Query = SQLQueries.CreateUser(userName.getText(), myPass);
		    	if (Query) {
		    		infoBox("Register successful, please log in.","Success");
		    		displayLogin();
		    		//SQLQueries.SetConnectedUser(userName.getText()); //set connected user
		    		return false;
		    	} else
		    	{
		    		infoBox("Register failed, username already in use.","Failed");
		    		displayLogin();
		    		return false;
		    		
		    	}
		    	
		    	//displayLogin();

		    }
		    if (result == 2) { //chose exit, leave program
		    	return false;
		    }
		return true;
	}
	
	private static void infoBox(String infoMessage, String titleBar) //info box for pop up messages
    {
        JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
	
	private static void displayFileAdd(int type) { //display file dialog box with option for update or new file.
		JPanel menuPanel = new JPanel();
        BoxLayout newMenuLayout = new BoxLayout(menuPanel, BoxLayout.PAGE_AXIS);
        menuPanel.setLayout(newMenuLayout);
		JPanel browsePanel = new JPanel(); //panels and panels of stuff
		JPanel descPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));	
		JTextField itemDescription = new JTextField(50);		
		descPanel.add(new JLabel("File Description:"));
		descPanel.add(itemDescription);
		JComboBox<String> itemCategory = new JComboBox<String>(varDefinitions.listChoices);
		descPanel.add(itemCategory);
		browsePanel.add(new JLabel("File Location:"));
		JTextField itemLocation = new JTextField(50);	
		browsePanel.add(itemLocation);		
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		JFileChooser fc = new JFileChooser();
        		int returnVal = fc.showOpenDialog(null);
        		
        		if (returnVal == JFileChooser.APPROVE_OPTION) { //we got file path, put it to path box
        			File selectedFile = fc.getSelectedFile();
        			itemLocation.setText(selectedFile.getAbsolutePath());
        		}
        	    }        	
        });		
		browsePanel.add(btnBrowse);
		menuPanel.add(descPanel);	 //add all the panels!
		menuPanel.add(browsePanel);		

		if (type == 0) { //regular new file
			int result = JOptionPane.showConfirmDialog(null, menuPanel, "Enter details for file to add", JOptionPane.OK_CANCEL_OPTION);
		    
		    if (result == JOptionPane.OK_OPTION) {
		    	int generatedID = SQLQueries.CreateEntry(SQLQueries.GetConnectedUser(), itemLocation.getText(), itemDescription.getText(),itemCategory.getSelectedIndex()); //create entry to database
		    	fileDataBase.addFile( generatedID, itemLocation.getText(), itemDescription.getText(), itemCategory.getSelectedIndex()); //reference in our list too
		    	refreshList(fileNames); //refresh the list
		    }
	
		}
		if (type == 1) { //modify file
			int convertIndex = cIndex(); //get index
			itemDescription.setText(fileDataBase.getIndexName(convertIndex));
			itemLocation.setText(fileDataBase.getIndexPath(convertIndex));
			itemCategory.setSelectedIndex(fileDataBase.getIndexItem(convertIndex));
			int itemID = fileDataBase.getItemID(convertIndex); //get item ID for query
			int result = JOptionPane.showConfirmDialog(null, menuPanel, "Enter details for file to modify", JOptionPane.OK_CANCEL_OPTION);
		    
		    if (result == JOptionPane.OK_OPTION) {
		    	
		    	try {
//		    		public static int UpdateEntry(int FileID, int UserID, String filename, String filedesc, int filetype) { //update entry
		    	SQLQueries.UpdateEntry(itemID, SQLQueries.GetConnectedUser(), itemLocation.getText(), itemDescription.getText(), itemCategory.getSelectedIndex());
		    	fileDataBase.updateFile(fileDataBase.getItemID(convertIndex),convertIndex, itemDescription.getText(), itemLocation.getText(), itemCategory.getSelectedIndex() );
		    	//fileDataBase.addFile( itemLocation.getText(), itemDescription.getText(), itemCategory.getSelectedIndex());
		    	refreshList(fileNames); //refresh the list
		    	}
		    	catch(IndexOutOfBoundsException err) {
		    		System.err.println("Caught ArrayIndexOutOfBoundsException: " + err.getMessage());	
		    	}
		    }
	
		}
	};

	private static void showNewMenu() { //shows new file popup menu

	displayFileAdd(0);	

	}
	
	
	public static void main(String[] args) {
		

		// TODO Auto-generated method stub
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	//SQLQueries.OpenConnection(); // for testing
            	boolean loggedIn = displayLogin();
            	
                if (loggedIn) {
                	SQLQueries.GetUserData(SQLQueries.GetConnectedUser());
                	refreshList(fileNames);  //refresh listboxes with new data
                	createAndShowGUI(); //successfully logged in.
                }
                                        
            }
        });
	}


	
}
