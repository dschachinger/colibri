/**************************************************************************************************
 * Copyright (c) 2016, Automation Systems Group, Institute of Computer Aided Automation, TU Wien
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *************************************************************************************************/

package at.ac.tuwien.auto.colibri.core.datastore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class DatabasePostgre
{
	private static final Logger log = Logger.getLogger(DatabasePostgre.class.getName());

	private static final long NO_PARENT = -1;

	private DataSource ds = null;

	public DatabasePostgre(DataSource ds)
	{
		this.ds = ds;
	}

	/**
	 * Get all mappings from DataConfigurations into the database
	 *
	 * @return Arraylist containing the DataConfigurationMappings
	 * @throws RuntimeException if an error occurred while requesting mappings from database
	 */
	public ArrayList<DataConfigurationMapping> getAllMappings() throws RuntimeException
	{
		ArrayList<DataConfigurationMapping> mappings = new ArrayList<DataConfigurationMapping>();
		java.sql.ResultSet rs = null;
		java.sql.ResultSet rsPar = null;
		Connection con = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		String sql1 = "SELECT dataconfig_uri, service_uri, table_name, parent "
				+ "FROM public.data_configuration_mapping;";
		String sql2 = "SELECT parameter_uri, column_name "
				+ "FROM public.parameter_mapping "
				+ "WHERE dataconfig_uri LIKE ? "
				+ ";";
		try
		{
			con = ds.getConnection();
			pstmt1 = con.prepareStatement(sql1);
			rs = pstmt1.executeQuery();

			while (rs.next())
			{
				DataConfigurationMapping mapping = new DataConfigurationMapping(rs.getString("dataconfig_uri"),
						rs.getString("service_uri"), rs.getString("table_name"), rs.getString("parent"));

				pstmt2 = con.prepareStatement(sql2);
				pstmt2.setString(1, mapping.getDataConfigurationURI());
				rsPar = pstmt2.executeQuery();

				while (rsPar.next())
				{
					mapping.addParameter(rsPar.getString("parameter_uri"), rsPar.getString("column_name"));
				}

				mappings.add(mapping);
				this.close(rsPar);
				this.close(pstmt2);
			}
		}
		catch (SQLException e)
		{
			this.close(rsPar);
			this.close(pstmt2);
			throw new RuntimeException(e);
		}
		finally
		{
			this.close(rs);
			this.close(pstmt1);
			this.close(con);
		}

		return mappings;
	}

	/**
	 * Creates new mappings of DataConfigurations in the database. Necessary mapping
	 * information is stored in the database and tables for the data values are created.
	 * The DataConfigurationMappings that shall be written to the database are provided
	 * as ArrayList. For nested DataConfigurations, the order of the elements in the
	 * ArrayList is important. Parent DataConfigurations must have lower indices than
	 * their children.
	 *
	 * @param con An open database connection
	 * @param dcm Data configuration mapping that shall be created
	 * @return True if the mapping was created successfully, otherwise false
	 */
	public boolean createMappings(ArrayList<DataConfigurationMapping> dcms)
	{
		Connection con = null;

		if (dcms.size() == 0)
			return true;

		try
		{
			con = ds.getConnection();
			con.setAutoCommit(false);

			for (DataConfigurationMapping dcm : dcms)
			{
				DataConfigurationMapping parent = null;
				if (dcm.getParentDataConfigurationURI() != null && dcm.getParentDataConfigurationURI().length() > 0)
				{
					parent = dcms.stream().filter(d -> d.getDataConfigurationURI().equals(dcm.getParentDataConfigurationURI())).findFirst().get();
				}
				createMapping(con, dcm, parent);
			}

			con.commit();
			return true;
		}
		catch (SQLException | RuntimeException e)
		{
			log.severe(e.getMessage());
			try
			{
				if (con != null)
					con.rollback();
			}
			catch (SQLException ee)
			{
				log.severe(ee.getMessage());
			}
		}
		finally
		{
			this.close(con);
		}

		return false;
	}

	/**
	 * Creates a new mapping of a DataConfiguration in the database. Necessary mapping
	 * information is stored in the database and tables for the data values are created.
	 *
	 * @param con An open database connection
	 * @param dcm Data configuration mapping that shall be created
	 * @param parent Parent Data configuration mapping
	 * 
	 * @throws SQLException if an error while writing into database occurred
	 * @throws NullPointerException if provided DataConfigurationMapping is null
	 * @throws RuntimeException if the parameter count of the provided
	 *         DataConfigurationMapping does not equal 2 or reading the name of the
	 *         parent table failed
	 */
	private void createMapping(Connection con, DataConfigurationMapping dcm, DataConfigurationMapping parent)
			throws SQLException, NullPointerException, RuntimeException
	{
		Statement stmtSeq = null;
		Statement stmtTable = null;
		PreparedStatement pstmtDataconf = null;
		PreparedStatement pstmtParam1 = null;
		PreparedStatement pstmtParam2 = null;
		String parentTable = null;

		if (dcm == null)
			throw new NullPointerException("Given mapping is null");

		String[] parameter_keys = dcm.getParameters().keySet().toArray(new String[dcm.getParameters().size()]);
		if (parameter_keys.length != 2)
			throw new RuntimeException("Wrong parameter count");

		if (parent != null)
		{
			parentTable = parent.getTableName();
		}

		String sqlSeq = "Create SEQUENCE " + dcm.getTableName() + "_id_seq;";
		String sqlTable = null;
		String sqlDataconf = "INSERT INTO public.data_configuration_mapping (dataconfig_uri, "
				+ "service_uri, table_name, parent) values (?, ?, ?, ?)";
		String sqlParam = "INSERT INTO public.parameter_mapping (parameter_uri, dataconfig_uri, "
				+ "column_name) values (?, ?, ?)";

		if (parentTable != null)
			sqlTable = "Create Table public." + dcm.getTableName()
					+ " ("
					+ "id bigint NOT NULL DEFAULT nextval('" + dcm.getTableName() + "_id_seq'::regClass), "
					+ "uri text NULL, "
					+ dcm.getParameterColumn(parameter_keys[0]) + " text NOT NULL, "
					+ dcm.getParameterColumn(parameter_keys[1]) + " text NOT NULL, "
					+ dcm.getParameterColumn(parameter_keys[0]) + "_uri text NULL, "
					+ dcm.getParameterColumn(parameter_keys[1]) + "_uri text NULL, "
					+ "parent bigint NOT NULL REFERENCES " + parentTable + " (id), "
					+ "CONSTRAINT pk_" + dcm.getTableName() + " PRIMARY KEY (id)"
					+ ")";
		else
			sqlTable = "Create Table public." + dcm.getTableName()
					+ " ("
					+ "id bigint NOT NULL DEFAULT nextval('" + dcm.getTableName() + "_id_seq'::regClass), "
					+ "uri text NULL, "
					+ dcm.getParameterColumn(parameter_keys[0]) + " text NOT NULL, "
					+ dcm.getParameterColumn(parameter_keys[1]) + " text NOT NULL, "
					+ dcm.getParameterColumn(parameter_keys[0]) + "_uri text NULL, "
					+ dcm.getParameterColumn(parameter_keys[1]) + "_uri text NULL, "
					+ "CONSTRAINT pk_" + dcm.getTableName() + " PRIMARY KEY (id)"
					+ ")";

		try
		{
			stmtSeq = con.createStatement();
			stmtTable = con.createStatement();

			stmtSeq.execute(sqlSeq);
			stmtTable.execute(sqlTable);

			pstmtDataconf = con.prepareStatement(sqlDataconf);
			pstmtDataconf.setString(1, dcm.getDataConfigurationURI());
			pstmtDataconf.setString(2, dcm.getServiceURI());
			pstmtDataconf.setString(3, dcm.getTableName());
			pstmtDataconf.setString(4, dcm.getParentDataConfigurationURI());

			int affRows = pstmtDataconf.executeUpdate();
			if (affRows > 0)
			{
				pstmtParam1 = con.prepareStatement(sqlParam);
				pstmtParam1.setString(1, parameter_keys[0]);
				pstmtParam1.setString(2, dcm.getDataConfigurationURI());
				pstmtParam1.setString(3, dcm.getParameterColumn(parameter_keys[0]));
				pstmtParam1.executeUpdate();

				pstmtParam2 = con.prepareStatement(sqlParam);
				pstmtParam2.setString(1, parameter_keys[1]);
				pstmtParam2.setString(2, dcm.getDataConfigurationURI());
				pstmtParam2.setString(3, dcm.getParameterColumn(parameter_keys[1]));
				pstmtParam2.executeUpdate();
			}
			else
			{
				throw new SQLException("Inserting DataConfiguration failed");
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			this.close(stmtSeq);
			this.close(stmtTable);
			this.close(pstmtDataconf);
			this.close(pstmtParam1);
			this.close(pstmtParam2);
		}
	}

	/**
	 * Insert a data value and its children into database
	 *
	 * @param query SPARQL query string
	 * @return True if insert was successful, otherwise false
	 */
	public boolean insertDataValue(DataValue datavalue)
	{
		Connection con = null;

		if (datavalue == null)
			return false;

		log.info("Inserting " + datavalue.getURI());
		try
		{
			con = ds.getConnection();
			con.setAutoCommit(false);

			insertDataValue(con, datavalue, NO_PARENT);

			con.commit();
			return true;
		}
		catch (SQLException e)
		{
			log.severe(e.getMessage());
			try
			{
				if (con != null)
					con.rollback();
			}
			catch (SQLException ee)
			{
				log.severe(ee.getMessage());
			}
		}
		finally
		{
			this.close(con);
		}

		return false;
	}

	private void insertDataValue(Connection con, DataValue datavalue, long parentID) throws SQLException
	{
		String tableName = null;
		String columnName1 = null;
		String columnName2 = null;
		String columnValue1 = null;
		String columnValue2 = null;
		PreparedStatement pstmt = null;
		String sql = null;
		java.sql.ResultSet rs = null;
		long datavalueID = -1;

		if (con == null)
			return;

		if (datavalue != null)
		{
			tableName = datavalue.getDataConfigurationMapping().getTableName();
			columnName1 = datavalue.getDataConfigurationMapping().getParameterColumn(datavalue.getParameters().getParameter1());
			columnName2 = datavalue.getDataConfigurationMapping().getParameterColumn(datavalue.getParameters().getParameter2());

			columnValue1 = datavalue.getValue(datavalue.getParameters().getParameter1());
			columnValue2 = datavalue.getValue(datavalue.getParameters().getParameter2());

			datavalue.getURI();
			datavalue.getParameters().getParameter1();
			datavalue.getParameters().getParameter2();

			if (datavalue.getDataConfigurationMapping().getParentDataConfigurationURI() != null && parentID != NO_PARENT)
				sql = "INSERT INTO public." + tableName + " (" + columnName1 + ", " + columnName2 + ", uri, " + columnName1
						+ "_uri, " + columnName2 + "_uri, parent) values (?, ?, ?, ?, ?, ?)";
			else
				sql = "INSERT INTO public." + tableName + " (" + columnName1 + ", " + columnName2 + ", uri, " + columnName1
						+ "_uri, " + columnName2 + "_uri) values (?, ?, ?, ?, ?)";

			try
			{
				pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, columnValue1);
				pstmt.setString(2, columnValue2);
				pstmt.setString(3, datavalue.getURI());
				pstmt.setString(4, datavalue.getParameters().getParameter1());
				pstmt.setString(5, datavalue.getParameters().getParameter2());
				if (datavalue.getDataConfigurationMapping().getParentDataConfigurationURI() != null && parentID != NO_PARENT)
					pstmt.setLong(6, parentID);
				int affrows = pstmt.executeUpdate();

				if (affrows > 0)
				{
					rs = pstmt.getGeneratedKeys();
					if (rs.next())
					{
						datavalueID = rs.getLong(1);
					}
				}

				this.close(pstmt);
				this.close(rs);

				for (DataValue child : datavalue.getChildren())
				{
					insertDataValue(con, child, datavalueID);
				}
			}
			catch (SQLException e)
			{
				this.close(pstmt);
				this.close(rs);
				throw e;
			}
		}
	}

	/**
	 * Initialization of database. Creates tables for mapping information.
	 *
	 * @return True if initialization was successful, otherwise false
	 */
	public boolean initialDatabaseSetup()
	{
		Connection con = null;
		Statement stmt = null;

		try
		{
			con = ds.getConnection();
			stmt = con.createStatement();

			if (!tableExists("public", "data_configuration_mapping"))
			{
				stmt.execute("Create TABLE public.data_configuration_mapping"
						+ "("
						+ "dataconfig_uri text NOT NULL, "
						+ "service_uri text NOT NULL, "
						+ "table_name text NOT NULL, "
						+ "parent text NULL REFERENCES data_configuration_mapping (dataconfig_uri), "
						+ "CONSTRAINT pk_data_configuration PRIMARY KEY (dataconfig_uri)"
						+ ")");
			}

			if (!tableExists("public", "parameter_mapping"))
			{
				stmt.execute("Create TABLE public.parameter_mapping"
						+ "("
						+ "parameter_uri text NOT NULL, "
						+ "dataconfig_uri text NOT NULL REFERENCES data_configuration_mapping (dataconfig_uri), "
						+ "column_name text NOT NULL, "
						+ "CONSTRAINT pk_parameter_mapping PRIMARY KEY (parameter_uri), "
						+ "UNIQUE (dataconfig_uri, column_name)"
						+ ")");
			}

			return true;
		}
		catch (SQLException | RuntimeException e)
		{
			return false;
		}
		finally
		{
			this.close(stmt);
			this.close(con);
		}
	}

	/**
	 * Get a data configuration mapping from the database
	 *
	 * @param URI of first parameter of the data configuration
	 * @param URI of second parameter of the data configuration
	 * @return Data configuration mapping with the given parameters. NULL if mapping with
	 *         given URL does no exist.
	 * @throws RuntimeException if an error occurred during requesting the mapping from the database
	 */
	public DataConfigurationMapping getDataConfigurationMapping(String parameter1_uri, String parameter2_uri) throws RuntimeException
	{
		java.sql.ResultSet rs = null;
		DataConfigurationMapping mapping = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = "SELECT d.dataconfig_uri, service_uri, table_name, parent, parameter_uri, column_name "
				+ "FROM public.data_configuration_mapping d "
				+ "JOIN public.parameter_mapping p on p.dataconfig_uri = d.dataconfig_uri "
				+ "WHERE p.parameter_uri LIKE ? OR p.parameter_uri LIKE ? ;";

		try
		{
			con = ds.getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, parameter1_uri);
			pstmt.setString(2, parameter2_uri);
			rs = pstmt.executeQuery();

			if (!rs.next())
				return null;

			mapping = new DataConfigurationMapping(rs.getString("dataconfig_uri"), rs.getString("service_uri"),
					rs.getString("table_name"), rs.getString("parent"));
			mapping.addParameter(rs.getString("parameter_uri"), rs.getString("column_name"));

			if (!rs.next())
				return null;

			mapping.addParameter(rs.getString("parameter_uri"), rs.getString("column_name"));

			return mapping;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			this.close(rs);
			this.close(pstmt);
			this.close(con);
		}
	}

	/**
	 * Get a data configuration mapping from the database
	 *
	 * @param URI of the data configuration
	 * @return Data configuration mapping with the given URI. NULL if mapping with
	 *         given URL does no exist.
	 * @throws RuntimeException if an error occurred during requesting the mapping from the database
	 */
	public DataConfigurationMapping getDataConfigurationMapping(String uri) throws RuntimeException
	{
		java.sql.ResultSet rsConf = null;
		java.sql.ResultSet rsParam = null;
		DataConfigurationMapping mapping = null;
		Connection con = null;
		PreparedStatement pstmtConf = null;
		PreparedStatement pstmtParam = null;
		String sqlConf = "SELECT service_uri, table_name, parent "
				+ "FROM public.data_configuration_mapping "
				+ "WHERE dataconfig_uri LIKE ?;";
		String sqlParam = "SELECT parameter_uri, column_name "
				+ "FROM public.parameter_mapping "
				+ "WHERE dataconfig_uri LIKE ?;";

		try
		{
			con = ds.getConnection();
			pstmtConf = con.prepareStatement(sqlConf);
			pstmtConf.setString(1, uri);

			rsConf = pstmtConf.executeQuery();

			if (rsConf.next())
			{
				mapping = new DataConfigurationMapping(uri, rsConf.getString("service_uri"),
						rsConf.getString("table_name"), rsConf.getString("parent"));

				pstmtParam = con.prepareStatement(sqlParam);
				pstmtParam.setString(1, uri);

				rsParam = pstmtParam.executeQuery();

				while (rsParam.next())
				{
					mapping.addParameter(rsParam.getString("parameter_uri"), rsParam.getString("column_name"));
				}
			}

			return mapping;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			this.close(rsConf);
			this.close(rsParam);
			this.close(pstmtConf);
			this.close(pstmtParam);
			this.close(con);
		}
	}

	/**
	 * Check if a particular table exists in the database
	 *
	 * @param schemaName Name of the database schema
	 * @param tableName Name of the table
	 * @return True if the table exists, otherwise false
	 * @throws RuntimeException if on error occurred during checking the database
	 */
	public boolean tableExists(String schemaName, String tableName) throws RuntimeException
	{
		java.sql.ResultSet rs = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = "SELECT EXISTS ( "
				+ "SELECT 1 "
				+ "FROM pg_catalog.pg_class c "
				+ "JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace "
				+ "WHERE n.nspname = ? "
				+ "AND c.relname = ? "
				+ "AND c.relkind = 'r' "
				+ ");";

		try
		{
			con = ds.getConnection();

			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, schemaName);
			pstmt.setString(2, tableName);
			rs = pstmt.executeQuery();

			if (rs.next())
				return rs.getBoolean(1);

			return false;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			this.close(rs);
			this.close(pstmt);
			this.close(con);
		}
	}

	private DatabasePostgre close(AutoCloseable closeable)
	{
		try
		{
			if (closeable != null)
				closeable.close();
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
		}
		return this;
	}
}
