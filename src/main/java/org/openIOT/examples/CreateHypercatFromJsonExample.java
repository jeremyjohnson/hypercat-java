package org.openIOT.examples;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openIOT.Hypercat;

public class CreateHypercatFromJsonExample extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        String json = req.getParameter("json");

        Hypercat hc = null;
        String constructorOutput = "";

        try {
            hc = new Hypercat(json, true);
            constructorOutput = hc.toPrettyJson();

        } catch (Exception e) {
            constructorOutput = "Error creating hypercat from JSON string: " + e;
        }

        RequestDispatcher rd = req.getRequestDispatcher("displayHypercat");
        req.setAttribute("hypercat", constructorOutput);
        req.setAttribute("source", "a JSON string input by user");
        rd.forward(req, res);
    }

    public void init(ServletConfig servletconfig) {
        try {
            super.init();
        } catch (ServletException e) {
        }
    }

    public String getServletInfo() {
        return "A servlet that generates a hyercat object from an input JSON string";
    }

}
