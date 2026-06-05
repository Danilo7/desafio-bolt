package com.bolt.clientes.controller;

import com.bolt.clientes.client.ViaCepClient;
import com.bolt.clientes.client.ViaCepResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de INTEGRAÇÃO — sobem o contexto Spring inteiro (controller + service
 * + JPA + banco H2 em memória) e exercitam a API de ponta a ponta via HTTP
 * simulado (MockMvc). Só o ViaCEP é mockado, para não depender de rede externa.
 *
 *  @SpringBootTest       -> carrega toda a aplicação para o teste.
 *  @AutoConfigureMockMvc -> habilita o MockMvc (faz requisições HTTP simuladas).
 *  @MockBean             -> substitui o ViaCepClient real por um dublê no contexto.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // faz rollback ao fim de cada teste -> banco limpo e testes isolados
class ClienteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // converte objeto <-> JSON

    @MockBean
    private ViaCepClient viaCepClient;

    /** Monta o corpo JSON de um cliente com determinado documento e instalação. */
    private String clienteJson(String documento, String numeroInstalacao) throws Exception {
        Map<String, Object> endereco = Map.of("cep", "30130-010", "numero", "100");
        Map<String, Object> unidade = Map.of(
                "nome", "Minha casa",
                "numeroInstalacao", numeroInstalacao,
                "endereco", endereco
        );
        Map<String, Object> cliente = Map.of(
                "nome", "Fulano de Tal",
                "documento", documento,
                "endereco", endereco,
                "unidadesConsumidoras", new Object[]{unidade}
        );
        return objectMapper.writeValueAsString(cliente);
    }

    private void mockViaCep(String uf) {
        when(viaCepClient.buscarEndereco(any())).thenReturn(
                new ViaCepResponse("30130-010", "Rua dos Carijós", "Centro",
                        "Belo Horizonte", uf, null));
    }

    @Test
    void deveCadastrarEConsultarCliente() throws Exception {
        mockViaCep("MG");

        // POST -> 201 Created
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson("111", "INST-1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.documento").value("111"))
                .andExpect(jsonPath("$.endereco.cidade").value("Belo Horizonte"))
                .andExpect(jsonPath("$.ativo").value(true));

        // GET lista -> 200 e contém o cliente
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documento").value("111"));
    }

    @Test
    void deveRetornar409ParaDocumentoDuplicado() throws Exception {
        mockViaCep("MG");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson("222", "INST-2")))
                .andExpect(status().isCreated());

        // Segundo cadastro com mesmo documento -> 409 Conflict
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson("222", "INST-3")))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar422ParaEstadoNaoAtendido() throws Exception {
        mockViaCep("SP"); // estado bloqueado

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson("333", "INST-4")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deveRetornar404ParaClienteInexistente() throws Exception {
        mockMvc.perform(get("/api/clientes/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRealizarRemocaoLogica() throws Exception {
        mockViaCep("MG");

        // Cadastra e captura o id retornado.
        String body = mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson("444", "INST-5")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer id = objectMapper.readTree(body).get("id").asInt();

        // DELETE -> 204 No Content
        mockMvc.perform(delete("/api/clientes/" + id))
                .andExpect(status().isNoContent());

        // Após soft delete, o GET por id não encontra (404).
        mockMvc.perform(get("/api/clientes/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400ParaPayloadInvalido() throws Exception {
        // Sem nome/documento -> falha de validação @Valid -> 400
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
