package com.bank.ledger.infrastructure.adapter.in.web;

import com.bank.ledger.application.port.in.TransferCommand;
import com.bank.ledger.application.port.in.TransferMoneyUseCase;
import com.bank.ledger.application.port.in.TransferResult;
import com.bank.ledger.domain.model.Money;
import com.bank.ledger.infrastructure.adapter.in.web.dto.TransferRequest;
import com.bank.ledger.infrastructure.adapter.in.web.dto.TransferResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/transfers")
@Tag(name = "Transfers", description = "Endpoints for executing ledger transaction transfers")
public class TransferController
{
    private final TransferMoneyUseCase useCase;

    public TransferController(TransferMoneyUseCase useCase)
    {
        this.useCase = useCase;
    }

    @PostMapping
    @Operation(summary = "Execute money transfer", description = "Perforns a double-entry debit and credit transfer between two accounts with pessimistic locking and idempotency guarantees.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transfer completed successfully",
                    content = @Content(schema = @Schema(implementation = TransferResponse.class))),
            @ApiResponse(responseCode = "422", description = "Insufficient funds in source account",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Source or target account not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or currency mismatch",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
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
