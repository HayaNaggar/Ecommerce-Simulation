import java.time.LocalDate;
import java.util.*;

// Shipping interface for items that need shipping
interface Shippable {
    String getName();
    double getWeight(); //as  required in the pdf
}

// Base Product class
abstract class Product {
    private String name;
    private double price;
    private int quantity;
    
    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
    
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public boolean isAvailable(int requestedQuantity) {
        return quantity >= requestedQuantity;
    }
    
    public void reduceQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
        }
    }
    // lazem tb2a inherited
    public abstract boolean isExpired();
    public abstract boolean requiresShipping();
}

// Products that can expire
abstract class CanExpireProduct extends Product {
    private LocalDate expiryDate;
    
    public CanExpireProduct(String name, double price, int quantity, LocalDate expiryDate) {
        super(name, price, quantity);
        this.expiryDate = expiryDate;
    }
    
    @Override
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }
    
    public LocalDate getExpiryDate() { return expiryDate; }
}

// Products that cannot expire
abstract class CannotExpireProduct extends Product {
    public CannotExpireProduct(String name, double price, int quantity) {
        super(name, price, quantity);
    }
    
    @Override
    public boolean isExpired() {
        return false;
    }
}

// Specific product implementations
//shippable w can expire
class Cheese extends CanExpireProduct implements Shippable {
    private double weight;
    
    public Cheese(String name, double price, int quantity, LocalDate expiryDate, double weight) {
        super(name, price, quantity, expiryDate);
        this.weight = weight;
    }
    
    @Override
    public boolean requiresShipping() { return true; }
    
    @Override
    public double getWeight() { return weight; }
}

class Biscuits extends CanExpireProduct implements Shippable {
    private double weight;
    
    public Biscuits(String name, double price, int quantity, LocalDate expiryDate, double weight) {
        super(name, price, quantity, expiryDate);
        this.weight = weight;
    }
    
    @Override
    public boolean requiresShipping() { return true; }
    
    @Override
    public double getWeight() { return weight; }
}
//shippable w cannot expire
class TV extends CannotExpireProduct implements Shippable {
    private double weight;
    
    public TV(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }
    
    @Override
    public boolean requiresShipping() { return true; }
    
    @Override
    public double getWeight() { return weight; }
}

class Mobile extends CannotExpireProduct implements Shippable {
    private double weight;
    
    public Mobile(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }
    
    @Override
    public boolean requiresShipping() { return true; }
    
    @Override
    public double getWeight() { return weight; }
}
//not shippable w cannot expire
class ScratchCard extends CannotExpireProduct {
    public ScratchCard(String name, double price, int quantity) {
        super(name, price, quantity);
    }
    
    @Override
    public boolean requiresShipping() { return false; }
}

// Cart item to store product and quantity
class CartItem {
    private Product product;
    private int quantity;
    
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
    
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    
    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }
}

// Customer class
class Customer {
    private String name;
    private double balance;
    
    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }
    
    public String getName() { return name; }
    public double getBalance() { return balance; }
    
    public void deductBalance(double amount) {
        balance -= amount;
    }
    
    public boolean hasEnoughBalance(double amount) {
        return balance >= amount;
    }
}

// Shopping cart
class Cart {
    private List<CartItem> items;
    
    public Cart() {
        this.items = new ArrayList<>();
    }
    //TODO azabat el validations
    public void add(Product product, int quantity) throws Exception {
        if (product.isExpired()) {
            throw new Exception("Product selected is expired: " + product.getName());
        }
        
        if (!product.isAvailable(quantity)) {
            throw new Exception("Not enough stock for product: " + product.getName() + 
          ". Available: " + product.getQuantity() + ", Requested: " + quantity);
        }
        
        // Check if product already exists in cart
        for (CartItem item : items) {
            if (item.getProduct().getName().equals(product.getName())) {
                int totalQuantity = item.getQuantity() + quantity;
                if (!product.isAvailable(totalQuantity)) {
                    throw new Exception("Not enough stock for product: " + product.getName() + 
                  ". Available: " + product.getQuantity() + ", Total requested: " + totalQuantity);
                }
                items.remove(item);
                items.add(new CartItem(product, totalQuantity));
                return;
            }
        }
        
        items.add(new CartItem(product, quantity));
    }
    
    public List<CartItem> getItems() { return items; }
    
    public boolean isEmpty() { return items.isEmpty(); }
    
    public double calculateSubtotal() {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }
    
    public void clear() { items.clear(); }
}

// Shipping service
class ShippingService {
    private static final double SHIPPING_RATE_PER_KG = 10.0;
    private static final double BASE_SHIPPING_FEE = 20.0;
    
    public static double calculateShippingFee(List<Shippable> shippableItems) {
        if (shippableItems.isEmpty()) {
            return 0.0;
        }
        
        double totalWeight = shippableItems.stream()
                                          .mapToDouble(Shippable::getWeight)
                                          .sum();
        
        return BASE_SHIPPING_FEE + (totalWeight * SHIPPING_RATE_PER_KG);
    }
    
