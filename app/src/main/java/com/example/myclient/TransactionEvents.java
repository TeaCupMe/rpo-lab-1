package com.example.myclient;

interface TransactionEvents {
    String enterPin(int ptc, String amount);
    void transactionResult(boolean result);
}
