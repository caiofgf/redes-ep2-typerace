package br.usp.each.typerace.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import br.usp.each.typerace.Jogador;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Date;

public class Server extends WebSocketServer {

    private Map<String, WebSocket> connections;
    private Map<String, Jogador> jogadores;
    private boolean GameNow;
    private Date comeco;
    private Set<String> palavras;


    public Server(int port, Map<String, WebSocket> connections) {
        super(new InetSocketAddress(port));
        this.connections = connections;
        this.jogadores = new HashMap<>();
        this.GameNow = false;
        this.palavras = new HashSet<>();
        this.palavras.add("banana");
        this.palavras.add("cupcake");
        this.palavras.add("maca");
        this.palavras.add("pet");
        this.palavras.add("redes");
        this.palavras.add("livro");
        this.palavras.add("manga");
        this.palavras.add("sala");
        this.palavras.add("ceu");
        this.palavras.add("paralelepipedo");
        this.palavras.add("estetoscopio");
        this.palavras.add("maria");
        this.palavras.add("jose");
        this.palavras.add("caixa");
        this.palavras.add("universidade");
        this.palavras.add("keralux");
        this.palavras.add("maximiliano");
        this.palavras.add("computadores");
        this.palavras.add("pacote");
        this.palavras.add("uganda");
        this.palavras.add("togo");
        this.palavras.add("jogo");
        this.palavras.add("biblioteca");
        this.palavras.add("implementacao");
        this.palavras.add("contextualizacao");
        this.palavras.add("abnegacao");
        this.palavras.add("determinacao");
        this.palavras.add("foco");
        this.palavras.add("desproporcionadamente");
        this.palavras.add("orrinolaringologista");
        this.palavras.add("caminho");
    }

    private String configuraId(String url){
        String urisplit[] = url.split("="); 
        return urisplit[1];
    }

    private String placar(long time){
        List<Integer> list = new ArrayList<Integer>();
        Map<Integer, Jogador> map = new HashMap<>();
        for(Jogador jogador : this.jogadores.values()){
            int acertos = jogador.getAcertos();
            while(map.containsKey(acertos)) acertos++;
            list.add(acertos);
            map.put(acertos, jogador);
        }
        Collections.sort(list, Collections.reverseOrder());
        String resp = "O jogo acabou, partiu ver quem ganhou";
        int placar = 1;
        for(Integer acertos : list){
            Jogador p = map.get(acertos);
            resp = resp + "\n"+placar +" - " + p.getIdJogador() +" com "+p.getAcertos()+ " acertos e " +p.getErros()+ " erros";
            placar++;
        }
        resp = resp + "\nA partida durou "+time+" segundos";
        return resp;
    }


    @Override //Quando alguem entra no jogo
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        // TODO: Implementao

            String newPlayer = configuraId(conn.getResourceDescriptor());
            if(this.connections.keySet().contains(newPlayer)){
                this.connections.get(newPlayer).send("Erro, alguem tentou usar seu nick, você sera desconectado");
                onClose(this.connections.get(newPlayer), 1002, "Erro", true);
                conn.send("Erro,você tentou usar o nick de alguem, você sera desconectado");
                conn.close();
            }
            else {
                this.connections.put(newPlayer, conn);
                this.connections.forEach((ids,conns) -> conns.send(newPlayer+" se juntou ao jogo\nTemos "+this.connections.size()+" Jogadores"));
                conn.send("Bem vindo ao typerace "+newPlayer+" segue uma lista de comando fresquinha:\n-> Digite start para começar um jogo.\n-> Digite stop para terminar um jogo em andamento.\n-> Digite exit para sair do saquao quando nao estiver em um jogo.\n-> Digite status para saber quem estah no salao, \n-> Se você entrou e estah tendo um jogo, aguarde até o jogo atual terminar ou digite stop para terminar o jogo e participar do proximo\n-> Se alguem tentar entrar com o seu nick, você será desconectado");
           }
        
    }

    @Override //Pede pra sair do jogo
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // TODO: Implementar
        String player = configuraId(conn.getResourceDescriptor());
        this.connections.remove(player);
        conn.close(code, reason);
    }

    @Override //Quando mandam qualquer coisa para o servidor
    public void onMessage(WebSocket conn, String message) {
        // TODO: Implementar
        System.out.println(message);
        try {                
        if(message.equalsIgnoreCase("start")){
            if(!this.GameNow){ 
                //começa o jogo
                for(String newPlayer : this.connections.keySet()){
                Jogador novo = new Jogador(newPlayer, palavras);
                this.jogadores.put(newPlayer, novo);
                }
                for(Jogador player : this.jogadores.values()){
                    String send = player.PalavrasMessage();
                    String id = player.getIdJogador();
                    this.connections.get(id).send("O jogo começou, se prepara que lá vem suas palavras:\n" + send);
                }
                this.GameNow = true;
                this.comeco = new Date();
            } 
            else conn.send("O jogo já começou\n" + this.jogadores.get(configuraId(conn.getResourceDescriptor())).PalavrasMessage());
        }
        else if(message.equalsIgnoreCase("stop")){
            //termina o jogo
            if(this.GameNow){
                this.GameNow= false;
                long time =  System.currentTimeMillis() - comeco.getTime();
                this.connections.forEach((ids, conns) -> conns.send(placar(time/1000)));
                this.jogadores.clear();

            }
            else conn.send("Não tem nenhum jogo rolando, para iniciar um jogo, digite start");
        }
        else if(message.equalsIgnoreCase("exit")){
            //Desconecta jogador
            if(this.GameNow) conn.send("Estamos no meio de um jogo, para sair, antes pare o jogo digitando stop");
            else{
                this.connections.forEach((ids,conns) -> conns.send(configuraId(conn.getResourceDescriptor()) +" saiu do jogo"));
                onClose(conn, 1000, "Jogador pediu", true);
                this.connections.forEach((ids, conns) -> conns.send("Sobraram " + this.connections.size() +" jogadores"));
            }
        }
        else if(message.equalsIgnoreCase("status")){
            for(String id : this.connections.keySet()) this.connections.forEach((ids,conns) -> conns.send(id+" está no jogo"));
            this.connections.forEach((ids, conns) -> conns.send("Temos "+this.connections.size()+" jogadores no jogo"));
        }
        else{
            if(this.GameNow){
                String id = configuraId(conn.getResourceDescriptor());
                Jogador jogador = this.jogadores.get(id);
                if(jogador.verificaPalavra(message.toLowerCase()))conn.send(jogador.getIdJogador() +" Acertou");
                else conn.send("Errou");
                conn.send(jogador.PalavrasMessage());
            }
            else{
                conn.send("O jogo não começou ainda, para começar um jogo, digite start. Para sair do saguão digite exit");
            }
        }
    } catch (Exception e) {
       System.out.println(e.toString());
    }
    }

    @Override //Caso aconteça algo
    public void onError(WebSocket conn, Exception ex) {
        conn.send(ex.getMessage());
    }

    @Override //Quando o server começar
    public void onStart() {
        System.out.println("Server Online na porta " + super.getPort());
    }
}
