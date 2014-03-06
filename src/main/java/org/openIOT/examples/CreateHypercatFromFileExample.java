package org.openIOT.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.openIOT.Hypercat;

public class CreateHypercatFromFileExample extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        Hypercat hc = null;
        String constructorOutput = "";
        File infile = null;
        FileReader fr = null;

        final String UPLOAD_DIRECTORY = "/tmp";

        // process only if its multipart content
        if (ServletFileUpload.isMultipartContent(request)) {
            try {
                List<FileItem> multiparts = new ServletFileUpload(
                        new DiskFileItemFactory()).parseRequest(request);

                for (FileItem item : multiparts) {
                    if (!item.isFormField()) {
                        infile = new File(item.getName());
                        String name = infile.getName();
                        String fullfilepath = UPLOAD_DIRECTORY + File.separator + name;
                        item.write(new File(fullfilepath));
                        try {
                            fr = new FileReader(fullfilepath);
                            constructorOutput = "FileReader=" + fr.toString();

                        } catch (FileNotFoundException e) {
                            constructorOutput = "File not found!" + e;
                        }
                    }
                }
            } catch (Exception ex) {
                constructorOutput = "File Upload Failed due to " + ex;
            }

            // File uploaded successfully, so try and make a hypercat object out
            // of it
            if (fr != null)
            {
                hc = new Hypercat(fr);
                constructorOutput = hc.toPrettyJson();
            }

            RequestDispatcher rd = request.getRequestDispatcher("displayHypercat");

            request.setAttribute("hypercat", constructorOutput);
            request.setAttribute("source", "a file of JSON uploaded by the user");
            rd.forward(request, res);
        }
    }

    public void init(ServletConfig servletconfig) {
        try {
            super.init();
        } catch (ServletException e) {
        }
    }

    public String getServletInfo() {
        return "Servlet that creates a Hypercat object from a file of JSON ";
    }

}
