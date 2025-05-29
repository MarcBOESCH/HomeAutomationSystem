package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.grpc.OrderReply;
import at.fhv.sysarch.lab2.homeautomation.order.OrderExecutor;
import at.fhv.sysarch.lab2.homeautomation.order.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SmartFridge extends AbstractBehavior<SmartFridge.FridgeCommand> {
    public interface FridgeCommand {}
    private final List<Product> inventory = new ArrayList<>();
    private final List<Product> orderHistory = new ArrayList<>();
    private final ActorRef<WeightSensor.WeightCommand> weightSensor;
    private final ActorRef<SpaceSensor.SpaceCommand> spaceSensor;
    private final HashMap<String, Integer> automaticOrders = new HashMap<>();

    /// Messages:

    public static final class FridgeOrder implements FridgeCommand {
        private final String product;
        private final int amount;
        public FridgeOrder(String product, int amount) {
            this.product = product;
            this.amount = amount;
        }
    }
    public static final class Consume implements FridgeCommand {
        private final String product;
        public Consume(String product){
            this.product = product;
        }
    }

    public static final class OrderSuccessful implements FridgeCommand {
        private final OrderReply orderReply;
        private final String productName;
        public OrderSuccessful(OrderReply orderReply, String productName){
            this.orderReply = orderReply;
            this.productName = productName;
        }
    }

    public static final class OrderUnsuccessful implements FridgeCommand {
        private final String reason;
        public OrderUnsuccessful(String reason){
            this.reason = reason;
        }
    }

    public static final class QueryFridgeContent implements FridgeCommand{
        public QueryFridgeContent(){}
    }

    public static final class QueryOrderHistory implements FridgeCommand{
        public QueryOrderHistory(){}
    }

    public static final class SetAutomaticOrder implements FridgeCommand{
        private final String productToOrder;
        private final int amount;
        public SetAutomaticOrder(String productToOrder, int amount){
            this.productToOrder = productToOrder;
            this.amount = amount;
        }
    }
    public static final class CheckForAutomaticOrder implements FridgeCommand{
        private final String productName;
        public CheckForAutomaticOrder(String productName){
            this.productName = productName;
        }
    }

    public static Behavior<FridgeCommand> create(){return Behaviors.setup(SmartFridge::new);}

    private SmartFridge(ActorContext<FridgeCommand> context){
        super(context);
        getContext().getLog().info("SmartFridge started");
        this.weightSensor = getContext().spawn(WeightSensor.create(), "WeightSensor");
        this.spaceSensor = getContext().spawn(SpaceSensor.create(), "SpaceSensor");
    }

    /// Behaviors:

    private Behavior<FridgeCommand> onFridgeOrder(FridgeOrder message){
        ActorRef<OrderExecutor.OrderCommand> orderExecutor = getContext().spawn(OrderExecutor.create(message.product,
                 weightSensor,
                spaceSensor,
                getContext().getSelf(),
                message.amount),
                "OrderExecutor" + message.product);

        return this;
    }

    private Behavior<FridgeCommand> onOrderSuccessful(OrderSuccessful order) {
        int amountOrdered = order.orderReply.getAmount();
        Product newProduct = new Product(order.productName, order.orderReply.getPrice(), order.orderReply.getWeight());

        while (amountOrdered > 0) {
            this.inventory.add(newProduct);
            this.orderHistory.add(newProduct);
            amountOrdered--;
        }
        return this;
    }

    private Behavior<FridgeCommand> onConsume(Consume consume){
        for(Product product : inventory){
            if (product.getName().equals(consume.product)){
                inventory.remove(product);
                getContext().getLog().info("{} has been consumed", product.getName());
                break;
            }
        }
        return this;
    }

    private Behavior<FridgeCommand> onQueryFridgeContent(QueryFridgeContent queryFridgeContent){
        StringBuilder sb = new StringBuilder();
        for(Product product : inventory){
            sb.append(product.getName() + ", ");
        }
        getContext().getLog().info(sb.toString());

        return this;
    }

    private Behavior<FridgeCommand> onQueryOrderHistory(QueryOrderHistory queryOrderHistory){
        StringBuilder sb = new StringBuilder();
        int counter = 1;
        for(Product product : orderHistory){
            sb.append(counter + " Product: " + product.getName()+ ", Price: " + product.getPrice() + ";\n ");
            counter++;
        }
        getContext().getLog().info(sb.toString());
        return this;
    }

    private Behavior<FridgeCommand> onSetAutomaticOrder(SetAutomaticOrder setAutomaticOrder){
        this.automaticOrders.put(setAutomaticOrder.productToOrder, setAutomaticOrder.amount);
        getContext().getSelf().tell(new CheckForAutomaticOrder(setAutomaticOrder.productToOrder));
        return this;
    }

    private Behavior<FridgeCommand> onCheckForAutomaticOrder(CheckForAutomaticOrder product){
        if(!automaticOrders.isEmpty()){
            boolean checkIfEmpty = true;
            if(automaticOrders.containsKey(product.productName)){
                for(Product item : inventory){
                    if(item.getName().equals(product.productName)){
                        checkIfEmpty = false;
                        break;
                    }
                }
            }
            if(checkIfEmpty){
                getContext().getSelf().tell(new FridgeOrder(product.productName, automaticOrders.get(product.productName)));
            }
        }
        return this;
    }

    private Behavior<FridgeCommand> onOrderUnsuccessful(OrderUnsuccessful failure){

        getContext().getLog().error(failure.reason);
        return this;
    }

    @Override
    public Receive<FridgeCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(FridgeOrder.class, this::onFridgeOrder)
                .onMessage(OrderSuccessful.class, this::onOrderSuccessful)
                .onMessage(Consume.class, this::onConsume)
                .onMessage(QueryFridgeContent.class, this::onQueryFridgeContent)
                .onMessage(QueryOrderHistory.class, this::onQueryOrderHistory)
                .onMessage(SetAutomaticOrder.class, this::onSetAutomaticOrder)
                .onMessage(CheckForAutomaticOrder.class, this::onCheckForAutomaticOrder)
                .onMessage(OrderUnsuccessful.class, this::onOrderUnsuccessful)
                .build();

    }
}
