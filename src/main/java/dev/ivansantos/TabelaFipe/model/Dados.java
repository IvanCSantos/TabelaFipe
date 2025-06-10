package dev.ivansantos.TabelaFipe.model;

import dev.ivansantos.TabelaFipe.service.Nomeavel;

public record Dados(String codigo, String nome) implements Nomeavel {

  @Override
  public String getNome() {
    return nome;
  }

  public String getCodigo() {
    return codigo;
  }

}
