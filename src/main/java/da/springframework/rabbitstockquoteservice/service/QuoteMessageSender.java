package da.springframework.rabbitstockquoteservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import da.springframework.rabbitstockquoteservice.config.RabbitConfig;
import da.springframework.rabbitstockquoteservice.model.Quote;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.OutboundMessageResult;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Sender;

@Slf4j
@RequiredArgsConstructor
@Component
public class QuoteMessageSender {

    private final ObjectMapper objectMapper;
    private final Sender sender;

    @SneakyThrows
    public Mono<Void> sendQuoteMessage(Quote quote) {

        // convert the quote object to a JSON payload
        byte[] jsonBytes = objectMapper.writeValueAsBytes(quote);

        Flux<OutboundMessageResult> confirmations = sender.sendWithPublishConfirms(Flux.just(new OutboundMessage("", RabbitConfig.QUEUE, jsonBytes)));

        sender.declareQueue(QueueSpecification.queue(RabbitConfig.QUEUE))
                .thenMany(confirmations)
                .doOnError(throwable -> log.error("Send failed", throwable))
                .subscribe(outboundMessageResult -> {
                    if (outboundMessageResult.isAck()) {
                        log.info("Message sent successfully {}", new String(outboundMessageResult.getOutboundMessage().getBody()));
                    }
                });

        return Mono.empty();
    }
}
