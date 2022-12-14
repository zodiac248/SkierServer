import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class MyContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("35.166.230.31");
        connectionFactory.setPort(5672);
        connectionFactory.setPassword("admin");
        connectionFactory.setUsername("admin");
        try {
            Connection connection = connectionFactory.newConnection();
            RMQChannelFactory rmqChannelFactory = new RMQChannelFactory(connection);
            RMQChannelPool rmqChannelPool = new RMQChannelPool(128,rmqChannelFactory);
            context.setAttribute("channelPool", rmqChannelPool);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }
}
