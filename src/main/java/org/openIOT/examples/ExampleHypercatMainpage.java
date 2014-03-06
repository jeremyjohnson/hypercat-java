package org.openIOT.examples;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExampleHypercatMainpage extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        String name = req.getParameter("name");
        out.println("<HTML>");
        out.println("<HEAD><TITLE> CREATE HYPERCAT EXAMPLE </TITLE></HEAD>");
        out.println("<BODY>");
        out.println("<H2>CREATE HYPERCAT EXAMPLE</H2><HR>");
        out.println("<BR> Use the forms below to create example Hypercats<BR>");
        out.println("<DL><LI>"
                + "<h3> type in a description, then click 'submit' to make a basic hypercat</h3>"
                + "<FORM action='manualHypercat'>"
                + " <input type='text' name='description'><br>"
                + " <input type='SUBMIT'><br>"
                + "</FORM>"
                + "</LI>"
                + "<LI>"
                + "<h3> paste in a JSON string defining a hypercat, then click 'submit' to generate a hypercat</h3>"
                + "<FORM action='jsonHypercat'>"
                + " <input type='text' name='json'><br>"
                + " <input type='SUBMIT'><br>"
                + "</FORM>"
                + "</LI>"
                + "<LI>"
                + "<h3> select a file containing a JSON string defining a hypercat, then click 'upload' to generate a hypercat</h3>"

                + "<form method='POST' action='fileHypercat' enctype='multipart/form-data' >"
                + "File:"
                + "<input type='file' name='file' id='file' /> </br>"

                + "<input type='submit'  value='upload' />"
                + "</form>"
                + "</LI> </DL>");
        out.println("</BODY> </HTML>");
    }

    public void init(ServletConfig servletconfig) {
        try {
            super.init();
        } catch (ServletException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getServletInfo() {
        return "Servlet generating the main examples page";
    }

}
