package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import java.util.Optional;
import provided.util.DaoBase;

public class ProjectsDao extends DaoBase {
	// static variables are all associated with a table in our database
	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";
	
	
	public Project insertProject(Project project) {
		//first part of the method writes our SQL statement
		
		//@formatter:off
		String sql = ""
				+ "INSERT INTO " + PROJECT_TABLE + " "
				+ "(project_name, estimated_hours, actual_hours, difficulty, notes) "
				+ "VALUES "
				+ "(?, ?, ?, ?, ?)";
		//@formatter:on
		
		//first (outer) try/catch block runs the connection and throws a SQL Exception if there was a connection error
		try(Connection conn = DbConnection.getConnection()){
			// transaction is started here 
			startTransaction(conn);
			
			// second (inner) try/catch is our Prepared Statement which validates the inputs and protects against SQL injection attacks
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				// this block checks each parameter
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				
				//below saves the update & assigns an ID to project ID
				stmt.executeUpdate();
				Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
				
				//commits changes to the database
				commitTransaction(conn);
				
				project.setProjectId(projectId);
				return project;
			}
			catch(Exception e) {
				//if Prepared statement try fails, it rolls the transaction back
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	// creates a transaction with DB to show all projects & IDs stored in the project schema
	public List<Project> fetchAllProjects() {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";
		
		// opens the connection
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			// prepared statement to validate & protect against SQL injection attacks
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				try(ResultSet rs = stmt.executeQuery()){
					List<Project> projects = new LinkedList<>();
					
					// loops through result set and adds each project to the projects list
					while(rs.next()) {
						projects.add(extract(rs, Project.class));
					}
					return projects;
				}
			} catch(Exception e) {
				throw new DbException(e);
			}
			
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	// strats a transaction with the DB to retrieve all information on a selected project
	public Optional<Project> fetchProjectById(Integer projectId) {
		// initial query uses the project id to identify the project selected
		String sql = "SELECT * FROM " + PROJECT_TABLE +" WHERE project_id = ?";
		
		// opens connection with DB
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try {
				Project project = null;
				
				// prepared statement used to validate the input from the user
				try(PreparedStatement stmt = conn.prepareStatement(sql)){
					setParameter(stmt, 1, projectId, Integer.class);
					
					// try with resource statement to grab the Project results and assign them to a Project variable (established earlier
					try(ResultSet rs = stmt.executeQuery()){
						if(rs.next()) {
							project = extract(rs, Project.class);
						}
					}
				}
				
				// checks to make sure the Project object isn't null and adds the values from Material, Step & Categories schema
				if(Objects.nonNull(project)) {
					project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
					project.getSteps().addAll(fetchStepsForProject(conn, projectId));
					project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
				}
				// commits the transaction
				commitTransaction(conn);
				// returns retrieved info to Service layer
				return Optional.ofNullable(project);
				
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	// method grabbing info from Category schema related to the project that was selected by user, works the same as the Project, Steps and Materials fetch methods
	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException {
		// Query includes a Join statement as it needs to include the category id reference fromt he Project Category table
		
		// @formatter:off
		String sql = ""
				+ "SELECT c.* FROM " + CATEGORY_TABLE + " c "
				+ "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
				+ "WHERE project_id = ?";
		// @formatter:on
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()){
				List<Category> categories = new LinkedList<>();
				
				while(rs.next()) {
					categories.add(extract(rs, Category.class));
				}
			return categories;
			}
			
		}

	}


	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + STEP_TABLE + " s WHERE s.project_id = ?";
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()){
				List<Step> steps = new LinkedList<>();
				
				while(rs.next()) {
					steps.add(extract(rs, Step.class));
				}
			return steps;
			}
			
		}
		
	}


	private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + MATERIAL_TABLE + " m WHERE m.project_id = ?";
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()){
				List<Material> materials = new LinkedList<>();
				
				while(rs.next()) {
					materials.add(extract(rs, Material.class));
				}
			return materials;
			}
			
		}
	}
	
	//performs the update SQL transaction with SQL statement modified by the user input and returns true or false if the update was done successfully
	public boolean modifyProjectDetails(Project project) {
		// @formatter:off
		String sql =""
				+"UPDATE " + PROJECT_TABLE + " SET "
				+"project_name = ?, "
				+"estimated_hours = ?, "
				+"actual_hours = ?, "
				+"difficulty = ?, "
				+"notes = ? "
				+"WHERE project_id = ?";
		// @formatter:on
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			// prepared statement is used to validate all inputs can be used as parameters in the SQL statement
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				setParameter(stmt, 6, project.getProjectId(), Integer.class);
				
				boolean updated = stmt.executeUpdate() == 1;
				commitTransaction(conn);
				
				return updated;
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}
	
	//performs the delete SQL transaction with SQL statement whose ID is based on user input. Returns true or false if the delete was done successfully
	public boolean deleteProject(Integer projectId) {
		String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, projectId, Integer.class);
				
				boolean deleted = stmt.executeUpdate() == 1;
				
				commitTransaction(conn);
				return deleted;
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
			
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

}
