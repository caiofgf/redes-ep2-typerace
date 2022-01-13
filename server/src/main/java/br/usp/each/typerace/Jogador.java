package br.usp.each.typerace;


import java.util.HashSet;
import java.util.Set;

public class Jogador {

    private String idJogador;
    private Set<String> palavras;
    private int acertos;
    private int erros;

    public Jogador(String idJogador, Set<String> palavras){
        this.idJogador = idJogador;
        this.palavras = new HashSet<>();
        this.palavras.addAll(palavras);
        this.acertos =0;
        this.erros = 0;
    }

    public boolean verificaPalavra(String palavra){
        if(this.palavras.contains(palavra)){
            this.palavras.remove(palavra);
            this.acertos++;
            return true;
    }
    this.erros++;
    return false;
    }
    public String PalavrasMessage(){
        String resp = "";
        int cont = 0;
        if(this.palavras.size() == 0) resp = "Você terminou as suas palavras. Digite stop para terminar o jogo para todos, ou de mais um tempo para os outros jogadores";
        else for(String palavra : this.palavras){
            cont++;
            if(cont%5 == 0)resp = palavra + "\n" + resp;
            else  resp = palavra +"     "+ resp;
        }
        return resp;
    }

    public String getIdJogador(){
        return this.idJogador;
    }
    public int getAcertos(){
        return this.acertos;
    }
    public int getErros(){
        return this.erros;
    }
}

