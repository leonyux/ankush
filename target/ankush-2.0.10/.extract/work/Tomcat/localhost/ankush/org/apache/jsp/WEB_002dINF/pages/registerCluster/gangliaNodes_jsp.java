/*
 * Generated by the Jasper component of Apache Tomcat
 * Version: Apache Tomcat/7.0.37
 * Generated at: 2015-06-04 02:42:15 UTC
 * Note: The last modified time of this file was set to
 *       the last modified time of the source file after
 *       generation to assist with modification tracking.
 */
package org.apache.jsp.WEB_002dINF.pages.registerCluster;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

public final class gangliaNodes_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static final javax.servlet.jsp.JspFactory _jspxFactory =
          javax.servlet.jsp.JspFactory.getDefaultFactory();

  private static java.util.Map<java.lang.String,java.lang.Long> _jspx_dependants;

  private javax.el.ExpressionFactory _el_expressionfactory;
  private org.apache.tomcat.InstanceManager _jsp_instancemanager;

  public java.util.Map<java.lang.String,java.lang.Long> getDependants() {
    return _jspx_dependants;
  }

  public void _jspInit() {
    _el_expressionfactory = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();
    _jsp_instancemanager = org.apache.jasper.runtime.InstanceManagerFactory.getInstanceManager(getServletConfig());
  }

  public void _jspDestroy() {
  }

  public void _jspService(final javax.servlet.http.HttpServletRequest request, final javax.servlet.http.HttpServletResponse response)
        throws java.io.IOException, javax.servlet.ServletException {

    final javax.servlet.jsp.PageContext pageContext;
    javax.servlet.http.HttpSession session = null;
    final javax.servlet.ServletContext application;
    final javax.servlet.ServletConfig config;
    javax.servlet.jsp.JspWriter out = null;
    final java.lang.Object page = this;
    javax.servlet.jsp.JspWriter _jspx_out = null;
    javax.servlet.jsp.PageContext _jspx_page_context = null;


    try {
      response.setContentType("text/html");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write("<!------------------------------------------------------------------------------\n");
      out.write("-  ===========================================================\n");
      out.write("-  Ankush : Big Data Cluster Management Solution\n");
      out.write("-  ===========================================================\n");
      out.write("-  \n");
      out.write("-  (C) Copyright 2014, by Impetus Technologies\n");
      out.write("-  \n");
      out.write("-  This is free software; you can redistribute it and/or modify it under\n");
      out.write("-  the terms of the GNU Lesser General Public License (LGPL v3) as\n");
      out.write("-  published by the Free Software Foundation;\n");
      out.write("-  \n");
      out.write("-  This software is distributed in the hope that it will be useful, but\n");
      out.write("-  WITHOUT ANY WARRANTY; without even the implied warranty of\n");
      out.write("-  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n");
      out.write("-  See the GNU Lesser General Public License for more details.\n");
      out.write("-  \n");
      out.write("-  You should have received a copy of the GNU Lesser General Public License \n");
      out.write("-  along with this software; if not, write to the Free Software Foundation, \n");
      out.write("- Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.\n");
      out.write("------------------------------------------------------------------------------->\n");
      out.write("\n");
      out.write("<body>\n");
      out.write("\t<div class=\"\">\n");
      out.write("\t\t<div class=\"\">\n");
      out.write("\t\t\t<div class=\"\">\n");
      out.write("\t\t\t\t<div class=\"col-md-4\">\n");
      out.write("\t\t\t\t</div>\n");
      out.write("\t\t\t\t<div class=\"col-md-1\">\n");
      out.write("\t\t\t\t</div>\n");
      out.write("\t\t\t\t<div class=\"col-md-7 text-right mrgt20 padr45\">\n");
      out.write("\t\t\t\t</div>\n");
      out.write("\t\t\t</div>\n");
      out.write("\t\t</div>\n");
      out.write("\n");
      out.write("\t\t<div class=\"\" id=\"main-content\">\n");
      out.write("\t\t\t<div class=\"container-fluid mrgnlft8\">\n");
      out.write("\t\t\t\t<div class=\"row\">\n");
      out.write("\t\t\t\t</div>\n");
      out.write("\t\t\t\t<div class=\"panel\">\n");
      out.write("\t\t\t\t\t<div class=\"panel-heading\">\n");
      out.write("\t\t\t\t\t\t<div class=\"\">\n");
      out.write("\t\t\t\t\t\t\t<div class=\"\">\n");
      out.write("\t\t\t\t\t\t\t\t<h3 class=\"panel-title col-md-3 mrgt5\">Ganglia Node Mapping</h3>\n");
      out.write("\t\t\t\t\t\t\t\t<button id=\"gangliaNodesRevert\" class=\"btn btn-default\"\n");
      out.write("\t\t\t\t\t\t\t\t\tonclick=\"com.impetus.ankush.hybridClusterCreation.dynamicRowRemove();\">Cancel</button>\n");
      out.write("\t\t\t\t\t\t\t\t<button class=\"btn btn-default\" id=\"gangliaNodesApply\"\n");
      out.write("\t\t\t\t\t\t\t\t\tonclick=\"com.impetus.ankush.hybrid_Ganglia.gangliaNodesPopulateApplyValidate();\">Apply</button>\n");
      out.write("\t\t\t\t\t\t\t</div>\n");
      out.write("\t\t\t\t\t\t\t<div class=\"pull-right panelSearch\">\n");
      out.write("\t\t\t\t\t\t\t\t<input type=\"text\" id=\"gangliaNodesSearchBox\" style=\"\"\n");
      out.write("\t\t\t\t\t\t\t\t\tclass=\"input-medium form-control\" placeholder=\"Search\" />\n");
      out.write("\t\t\t\t\t\t\t</div>\n");
      out.write("\t\t\t\t\t\t\t<div id=\"errorDivGangliaNodes\" class=\"errorDiv mrgt10\"\n");
      out.write("\t\t\t\t\t\t\t\tstyle=\"display: none;\"></div>\n");
      out.write("\t\t\t\t\t\t</div>\n");
      out.write("\t\t\t\t\t</div>\n");
      out.write("\t\t\t\t\t<div class=\"row panel-body\">\n");
      out.write("\t\t\t\t\t\t<div class=\"col-md-12 \" style=\"\">\n");
      out.write("\t\t\t\t\t\t\t<table class=\"table table-striped\" id=\"gangliaNodeTable\"\n");
      out.write("\t\t\t\t\t\t\t\tstyle=\"border: 1px solid #E1E3E4; border-top: 2px solid #E1E3E4\">\n");
      out.write("\t\t\t\t\t\t\t\t<thead\n");
      out.write("\t\t\t\t\t\t\t\t\tstyle=\"text-align: left; border-bottom: 1px solid #E1E3E4\">\n");
      out.write("\t\t\t\t\t\t\t\t\t<tr>\n");
      out.write("\t\t\t\t\t\t\t\t\t\t<th>Host Name</th>\n");
      out.write("\t\t\t\t\t\t\t\t\t\t<th>Node Roles</th>\n");
      out.write("\t\t\t\t\t\t\t\t\t\t<th>GangliaMaster</th>\n");
      out.write("\t\t\t\t\t\t\t\t\t\t<th>OS</th>\n");
      out.write("\t\t\t\t\t\t\t\t\t\t<th></th>\n");
      out.write("\t\t\t\t\t\t\t\t\t</tr>\n");
      out.write("\t\t\t\t\t\t\t\t</thead>\n");
      out.write("\t\t\t\t\t\t\t</table>\n");
      out.write("\t\t\t\t\t\t</div>\n");
      out.write("\t\t\t\t\t</div>\n");
      out.write("\t\t\t\t</div>\n");
      out.write("\t\t\t</div>\n");
      out.write("\t\t</div>\n");
      out.write("\t</div>\n");
      out.write("\t<script>\n");
      out.write("\t\t$(document).ready(function() {\n");
      out.write("\t\t\tgangliaNodeTable = $(\"#gangliaNodeTable\").dataTable({\n");
      out.write("\t\t\t\t\"bJQueryUI\" : true,\n");
      out.write("\t\t\t\t\"bPaginate\" : false,\n");
      out.write("\t\t\t\t\"bLengthChange\" : false,\n");
      out.write("\t\t\t\t\"bFilter\" : true,\n");
      out.write("\t\t\t\t\"bSort\" : true,\n");
      out.write("\t\t\t\t\"bInfo\" : false,\n");
      out.write("\t\t\t\t\"aaSorting\" : [ [ 0, \"asc\" ] ],\n");
      out.write("\t\t\t\t\"aoColumnDefs\" : [ {\n");
      out.write("\t\t\t\t\t'bSortable' : false,\n");
      out.write("\t\t\t\t\t'aTargets' : [ 1, 2, 3, 4, ]\n");
      out.write("\t\t\t\t} ],\n");
      out.write("\t\t\t});\n");
      out.write("\t\t\t$('#gangliaNodeTable_filter').hide();\n");
      out.write("\t\t\tgangliaNodeTable = $('#gangliaNodeTable').dataTable();\n");
      out.write("\t\t\t$('#gangliaNodesSearchBox').keyup(function() {\n");
      out.write("\t\t\t\tgangliaNodeTable.fnFilter($(this).val());\n");
      out.write("\t\t\t});\n");
      out.write("\t\t\tcom.impetus.ankush.hybrid_Ganglia.gangliaNodesPopulate();\n");
      out.write("\t\t});\n");
      out.write("\t</script>\n");
      out.write("</body>\n");
    } catch (java.lang.Throwable t) {
      if (!(t instanceof javax.servlet.jsp.SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          try { out.clearBuffer(); } catch (java.io.IOException e) {}
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
        else throw new ServletException(t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}
