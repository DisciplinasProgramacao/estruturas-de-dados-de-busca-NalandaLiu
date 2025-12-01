
public class Fornecedor {
    private String nome;
    private int documento;
    private static int ultimoID = 10_000;
    
    private AVL<Integer, Produto> produtos;

    public Fornecedor(String nome) {
      ;
       String[] partes = nome.trim().split("\\s+");
        if (nome == null || nome.trim().isEmpty())||partes.length < 2)
            throw new IllegalArgumentException("O nome do fornecedor deve ter duas palavras");

        this.nome = nome;
        this.documento = ultimoID++;
        this.produtos = new AVL<>();
    }
    
    public void adicionarProduto(Produto produto) {

        if (produto == null)
            throw new IllegalArgumentException("Produto nulo não pode ser adicionado.");

        this.produtos.inserir(produto.hashCode(), produto);
    }

    public AVL<Integer, Produto> getProdutos() {
        return this.produtos;
    }

@Override
	public int hashCode() {
        return Integer.hashCode(this.documento);
    }

  @Override
	public String toString() {
        return "Fornecedor [nome=" + nome + ", documento=" + documento + "]";
    }
    
}