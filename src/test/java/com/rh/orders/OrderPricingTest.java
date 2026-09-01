package com.rh.orders;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class OrderPricingTest {

    @Test
    void noDiscounts() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "customerId": "C1",
                  "loyaltyTier": "NONE",
                  "items": [{"productId": "P1", "quantity": 1, "unitPrice": 50.00}]
                }
                """)
            .when().post("/orders/price")
            .then()
            .statusCode(200)
            .body("subtotal", is(50.00f),
                  "loyaltyDiscount", is(0.00f),
                  "volumeDiscount", is(0.00f),
                  "promoDiscount", is(0.00f),
                  "totalDiscount", is(0.00f),
                  "finalPrice", is(50.00f));
    }

    @Test
    void goldLoyaltyWithVolumeDiscount() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "customerId": "C2",
                  "loyaltyTier": "GOLD",
                  "items": [{"productId": "P1", "quantity": 10, "unitPrice": 60.00}]
                }
                """)
            .when().post("/orders/price")
            .then()
            .statusCode(200)
            .body("subtotal", is(600.00f),
                  "loyaltyDiscount", is(60.00f),
                  "volumeDiscount", is(30.00f),
                  "finalPrice", is(510.00f));
    }

    @Test
    void promoCodeSave10() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "customerId": "C3",
                  "loyaltyTier": "NONE",
                  "items": [{"productId": "P1", "quantity": 2, "unitPrice": 30.00}],
                  "promoCode": "SAVE10"
                }
                """)
            .when().post("/orders/price")
            .then()
            .statusCode(200)
            .body("subtotal", is(60.00f),
                  "promoDiscount", is(6.00f),
                  "finalPrice", is(54.00f));
    }

    @Test
    void promoCodeFlat20() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "customerId": "C4",
                  "loyaltyTier": "SILVER",
                  "items": [{"productId": "P1", "quantity": 1, "unitPrice": 80.00}],
                  "promoCode": "FLAT20"
                }
                """)
            .when().post("/orders/price")
            .then()
            .statusCode(200)
            .body("subtotal", is(80.00f),
                  "loyaltyDiscount", is(4.00f),
                  "promoDiscount", is(20.00f),
                  "finalPrice", is(56.00f));
    }

    @Test
    void promoCodeSummer25BelowMinimum() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "customerId": "C5",
                  "loyaltyTier": "NONE",
                  "items": [{"productId": "P1", "quantity": 1, "unitPrice": 30.00}],
                  "promoCode": "SUMMER25"
                }
                """)
            .when().post("/orders/price")
            .then()
            .statusCode(200)
            .body("promoDiscount", is(0.00f),
                  "finalPrice", is(30.00f));
    }

    @Test
    void platinumWithHighVolume() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "customerId": "C6",
                  "loyaltyTier": "PLATINUM",
                  "items": [{"productId": "P1", "quantity": 5, "unitPrice": 250.00}]
                }
                """)
            .when().post("/orders/price")
            .then()
            .statusCode(200)
            .body("subtotal", is(1250.00f),
                  "loyaltyDiscount", is(187.50f),
                  "volumeDiscount", is(100.00f),
                  "totalDiscount", is(287.50f),
                  "finalPrice", is(962.50f));
    }

    @Test
    void allDiscountsStacked() {
        // PLATINUM(15%) + FLAT20 on $80 subtotal
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "customerId": "C7",
                  "loyaltyTier": "PLATINUM",
                  "items": [{"productId": "P1", "quantity": 1, "unitPrice": 80.00}],
                  "promoCode": "FLAT20"
                }
                """)
            .when().post("/orders/price")
            .then()
            .statusCode(200)
            .body("subtotal", is(80.00f),
                  "loyaltyDiscount", is(12.00f),
                  "volumeDiscount", is(0.00f),
                  "promoDiscount", is(20.00f),
                  "totalDiscount", is(32.00f),
                  "finalPrice", is(48.00f));
    }

    @Test
    void validationRejectsEmptyItems() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "customerId": "C8",
                  "loyaltyTier": "NONE",
                  "items": []
                }
                """)
            .when().post("/orders/price")
            .then()
            .statusCode(400);
    }

    @Test
    void validationRejectsMissingCustomerId() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "loyaltyTier": "NONE",
                  "items": [{"productId": "P1", "quantity": 1, "unitPrice": 10.00}]
                }
                """)
            .when().post("/orders/price")
            .then()
            .statusCode(400);
    }

    @Test
    void unknownPromoCodeIgnored() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "customerId": "C9",
                  "loyaltyTier": "NONE",
                  "items": [{"productId": "P1", "quantity": 1, "unitPrice": 100.00}],
                  "promoCode": "INVALID"
                }
                """)
            .when().post("/orders/price")
            .then()
            .statusCode(200)
            .body("promoDiscount", is(0.00f),
                  "volumeDiscount", is(3.00f),
                  "finalPrice", is(97.00f));
    }
}
