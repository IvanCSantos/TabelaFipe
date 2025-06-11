package dev.ivansantos.TabelaFipe.principal;

import dev.ivansantos.TabelaFipe.model.Dados;
import dev.ivansantos.TabelaFipe.model.Modelos;
import dev.ivansantos.TabelaFipe.model.Veiculo;
import dev.ivansantos.TabelaFipe.service.ConsumoApi;
import dev.ivansantos.TabelaFipe.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
  private Scanner scanner = new Scanner(System.in);
  private ConsumoApi consumo = new ConsumoApi();
  private ConverteDados conversor = new ConverteDados();
  private enum VeiculoTipo {
    CARRO(1,"carros"),
    MOTO(2, "motos"),
    CAMINHAO(3,"caminhoes");

    private final int id;
    private final String nome;

    VeiculoTipo(int id, String nome) {
      this.id = id;
      this.nome = nome;
    }

    public String getNome() {
      return nome;
    }

    public int getId() {
      return id;
    }

    public static VeiculoTipo fromId(int id) {
      for (VeiculoTipo veiculo : VeiculoTipo.values()) {
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
    List<VeiculoTipo> tiposVeiculo = List.of(VeiculoTipo.values());
    System.out.println("*** OPÇÕES ***");
    tiposVeiculo.forEach(System.out::println);
    System.out.print("Informe uma opção: ");
    int opcao = scanner.nextInt();
    String endereco = URL_BASE + VeiculoTipo.fromId(opcao).nome + "/marcas";

    // Marcas
    var jsonMarcas = consultaApi(endereco);
    var marcas = conversor.obterLista(jsonMarcas, Dados.class);
    imprimeOpcoesMenu(marcas);
    opcao = scanner.nextInt();
    endereco += "/" + opcao + "/modelos";
    var jsonModelos = consultaApi(endereco);
    var modelos = conversor.obterDados(jsonModelos, Modelos.class);
    int finalOpcaoMarca = opcao;
    var marca = marcas.stream()
            .filter(dado -> dado.codigo().equals(String.valueOf(finalOpcaoMarca)))
            .collect(Collectors.toList()).get(0).getNome(); // Recupera nome da marca
    System.out.println("Você escolheu " + marca);
    System.out.println("Modelos da marca " + marca + ":");

    // Filtra modelos
    modelos.modelos().stream().forEach(System.out::println);
    System.out.println("Informe o nome de um modelo da marca para filtrar: ");
    scanner.nextLine();
    var modeloFiltrado = scanner.nextLine();

    System.out.println(modeloFiltrado);

    List<Dados> modelosFiltrados = modelos.modelos().stream()
            .filter(m -> m.nome().toLowerCase().contains(modeloFiltrado.toLowerCase()))
                    .collect(Collectors.toList());

    // Modelos
    imprimeOpcoesMenu(modelosFiltrados);
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
    List<Veiculo> veiculos = new ArrayList<>();

    for (int i = 0; i < anos.size(); i++) {
      var enderecoAnos = endereco + "/" + anos.get(i).codigo();
      var jsonVeiculo = consumo.obterDados(enderecoAnos);
      var veiculo = conversor.obterDados(jsonVeiculo, Veiculo.class);
      veiculos.add(veiculo);
    }

    veiculos.stream()
            .forEach(System.out::println);

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
