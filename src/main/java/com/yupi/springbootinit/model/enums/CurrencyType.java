package com.yupi.springbootinit.model.enums;

public enum CurrencyType {
    GEM("宝石"),    // 宝石
    ORE("矿石");     // 矿石
    
    private final String displayName;
    
    CurrencyType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}