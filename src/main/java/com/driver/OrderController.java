package com.driver;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("orders")
public class OrderController {

    Map<String,Order> Store_order=new HashMap<>();
    Map<String,DeliveryPartner> Store_Deliver=new HashMap<>();
    Map<String,List<String>> parter_order_map=new HashMap<>();

    HashMap<String,Integer> order_time_map = new HashMap<>();
    @PostMapping("/add-order")
    public ResponseEntity<String> addOrder(@RequestBody Order order){
                Store_order.put(order.getId(),order);
                List<Integer> time=new ArrayList<>();
                time.add(order.getDeliveryTime());
                Collections.sort(time);

                order_time_map.put(order.getId(), order.getDeliveryTime());
        return new ResponseEntity<>("New order added successfully", HttpStatus.CREATED);
    }

    @PostMapping("/add-partner/{partnerId}")
    public ResponseEntity<String> addPartner(@PathVariable String partnerId){
          DeliveryPartner dv=new DeliveryPartner(partnerId);
          Store_Deliver.put(partnerId,dv);
        return new ResponseEntity<>("New delivery partner added successfully", HttpStatus.CREATED);
    }

    @PutMapping("/add-order-partner-pair")
    public ResponseEntity<String> addOrderPartnerPair(@RequestParam String orderId, @RequestParam String partnerId){
        if(parter_order_map.containsKey(partnerId)){
            parter_order_map.get(partnerId).add(orderId);
        }
        else{
            List<String> add_n=new ArrayList<>();
            add_n.add(orderId);
            parter_order_map.put(partnerId,add_n);
        }
        //This is basically assigning that order to that partnerId
        return new ResponseEntity<>("New order-partner pair added successfully", HttpStatus.CREATED);
    }

    @GetMapping("/get-order-by-id/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable String orderId){

        Order order= null;
        //order should be returned with an orderId.
        if(Store_order.containsKey(orderId)){
            order=Store_order.get(orderId);
        }
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/get-partner-by-id/{partnerId}")
    public ResponseEntity<DeliveryPartner> getPartnerById(@PathVariable String partnerId){

        DeliveryPartner deliveryPartner = null;
        if(Store_Deliver.containsKey(partnerId)){
            deliveryPartner=Store_Deliver.get(partnerId);
        }

        //deliveryPartner should contain the value given by partnerId

        return new ResponseEntity<>(deliveryPartner, HttpStatus.CREATED);
    }

    @GetMapping("/get-order-count-by-partner-id/{partnerId}")
    public ResponseEntity<Integer> getOrderCountByPartnerId(@PathVariable String partnerId){

        Integer orderCount = 0;
        if(parter_order_map.containsKey(partnerId)){
            orderCount=parter_order_map.get(partnerId).size();
        }
        //orderCount should denote the orders given by a partner-id

        return new ResponseEntity<>(orderCount, HttpStatus.CREATED);
    }

    @GetMapping("/get-orders-by-partner-id/{partnerId}")
    public ResponseEntity<List<String>> getOrdersByPartnerId(@PathVariable String partnerId){
        List<String> orders = null;
        if(parter_order_map.containsKey(partnerId)){
            orders=parter_order_map.get(partnerId);
        }
        //orders should contain a list of orders by PartnerId

        return new ResponseEntity<>(orders, HttpStatus.CREATED);
    }

    @GetMapping("/get-all-orders")
    public ResponseEntity<List<String>> getAllOrders(){
        List<String> orders = null;
        for(String key: Store_order.keySet())
        {
            orders.add(key);
        }
        //Get all orders
        return new ResponseEntity<>(orders, HttpStatus.CREATED);
    }

    @GetMapping("/get-count-of-unassigned-orders")
    public ResponseEntity<Integer> getCountOfUnassignedOrders(){
         Integer countOfOrders = 0;
         int count=0;
         for(String str:parter_order_map.keySet()){
              count+=parter_order_map.get(str).size();
         }
        countOfOrders = Store_order.size() - count;
        //Count of orders that have not been assigned to any DeliveryPartner

        return new ResponseEntity<>(countOfOrders, HttpStatus.CREATED);
    }

    @GetMapping("/get-count-of-orders-left-after-given-time/{partnerId}")
    public ResponseEntity<Integer> getOrdersLeftAfterGivenTimeByPartnerId(@PathVariable String time, @PathVariable String partnerId){

        Integer countOfOrders = 0;
        int a = Integer.valueOf(time.substring(0,3));
        int b = Integer.valueOf((time.substring(4,time.length())));
        int t = (a*60) + b;

        List<String> list= parter_order_map.get(partnerId);

        for(String str:list){
            if(t<order_time_map.get(str)){
                countOfOrders++;
            }
        }
        //countOfOrders that are left after a particular time of a DeliveryPartner

        return new ResponseEntity<>(countOfOrders, HttpStatus.CREATED);
    }

    @GetMapping("/get-last-delivery-time/{partnerId}")
    public ResponseEntity<String> getLastDeliveryTimeByPartnerId(@PathVariable String partnerId){
        String time = null;
        int max_time = 0;
        List<String> list = parter_order_map.get(partnerId);

        for(String str : list)
        {
            if(max_time < order_time_map.get(str))
            {
                max_time = order_time_map.get(str);
            }
        }

        int hour = max_time/60;
        int m = max_time%60;
        time = Integer.toString(hour) + Integer.toString(m);
        //Return the time when that partnerId will deliver his last delivery order.

        return new ResponseEntity<>(time, HttpStatus.CREATED);
    }

    @DeleteMapping("/delete-partner-by-id/{partnerId}")
    public ResponseEntity<String> deletePartnerById(@PathVariable String partnerId){
        parter_order_map.remove(partnerId);
        Store_Deliver.remove(partnerId);
        //Delete the partnerId
        //And push all his assigned orders to unassigned orders.

        return new ResponseEntity<>(partnerId + " removed successfully", HttpStatus.CREATED);
    }

    @DeleteMapping("/delete-order-by-id/{orderId}")
    public ResponseEntity<String> deleteOrderById(@PathVariable String orderId){

        Store_order.remove(orderId);
        for(String key: parter_order_map.keySet()){
            List<String> list=parter_order_map.get(key);
            for(String str:list){
                if(str.equals(orderId)){
                    list.remove(orderId);
                    parter_order_map.remove(key);
                    parter_order_map.put(key,list);
                }
            }
        }
        //Delete an order and also
        // remove it from the assigned order of that partnerId

        return new ResponseEntity<>(orderId + " removed successfully", HttpStatus.CREATED);
    }
}
