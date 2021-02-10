package com.javastart.transfer.action;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ActionManager {

    public static Boolean depositSucceed = false;

    public static Boolean paymentSucceed = false;

    public static Boolean getDepositSucceed() {
        return depositSucceed;
    }

    public static void setDepositSucceed(Boolean depositSucceed) {
        ActionManager.depositSucceed = depositSucceed;
    }

    public static Boolean getPaymentSucceed() {
        return paymentSucceed;
    }

    public static void setPaymentSucceed(Boolean paymentSucceed) {
        ActionManager.paymentSucceed = paymentSucceed;
    }
}
