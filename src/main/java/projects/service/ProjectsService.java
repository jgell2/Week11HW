package projects.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import projects.dao.ProjectsDao;
import projects.entity.Project;
import projects.exception.DbException;

public class ProjectsService {
	// creates an instance of the projectDao class which is where our SQL statement will be put together and sent off to the database
	private ProjectsDao projectDao = new ProjectsDao();
	
	// method calls the insertProject method on the projectDao and uses input entered by the user
	public Project addProject(Project project) {
		return projectDao.insertProject(project);
	}
	
	// retrieves all project names and IDs from DAO layer and returns it to I/O layer
	public List<Project> fetchAllProjects() {
		return projectDao.fetchAllProjects();
	}
	
	//retrieves a single project based on user input from DAO layer and returns it to I/O layer. Also, validates that the selection can be made.
	public Project fetchProjectById(Integer projectId) {
		return projectDao.fetchProjectById(projectId).orElseThrow(() -> new NoSuchElementException("Project with project ID=" + projectId + "does not exist."));
			
	}
	
	//ensures that the the project to be updated exists & passes user input info through to the data layer
	public void modifyProjectDetails(Project project) {
		if(!projectDao.modifyProjectDetails(project)) {
			throw new DbException("Project with ID=" + project.getProjectId() + " does not exist.");
		}
	}

	public void deleteProject(Integer projectId) {
		if(!projectDao.deleteProject(projectId)) {
			throw new DbException("Project with ID=" + projectId + " does not exist.");
		}
		
	}

}
