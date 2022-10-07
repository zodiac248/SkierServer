import Schema.LiftRide;
import com.google.gson.Gson;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class SkierServlet extends HttpServlet {
    private int counter = 0;
    private int numRequests = 0;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response){
        response.setContentType("text/plain");
        String urlPath = request.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            try {
                response.getWriter().write("missing paramterers");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)
        if (!isUrlValid(urlParts)) {
            response.setStatus(404);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            // do any sophisticated processing with urlParts which contains all the url params
            // TODO: process url params in `urlParts`
            try {
                response.getWriter().write("It works!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isUrlValid(String[] urlPath) {
        // TODO: validate the request url path according to the API spec
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        if(!urlPath[2].equals("seasons") || !urlPath[4].equals("days")||!urlPath[6].equals("skiers")){
            return false;
        }

        try{
            int resortID = Integer.parseInt(urlPath[1]);
            String seasonID = urlPath[3];
            int day = Integer.parseInt(urlPath[5]);
            int skierID = Integer.parseInt(urlPath[7]);

            if(!(1<=day&& day<=366)){
                return false;
            }
        }catch (RuntimeException e){
            return false;
        }
        return true;
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/plain");
        String urlPath = request.getPathInfo();
        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            try {
                response.getWriter().write("missing paramterers");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        Gson gson = new Gson();
        StringBuilder sb = new StringBuilder();
        String line = "";
        try{
            while((line=request.getReader().readLine())!=null){
                sb.append(line);
            }
            LiftRide liftRide = gson.fromJson(sb.toString(), LiftRide.class);

        } catch (Exception e){
            response.setStatus(403);
        }





        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)
        if (!isUrlValid(urlParts)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.setStatus(HttpServletResponse.SC_CREATED);
            // do any sophisticated processing with urlParts which contains all the url params
            // TODO: process url params in `urlParts`
            try {
                BufferedReader br = request.getReader();
                line = "";
                sb = new StringBuilder();
                while((line = br.readLine())!=null){
                    if(line.length()==0){
                        break;
                    }
                    sb.append(line);
                }
                String record = sb.toString();
                gson = new Gson();
                LiftRide l1 = gson.fromJson(record, LiftRide.class);
                PrintWriter pw = response.getWriter();
                pw.close();
                br.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

}
