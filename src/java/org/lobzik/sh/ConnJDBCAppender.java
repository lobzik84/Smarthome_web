package org.lobzik.sh;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.jdbc.JDBCAppender;
import org.lobzik.tools.db.mysql.DBTools;

public class ConnJDBCAppender extends JDBCAppender
{

	private static DataSource ds = null;

	private ConnJDBCAppender(DataSource dsc)
	{
		super();
		ds = dsc;
		
	}
	
	protected  Connection getConnection() throws SQLException
	{

		return ds.getConnection();
	}
	
	protected  void	closeConnection(Connection con) 
	{
		DBTools.closeConnection(con);
	}
	
	public static Appender getAppenderInstance(DataSource dsc, int moduleId)
	{
		ConnJDBCAppender jdbcAppender = new ConnJDBCAppender(dsc);
  		jdbcAppender.setLayout(new PatternLayout());
  		jdbcAppender.setSql("INSERT INTO LOGS VALUES (0," + moduleId + ",'%d{yyyy-MM-dd HH:mm:ss.SSS}','%p','%m') \n");
		AsyncAppender asyncAppender = new AsyncAppender();
  		asyncAppender.setBufferSize(1000);
  		asyncAppender.addAppender(jdbcAppender);
  		return asyncAppender;
	}
}