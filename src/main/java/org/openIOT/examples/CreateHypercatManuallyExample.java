package org.openIOT.examples;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openIOT.Hypercat;

public class CreateHypercatManuallyExample extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        String description = req.getParameter("description");

        Hypercat hc = new Hypercat(description);
        RequestDispatcher rd = req.getRequestDispatcher("displayHypercat");
        req.setAttribute("hypercat", hc.toPrettyJson());
        req.setAttribute("source", "a manual description input by user");
        rd.forward(req, res);

    }

    public void init(ServletConfig servletconfig) {
        try {
            super.init();
        } catch (ServletException e) {
        }
    }

    public String getServletInfo() {
        return "servlet to generate simplest possible hypercat with manual description ";
    }

}