    public static void processShipment(List<Shippable> shippableItems) {
        if (shippableItems.isEmpty()) {
            return;
        }
        //console output
        System.out.println("** Shipment notice **");
        
        // Group items by name for display
        Map<String, Integer> itemCounts = new HashMap<>();
        Map<String, Double> itemWeights = new HashMap<>();
        
        for (Shippable item : shippableItems) {
            String name = item.getName();
            itemCounts.put(name, itemCounts.getOrDefault(name, 0) + 1);
            itemWeights.put(name, item.getWeight());
        }
        
        double totalWeight = 0;
        for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
            String name = entry.getKey();
            int count = entry.getValue();
            double weight = itemWeights.get(name) * count;
            totalWeight += weight;
            
            System.out.printf("%dx %s %.0fg%n", count, name, weight * 1000);
        }
        
        System.out.printf("Total package weight %.1fkg%n", totalWeight);
    }
}

// Main e-commerce system
class ECommerce {
    
    public static void checkout(Customer customer, Cart cart) {
        try {
            if (cart.isEmpty()) {
                throw new Exception("Cart is empty");
            }
            
            for (CartItem item : cart.getItems()) {
                Product product = item.getProduct();
                
                if (product.isExpired()) {
                    throw new Exception("Product expired: " + product.getName());
                }
                
                if (!product.isAvailable(item.getQuantity())) {
                    throw new Exception("Product out of stock: " + product.getName());
                }
            }
            //making surre cart msh empty w mafesh 7aga expired w fe quantity kefaya
            double subtotal = cart.calculateSubtotal();
            //bagama3 el shippables 
            List<Shippable> shippableItems = new ArrayList<>();
            for (CartItem item : cart.getItems()) {
                if (item.getProduct().requiresShipping()) {
                    for (int i = 0; i < item.getQuantity(); i++) {
                        shippableItems.add((Shippable) item.getProduct());
                    }
                }
            }
            
            double shippingFee = ShippingService.calculateShippingFee(shippableItems);
            double totalAmount = subtotal + shippingFee;
            //at2aked en fe enough balance

            if (!customer.hasEnoughBalance(totalAmount)) {
                throw new Exception("Insufficient balance. Required: " + totalAmount + 
                                  ", Available: " + customer.getBalance());
            }
            //b display details el checkout
            ShippingService.processShipment(shippableItems);
            //deduct balance and reduce product quantities
            customer.deductBalance(totalAmount);
            
            for (CartItem item : cart.getItems()) {
                item.getProduct().reduceQuantity(item.getQuantity());
            }
            
            System.out.println("** Checkout receipt **");
            for (CartItem item : cart.getItems()) {
                System.out.printf("%dx %s %.0f%n", item.getQuantity(), 
                                item.getProduct().getName(), item.getTotalPrice());
            }
            System.out.println("----------------------");
            System.out.printf("Subtotal %.0f%n", subtotal);
            System.out.printf("Shipping %.0f%n", shippingFee);
            System.out.printf("Amount %.0f%n", totalAmount);
            System.out.printf("Customer balance after payment: %.0f%n", customer.getBalance());
            
            cart.clear();
            
        } catch (Exception e) {
            System.err.println("Checkout failed: " + e.getMessage());
        }
    }
}
//TODO TestCases
// class to test the system (el testcases)
class ECommerceDemo {
    
    public static void checkout(Customer customer, Cart cart) {
        ECommerce.checkout(customer, cart);
    }
    
    public static void main(String[] args) {
        Cheese cheese = new Cheese("Cheese", 100, 10, LocalDate.now().plusDays(5), 0.2);
        Biscuits biscuits = new Biscuits("Biscuits", 150, 8, LocalDate.now().plusDays(10), 0.7);
        TV tv = new TV("TV", 25000, 3, 15.0);
        Mobile mobile = new Mobile("Mobile", 15000, 5, 0.3);
        ScratchCard scratchCard = new ScratchCard("Scratch Card", 50, 20);
        
        Customer customer = new Customer("John Doe", 30000);
        
        System.out.println("=== Test Case 1: Successful Checkout ===");
        Cart cart1 = new Cart();
        try {
            cart1.add(cheese, 2);
            cart1.add(biscuits, 1);
            cart1.add(scratchCard, 1);
            checkout(customer, cart1);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        System.out.println("\n=== Test Case 2: Empty Cart ===");
        Cart cart2 = new Cart();
        checkout(customer, cart2);
        
        System.out.println("\n=== Test Case 3: Insufficient Balance ===");
        Customer poorCustomer = new Customer("Harry Brian", 100);
        Cart cart3 = new Cart();
        try {
            cart3.add(tv, 1);
            checkout(poorCustomer, cart3);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        System.out.println("\n=== Test Case 4: Out of Stock ===");
        Cart cart4 = new Cart();
        try {
            cart4.add(cheese, 15);
            checkout(customer, cart4);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        System.out.println("\n=== Test Case 5: Expired Product ===");
        Cheese expiredCheese = new Cheese("Expired Cheese", 100, 5, LocalDate.now().minusDays(1), 0.2);
        Cart cart5 = new Cart();
        try {
            cart5.add(expiredCheese, 1);
            checkout(customer, cart5);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        System.out.println("\n=== Test Case 6: Mixed Products with Shipping ===");
        Cart cart6 = new Cart();
        try {
            cart6.add(tv, 1);
            cart6.add(mobile, 2);
            cart6.add(scratchCard, 3);
            checkout(customer, cart6);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
