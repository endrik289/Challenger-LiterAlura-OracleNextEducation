package br.com.alura.literalura.principal;

import br.com.alura.literalura.model.DadosLivro;
import br.com.alura.literalura.model.DadosResultados;
import br.com.alura.literalura.service.ConsumoApi;
import br.com.alura.literalura.service.ConverteDados;

import java.util.Scanner;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://gutendex.com/books/?search=";

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    \n*** Bem-vindo ao LiterAlura ***
                    Escolha o número de sua opção:
                    
                    1 - Buscar livro pelo título
                    0 - Sair
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine(); // Consome o "Enter" que sobra no Scanner

            switch (opcao) {
                case 1:
                    buscarLivroWeb();
                    break;
                case 0:
                    System.out.println("Saindo do LiterAlura... Até logo!");
                    break;
                default:
                    System.out.println("Opção inválida! Tente novamente.");
            }
        }
    }

    private void buscarLivroWeb() {
        System.out.println("Digite o nome do livro que você deseja buscar:");
        var nomeLivro = leitura.nextLine();

        // Faz a busca na API
        var json = consumo.obterDados(ENDERECO + nomeLivro.replace(" ", "%20"));
        var dados = conversor.obterDados(json, DadosResultados.class);

        // Verifica se a API encontrou algum livro
        if (dados.resultados() != null && !dados.resultados().isEmpty()) {

            // Pega o PRIMEIRO livro da lista (Regra do Passo 7)
            DadosLivro primeiroLivro = dados.resultados().get(0);

            System.out.println("\n----- LIVRO ENCONTRADO -----");
            System.out.println("Título: " + primeiroLivro.titulo());

            // Pega o nome do autor (se existir)
            if (primeiroLivro.autores() != null && !primeiroLivro.autores().isEmpty()) {
                System.out.println("Autor: " + primeiroLivro.autores().get(0).nome());
            } else {
                System.out.println("Autor: Desconhecido");
            }

            // Pega apenas o primeiro idioma (Regra do Passo 7)
            if (primeiroLivro.idiomas() != null && !primeiroLivro.idiomas().isEmpty()) {
                System.out.println("Idioma: " + primeiroLivro.idiomas().get(0));
            }

            System.out.println("Downloads: " + primeiroLivro.numeroDeDownloads());
            System.out.println("----------------------------\n");

        } else {
            System.out.println("\nLivro não encontrado! Verifique o nome e tente novamente.\n");
        }
    }
}