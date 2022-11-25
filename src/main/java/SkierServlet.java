import Schema.LiftRide;
import Schema.SkiRequest;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.rmi.server.ServerCloneException;
import java.util.stream.Collectors;

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
        SkiRequest skiRequest = new SkiRequest();
        if (!isUrlValid(urlParts, skiRequest)) {
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

    private boolean isUrlValid(String[] urlPath, SkiRequest skiRequest) {
        // TODO: validate the request url path according to the API spec
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        if(!urlPath[2].equals("seasons") || !urlPath[4].equals("days")||!urlPath[6].equals("skiers")){
            return false;
        }

        try{
            int resortID = Integer.parseInt(urlPath[1]);
            String seasonID = urlPath[3];
            String dayID = urlPath[5];
            int skierID = Integer.parseInt(urlPath[7]);
            int day = Integer.parseInt(dayID);
            skiRequest.setSkierID(skierID);
            skiRequest.setDayID(dayID);
            skiRequest.setSeasonID(seasonID);
            skiRequest.setResortID(resortID);
            if(!(day>=1 && day<=365)){
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
        String record = "";
        SkiRequest skiRequest = new SkiRequest();
        try{
            while((line=request.getReader().readLine())!=null){
                sb.append(line);
            }

            record = sb.toString();
            LiftRide liftRide = gson.fromJson(record, LiftRide.class);
            skiRequest.setTime(liftRide.getTime());
            skiRequest.setLiftID(liftRide.getLiftID());
        } catch (Exception e){
            response.setStatus(403);
        }





        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)
        if (!isUrlValid(urlParts, skiRequest)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {

            // do any sophisticated processing with urlParts which contains all the url params
            // TODO: process url params in `urlParts`
            ServletContext context = getServletContext();
            RMQChannelPool channelPool = (RMQChannelPool) context.getAttribute("channelPool");
            try {
                Channel channel = channelPool.borrowObject();
                channel.queueDeclare("mainQueue",false, false, false, null);
                String message = gson.toJson(skiRequest);

                channel.basicPublish("","mainQueue",null,message.getBytes());
                channelPool.returnObject(channel);
                response.setStatus(HttpServletResponse.SC_CREATED);
            } catch (IOException e) {

                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static String getBody(HttpServletRequest request) throws IOException {

        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }

        body = stringBuilder.toString();
        return body;
    }

}
