package com.bank.ledger.infrastructure.adapter.in.web;

import com.bank.ledger.application.port.in.TransferCommand;
import com.bank.ledger.application.port.in.TransferMoneyUseCase;
import com.bank.ledger.application.port.in.TransferResult;
import com.bank.ledger.domain.model.Money;
import com.bank.ledger.infrastructure.adapter.in.web.dto.TransferRequest;
import com.bank.ledger.infrastructure.adapter.in.web.dto.TransferResponse;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/transfers")
public class TransferController
{
    private final TransferMoneyUseCase useCase;

    public TransferController(TransferMoneyUseCase useCase)
    {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> executeTransfer(@Valid @RequestBody TransferRequest request)
    {
        TransferCommand command = new TransferCommand(
                request.referenceId(),
                request.sourceAccountId(),
                request.targetAccountId(),
                Money.of(request.amount().toString(), request.currency())
        );

        TransferResult result = useCase.transfer(command);

        TransferResponse response = new TransferResponse(
                result.transactionId(),
                result.referenceId(),
                result.sourceAccountId(),
                result.targetAccountId(),
                result.amount().amount(),
                result.amount().currency().getCurrencyCode(),
                result.status(),
                result.timestamp()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
