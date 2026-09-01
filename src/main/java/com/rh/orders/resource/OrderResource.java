package com.rh.orders.resource;

import com.rh.orders.model.OrderRequest;
import com.rh.orders.model.OrderResponse;
import com.rh.orders.service.PricingService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    PricingService pricingService;

    @POST
    @Path("/price")
    public OrderResponse calculatePrice(@Valid OrderRequest request) {
        return pricingService.calculatePrice(request);
    }
}
