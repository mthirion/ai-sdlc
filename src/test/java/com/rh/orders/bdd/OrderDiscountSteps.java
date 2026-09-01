package com.rh.orders.bdd;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;

public class OrderDiscountSteps {

    private List<Map<String, Object>> items;
    private String loyaltyTier;
    private String promoCode;
    private Response response;

    @Before
    public void setUp() {
        items = new ArrayList<>();
        loyaltyTier = "NONE";
        promoCode = null;
        response = null;
        io.restassured.RestAssured.port = Integer.getInteger("quarkus.http.test-port", 8083);
    }

    @Given("an order with the following items:")
    public void anOrderWithItems(DataTable dataTable) {
        for (Map<String, String> row : dataTable.asMaps()) {
            Map<String, Object> item = new HashMap<>();
            item.put("productId", row.get("productId"));
            item.put("quantity", Integer.parseInt(row.get("quantity")));
            item.put("unitPrice", new BigDecimal(row.get("unitPrice")));
            items.add(item);
        }
    }

    @Given("an order with no items")
    public void anOrderWithNoItems() {
        items.clear();
    }

    @And("the customer has {string} loyalty status")
    public void theCustomerHasLoyaltyStatus(String tier) {
        this.loyaltyTier = tier;
    }

    @And("no promo code is applied")
    public void noPromoCode() {
        this.promoCode = null;
    }

    @And("the promo code {string} is applied")
    public void promoCodeApplied(String code) {
        this.promoCode = code;
    }

    @And("today's date is after {string}")
    public void todaysDateIsAfter(String date) {
        // Precondition: requires a Clock injection in PricingService to test properly.
    }

    @When("I submit the order to calculate the price")
    public void submitOrder() {
        Map<String, Object> body = new HashMap<>();
        body.put("customerId", "TEST-CUST-001");
        body.put("loyaltyTier", loyaltyTier);
        body.put("items", items);
        if (promoCode != null) {
            body.put("promoCode", promoCode);
        }

        response = given()
                .contentType("application/json")
                .body(body)
                .when()
                .post("/orders/price");
    }

    @Then("the response status should be {int}")
    public void responseStatusShouldBe(int statusCode) {
        response.then().statusCode(statusCode);
    }

    @Then("the subtotal should be {double}")
    public void subtotalShouldBe(double expected) {
        response.then().body("subtotal",
                comparesEqualTo(new BigDecimal(String.valueOf(expected)).floatValue()));
    }

    @Then("the loyalty discount should be {double}")
    public void loyaltyDiscountShouldBe(double expected) {
        response.then().body("loyaltyDiscount",
                comparesEqualTo(new BigDecimal(String.valueOf(expected)).floatValue()));
    }

    @Then("the volume discount should be {double}")
    public void volumeDiscountShouldBe(double expected) {
        response.then().body("volumeDiscount",
                comparesEqualTo(new BigDecimal(String.valueOf(expected)).floatValue()));
    }

    @Then("the promo discount should be {double}")
    public void promoDiscountShouldBe(double expected) {
        response.then().body("promoDiscount",
                comparesEqualTo(new BigDecimal(String.valueOf(expected)).floatValue()));
    }

    @Then("the total discount should be {double}")
    public void totalDiscountShouldBe(double expected) {
        response.then().body("totalDiscount",
                comparesEqualTo(new BigDecimal(String.valueOf(expected)).floatValue()));
    }

    @Then("the final price should be {double}")
    public void finalPriceShouldBe(double expected) {
        response.then().body("finalPrice",
                comparesEqualTo(new BigDecimal(String.valueOf(expected)).floatValue()));
    }

    @Then("the error message should be {string}")
    public void errorMessageShouldBe(String message) {
        response.then().body("message", equalTo(message));
    }
}
