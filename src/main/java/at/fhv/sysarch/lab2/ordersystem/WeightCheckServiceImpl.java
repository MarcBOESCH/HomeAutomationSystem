package at.fhv.sysarch.lab2.ordersystem;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderRequest;
import at.fhv.sysarch.lab2.homeautomation.grpc.ProductWeightReply;
import at.fhv.sysarch.lab2.homeautomation.grpc.weightCheckService;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class WeightCheckServiceImpl implements weightCheckService {
    ActorRef<WeightCheckProcessor.WeightCommand> weightCheckProcessor;
    ActorSystem<?> system;

    public WeightCheckServiceImpl(ActorRef<WeightCheckProcessor.WeightCommand> weightCheckProcessor, ActorSystem<?> system){
        this.weightCheckProcessor = weightCheckProcessor;
        this.system = system;
    }

    @Override
    public CompletionStage<ProductWeightReply> order(OrderRequest in) {
        return AskPattern.ask(
                weightCheckProcessor,
                replyTo -> new WeightCheckProcessor.WeightCheck(in, replyTo),
                Duration.ofSeconds(3),
                system.scheduler()
        );
    }




}
