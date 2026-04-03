package com.clubix.api.flows.account.balance;

public record CheckBalanceState(String target, boolean awaitingConfirmation) {

    public static CheckBalanceState empty() {
        return new CheckBalanceState(null, false);
    }

    public boolean hasTarget() {
        return target != null && !target.isBlank();
    }

    public boolean complete() {
        return hasTarget();
    }

    public CheckBalanceState withTarget(String newTarget) {
        return new CheckBalanceState(newTarget, this.awaitingConfirmation);
    }

    public CheckBalanceState awaitingConfirmation(boolean value) {
        return new CheckBalanceState(this.target, value);
    }
}
