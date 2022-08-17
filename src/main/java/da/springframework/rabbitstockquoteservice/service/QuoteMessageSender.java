package da.springframework.rabbitstockquoteservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import da.springframework.rabbitstockquoteservice.config.RabbitConfig;
import da.springframework.rabbitstockquoteservice.model.Quote;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

@RequiredArgsConstructor
@Component
public class QuoteMessageSender {

    private final ObjectMapper objectMapper;
    private final Sender sender;

    @SneakyThrows
    public Mono<Void> sendQuoteMessage(Quote quote) {

        // convert the quote object to a JSON payload
        byte[] jsonBytes = objectMapper.writeValueAsBytes(quote);

        return sender.send(Mono.just(new OutboundMessage("", RabbitConfig.QUEUE, jsonBytes)));
    }
}
