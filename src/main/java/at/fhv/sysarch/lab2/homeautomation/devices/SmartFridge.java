package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.order.OrderExecutor;
import at.fhv.sysarch.lab2.homeautomation.order.Product;

import java.util.ArrayList;
import java.util.List;

public class SmartFridge extends AbstractBehavior<SmartFridge.FridgeCommand> {
    public interface FridgeCommand {}
    private final List<Product> inventory = new ArrayList<>();
    private final List<Product> orderHistory = new ArrayList<>();
    private final ActorRef<WeightSensor.WeightCommand> weightSensor;
    private final ActorRef<SpaceSensor.SpaceCommand> spaceSensor;

    /// Messages:

    public static final class FridgeOrder implements FridgeCommand {
        public final Product product;
        public FridgeOrder(Product product) {
            this.product = product;
        }
    }

    public static Behavior<FridgeCommand> create(){return Behaviors.setup(SmartFridge::new);}

    private SmartFridge(ActorContext<FridgeCommand> context){
        super(context);
        getContext().getLog().info("SmartFridge started");
        //TODO: Spawn WeightSensor and SpaceSensor.
        this.weightSensor = getContext().spawn(WeightSensor.create(), "WeightSensor");
        this.spaceSensor = getContext().spawn(SpaceSensor.create(), "SpaceSensor");
    }

    /// Behaviors:

    private Behavior<FridgeCommand> onFridgeOrder(FridgeOrder message){
        ActorRef<OrderExecutor.OrderCommand> orderExecutor = getContext().spawn(OrderExecutor.create(), "OrderExecutor" + message.product.getName());
        return this;
    }

    @Override
    public Receive<FridgeCommand> createReceive() {
        return null;
    }
}
