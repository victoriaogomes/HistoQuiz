package com.example.histoquiz.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import com.example.histoquiz.R;

/**
 * Classe utilizada para manipular os cliques recebidos na tela de menu do jogo, redirecionando o
 * usuário para as activities corretas relativas a cada interação
 */
public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    protected Button button1, button2, button3, button4, voltar;

    // Variáveis para o controle da tela como fullscreen
    private final Handler mHideHandler = new Handler();
    private View mContentView;


    /**
     * Método chamado assim que essa activity é invocada.
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        initGui();
        setInicialMenuOptions();
        mContentView = findViewById(R.id.fullContent);
        hideNow();
    }


    /**
     * Runnable utilizado para remover automaticamente a barra de botões e a de status dessa
     * activity
     */
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };


    /**
     * Runnable utilizado para exibir a barra de botões e a de status dessa activity quando o
     * usuário solicitar
     */
    private final Runnable mShowPart2Runnable = () -> {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    };


    /**
     * Programa uma chamada ao método hide() após uma quantidade delayMillis de millisegundos,
     * cancelando qualquer chamada programada previamente
     */
    public void hideNow() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, 0);
    }


    /** Método utilizado para obter uma referência para os quatro botões presentes na view
     * relacionada a essa activity
     */
    protected void initGui(){
        button1 = findViewById(R.id.button1);
        button1.setOnClickListener(this);
        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(this);
        button3 = findViewById(R.id.button3);
        button3.setOnClickListener(this);
        button4 = findViewById(R.id.button4);
        button4.setOnClickListener(this);
        voltar = findViewById(R.id.voltar);
        voltar.setOnClickListener(this);
    }


    /**
     * Método utilizado para redirecionar os cliques recebidos nos botões da view relacionada
     * a essa activity para o método que lidará corretamente com ele
     * @param v - view onde o clique ocorreu
     */
    @Override
    public void onClick(View v) {
        String tag = (String) v.getTag();
        switch (tag){
            case "MINHA_CONTA":            myAccount();            break;
            case "JOGAR":                  setPlayOptions();       break;
            case "REVISAR":                setReviewOptions();     break;
            case "SOBRE_O_JOGO":           setAboutOptions();      break;
            case "JOGAR_ONLINE":           setPlayOnlineOptions(); break;
            case "JOGAR_PC":               playAgainstPC();        break;
            case "CONVIDAR_AMIGO":         inviteFriend();         break;
            case "MODO_ALEATORIO":         randomMode();           break;
            case "O_QUE_E":                whatItIs();             break;
            case "COMO_JOGAR":             howToPlay();            break;
            case "FALE_CONOSCO":           contactUs();            break;
            case "SISTEMA_REPRODUTOR":     showReview(1);     break;
            case "SISTEMA_DIGESTORIO":     showReview(2);     break;
            case "SISTEMA_RESPIRATORIO":   showReview(3);     break;
            case "SISTEMA_CARDIOVASCULAR": showReview(4);     break;
            case "MENU_INICIAL":           setInicialMenuOptions();break;
        }
    }


    /**
     * Método utilizado para trocar para a activity responsável por exibir os
     * detalhes da conta do usuário
     */
    protected void myAccount(){
        Intent troca = new Intent(MenuActivity.this, MyAccountActivity.class);
        startActivity(troca);
    }


    /**
     * Método que exibe ao jogador as opções de jogo disponíveis: jogar contra
     * o computador ou jogar online. Para isso, ele se aproveita dos botões do
     * menu: "jogar" vira "jogar online" e "revisar" vira "jogar contra o com-
     * putador". Os demais botões são desabilitados e ficam invisíveis
     */
    protected void setPlayOptions(){
        button1.setVisibility(View.GONE);
        button1.setEnabled(false);
        button4.setVisibility(View.INVISIBLE);
        button4.setEnabled(false);
        button2.setText(R.string.jogarOnline);
        button2.setTag("JOGAR_ONLINE");
        button3.setText(R.string.jogarPC);
        button3.setTag("JOGAR_PC");
        voltar.setVisibility(View.VISIBLE);
        voltar.setEnabled(true);
        voltar.setTag("MENU_INICIAL");
    }


    /**
     * Método utilizado para redirecionar dessa activity para a que exibe
     */
    protected void setReviewOptions(){
        button1.setText(R.string.sistemaReprodutor);
        button1.setTag("SISTEMA_REPRODUTOR");
        button2.setText(R.string.sistemaDigestorio);
        button2.setTag("SISTEMA_DIGESTORIO");
        button3.setText(R.string.sistemaCardiopulmonar);
        button3.setTag("SISTEMA_RESPIRATORIO");
        button4.setText(R.string.sistemaOsteomuscular);
        button4.setTag("SISTEMA_CARDIOVASCULAR");
        voltar.setVisibility(View.VISIBLE);
        voltar.setEnabled(true);
        voltar.setTag("MENU_INICIAL");
    }


    /**
     * Método que exibe ao jogador as opções sobre o jogo: o que é, como jogar e fale co-
     * nosco. Para isso, ele se aproveita dos botões do menu: "jogar", transformando-o em
     * "O que é"; "revisar", transformando-o em "como jogar"; e "sobre o jogo", transfor-
     * mando-o em "fale conosco"
     */
    private void setAboutOptions(){
        button1.setVisibility(View.GONE);
        button2.setText(R.string.oQue);
        button2.setTag("O_QUE_E");
        button3.setText(R.string.comoJogar);
        button3.setTag("COMO_JOGAR");
        button4.setText(R.string.faleConosco);
        button4.setTag("FALE_CONOSCO");
        voltar.setVisibility(View.VISIBLE);
        voltar.setEnabled(true);
        voltar.setTag("MENU_INICIAL");
    }


    /**
     * Método que exibe ao jogador as opções de jogo online disponíveis: jogar contra
     * um amigo ou jogar no modo aleatório (o jogo escolhe um oponente qualquer). Para
     * isso, ele se aproveita dos botões do menu: "jogar"(que já tinha se tornado "jo-
     * gar online"), transformando-o em "Convidar amigo", e "revisar" (que já tinha se
     * tornado "jogar contra o computador"), transformando-o em "modo aleatório"
     */
    private void setPlayOnlineOptions(){
        button2.setText(R.string.convidarAmigo);
        button2.setTag("CONVIDAR_AMIGO");
        button3.setText(R.string.modoAleatorio);
        button3.setTag("MODO_ALEATORIO");
        voltar.setVisibility(View.VISIBLE);
        voltar.setEnabled(true);
        voltar.setTag("JOGAR");
    }


    /**
     * Método utilizado para redirecionar o jogador diretamente para a tela de jogo,
     * já que ele irá jogar contra o próprio celular
     */
    protected void playAgainstPC(){
        Intent troca = new Intent(MenuActivity.this, GameActivity.class);
        troca.putExtra("matchCreator", true);
        troca.putExtra("PCopponent", true);
        troca.putExtra("opponentUID", "0");
        startActivityForResult(troca, 999);
    }

    /**
     * Método utilizado para redirecionar dessa activity para a que exibe a tela
     * utilizada para convidar amigo para jogar
     */
    private void inviteFriend(){
        Intent troca = new Intent(MenuActivity.this, InviteFriendToPlayActivity.class);
        startActivity(troca);
    }


    /**
     * Método utilizado para redirecionar dessa activity para a de jogo, após encon-
     * trar um oponente aleatório para jogar contra esse usuário
     */
    private void randomMode(){

    }


    /**
     * Método utilizado para redirecionar dessa activity para a que descreve
     * o que o HistoQuiz é
     */
    private void whatItIs(){

    }

    /**
     * Método utilizado para redirecionar dessa activity para a que descreve
     * como jogar o HistoQuiz
     */
    private void howToPlay(){

    }


    /**
     * Método utilizado para redirecionar dessa activity para a que permite
     * ao usuário enviar um email para os desenvolvedores do HistoQuiz
     */
    private void contactUs(){

    }


    /**
     * Método utilizado para redirecionar dessa activity para a que exibe ao usuário
     * os dados para ele revisar sobre o sistema escolhido
     * @param kind - sistema do corpo humano que o usuário deseja revisar, a saber:
     *               1 - Sistema reprodutor
     *               2 - Sistema digestório
     *               3 - Sistema respiratório
     *               4 - Sistema cardiovascular
     */
    private void showReview(int kind){

    }


    /**
     * Método utilizado para setar tags para os botões presentes na view relacionada
     * a essa activity para a sua primeira execução, que exibe o menu principal
     */
    protected void setInicialMenuOptions(){
        button1.setText(R.string.minhaConta);
        button1.setTag("MINHA_CONTA");
        button1.setVisibility(View.VISIBLE);
        button1.setEnabled(true);
        button2.setText(R.string.jogar);
        button2.setTag("JOGAR");
        button2.setVisibility(View.VISIBLE);
        button2.setEnabled(true);
        button3.setText(R.string.revisar);
        button3.setTag("REVISAR");
        button3.setVisibility(View.VISIBLE);
        button3.setEnabled(true);
        button4.setText(R.string.sobreJogo);
        button4.setTag("SOBRE_O_JOGO");
        button4.setVisibility(View.VISIBLE);
        button4.setEnabled(true);
        voltar.setVisibility(View.INVISIBLE);
        voltar.setEnabled(false);
    }


    /**
     * Sobrescreve o método que define o que deve ser feito quando o botão
     * pressionar do celular for pressionado. Como não quero que ele seja
     * utilizado, o método fica vazio
     */
    @Override
    public void onBackPressed() {
        // Não faz nada
    }
}