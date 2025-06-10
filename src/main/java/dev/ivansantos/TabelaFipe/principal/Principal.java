package dev.ivansantos.TabelaFipe.principal;

import dev.ivansantos.TabelaFipe.model.Dados;
import dev.ivansantos.TabelaFipe.model.Modelos;
import dev.ivansantos.TabelaFipe.service.ConsumoApi;
import dev.ivansantos.TabelaFipe.service.ConverteDados;
import dev.ivansantos.TabelaFipe.service.Nomeavel;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {
  private Scanner scanner = new Scanner(System.in);
  private ConsumoApi consumo = new ConsumoApi();
  private ConverteDados conversor = new ConverteDados();
  private enum Veiculo {
    CARRO(1,"carros"),
    MOTO(2, "motos"),
    CAMINHAO(3,"caminhoes");

    private final int id;
    private final String nome;

    Veiculo(int id, String nome) {
      this.id = id;
      this.nome = nome;
    }

    public String getNome() {
      return nome;
    }

    public int getId() {
      return id;
    }

    public static Veiculo fromId(int id) {
      for (Veiculo veiculo : Veiculo.values()) {
        if (veiculo.getId() == id) {
          return veiculo;
        }
      }
      throw new IllegalArgumentException("ID inválido: " + id);
    }

  }
  private final String URL_BASE = "https://parallelum.com.br/fipe/api/v1/";

  public void exibeMenu() {

    // Tipos de veiculos
    List<Veiculo> tiposVeiculo = List.of(Veiculo.values());
    System.out.println("*** OPÇÕES ***");
    tiposVeiculo.forEach(System.out::println);
    System.out.print("Informe uma opção: ");
    int opcao = scanner.nextInt();
    String endereco = URL_BASE + Veiculo.fromId(opcao).nome + "/marcas";

    // Marcas
    var jsonMarcas = consultaApi(endereco);
    var marcas = conversor.obterLista(jsonMarcas, Dados.class);
    imprimeOpcoesMenu(marcas);
    opcao = scanner.nextInt();
    endereco += "/" + opcao + "/modelos";


    // Modelos
    var jsonModelos = consultaApi(endereco);
    var modelos = conversor.obterDados(jsonModelos, Modelos.class);
    int finalOpcaoMarca = opcao;
    var marca = marcas.stream()
            .filter(dado -> dado.codigo().equals(String.valueOf(finalOpcaoMarca)))
            .collect(Collectors.toList()).get(0).getNome(); // Recupera nome da marca
    System.out.println("Você escolheu " + marca);
    System.out.println("Modelos da marca " + marca + ":");
    imprimeOpcoesMenu(modelos.modelos());
    opcao = scanner.nextInt();
    int finalOpcaoModelo = opcao;
    var modelo = modelos.modelos().stream()
            .filter(dado -> dado.codigo().equals((String.valueOf(finalOpcaoModelo))))
            .collect(Collectors.toList()).get(0).getNome();
    endereco += "/" + opcao + "/anos";

    // Anos
    System.out.println("Você escolheu " + modelo);
    var jsonAnos = consultaApi(endereco);
    var anos = conversor.obterLista(jsonAnos, Dados.class);
    System.out.println("Anos do modelo" + modelo);
    imprimeOpcoesMenu(anos);
    opcao = scanner.nextInt();
    endereco += "/" + opcao;

    var jsonModeloAno = consultaApi(endereco);
    var modeloAno = conversor.obterDados(jsonModeloAno, Dados.class);
    System.out.println(modeloAno);


    //"/modelos/{id_modelo}/anos/{ano}"
  }

  public void imprimeOpcoesMenu(List<Dados> dados) {
    System.out.println("*** OPÇÕES ***");
    dados.stream()
            .sorted(Comparator.comparing(Dados::codigo))
            .forEach(dado ->
                    System.out.println(dado.getCodigo() + ". " + dado.getNome())
            );
    System.out.print("Informe uma opção: ");
  }

  public String consultaApi(String url) {
    var json = consumo.obterDados(url);

    return json;
  }
}
