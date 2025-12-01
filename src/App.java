import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Function;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App {

	/** Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto */
    static String nomeArquivoDados;
    
    /** Scanner para leitura de dados do teclado */
    static Scanner teclado;

    /** Quantidade de produtos cadastrados atualmente na lista */
    static int quantosProdutos = 0;

    static AVL<String, Produto> produtosBalanceadosPorNome;
    
    static AVL<Integer, Produto> produtosBalanceadosPorId;
    
    static TabelaHash<Produto, Lista<Pedido>> pedidosPorProduto;

    /** Árvore AVL para armazenar os fornecedores (chave: documento) */
    static AVL<Integer, Fornecedor> fornecedoresBalanceadosPorDocumento;

    /** Tabela hash que associa um Produto à lista de seus fornecedores */
    static TabelaHash<Produto, Lista<Fornecedor>> fornecedoresPorProduto;
    
    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho() {
        System.out.println("AEDs II COMÉRCIO DE COISINHAS");
        System.out.println("=============================");
    }
   
    static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {
        
    	T valor;
        
    	System.out.println(mensagem);
    	try {
            valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
        		| InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
        return valor;
    }
    
    /** Imprime o menu principal, lê a opção do usuário e a retorna (int).
     * Perceba que poderia haver uma melhor modularização com a criação de uma classe Menu.
     * @return Um inteiro com a opção do usuário.
    */
    static int menu() {
        cabecalho();
        System.out.println("1 - Procurar produto, por id");
        System.out.println("2 - Gravar, em arquivo, pedidos de um produto");
        System.out.println("3 - Relatório de fornecedor, por documento");
        System.out.println("4 - Gravar, em arquivo, fornecedores de um produto");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opção: ");
        return Integer.parseInt(teclado.nextLine());
    }
    
    /**
     * Lê os dados de um arquivo-texto e retorna uma árvore de produtos. Arquivo-texto no formato
     * N (quantidade de produtos) <br/>
     * tipo;descrição;preçoDeCusto;margemDeLucro;[dataDeValidade] <br/>
     * Deve haver uma linha para cada um dos produtos. Retorna uma árvore vazia em caso de problemas com o arquivo.
     * @param nomeArquivoDados Nome do arquivo de dados a ser aberto.
     * @return Uma árvore com os produtos carregados, ou vazia em caso de problemas de leitura.
     */
    static <K> AVL<K, Produto> lerProdutos(String nomeArquivoDados, Function<Produto, K> extratorDeChave) {
    	
    	Scanner arquivo = null;
    	int numProdutos;
    	String linha;
    	Produto produto;
    	AVL<K, Produto> produtosCadastrados;
    	K chave;
    	
    	try {
    		arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));
    		
    		numProdutos = Integer.parseInt(arquivo.nextLine());
    		produtosCadastrados = new AVL<K, Produto>();
    		
    		for (int i = 0; i < numProdutos; i++) {
    			linha = arquivo.nextLine();
    			produto = Produto.criarDoTexto(linha);
    			chave = extratorDeChave.apply(produto);
    			produtosCadastrados.inserir(chave, produto);
    		}
    		quantosProdutos = numProdutos;
    		
    	} catch (IOException excecaoArquivo) {
    		produtosCadastrados = null;
    	} finally {
    		arquivo.close();
    	}
    	
    	return produtosCadastrados;
    }
    
    static <K> Produto localizarProduto(ABB<K, Produto> produtosCadastrados, K procurado) {
    	
    	Produto produto;
    	
    	cabecalho();
    	System.out.println("Localizando um produto...");
    	
    	try {
    		produto = produtosCadastrados.pesquisar(procurado);
    	} catch (NoSuchElementException excecao) {
    		produto = null;
    	}
    	
    	System.out.println("Número de comparações realizadas: " + produtosCadastrados.getComparacoes());
    	System.out.println("Tempo de processamento da pesquisa: " + produtosCadastrados.getTempo() + " ms");
        
    	return produto;
    	
    }

