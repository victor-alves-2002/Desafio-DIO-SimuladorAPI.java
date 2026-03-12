import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

// 1. PADRÃO SINGLETON - Garante uma única instância do "servidor"
class ServidorAPI {
    private static ServidorAPI instancia;
    
    private ServidorAPI() {
        System.out.println(">> Servidor API iniciado...");
    }
    
    public static ServidorAPI getInstancia() {
        if (instancia == null) {
            instancia = new ServidorAPI();
        }
        return instancia;
    }
    
    public void processarRequisicao(String projeto) {
        System.out.println(">> Processando requisição para: " + projeto);
    }
}

// 2. PADRÃO STRATEGY - Diferentes formas de processar respostas
interface EstrategiaResposta {
    String gerarResposta(String projeto);
}

class RespostaSucesso implements EstrategiaResposta {
    @Override
    public String gerarResposta(String projeto) {
        return "Status: " + projeto;
    }
}

class RespostaErro implements EstrategiaResposta {
    @Override
    public String gerarResposta(String projeto) {
        return "Projeto nao encontrado";
    }
}

// 3. PADRÃO FACTORY - Cria os objetos de projeto
class ProjetoFactory {
    public static Projeto criarProjeto(String nome, String status) {
        return new Projeto(nome, status);
    }
}

// 4. PADRÃO OBSERVER - Notifica quando um projeto é consultado
interface Observador {
    void atualizar(String projeto);
}

class Logger implements Observador {
    @Override
    public void atualizar(String projeto) {
        System.out.println("[LOG] Projeto consultado: " + projeto);
    }
}

// Classe principal de negócio
class Projeto {
    private String nome;
    private String status;
    
    public Projeto(String nome, String status) {
        this.nome = nome;
        this.status = status;
    }
    
    public String getNome() { return nome; }
    public String getStatus() { return status; }
}

// 5. PADRÃO CHAIN OF RESPONSIBILITY - Cadeia de validação
abstract class Validador {
    protected Validador proximo;
    
    public void setProximo(Validador proximo) {
        this.proximo = proximo;
    }
    
    public abstract String validar(String projeto, Map<String, Projeto> projetos);
}

class ValidadorEntradaVazia extends Validador {
    @Override
    public String validar(String projeto, Map<String, Projeto> projetos) {
        if (projeto == null || projeto.trim().isEmpty()) {
            return "Entrada invalida: nome do projeto vazio";
        }
        return proximo != null ? proximo.validar(projeto, projetos) : null;
    }
}

class ValidadorProjetoExistente extends Validador {
    @Override
    public String validar(String projeto, Map<String, Projeto> projetos) {
        if (!projetos.containsKey(projeto)) {
            return "Projeto nao encontrado";
        }
        return proximo != null ? proximo.validar(projeto, projetos) : null;
    }
}

// 6. PADRÃO BUILDER - Constrói a resposta formatada
class RespostaBuilder {
    private StringBuilder resposta = new StringBuilder();
    
    public RespostaBuilder adicionarTitulo(String titulo) {
        resposta.append("=== ").append(titulo).append(" ===\n");
        return this;
    }
    
    public RespostaBuilder adicionarConteudo(String conteudo) {
        resposta.append(conteudo).append("\n");
        return this;
    }
    
    public RespostaBuilder adicionarRodape() {
        resposta.append("=================");
        return this;
    }
    
    public String construir() {
        return resposta.toString();
    }
}

// Classe principal do sistema
public class SimuladorAPI {
    private Map<String, Projeto> projetos;
    private Validador cadeiaValidacao;
    private EstrategiaResposta estrategia;
    private Logger logger;
    
    public SimuladorAPI() {
        inicializarProjetos();
        configurarValidacao();
        this.logger = new Logger();
        this.estrategia = new RespostaSucesso();
        
        // Singleton em ação
        ServidorAPI servidor = ServidorAPI.getInstancia();
    }
    
    private void inicializarProjetos() {
        projetos = new HashMap<>();
        
        // Factory em ação
        projetos.put("Apollo", ProjetoFactory.criarProjeto("Apollo", "Em andamento"));
        projetos.put("Orion", ProjetoFactory.criarProjeto("Orion", "Concluido"));
        projetos.put("Zeus", ProjetoFactory.criarProjeto("Zeus", "Pendente"));
        projetos.put("Hermes", ProjetoFactory.criarProjeto("Hermes", "Cancelado"));
    }
    
    private void configurarValidacao() {
        // Chain of Responsibility em ação
        Validador validador1 = new ValidadorEntradaVazia();
        Validador validador2 = new ValidadorProjetoExistente();
        
        validador1.setProximo(validador2);
        this.cadeiaValidacao = validador1;
    }
    
    public String consultarProjeto(String nomeProjeto) {
        // Observer em ação
        logger.atualizar(nomeProjeto);
        
        // Chain em ação
        String erro = cadeiaValidacao.validar(nomeProjeto, projetos);
        if (erro != null) {
            estrategia = new RespostaErro();
            return construirResposta(erro);
        }
        
        // Sucesso
        estrategia = new RespostaSucesso();
        Projeto projeto = projetos.get(nomeProjeto);
        return construirResposta(projeto.getStatus());
    }
    
    private String construirResposta(String conteudo) {
        // Builder em ação
        return new RespostaBuilder()
                .adicionarTitulo("CONSULTA DE PROJETOS")
                .adicionarConteudo(estrategia.gerarResposta(conteudo))
                .adicionarRodape()
                .construir();
    }
    
    public static void main(String[] args) {
        SimuladorAPI api = new SimuladorAPI();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== SIMULADOR DE API COM DESIGN PATTERNS ===\n");
        System.out.println("Projetos disponíveis: Apollo, Orion, Zeus, Hermes\n");
        
        while (true) {
            System.out.print("Digite o nome do projeto (ou 'sair' para encerrar): ");
            String entrada = scanner.nextLine().trim();
            
            if (entrada.equalsIgnoreCase("sair")) {
                System.out.println("\nEncerrando simulador...");
                break;
            }
            
            String resposta = api.consultarProjeto(entrada);
            System.out.println("\n" + resposta + "\n");
            System.out.println("----------------------------------------\n");
        }
        
        scanner.close();
    }
}
