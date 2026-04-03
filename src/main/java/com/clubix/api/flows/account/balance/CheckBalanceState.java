package com.clubix.api.flows.account.balance;

public record CheckBalanceState(String target) {

    public static CheckBalanceState empty() {
        return new CheckBalanceState(null);
    }

    public boolean hasTarget() {
        return target != null && !target.isBlank();
    }

    public boolean complete() {
        return hasTarget();
    }

    public CheckBalanceState withTarget(String newTarget) {
        return new CheckBalanceState(newTarget);
    }
}
