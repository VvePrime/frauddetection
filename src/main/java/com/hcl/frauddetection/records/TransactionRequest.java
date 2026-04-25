package com.hcl.frauddetection.records;

public record TransactionRequest(String transactionId,
                                 Long userId,
                                 Double amount,
                                 Double latitude,
                                 Double longitude) {

}
