package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class Main {

    private static final RecadoDAO DAO = new RecadoDAO();

    static void main() throws Exception {
        testarConexao();

            HttpServer servidor = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);

        servidor.createContext("/api/recados", Main::atenderRecados);
        servidor.createContext("/", Main::abrirPagina);
        servidor.start();

        System.out.println("Mural aberto em http://localhost:8080");
    }
    private static void testarConexao() throws SQLException{
        try(Connection ignored = Conexao.abrir()){
            System.out.println("Banco de dados conectado!!!");
        }
    }

    private static void atenderRecados(HttpExchange troca) throws IOException{
        //permite que o app Android e outros clientes acessem a API
        troca.getRequestHeaders().set("Acess-Control-Allow_Origin", "*");
        troca.getRequestHeaders().set("Acess-Control-Allow-Methods", "GET,POST,OPTIONS");
        troca.getRequestHeaders().set("Acess-Control-Allow-Headers", "Content-Type");

        try {
            if (troca.getRequestMethod().equals("OPTIONS")){
                troca.sendResponseHeaders(204,-1);
                troca.close();
            } else if (troca.getRequestMethod().equals("GET")) {
                listar(troca);
            } else if (troca.getRequestMethod().equals("POST")) {
                cadastrar(troca);
            }else {
                troca.getRequestHeaders().set("Allow", "GET,POST,OPTIONS");
                responder(troca,405, "{\"erro\":\"Método não permitido\"}");
            }
        }catch (SQLException erro){
            erro.printStackTrace();
            responder(troca,500,"{\"erro\":\"Erro ao acessar o banco\"}");
        }
    }
    private static void cadastrar(HttpExchange troca) throws IOException, SQLException{
        //Map; guarda informações no formato chave e valor, como um pequeno dicionario
        Map<String, String> dados = lerFormulario(troca);
        String autor = dados.getOrDefault("autor","").trim();
        String mensagem = dados.getOrDefault("mensagem","").trim();
        if (autor.isEmpty() || mensagem.isEmpty()){
            responder(troca, 400,"{\"erro\":\"Preencha todos os campos\"}");
            return;
        }
        DAO.cadastrar(new Recado(0,autor,mensagem));
        responder(troca,201,"{\"mensagem\":\"Recado cadastrado\"}");
    }
    private static void listar(HttpExchange troca) throws IOException, SQLException{
        List<Recado> recados = DAO.listar();
        //StringBuilder: classe para construir e alterar textos sem criar novas Strings
        //a cada mudança
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < recados.size(); i++){
            if (i > 0){
                json.append(",");
            }
            json.append(recados.get(i).paraJson());
        }
        json.append("]");
        responder(troca,200,json.toString());
    }

}
