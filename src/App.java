import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App {

	/** Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto */
    static String nomeArquivoDados;
    
    /** Scanner para leitura de dados do teclado */
    static Scanner teclado;

    /** Quantidade de produtos cadastrados atualmente na lista */
    static int quantosProdutos = 0;

    static ABB<String, Produto> produtosCadastradosPorNome;
    
    static ABB<Integer, Produto> produtosCadastradosPorId;
    
    static AVL<String, Produto> produtosBalanceadosPorNome;
    
    static AVL<Integer, Produto> produtosBalanceadosPorId;
    
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
        System.out.println("1 - Carregar produtos por nome/descrição");
        System.out.println("2 - Carregar produtos por id");
        System.out.println("3 - Procurar produto, por nome");
        System.out.println("4 - Procurar produto, por id");
        System.out.println("5 - Remover produto, por nome");
        System.out.println("6 - Remover produto, por id");
        System.out.println("7 - Recortar a lista de produtos, por nome");
        System.out.println("8 - Recortar a lista de produtos, por id");
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
    static <K extends Comparable<K>> AVL<K, Produto> lerProdutos(String nomeArquivoDados, Function<Produto, K> extratorDeChave) {
    	
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
    
    /** Localiza um produto na árvore de produtos organizados por id, a partir do código de produto informado pelo usuário, e o retorna. 
     *  Em caso de não encontrar o produto, retorna null */
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
    
    /** Localiza e remove um produto da árvore de produtos organizados por id, a partir do código de produto informado pelo usuário, e o retorna. 
     *  Em caso de não encontrar o produto, retorna null */
    static Produto removerProdutoId(ABB<Integer, Produto> produtosCadastrados) {
         cabecalho();
         System.out.println("Localizando o produto por id");
         int id = lerOpcao("Digite o id do produto que deve ser removido", Integer.class);
         Produto localizado =  removerProduto(produtosCadastrados, id);
         return localizado;
    }

     /** Localiza e remove um produto na árvore de produtos organizados por nome, a partir do nome de produto informado pelo usuário, e o retorna. 
      *  A busca não é sensível ao caso. Em caso de não encontrar o produto, retorna null */
    static Produto removerProdutoNome(ABB<String, Produto> produtosCadastrados) {
    	String descricao;
         
    	cabecalho();
        System.out.println("Localizando o produto por nome");
        System.out.print("Digite a descrição do produto que deve ser removido: ");
        descricao = teclado.nextLine();
        Produto localizado =  removerProduto(produtosCadastrados, descricao);
        return localizado;
    }

    static <K> Produto removerProduto(ABB<K, Produto> produtosCadastrados, K chave){
         cabecalho();
         Produto localizado =  produtosCadastrados.remover(chave);
         return localizado;
    }
    
    private static <K> void recortarProdutos(ABB<K, Produto> produtosCadastrados, String tipoChave) {
    	
    	cabecalho();
    	System.out.println("Recortando produtos por " + tipoChave + "...");
    	
    	if (produtosCadastrados == null || produtosCadastrados.vazia()) {
    		System.out.println("Nenhuma árvore carregada ou árvore vazia!");
    		return;
    	}
    	
    	System.out.print("Digite o " + tipoChave + " inicial: ");
    	String inputInicio = teclado.nextLine();
    	
    	System.out.print("Digite o " + tipoChave + " final: ");
    	String inputFim = teclado.nextLine();
    	
    	K chaveInicio = (K) converterChave(inputInicio, produtosCadastrados);
    	K chaveFim = (K) converterChave(inputFim, produtosCadastrados);
    	
    	Lista<Produto> resultado = produtosCadastrados.recortar(chaveInicio, chaveFim);
    	
    	if (resultado.vazia()) {
    		System.out.println("Nenhum produto encontrado no intervalo especificado.");
    	} else {
    		System.out.println("Produtos encontrados no intervalo:");
    		System.out.println(resultado);
    	}
    }
    
    /**
     * Converte uma string de entrada para o tipo de chave apropriado.
     * @param entrada a string de entrada do usuário.
     * @param arvore a árvore que define o tipo de chave.
     * @return o valor convertido para o tipo apropriado.
     */
    private static <K> K converterChave(String entrada, ABB<K, Produto> arvore) {
    	
    	
    	K exemploChave = null;
    	try {
    		
    		return (K) entrada;
    	} catch (NumberFormatException e) {
    		return (K) entrada;
    	}
    }
    
    private static void recortarProdutosNome(ABB<String, Produto> produtosCadastrados) {
     	recortarProdutos(produtosCadastrados, "descrição");
     }
     
    private static void recortarProdutosId(ABB<Integer, Produto> produtosCadastrados) {
     	
    	cabecalho();
    	System.out.println("Recortando produtos por identificador...");
    	
    	if (produtosCadastrados == null || produtosCadastrados.vazia()) {
    		System.out.println("Nenhuma árvore carregada ou árvore vazia!");
    		return;
    	}
    	
    	System.out.print("Digite o identificador inicial: ");
    	int idInicio = Integer.parseInt(teclado.nextLine());
    	
    	System.out.print("Digite o identificador final: ");
    	int idFim = Integer.parseInt(teclado.nextLine());
    	
    	Lista<Produto> resultado = produtosCadastrados.recortar(idInicio, idFim);
    	
    	if (resultado.vazia()) {
    		System.out.println("Nenhum produto encontrado no intervalo especificado.");
    	} else {
    		System.out.println("Produtos encontrados no intervalo:");
    		System.out.println(resultado);
    	}
    }
    
	public static void main(String[] args) {
		teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        nomeArquivoDados = "produtos.txt";
        
        int opcao = -1;
      
        do{
            opcao = menu();
            switch (opcao) {
            	case 1 -> produtosCadastradosPorNome = lerProdutos(nomeArquivoDados, (p -> p.descricao));
            	case 2 -> produtosCadastradosPorId = lerProdutos(nomeArquivoDados, (p -> p.idProduto));
            	case 3 -> mostrarProduto(localizarProdutoNome(produtosCadastradosPorNome));
            	case 4 -> mostrarProduto(localizarProdutoID(produtosCadastradosPorId));
            	case 5 -> mostrarProduto(removerProdutoNome(produtosCadastradosPorNome));
            	case 6 -> mostrarProduto(removerProdutoId(produtosCadastradosPorId));
            	case 7 -> recortarProdutosNome(produtosCadastradosPorNome); 
            	case 8 -> recortarProdutosId(produtosCadastradosPorId); 
            }
            pausa();
        }while(opcao != 0);       

        teclado.close();    
    }
}