static <K> AVL<K, Fornecedor> lerFornecedores(String nomeArquivoDados, Function<Fornecedor, K> extratorDeChave) {
    
    Scanner arquivo = null;
    int numFornecedores;
    String linha;
    Fornecedor fornecedor;
    AVL<K, Fornecedor> fornecedoresCadastrados;
    K chave;
    
    try {
        arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));
        
        numFornecedores = Integer.parseInt(arquivo.nextLine());
        fornecedoresCadastrados = new AVL<K, Fornecedor>();
        
        for (int i = 0; i < numFornecedores; i++) {
            linha = arquivo.nextLine().trim();
            fornecedor = new Fornecedor(linha, produtosBalanceadosPorId);
            chave = extratorDeChave.apply(fornecedor);
            fornecedoresCadastrados.inserir(chave, fornecedor);
        }
        
    } catch (IOException excecaoArquivo) {
        fornecedoresCadastrados = null;
    } finally {
        if (arquivo != null) arquivo.close();
    }
    
    return fornecedoresCadastrados;
}

    
    static Produto localizarProdutoID(ABB<Integer, Produto> produtosCadastrados) {
        
        int idProduto = lerOpcao("Digite o identificador do produto desejado: ", Integer.class);
        
        return localizarProduto(produtosCadastrados, idProduto);
    }
    
    /** Localiza um produto na árvore de produtos organizados por nome, a partir do nome de produto informado pelo usuário, e o retorna. 
     *  A busca não é sensível ao caso. Em caso de não encontrar o produto, retorna null */
    static Produto localizarProdutoNome(ABB<String, Produto> produtosCadastrados) {
        
    	String descricao;
    	
    	System.out.println("Digite o nome ou a descrição do produto desejado:");
        descricao = teclado.nextLine();
        
        return localizarProduto(produtosCadastrados, descricao);
    }
    
    private static void mostrarProduto(Produto produto) {
    	
        cabecalho();
        String mensagem = "Dados inválidos para o produto!";
        
        if (produto != null){
            mensagem = String.format("Dados do produto:\n%s", produto);
        }
        
        System.out.println(mensagem);
    }
    
    private static Lista<Pedido> gerarPedidos(int quantidade) {
        Lista<Pedido> pedidos = new Lista<>();
        Random sorteio = new Random(42);
        int quantProdutos;
        int formaDePagamento;
        for (int i = 0; i < quantidade; i++) {
        	formaDePagamento = sorteio.nextInt(2) + 1;
            Pedido pedido = new Pedido(LocalDate.now(), formaDePagamento);
            quantProdutos = sorteio.nextInt(8) + 1;
            for (int j = 0; j < quantProdutos; j++) {
                int id = sorteio.nextInt(7750) + 10_000;
                Produto produto = produtosBalanceadosPorId.pesquisar(id);
                pedido.incluirProduto(produto);
                inserirNaTabela(produto, pedido);
            }
            pedidos.inserirFinal(pedido);
        }
        return pedidos;
    }
    
    private static void inserirNaTabela(Produto produto, Pedido pedido) {
        
    	Lista<Pedido> pedidosDoProduto;
    	
    	try {
    		pedidosDoProduto = pedidosPorProduto.pesquisar(produto);
    	} catch (NoSuchElementException excecao) {
    		pedidosDoProduto = new Lista<>();
    		pedidosPorProduto.inserir(produto, pedidosDoProduto);
    	}
    	pedidosDoProduto.inserirFinal(pedido);
    }
    
    static void pedidosDoProduto() {
    	
    	Lista<Pedido> pedidosDoProduto;
    	Produto produto = localizarProdutoID(produtosBalanceadosPorId);
    	String nomeArquivo;
    	
    	if (produto == null) {
    		System.out.println("Produto não encontrado. Não é possível gerar o relatório.");
    		return;
    	}
    	
    	nomeArquivo = "RelatorioProduto" + produto.hashCode() + ".txt";
    	
        try {
        	FileWriter arquivoRelatorio = new FileWriter(nomeArquivo, Charset.forName("UTF-8"));
    		
        	try {
        		pedidosDoProduto = pedidosPorProduto.pesquisar(produto);
        		arquivoRelatorio.append("Relatório de Pedidos do Produto: " + produto + "\n");
        		arquivoRelatorio.append("=====================================\n\n");
        		arquivoRelatorio.append(pedidosDoProduto.toString() + "\n");
        	} catch (NoSuchElementException excecao) {
        		arquivoRelatorio.append("Produto: " + produto + "\n");
        		arquivoRelatorio.append("=====================================\n");
        		arquivoRelatorio.append("Nenhum pedido foi encontrado para este produto.\n");
        	}
        	
            arquivoRelatorio.close();
            System.out.println("Dados salvos em " + nomeArquivo);
        } catch(IOException excecao) {
            System.out.println("Problemas para criar o arquivo " + nomeArquivo + ". Tente novamente");        	
        }
    }

    /**
     * Gera e retorna o relatório completo de um fornecedor escolhido pelo usuário
     * por meio do seu documento identificador.
     * @return String com o relatório do fornecedor ou mensagem de erro caso não encontrado.
     */
    static String relatorioDeFornecedor() {
        cabecalho();
        System.out.println("Gerando relatório de fornecedor...");

        Integer documento = lerOpcao("Digite o documento do fornecedor desejado: ", Integer.class);
        if (documento == null) {
            return "Documento inválido.";
        }

        try {
            Fornecedor fornecedor = fornecedoresBalanceadosPorDocumento.pesquisar(documento);
            StringBuilder sb = new StringBuilder();
            sb.append("Relatório do Fornecedor\n");
            sb.append("======================\n");
            sb.append(fornecedor.toString()).append("\n\n");
            sb.append("Produtos fornecidos:\n");
            try {
                AVL<Integer, Produto> produtos = fornecedor.getProdutos();
                sb.append(produtos.percorrer());
            } catch (IllegalStateException e) {
                sb.append("Nenhum produto cadastrado para este fornecedor.\n");
            }
            return sb.toString();
        } catch (NoSuchElementException e) {
            return "Fornecedor não encontrado.";
        }
    }

    /**
     * Gera, em arquivo, um relatório com todos os fornecedores de um produto
     * escolhido pelo usuário.
     */
    static void fornecedoresDoProduto() {
        Produto produto = localizarProdutoID(produtosBalanceadosPorId);
        String nomeArquivo;

        if (produto == null) {
            System.out.println("Produto não encontrado. Não é possível gerar o relatório.");
            return;
        }

        nomeArquivo = "RelatorioFornecedoresProduto" + produto.hashCode() + ".txt";

        try {
            FileWriter arquivoRelatorio = new FileWriter(nomeArquivo, Charset.forName("UTF-8"));
            try {
                Lista<Fornecedor> listaFornecedores = fornecedoresPorProduto.pesquisar(produto);
                arquivoRelatorio.append("Relatório de Fornecedores do Produto: " + produto + "\n");
                arquivoRelatorio.append("=====================================\n\n");
                arquivoRelatorio.append(listaFornecedores.toString() + "\n");
            } catch (NoSuchElementException excecao) {
                arquivoRelatorio.append("Produto: " + produto + "\n");
                arquivoRelatorio.append("=====================================\n");
                arquivoRelatorio.append("Nenhum fornecedor foi encontrado para este produto.\n");
            }
            arquivoRelatorio.close();
            System.out.println("Dados salvos em " + nomeArquivo);
        } catch (IOException excecao) {
            System.out.println("Problemas para criar o arquivo " + nomeArquivo + ". Tente novamente");
        }
    }
    
	public static void main(String[] args) {
		teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        nomeArquivoDados = "produtos.txt";
        produtosBalanceadosPorId = lerProdutos(nomeArquivoDados, Produto::hashCode);
        produtosBalanceadosPorNome = new AVL<>(produtosBalanceadosPorId, produto -> produto.descricao, String::compareTo);
        pedidosPorProduto = new TabelaHash<>((int)(quantosProdutos * 1.25));
        fornecedoresBalanceadosPorDocumento = new AVL<>();
        fornecedoresPorProduto = new TabelaHash<>((int)(quantosProdutos * 1.25));
        
        gerarPedidos(25_000);
       
        int opcao = -1;
      
        do {
            opcao = menu();
            switch (opcao) {
            	case 1 -> mostrarProduto(localizarProdutoID(produtosBalanceadosPorId));
            	case 2 -> pedidosDoProduto(); 
            	case 3 -> System.out.println(relatorioDeFornecedor());
            	case 4 -> fornecedoresDoProduto();
            }
            pausa();
        } while(opcao != 0);       

        teclado.close();    
    }
}