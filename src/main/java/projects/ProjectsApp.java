package projects;

import projects.dao.DbConnection;

public class ProjectsApp {

	public static void main(String[] args) {
		//calls method on DbConnection class
		DbConnection.getConnection();
	}

}
