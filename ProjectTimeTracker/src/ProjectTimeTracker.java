import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// A calendar that saves activites in a plain text file
public class ProjectTimeTracker {

	private Shell projectShell;
	private DateTime contentActivitiesDate;
	private Composite contentCalendar, contentActivities;
	private Label contentActivitiesLabel;
	private Button contentActivitiesAddButton, contentActivitiesRemoveButton;
	private File contentActivitiesFile;
	private List activitiesList;
	private ArrayList<String> removedActivitiesArrayList;
	
	
	// Constructor
	public ProjectTimeTracker() {

		// File to store activities
		contentActivitiesFile = new File("C:/TimeTracker/activities.txt");
		contentActivitiesFile = initActivitiesFile(contentActivitiesFile);

		// SWT window setup
		Display myDisplay = new Display();
		projectShell = new Shell(myDisplay);	
		
		// Setup display for "contentCalendar" and "contentActivities" Composites
		contentCalendar = initProjectTimeTrackerLayout(projectShell, contentCalendar, 1);
		contentActivities = initProjectTimeTrackerLayout(projectShell, contentActivities, 1);
		
		// Label to display date in "contentActivities" Composite
		contentActivitiesLabel = new Label(contentActivities, SWT.BORDER);

		// Create "Add" Button
		contentActivitiesAddButton =  new Button(contentActivities, SWT.PUSH);

		// GridData for "Add" Button to make it even with "Remove" Button
		GridData addButtonGridData = new GridData();
		addButtonGridData.horizontalAlignment = GridData.FILL;
		addButtonGridData.grabExcessHorizontalSpace = true;
		contentActivitiesAddButton.setLayoutData(addButtonGridData);

		// Create "Remove" Buttons
		contentActivitiesRemoveButton =  new Button(contentActivities, SWT.PUSH);

		// GridData for "Remove" Button to make it even with "Add" Button
		GridData removeButtonGridData = new GridData();
		removeButtonGridData.horizontalAlignment = GridData.FILL;
		removeButtonGridData.grabExcessHorizontalSpace = true;
		contentActivitiesRemoveButton.setLayoutData(removeButtonGridData);		
				
		// Create List to display activities for the selected date
		activitiesList = new List(contentActivities, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
				
		// GridData for List to make it event with "Remove" and "Add" Buttons
		GridData activitiesListGridData = new GridData();
		activitiesListGridData.horizontalAlignment = GridData.FILL;
		activitiesListGridData.grabExcessHorizontalSpace = true;
		activitiesListGridData.heightHint = 50;
		activitiesList.setLayoutData(activitiesListGridData);		
		
		
		// Column 1
		displayCalendar();
		
		// Column 2
		displayActivities();
		
		// Compress window 
		projectShell.pack();
		projectShell.open();

		// Required to display SWT window
		while(!projectShell.isDisposed()) {
			if(!myDisplay.readAndDispatch()) 		
				myDisplay.sleep();
			}
						
		// Destroy SWT window 
		myDisplay.dispose();
				
	}
	
	
	// Helper method for making Composites with GridLayout
	// Return: Composite with a GridLayout that has the specified number or columns
	private Composite initProjectTimeTrackerLayout(Shell projectShell, Composite myComposite, int gridColumns) {
		
		// Setting up GridLayout
		projectShell.setLayout(new RowLayout());
		GridLayout myGridLayout = new GridLayout();
		
		// Set the number of columns
		myGridLayout.numColumns = gridColumns;

		myGridLayout.makeColumnsEqualWidth = true;
		
		myComposite = new Composite(projectShell, SWT.NONE);
		myComposite.setLayout(myGridLayout);

		return myComposite;
	}

		
	// Method to create a new activities.txt file if it doesn't already exist
	// Return: New plain text File 
	private File initActivitiesFile(File myActivities) {

		if((myActivities==null) || (myActivities.length()==0)  || (!myActivities.exists())) {
				
				try {
					// Making a new activities.txt file
					myActivities = new File("C:/TimeTracker/activities.txt");
					FileOutputStream myOutputStream = new FileOutputStream(myActivities);
					OutputStreamWriter myOutputStreamWriter = new OutputStreamWriter(myOutputStream);    
					Writer myWriter = new BufferedWriter(myOutputStreamWriter);
					myWriter.close();
				} catch (IOException e) {
					System.err.println("Problem writing to the file activities.txt");
				}
						
			}
		return myActivities;
	}
	
	
	// Method to write an activity to new activities.txt file
	private void writeActivitiesFile(Shell myShell, String myActivity) {

		String date = new String((contentActivitiesDate.getMonth() + 1) + "/" + contentActivitiesDate.getDay());
		String activity = new String(myActivity);
				
		// File must be there
		if(!(contentActivitiesFile==null)) {
			
			try {
				// Open activities.txt file and append new activity
				FileOutputStream myOutputStream = new FileOutputStream(contentActivitiesFile, true);
				OutputStreamWriter myOutputStreamWriter = new OutputStreamWriter(myOutputStream);    
				BufferedWriter myWriter = new BufferedWriter(myOutputStreamWriter);	

				// Write date and activity into the file
				myWriter.append(date);
				myWriter.newLine();
				myWriter.append(activity);
				myWriter.newLine();

				// Close the file
				myWriter.close();
			} catch (IOException e) {
				System.err.println("Problem writing to the file activities.txt");
			}	
						
		}

		// Update the list of activities for the date
		getActivities(contentActivitiesDate);

		// Update the current List with the recently added activity
		activitiesList.redraw();
		
		myShell.dispose();
	}
	

	// Method to add an activity into the current date
	private void addActivities(DateTime contentActivitiesDate) {

		final Shell myShell = new Shell(projectShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	
		int myMonth = (contentActivitiesDate.getMonth() + 1);
		
		
		// New Shell to display dialog 
		myShell.setLayout(new RowLayout());
		Composite myComposite = initProjectTimeTrackerLayout(myShell, contentCalendar, 1);
				
		// Text field for input
		final Text myText = new Text(myComposite, SWT.BORDER);

		myText.setText("Activity for " + " " + myMonth + "/" + contentActivitiesDate.getDay() + ": ");		

		
		// Button for adding a new activity
		Button myAddButton = new Button(myComposite, SWT.PUSH);
				
		myAddButton.setText("Add");		
		
		// Add a Listener to the Button
		myAddButton.addSelectionListener(new SelectionAdapter() {	
			public void widgetSelected(SelectionEvent myEvent) {
				writeActivitiesFile(myShell, myText.getText());
				
			}
		});

		
		// Button to cancel adding a new activity
		Button myCancelButton = new Button(myComposite, SWT.PUSH);
		myCancelButton.setText("Cancel");		
		
		// Add a Listener to the Button
		myCancelButton.addSelectionListener(new SelectionAdapter() {	
			public void widgetSelected(SelectionEvent myEvent) {
				myShell.dispose();
			}
		});
		
		myShell.pack();
	    myShell.open();
	    
	}
	
	
	// Remove the activities listed in removedActivitiesArrayList from the contentActivitiesFile
	private void removeActivities(DateTime contentActivitiesDate) {
				
		// Temporary file to hold updated information
		String myTempFileName = "C:/TimeTracker/activities.tmp";
								
		// Check if anything has to be removed
		if (!removedActivitiesArrayList.isEmpty()) {
																
			// File must be there and contentActivitiesDate must not be null
			if (contentActivitiesDate != null && contentActivitiesFile!=null) {
				// Date to find and remove
				String myDate = new String((contentActivitiesDate.getMonth() + 1) + "/" + contentActivitiesDate.getDay());

				// String ArrayList to hold dates and activities for updated new file
				ArrayList<String> myArrayList = new ArrayList<String>();			
				
				try {					
					
					// Make a BufferedReader from myActivitesFile
					BufferedReader myBufferedReader = new BufferedReader(new FileReader(contentActivitiesFile));

					// Current line and last line read from file
					String currentLineFromFile = new String();
					String lastLineFromFile = new String();
					String lineToNewFile = new String();
														
					// Go through each line from myActivitiesFile
					while (((currentLineFromFile = myBufferedReader.readLine()) != null) && (!currentLineFromFile.isEmpty())) {
					
						// Check if last line was the date needed 
						if (lastLineFromFile.equals(myDate)) {

							// Go through each item from removedActivitiesArrayList
							for (int i = 0; i < removedActivitiesArrayList.size(); i++) {

								// Check if next line is in removedActivitiesArrayList								
								if(currentLineFromFile.equals(removedActivitiesArrayList.get(i))) {							
									// Don't add activity or activity's date to new file if it matches 																			
									currentLineFromFile="";	
									lastLineFromFile="";																		
								}
								
							}

						}

						// Don't add any empty Strings
						if(!lineToNewFile.isEmpty()) {
							myArrayList.add(lineToNewFile);	
						}
						
						// Keep history of last 2 lines viewed
						lineToNewFile = lastLineFromFile; 	
						lastLineFromFile = currentLineFromFile;
							
					}

					// Don't add any empty Strings
					if(!lineToNewFile.isEmpty()) {
						myArrayList.add(lineToNewFile);														
					}
					
					// Don't add any empty Strings
					if(!lastLineFromFile.isEmpty()) {
						myArrayList.add(lastLineFromFile);
					}
					
					
					myBufferedReader.close();
					
										
				} catch (IOException e) {
					System.err.println("Problem reading from the file activities.txt");
					
				}			
		
				
				try {
					// Make a BufferedWriter to write to temporary file
					FileOutputStream myOutputStream = new FileOutputStream(myTempFileName, true);
					OutputStreamWriter myOutputStreamWriter = new OutputStreamWriter(myOutputStream);    
					BufferedWriter myWriter = new BufferedWriter(myOutputStreamWriter);	
					
					if (myArrayList != null) {							
						// Fill file with updated information from ArrayList myArrayList
						for(String myString : myArrayList) {
							myWriter.append(myString);	
							myWriter.newLine();
						}
						
					}
					myWriter.close();
				} catch (IOException e) {
					System.err.println("Problem writing to the file activities.txt");
				}	
			}
			
		}
		
		// Clear the removedActivitiesArrayList 
		removedActivitiesArrayList.clear();

	    // File (or directory) with new name
	    File myTempFile = new File(myTempFileName);

	    // Delete the old contentActivitiesFile
	    boolean deleteOldFile = contentActivitiesFile.delete();

	    // Check if old contentActivitiesFile was deleted 
	    if (deleteOldFile) {
	    	// Rename the temporary file to make it the new contentActivitiesFile
		    boolean renameNewFile = myTempFile.renameTo(contentActivitiesFile);

		    // Check if renaming was a success
		    if (renameNewFile) {
				
				// Update the list of activities for the date
				getActivities(contentActivitiesDate);

				// Update the current List
				activitiesList.redraw();
		    			    	
		    }
		    
	    }
		
	}

	// Get activites from contentActivitiesFile and display them
	private void getActivities(DateTime contentActivitiesDate) {
		
		ArrayList<String> myArrayList = new ArrayList<String>();
		
		// Clear the list of activites
		activitiesList.removeAll();
		
		// File must be there and contentActivitiesDate must not be null
		if (contentActivitiesDate != null && contentActivitiesFile!=null) {
	
			String myDate = new String((contentActivitiesDate.getMonth() + 1) + "/" + contentActivitiesDate.getDay());
			
			try {

				// Make a BufferedReader from myActivitesFile
				BufferedReader myBufferedReader = new BufferedReader(new FileReader(contentActivitiesFile));

				// Current line
				String lineFromFile;

				// Go through each line from file
				while ((lineFromFile = myBufferedReader.readLine()) != null) {
					
					// Fill ArrayList with file's content for date
					if (lineFromFile.equals(myDate)) {				
						myArrayList.add(myBufferedReader.readLine());
					}
				}
				myBufferedReader.close();
	
			} catch (IOException e) {
				System.err.println("Problem reading from the file activities.txt");
			
			}	
		}	

		if (myArrayList != null) {
		
			// Fill List activitiesList with information from ArrayList myArrayList
			for(String myString : myArrayList) {
				activitiesList.add(myString);				
			}
								
		}

	}
		
	
	// Make and display Calendar with Selection Listener in left column of main window
	private void displayCalendar() {
		
		// Add a Calendar to the contentCalendar Composite
		DateTime myCalendar = new DateTime(contentCalendar, SWT.CALENDAR);
		
		this.contentActivitiesDate = (DateTime)myCalendar;
	
		// Add a Listener to the Calenar
		myCalendar.addSelectionListener(new SelectionAdapter() {
			
			public void widgetSelected(SelectionEvent myEvent) {

				// Get the DateTime Object that generated the event, then display the activities for it		
				contentActivitiesDate = (DateTime)myEvent.getSource();
				getActivities(contentActivitiesDate);

				// Redraw the list
				activitiesList.redraw();
			}
		}
			);		
	}
	
	
	// Make and display list of activities and buttons to add or remove activities
	private void displayActivities() {	

		String numberSuffix = new String();
		
		if((contentActivitiesDate.getDay() == 1)||(contentActivitiesDate.getDay() == 21)||(contentActivitiesDate.getDay() == 31)) {
			numberSuffix = "st";
		}else if((contentActivitiesDate.getDay() == 2)||(contentActivitiesDate.getDay() == 22)) {
			numberSuffix = "nd";
		}else if((contentActivitiesDate.getDay() == 3)||(contentActivitiesDate.getDay() == 23)) {
			numberSuffix = "rd";
		}else{
			numberSuffix = "th";
		}
		
		// Set the Label
		contentActivitiesLabel.setText(" Activities for" + " " + contentActivitiesDate.getDay() + numberSuffix + ": ");		
			
		// Display the activites for the date
		getActivities(contentActivitiesDate);
			
		// Button for adding a new activity
		contentActivitiesAddButton.setText("Add Activity");		
				
		// Add a Listener to the Button
		contentActivitiesAddButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent myEvent) {
				addActivities(contentActivitiesDate);
			}
		});
		

		// Button for removing an existing activity
		contentActivitiesRemoveButton.setText("Remove Activity");		
				
		// Add a Listener to the Button
		contentActivitiesRemoveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent myEvent) {

				removeActivities(contentActivitiesDate);
			}
		});
		
		
		// Add a Listener to the List
		activitiesList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent myEvent) {

				removedActivitiesArrayList = new ArrayList<String>();

				// Getting the index of the item that generated the event
				int[] selectedActivityArrayList = activitiesList.getSelectionIndices();

				// Make a List of items to be removed
				for (int i = 0; i < selectedActivityArrayList.length; i++) {	
					removedActivitiesArrayList.add(activitiesList.getItem(i));
				}
			}
		});

	
		contentActivities.pack();		
		projectShell.pack();
	}

	
	
	
	
		
	public static void main(String[] args) {
		ProjectTimeTracker myProjectTimeTracker = new ProjectTimeTracker ();
	}

}
