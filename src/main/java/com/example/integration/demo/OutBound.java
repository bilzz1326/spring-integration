package com.example.integration.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.jms.Jms;
import javax.jms.ConnectionFactory;

import static java.util.stream.Collectors.toList;

@Configuration
@ImportResource("classpath:/xmls/queue.xml")
public class OutBound {

	@Autowired
	private ConnectionFactory connectionFactory;

	@Bean
	public IntegrationFlow toOutboundQueueFlow() {
		return IntegrationFlows.from("requestChannel")
				.split(s -> s.applySequence(true).get().getT2().setDelimiters("\\s"))
				.handle(Jms.outboundGateway(connectionFactory)
						.requestDestination("amq.outbound"))
				.resequence()
				.aggregate(aggregate ->
						aggregate.outputProcessor(g ->
								Joiner.on(" ").join(g.getMessages()
										.stream()
										.map(m -> (String) m.getPayload()).collect(toList())))
						, null)
				.get();
	}
}
