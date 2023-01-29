package projects;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectsService;

public class ProjectsApp {
	// scanner used for user input, project services creates an instance of the projects service class
	private Scanner scanner = new Scanner(System.in);
	private ProjectsService projectsService = new ProjectsService();
	private Project curProject;
	
	// list below is utilized in menu app to show the options
	
	// @formatter:off
	private List<String> operations = List.of(
		"1) Add a project",
		"2) List projects",
		"3) Select a project",
		"4) Update project details",
		"5) Delete a project"
	);
	// @formatter:on

	public static void main(String[] args) {
		// calls method that brings up the menu
		new ProjectsApp().processUserSelections();
	}
	
	
	private void processUserSelections() {
		//boolean variable used to run the while loop
		boolean done = false;
		
		//while loop runs until "done" is true"
		while(!done) {
			//try block used to throw exceptions
			try {
				
				//switch below is used to return something based on what the user entered, anything that isn't a 1 gets an error, nothing closes the app
				int selection = getUserSelection();
				
				switch(selection) {
				case -1:
					done = exitMenu();
					break;
					
				case 1:
					createProject();
					break;
					
				case 2:
					listProjects();
					break;
					
				case 3:
					selectProject();
					break;
					
				case 4:
					updateProjectDetails();
					break;
					
				case 5:
					deleteProject();
					break;
					
					default:
						System.out.println("\n" + selection + " is not a valid selection. Try again.");
				}
			
			}
			catch(Exception e) {
				System.out.println("\nError: "+ e + " Try again.");
			}
		}
				
	}

	/*
	 * method lists all projects available and asks the user to select one to delete based on its ID, the ID is checked to make sure the project exists then the id is sent to the Services layer
	 * this method also checks to make sure that the project being deleted is not the current project and if it is, it sets the current project to Null
	 */
	private void deleteProject() {
		listProjects();
		Integer projectId = getIntInput("Enter the ID of the project to delete.");
		
		if(Objects.nonNull(projectId)) {
			projectsService.deleteProject(projectId);
			
			System.out.println("You have deleted project " + projectId);
			
			if(Objects.nonNull(curProject) && curProject.getProjectId().equals(projectId)) {
				curProject = null;
			}
		}
		
	}

	
	/*
	 * method collects data to be updated from the user then instantiates a new project variable to assign the new values to, this is then sent off to the Services layer
	 * which verifies that it is not negative and then passes the data to the dao layer to interface with the database via a SQL command
	 */
	private void updateProjectDetails() {
		if(Objects.isNull(curProject)) {
			System.out.println("\nPlease select a project.");
			return;
			}
		
		String projectName = getStringInput("Enter project name [" + curProject.getProjectName() + "]");
		BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours [" + curProject.getEstimatedHours() + "]");
		BigDecimal actualHours = getDecimalInput("Enter the actual hours [" + curProject.getActualHours() + "]");
		Integer difficulty = getIntInput("Enter the project difficulty (1-5) [" + curProject.getDifficulty() + "]");
		String notes = getStringInput("Enter the project notes [" + curProject.getNotes() + "]");
		
		Project project = new Project();
		
		project.setProjectId(curProject.getProjectId());
		project.setProjectName(Objects.isNull(projectName) ? curProject.getProjectName() : projectName);
		project.setEstimatedHours(Objects.isNull(estimatedHours) ? curProject.getEstimatedHours() : estimatedHours);
		project.setActualHours(Objects.isNull(actualHours) ? curProject.getActualHours() : actualHours);
		project.setDifficulty(Objects.isNull(difficulty) ? curProject.getDifficulty() : difficulty);
		project.setNotes(Objects.isNull(notes) ? curProject.getNotes() : notes);
		
		projectsService.modifyProjectDetails(project);
		
		curProject = projectsService.fetchProjectById(curProject.getProjectId());
	}


	// sends user input to Project Service layer & keeps track of the project the user has selected with CurProject
	private void selectProject() {
		listProjects();
		
		Integer projectId = getIntInput("Select a project ID");
		
		curProject = null;
		
		curProject = projectsService.fetchProjectById(projectId);
		
		if(Objects.isNull(curProject)) {
			System.out.println("\nInvalid recipe selected. ");
		}
		
	}

	// sends user input to Project Services which retrieves the data from the DAO layer. Once the data is received by the DAO layer from the Service layer, this method prints out all projects
	private void listProjects() {
		List<Project> projects = projectsService.fetchAllProjects();
		
		System.out.println("\nProjects:");
		
		projects.forEach(project -> System.out.println("   " + project.getProjectId()+ ": " + project.getProjectName()));
		
		
	}


	private void createProject() {
		
		// this block prompts the user to enter information that will populate all columns in a row of the projects table
		String projectName = getStringInput("Enter the project name");
		BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
		BigDecimal actualHours = getDecimalInput("Enter the actual hours");
		Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
		String notes = getStringInput("Enter the project notes");
		
		// creates a new instance of the project class
		Project project = new Project();
		
		// assigns user input to the newly created project class
		
		project.setProjectName(projectName);
		project.setEstimatedHours(estimatedHours);
		project.setActualHours(actualHours);
		project.setDifficulty(difficulty);
		project.setNotes(notes);
		
		// sends the new project to the project services class which sends to the DAO class & that will be sent off to our database
		Project dbProject = projectsService.addProject(project);
		System.out.println("You have successfully created the project: " + dbProject);
	}


	private BigDecimal getDecimalInput(String prompt) {
		
		// this string validates that what the user entered is a valid decimal entry and rounds the decimal to two decimal places
		String input = getStringInput(prompt);
		
		if(Objects.isNull(input)) {
			return null;
		}
		
		try {
			return new BigDecimal(input).setScale(2);
		}
		catch(NumberFormatException e) {
			throw new DbException(input + " is not a valid decimal number.");
		}
	}

	// this method exits the menu
	private boolean exitMenu() {
		System.out.println("Exiting the menu");
		return true;
	}

	// this prompts the user to enter a selection and validates the selection to ensure it is not null
	private int getUserSelection() {
		printOperations();
		
		Integer input = getIntInput("Enter a menu selection");
		
		return Objects.isNull(input) ? -1 : input;
	}

	// this method does the same thing as the getDecInput method only validates the string
	private Integer getIntInput(String prompt) {
		String input = getStringInput(prompt);
		
		if(Objects.isNull(input)) {
			return null;
		}
		
		try {
			return Integer.parseInt(input);
		}
		catch(NumberFormatException e) {
			throw new DbException(input + " is not a valid number.");
		}
	}

	// this method is the method that takes the input from the user, it is the lowest level of the input
	private String getStringInput(String prompt) {
		System.out.print(prompt + ": ");
		String input = scanner.nextLine();
		
		return input.isBlank() ? null : input.trim();
		
	}

	// this method uses the List from the variables above to show the user all available options line by line
	private void printOperations() {
		System.out.println("\nThese are the available selections. Press the Enter key to quit.");
		
		operations.forEach(line -> System.out.println("   " + line));
		
		if(Objects.isNull(curProject)) {
			System.out.println("\nYou are not working with a project");
		} else {
			System.out.println("\nYou are working with project: "+ curProject);
		}
		
		
		
	}

}
