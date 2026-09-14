package org.example;

import java.sql.SQLException;

public class TesteDAO {
    static void main() throws SQLException {
        RecadoDAO dao = new RecadoDAO();

        dao.cadastrar(new Recado(0, "Heitor", "Teste feito pelo console!!!"));
        for (Recado recado: dao.listar()){
            System.out.println(recado.getAutor() + ": " + recado.getMensagem());
        }
    }





}
