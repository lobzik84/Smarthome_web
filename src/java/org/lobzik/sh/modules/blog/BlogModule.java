package org.lobzik.sh.modules.blog;

import java.sql.Connection;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lobzik.sh.CommonData;
import org.lobzik.sh.modules.ModuleAbstract;
import org.lobzik.tools.CommonTools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BlogModule  extends ModuleAbstract
{
	
	int postsOnPage = 10;
	
	public void start() throws Exception
	{
		//System.out.println("Blog module started!");
	}

	public void init(HashMap config) throws Exception {
		// TODO Auto-generated method stub

	}


	public void service(HttpServletRequest request, HttpServletResponse response)	throws Exception 
	{
		HashMap<String, Object> jspData = new HashMap<String, Object>();
		String moduleUrl = request.getContextPath() + request.getServletPath() + "/" + (String)((HashMap)request.getAttribute("ModuleMap")).get("URL") + "/";
		int index = request.getRequestURL().lastIndexOf(moduleUrl) + moduleUrl.length();
		String mode = request.getRequestURL().substring(index);
		
		if (mode.startsWith("edit"))
		{

		}
		else if (mode.startsWith("delete"))
		{

		}
		else
		{
			Connection conn = DBTools.openConnection(CommonData.dataSourceName);
			try 
			{	
				if (request.getMethod().equals("POST"))
				{
					String post = request.getParameter("post");
					if (post != null)
					{
						post = CommonTools.replaceTags(post);
						post = post.trim();
						if (post.length() > 0)
						{
							HashMap db = new HashMap();
							db.put("POST", post);
							db.put("DATE", new Date());
							String subject = request.getParameter("subject");
							if (subject != null)
							{
								subject = CommonTools.replaceTags(subject);
								subject = subject.trim();
								if (subject.length() > 0)
									db.put("SUBJECT", subject);
							}
							DBTools.insertRow("BLOG", db, conn);
							//conn.commit();
						}
					}
				}
				int pageNo = 1;
				if (mode.contains("/")) pageNo = CommonTools.parseInt(mode.substring(mode.indexOf("/") + 1), 1);
				ArrayList<HashMap> posts = new ArrayList<HashMap>();
				int pages = getBlog(posts, pageNo, postsOnPage, conn);
				DBTools.closeConnection(conn);
				jspData.put("Posts", posts);
				jspData.put("PageNo", pageNo); 
				jspData.put("Pages", pages);
			}
			catch (Exception e)
			{
				DBTools.closeConnection(conn);
				throw e;
			}	 
		}
		String templateJspName = (String)request.getAttribute("TemplateJspName");
		jspData.put("ModuleJspName", "/modules/blog/blog.jsp");
		RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher(templateJspName);
	    request.setAttribute("JspData", jspData);
	    disp.include(request, response);

	}
	
	private int getBlog(ArrayList<HashMap> posts, int pageNo, int linesOnPage, Connection conn) throws Exception
	{
		int pages = 1;
		String countSQL = "SELECT COUNT(*) AS CNT FROM BLOG ";
		int lines = CommonTools.parseInt(DBSelect.getRows(countSQL, conn).get(0).get("CNT"), 1);
		pages = (lines / linesOnPage) + 1;
		if (pageNo == 0) pageNo = pages;
		String sSQL = "SELECT * FROM BLOG ORDER BY DATE DESC LIMIT " + (pageNo - 1) * linesOnPage + "," + linesOnPage + ";";
				
		List<HashMap> result = DBSelect.getRows(sSQL, conn);
		for (HashMap h:result)
			posts.add(h);
		return pages;
	}
	


}
