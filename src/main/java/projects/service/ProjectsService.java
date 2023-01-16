package projects.service;

import projects.dao.ProjectsDao;
import projects.entity.Project;

public class ProjectsService {
	// creates an instance of the projectDao class which is where our SQL statement will be put together and sent off to the database
	private ProjectsDao projectDao = new ProjectsDao();
	
	// method calls the insertProject method on the projectDao and uses input entered by the user
	public Project addProject(Project project) {
		return projectDao.insertProject(project);
	}

}
