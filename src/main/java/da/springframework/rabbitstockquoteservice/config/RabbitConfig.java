package da.springframework.rabbitstockquoteservice.config;

import com.rabbitmq.client.Connection;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.*;

import javax.annotation.PreDestroy;

@Configuration
public class RabbitConfig {

    public static final String QUEUE = "quotes";

    Mono<Connection> connectionMono;

    @Bean
    Mono<Connection> connectionMono(CachingConnectionFactory cachingConnectionFactory){
        return Mono.fromCallable(() -> cachingConnectionFactory.getRabbitConnectionFactory().newConnection());
    }

    @PreDestroy // Before the shutting down, close de connection factory
    public void close() throws Exception {
        connectionMono.block().close();
    }

    @Bean
    Sender sender(Mono<Connection> mono) {
        // Sender use the cache connection for efficiency
        return RabbitFlux.createSender(new SenderOptions().connectionMono(mono));
    }

    @Bean
    Receiver receiver(Mono<Connection> mono) {
        // Receiver use the cache connection for efficiency
        return RabbitFlux.createReceiver(new ReceiverOptions().connectionMono(mono));
    }
}
