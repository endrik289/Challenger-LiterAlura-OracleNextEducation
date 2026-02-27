package br.com.alura.literalura.principal;

import br.com.alura.literalura.model.Autor;
import br.com.alura.literalura.model.DadosLivro;
import br.com.alura.literalura.model.DadosResultados;
import br.com.alura.literalura.model.Livro;
import br.com.alura.literalura.repository.AutorRepository;
import br.com.alura.literalura.repository.LivroRepository;
import br.com.alura.literalura.service.ConsumoApi;
import br.com.alura.literalura.service.ConverteDados;

import java.util.Scanner;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://gutendex.com/books/?search=";

    private LivroRepository livroRepository;
    private AutorRepository autorRepository;

    public Principal(LivroRepository livroRepository, AutorRepository autorRepository) {
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    \n*** Bem-vindo ao LiterAlura ***
                    Escolha o número de sua opção:
                    
                    1 - Buscar livro pelo título (e salvar)
                    2 - Listar livros registrados
                    3 - Listar autores registrados
                    4 - Listar autores vivos em um determinado ano
                    5 - Listar livros em um determinado idioma
                    0 - Sair
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1: buscarLivroWeb(); break;
                case 2: listarLivrosRegistrados(); break;
                case 3: listarAutoresRegistrados(); break;
                case 4: listarAutoresVivos(); break;
                case 5: listarLivrosPorIdioma(); break;
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
        var json = consumo.obterDados(ENDERECO + nomeLivro.replace(" ", "%20"));
        var dados = conversor.obterDados(json, DadosResultados.class);

        if (dados.resultados() != null && !dados.resultados().isEmpty()) {
            DadosLivro dadosLivro = dados.resultados().get(0);
            Autor autor = null;
            if (dadosLivro.autores() != null && !dadosLivro.autores().isEmpty()) {
                var dadosAutor = dadosLivro.autores().get(0);
                autor = autorRepository.findByNome(dadosAutor.nome());
                if (autor == null) {
                    autor = new Autor(dadosAutor);
                    autorRepository.save(autor);
                }
            }
            try {
                Livro livro = new Livro(dadosLivro);
                livro.setAutor(autor);
                livroRepository.save(livro);
                System.out.println("\n✅ Livro encontrado e salvo no banco de dados!");
                System.out.println("Título: " + livro.getTitulo());
                System.out.println("Autor: " + (autor != null ? autor.getNome() : "Desconhecido"));
                System.out.println("----------------------------\n");
            } catch (Exception e) {
                System.out.println("\n❌ Erro ao salvar: Este livro já está registrado no banco de dados!\n");
            }
        } else {
            System.out.println("\nLivro não encontrado na API! Verifique o nome e tente novamente.\n");
        }
    }

    private void listarLivrosRegistrados() {
        var livros = livroRepository.findAll();
        System.out.println("\n--- LIVROS REGISTRADOS NO BANCO ---");
        livros.forEach(l -> System.out.println("Título: " + l.getTitulo() + " | Idioma: " + l.getIdioma() + " | Autor: " + l.getAutor().getNome()));
        System.out.println("-----------------------------------\n");
    }

    private void listarAutoresRegistrados() {
        var autores = autorRepository.findAll();
        System.out.println("\n--- AUTORES REGISTRADOS NO BANCO ---");
        autores.forEach(a -> System.out.println("Nome: " + a.getNome() + " | Nasc/Fale: " + a.getAnoNascimento() + "-" + a.getAnoFalecimento()));
        System.out.println("------------------------------------\n");
    }

    private void listarAutoresVivos() {
        System.out.println("Digite o ano para buscar autores vivos:");
        try {
            var ano = Integer.valueOf(leitura.nextLine());

            var autoresVivos = autorRepository.buscarAutoresVivosNoAno(ano);
            System.out.println("\n--- AUTORES VIVOS NO ANO DE " + ano + " ---");
            if (autoresVivos.isEmpty()) {
                System.out.println("Nenhum autor encontrado para este ano em nosso banco.");
            } else {
                autoresVivos.forEach(a -> System.out.println("Nome: " + a.getNome() + " | Nascimento: " + a.getAnoNascimento() + " | Falecimento: " + a.getAnoFalecimento()));
            }
            System.out.println("---------------------------------------\n");

        } catch (NumberFormatException e) {
            System.out.println("\n❌ Erro: Por favor, digite um ano válido apenas com números (ex: 1800).\n");
        }
    }

    private void listarLivrosPorIdioma() {
        System.out.println("Digite o idioma para busca (ex: en, pt, fr, es):");
        var idioma = leitura.nextLine();

        var livrosPorIdioma = livroRepository.findByIdioma(idioma);
        System.out.println("\n--- LIVROS NO IDIOMA '" + idioma.toUpperCase() + "' ---");
        if (livrosPorIdioma.isEmpty()) {
            System.out.println("Nenhum livro encontrado com este idioma em nosso banco.");
        } else {
            livrosPorIdioma.forEach(l -> System.out.println("Título: " + l.getTitulo() + " | Autor: " + l.getAutor().getNome()));

            System.out.println("\n📊 Quantidade de livros encontrados neste idioma: " + livrosPorIdioma.size());
        }
        System.out.println("---------------------------------\n");
    }
}