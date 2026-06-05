package com.bolt.clientes.controller;

import com.bolt.clientes.dto.ClienteRequest;
import com.bolt.clientes.dto.ClienteResponse;
import com.bolt.clientes.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Camada de CONTROLLER (entrada HTTP da aplicação).
 *
 * Responsabilidade: receber requisições, validar o corpo (@Valid), delegar ao
 * service e devolver a resposta com o status HTTP correto. NÃO contém regra de
 * negócio — isso fica no ClienteService.
 *
 *  @RestController     -> os retornos viram JSON automaticamente.
 *  @RequestMapping     -> prefixo comum das rotas: /api/clientes.
 *  @Tag / @Operation   -> textos exibidos na documentação Swagger.
 */
@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Operações de cadastro e consulta de clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastrar um novo cliente")
    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = ClienteResponse.from(service.cadastrar(request));
        // 201 Created é a resposta correta para criação de recurso.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Atualizar um cliente existente")
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(ClienteResponse.from(service.atualizar(id, request)));
    }

    @Operation(summary = "Remover um cliente (remoção lógica)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        // 204 No Content: sucesso sem corpo de resposta.
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar todos os clientes ativos")
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarTodos() {
        List<ClienteResponse> lista = service.listarTodos().stream()
                .map(ClienteResponse::from)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @Operation(summary = "Listar os últimos 20 clientes em ordem decrescente")
    @GetMapping("/ultimos")
    public ResponseEntity<List<ClienteResponse>> listarUltimos() {
        List<ClienteResponse> lista = service.listarUltimos20().stream()
                .map(ClienteResponse::from)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @Operation(summary = "Obter um cliente pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ClienteResponse.from(service.buscarPorId(id)));
    }
}
