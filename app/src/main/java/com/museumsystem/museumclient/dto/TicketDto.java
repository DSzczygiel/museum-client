package com.museumsystem.museumclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class TicketDto implements Serializable {
    Long id;
    String customerEmail;
    String status;
    String date;
    String orderDate;
    int adultsNr;
    int childrenNr;
    double price;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    @JsonProperty("customer_email")
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public int getAdultsNr() {
        return adultsNr;
    }

    public void setAdultsNr(int adultsNr) {
        this.adultsNr = adultsNr;
    }

    public int getChildrenNr() {
        return childrenNr;
    }

    public void setChildrenNr(int childrenNr) {
        this.childrenNr = childrenNr;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "TicketDto{" +
                "id=" + id +
                ", customerEmail='" + customerEmail + '\'' +
                ", status='" + status + '\'' +
                ", date='" + date + '\'' +
                ", orderDate='" + orderDate + '\'' +
                ", adultsNr=" + adultsNr +
                ", childrenNr=" + childrenNr +
                ", price=" + price +
                '}';
    }
}
