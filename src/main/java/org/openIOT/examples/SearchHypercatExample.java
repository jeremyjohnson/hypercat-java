package org.openIOT.examples;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openIOT.Hypercat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchHypercatExample extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        String querystring = req.getParameter("querystring");
        String hcstring = URLDecoder.decode(req.getParameter("hc"), "UTF-8");
        Hypercat hc = new Hypercat(hcstring, true);
        Hypercat resultsHypercat = hc.searchCat(querystring);

        RequestDispatcher rd = req.getRequestDispatcher("displayHypercat");
        req.setAttribute("hypercat", resultsHypercat.toPrettyJson());
        req.setAttribute("source", "searching the last hypercat for: " + querystring);
        rd.forward(req, res);
    }

    public void init(ServletConfig servletconfig) {
        try {
            super.init();
        } catch (ServletException e) {
        }
    }

    public String getServletInfo() {
        return "servlet to search a hypercat's items for a gven string and generate a results hypercat ";
    }

}
