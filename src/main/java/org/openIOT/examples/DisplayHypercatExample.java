package org.openIOT.examples;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DisplayHypercatExample extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        String hcstring = (String) req.getAttribute("hypercat");
        req.setAttribute("hypercat", "SETHERE");

        String source = (String) req.getAttribute("source");

        out.println("<HTML>");
        out.println("<HEAD><TITLE> EXAMPLE HYPERCAT DISPLAY</TITLE></HEAD>");
        out.println("<BODY>");
        out.println("<H2>EXAMPLE HYPERCAT DISPLAY</H2><HR>");
        out.println("<BR> The hypercat below was created via " + source + ":<BR>");
        out.println("Search this hypercat for: <FORM action='searchHypercat'>"
                + " <input type='text' name='querystring'>  "
                + " <input type='SUBMIT' value='Search'><br>"
                + " ( eg val=3 , or rel=urn:X-tsbiot:rels:1 )"
                + " <input type='HIDDEN' name='hc' id='hc' value='" + hcstring + "'><br>"
                + "</FORM>");

        out.println("<HR><textarea rows='80' cols='120'>" + hcstring + "</textarea><HR>");

        out.println("</BODY></HTML>");
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        doGet(req, res);
    }

    public void init(ServletConfig servletconfig) {
        try {
            super.init();
        } catch (ServletException e) {
        }
    }

    public String getServletInfo() {
        return "Hypercat display servlet";
    }

}
