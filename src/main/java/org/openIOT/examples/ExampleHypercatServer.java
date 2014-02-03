package org.openIOT.examples;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExampleHypercatServer extends HttpServlet {
    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
    
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        
        String name = req.getParameter("name");
        out.println("<HTML>");
        out.println("<HEAD><TITLE> EXAMPLE HYPERCAT SERVER </TITLE></HEAD>");
        out.println("<BODY>");
        out.println("Hello, " + name);
        out.println("</BODY></HTML>");
    }
    
    public String getServletInfo() {
    return "A servlet that knows the name of the person to whom it's" + 
    "saying hello";
}
    
    

}
