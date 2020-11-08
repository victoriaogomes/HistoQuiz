package com.example.histoquiz.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.histoquiz.R;
import com.example.histoquiz.dialogs.EndGameDialog;
import com.example.histoquiz.dialogs.GuessSlideDialog;
import com.example.histoquiz.dialogs.QuestionFeedBackDialog;
import com.example.histoquiz.dialogs.SelectQuestionDialog;
import com.example.histoquiz.dialogs.SlideImageDialog;
import com.example.histoquiz.model.Slide;
import com.example.histoquiz.util.ComputerOpponent;
import com.example.histoquiz.util.OnlineOpponent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;


/**
 * Classe utilizada para manipular atividades relativas a uma partida que são comuns a todas as mo-
 * dalidades de jogo (online - 1 vs 1, contra o computador e local - 2 vs 2)
 */
public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    public Map<Integer, Slide> mySlides = new HashMap<>();
    public FirebaseFirestore firestoreDatabase;
    public LinearLayout dialogs;
    protected String opponentUID;
    protected FirebaseUser user;
    public boolean matchCreator, PCopponent;
    public HashMap<String, Map<String, Object>> perguntas;
    public String slideToGuess = "firstSlide";
    protected int category, question;
    public TextView questionText, scorePlayer1, scorePlayer2;
    protected Button yesAnswer, noAnswer;
    protected ImageButton selectQuestionButton;
    public HashMap<Integer, Slide> slides = new HashMap<>();
    protected CountDownTimer countDownTimer;
    public TextView timer;
    protected volatile boolean questionsDone = false, slidesDone = false;
    protected ImageButton [] opponentSlidesButtons;
    protected ImageView [] opponentSlidesCheck;
    protected ImageView [] mySlidesCheck;

    //Instância de classes utilizadas para controlar especificidades de certos modos de jogo
    public ComputerOpponent computerOpponent;
    public OnlineOpponent onlineOpponent;

    // Dialogs utilizados para exibir as "subtelas" necessárias no jogo
    protected SelectQuestionDialog selectQuestionDialog;
    protected GuessSlideDialog guessSlideDialog;
    protected QuestionFeedBackDialog questionFeedBackDialog;
    protected SlideImageDialog slideImageDialog;
    protected EndGameDialog endGameDialog;

    // Variáveis para o controle da tela como fullscreen
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHideRunnable = this::hide;


    /**
     * Método executado no instante em que essa activity é criada, seta qual view será associada a
     * essa classe
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        Intent intent = getIntent();
        matchCreator = intent.getBooleanExtra("matchCreator", true);
        opponentUID = intent.getStringExtra("opponentUID");
        PCopponent = intent.getBooleanExtra("PCopponent", true);
        setContentView(R.layout.activity_game);
        initGUI();
        getSlides();
        getQuestions();
        countDownTimer = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }
            @Override
            public void onFinish() {
                if(!(questionsDone && slidesDone)){
                    countDownTimer.start();
                }
                else{
                    if(!PCopponent){ // Caso o oponente dessa partida NÃO seja o computador
                        onlineOpponent = new OnlineOpponent(GameActivity.this, opponentUID, matchCreator);
                    }
                    else {
                        computerOpponent = new ComputerOpponent(GameActivity.this, perguntas, slides);
                    }
                }
            }
        }.start();
        mContentView = findViewById(R.id.fullContent);
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
     * Método utilizado para fazer a primeira chamada ao método delayedHide, logo após a activitie
     * ser criada, unicamente para exibir brevemente ao usuário que os controles de tela estão
     * disponíveis
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(null);
        delayedHide();
    }


    /**
     * Programa uma chamada ao método hide() após uma quantidade delayMillis de millisegundos,
     * cancelando qualquer chamada programada previamente
     */
    private void delayedHide() {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, 100);
    }


    /**
     * Método utilizado para esconder a barra de botões
     */
    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }


    /**
     * Método utilizado para obter do firebase as perguntas que estão cadastradas no banco de dados
     */
    protected void getQuestions(){
        firestoreDatabase.collection("perguntas").get().addOnSuccessListener(queryDocumentSnapshots -> {
            perguntas = new HashMap<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                perguntas.put(document.getId(), document.getData());
            }
            questionsDone = true;
        });
    }


    /**
     * Método utilizado para obter do firebase as lâminas disponíveis para serem utilizadas durante
     * essa partida
     */
    protected void getSlides(){
        firestoreDatabase.collection("laminas").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                Slide slide = documentSnapshot.toObject(Slide.class);
                slide.setName(documentSnapshot.getId());
                slides.put(slide.getCode(), slide);
            }
            raffleSlides(queryDocumentSnapshots.size(), slides, 6);
            slidesDone = true;
        });
    }


    /**
     * Método utilizado para obter a referência para certos objetos da view que serão manipulados
     * por essa classe e para inicializar alguns outros objetos utilizados aqui no back-end
     */
    protected void initGUI(){
        questionText = findViewById(R.id.pergunta);
        dialogs = findViewById(R.id.dialogs);
        yesAnswer = findViewById(R.id.respSim);
        noAnswer = findViewById(R.id.respNao);
        scorePlayer1 = findViewById(R.id.pontuacaoJogador1);
        scorePlayer2 = findViewById(R.id.pontuacaoJogador2);
        timer = findViewById(R.id.timer);
        selectQuestionButton = findViewById(R.id.escolherPergunta);
        opponentSlidesButtons = new ImageButton[]{findViewById(R.id.oponenteLamina1), findViewById(R.id.oponenteLamina2), findViewById(R.id.oponenteLamina3)};
        opponentSlidesCheck = new ImageView[]{findViewById(R.id.oponenteCheckSlide1), findViewById(R.id.oponenteCheckSlide2), findViewById(R.id.oponenteCheckSlide3)};
        mySlidesCheck = new ImageView[]{findViewById(R.id.meuCheckSlide1), findViewById(R.id.meuCheckSlide2), findViewById(R.id.meuCheckSlide3)};
        opponentSlidesButtons[0].setOnClickListener(this);
        opponentSlidesButtons[0].setTag("OPPONENT_SLIDE_BUTTON");
        opponentSlidesButtons[1].setOnClickListener(this);
        opponentSlidesButtons[1].setTag("OPPONENT_SLIDE_BUTTON");
        opponentSlidesButtons[2].setOnClickListener(this);
        opponentSlidesButtons[2].setTag("OPPONENT_SLIDE_BUTTON");
        questionText.setText(getString(R.string.configJogo));
        yesAnswer.setVisibility(View.INVISIBLE);
        yesAnswer.setTag("YES_ANSWER");
        yesAnswer.setOnClickListener(this);
        noAnswer.setVisibility(View.INVISIBLE);
        noAnswer.setTag("NO_ANSWER");
        noAnswer.setOnClickListener(this);
        user = FirebaseAuth.getInstance().getCurrentUser();
        firestoreDatabase = FirebaseFirestore.getInstance();
        selectQuestionDialog = new SelectQuestionDialog(this);
        guessSlideDialog = new GuessSlideDialog(this);
        endGameDialog = new EndGameDialog(this);
    }


    /**
     * Método utilizado para exibir ao usuário a tela para que ele selecione uma questão para
     * enviar para seu oponente responder
     */
    public void showQuestionSelection(){
        selectQuestionDialog.show(getSupportFragmentManager(), "choose question dialog");
        hide();
    }


    /**
     * Método utilizado para deixar de exibir a tela utilizada para selecionar uma questão
     * para o oponente responder
     */
    public void closeQuestionSelection(){
        selectQuestionDialog.dismiss();
    }


    /**
     * Método utilizado para exibir ao usuário a tela para que ele adivinhe a sua lâmina
     * atual
     */
    public void showGuessSlide(){
        guessSlideDialog.show(getSupportFragmentManager(), "guess dialog");
        hide();
    }


    /**
     * Método utilizado para deixar de exibir ao usuário a tela para que ele adivinhe sua lâmina
     * atual
     */
    public void closeGuessSlide(){
        guessSlideDialog.dismiss();
    }


    /**
     * Método utilizado para exibir ao usuário a resposta a sua pergunta dada pelo seu oponente,
     * bem como a resposta correta dela
     */
    public void showQuestionFeedback(Boolean opponentAnswer, boolean correctAnswer){
        questionFeedBackDialog = new QuestionFeedBackDialog(this, opponentAnswer, correctAnswer);
        questionFeedBackDialog.show(getSupportFragmentManager(), "questionFeedBack");
        hide();
    }


    /**
     * Método utilizado para fechar o dialog responsável por fornecer ao jogador um feedback relati-
     * vo a resposta da pergunta que ele selecionou para enviar ao seu oponente
     */
    public void closeQuestionFeedback(){
        questionFeedBackDialog.dismiss();
    }


    /**
     * Método utilizado para exibir para o jogador a dialog contendo as imagens da lâmina que seu
     * oponente está tentando adivinhar no momento
     */
    public void showSlideImages(){
        slideImageDialog = new SlideImageDialog(this);
        slideImageDialog.show(getSupportFragmentManager(), "slide");
        hide();
    }


    /**
     * Método utilizado para fechar o dialog que exibe a imagem das lâminas que o oponente desse
     * jogador tenta adivinhar durante a partida
     */
    public void closeSlideImages(){
        slideImageDialog.dismiss();
    }


    /**
     * Método utilizado para exibir a mensagem recebida por parâmetro ao jogador
     * @param text - mensagem que deve ser exibida ao jogador
     */
    public void showTextToPlayer(String text){
        this.questionText.setText(text);
        this.questionText.setVisibility(View.VISIBLE);
        yesAnswer.setVisibility(View.INVISIBLE);
        noAnswer.setVisibility(View.INVISIBLE);
    }


    /**
     * Método utilizado para exibir ao jogador um fragmento contendo a pontuação de cada um dos
     * jogadores da partida que acabou de ser finalizada, informando quem é o ganhador
     */
    public void showEndGameDialog(){
        final FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(endGameDialog, "endGame").commitAllowingStateLoss();
        hide();
    }


    /**
     * Método utilizado para sortear as lâminas que este jogador deverá adivinhar e armazená-las
     * no firebase. Cada jogador é responsável por sortear suas lâminas e cadastrá-las na nuvem,
     * para que o seu oponente as obtenha
     */
    protected void raffleSlides(int slidesAmount, HashMap<Integer, Slide> slides, int raffleNumber) {
        Random rndGenerator = new Random();
        int raffledValue;
        mySlides.put(41, slides.get(41)); // Fibrocartilagem
        mySlides.put(40, slides.get(40)); // Cartilagem hialina
        mySlides.put(42, slides.get(42)); // Cartilagem elástica
        mySlides.put(43, slides.get(43)); // Tecido ósseo
        mySlides.put(46, slides.get(46)); // Placa metafisária
        mySlides.put(44, slides.get(44)); // Osteócito

//        for (int i = 0; i < raffleNumber; i++) {
//            raffledValue = rndGenerator.nextInt(slidesAmount);
//            while (mySlides.containsKey(raffledValue)) {
//                raffledValue = rndGenerator.nextInt(slidesAmount);
//            }
//            mySlides.put(raffledValue, slides.get(raffledValue));
//        }
    }


    /**
     * Método chamado a partir do momento que a execução dessa atividade é iniciada
     * @param requestCode - código de solicitação fornecido pelo método startActivityForResult(),
     *                      permitindo identificar de quem esse resultado veio.
     * @param resultCode - código de resultado retornado pela atividade filho por meio do método
     *                     setResult()
     * @param data - dados que foram retornados para essa activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999) {
            if(resultCode == RESULT_OK) {
                matchCreator = data.getBooleanExtra("matchCreator", true);
                opponentUID = data.getStringExtra("opponentUID");
                PCopponent = data.getBooleanExtra("PCopponent", true);
            }
        }
    }


    /**
     * Método utilizado para setar a categoria da questão selecionada pelo jogador para o seu opo-
     * nente responder
     * @param category - inteiro que identifica a categoria selecionada
     */
    public void setCategory(int category) {
        this.category = category;
    }


    /**
     * Método utilizado para setar a questão selecionada pelo jogador para seu oponente responder
     * @param question - inteiro que identifica a questão selecionada na lista de questões possíveis
     *                   para a categoria escolhida
     */
    public void setQuestion(int question) {
        this.question = question;
    }


    /**
     * Método utilizado para retornar a categoria da questão selecionada pelo jogador para enviar
     * para seu oponente responder
     * @return - inteiro que identifica a categoria selecionada na lista de categorias possíveis
     */
    public int getCategory(){
        return category;
    }


    /**
     * Método utilizado para retornar a questão selecionada pelo jogador para enviar para seu
     * oponente responder
     * @return - inteiro que identifica a questão selecionada na lista de questões possíveis para a
     *           categoria escolhida
     */
    public int getQuestion(){
        return question;
    }


    /**
     * Método que exibe na view a pergunta que o oponente enviou para este jogador responder
     * @param questionText - texto da questão a ser respondida
     */
    public void setQuestionForPlayerAnswer(String questionText) {
        this.questionText.setText(questionText);
        this.questionText.setVisibility(View.VISIBLE);
        yesAnswer.setVisibility(View.VISIBLE);
        noAnswer.setVisibility(View.VISIBLE);
    }


    /**
     * Método utilizado para realizar uma transição de estados após o usuário selecionar a pergunta
     * que deseja enviar ao oponente
     */
    public void handleQuestionSelectionButton(){
        if(PCopponent) computerOpponent._estado_G();
        else onlineOpponent._estado_F();

    }


    /**
     * Método utilizado para retornar o texto de uma questão selecionada
     * @param category - inteiro representado a categoria a qual a pergunta pertence
     * @param question - inteiro que representa a questão selecionada dentre as disponíveis nesta
     *                   categoria
     * @return - texto da questão selecionada
     */
    public String getQuestionText(int category, int question){
        String[] questions = Objects.requireNonNull(perguntas.get(getCategoryName(category))).keySet().toArray(new String[0]);
        Arrays.sort(questions, (o1, o2) -> {
            Collator usCollator = Collator.getInstance(new Locale("pt", "BR"));
            return usCollator.compare(o1, o2);
        });
        return questions[question];
    }


    /**
     * Método utilizado para obter o nome da categoria associada a determinado número
     * @param category - número relativo a categoria cujo nome deseja-se obter
     * @return - nome da categoria associada ao inteiro recebido por parâmetro
     */
    public String getCategoryName(int category){
        String[] categories = perguntas.keySet().toArray(new String[0]);
        Arrays.sort(categories, (o1, o2) -> {
            Collator usCollator = Collator.getInstance(new Locale("pt", "BR"));
            return usCollator.compare(o1, o2);
        });
        return categories[category];
    }


    /**
     * Método utilizado para pegar a resposta da pergunta atual relativa a lâmina atual que foi ca-
     * dastrada no firebase
     * @param category - categoria da pergunta
     * @param question - pergunta selecionada na lista disponível para essa categoria
     * @param slide - lâmina relativa a qual a pergunta deve ser respondida
     * @return - retorna true caso a resposta seja verdadeira, e false caso contrário
     */
    @SuppressWarnings("unchecked") // usado para suprimir o warning relativo ao cast no ArrayList
    public boolean getQuestionRealAnswer(int category, int question, int slide){
        String cat = getCategoryName(category);
        String quest = getQuestionText(category, question);
        ArrayList<Boolean> respostas = (ArrayList<Boolean>) Objects.requireNonNull(perguntas.get(cat)).get(quest);
        assert respostas != null;
        return respostas.get(slide);
    }


    /**
     * Método utilizado para modificar a pontuação de determinado player. Ele soma a pontuação atual
     * o valor recebido em "pontuation", que pode corresponder a um acréscimo (caso pontuation > 0)
     * ou um decréscimo (caso pontuation < 0)
     * @param player - jogador cuja pontuação deve ser modificada (1 ou 2)
     * @param pontuation - valor que deve ser adicionado (ou decrementado) a pontuação atual do player
     */
    public void changePlayerScore(int player, int pontuation){
        switch (player){
            case 1:
                if(getPlayerScore(1) + pontuation > 0) {
                    scorePlayer1.setText(String.format(Locale.getDefault(), "%d", getPlayerScore(1) + pontuation));
                }
                else{
                    scorePlayer1.setText("0");
                }
                if(!PCopponent) onlineOpponent.myRoomRef.child("score").setValue(getPlayerScore(1));
                break;
            case 2:
                if(getPlayerScore(2) + pontuation > 0) {
                    scorePlayer2.setText(String.format(Locale.getDefault(), "%d", getPlayerScore(2) + pontuation));
                }
                else{
                    scorePlayer2.setText("0");
                }
                if(!PCopponent) onlineOpponent.opponentRoomRef.child("score").setValue(getPlayerScore(2));
                break;
        }
    }


    /**
     * Método utilizado para obter o score de um determinado player na partida atual
     * @param player - número correspondente ao jogador (1 ou 2)
     * @return - pontuação do jogador selecionado
     */
    public int getPlayerScore(int player){
        switch (player){
            case 1: return Integer.parseInt(scorePlayer1.getText().toString());
            case 2: return Integer.parseInt(scorePlayer2.getText().toString());
            default: return 0;
        }
    }


    /**
     * Método utilizado para colocar o símbolo de ✓ no quadradinho correspondente a lâmina que foi
     * adivinhada
     * @param position - posição da lâmina adivinhada (0, 1 ou 2)
     * @param player - jogador cuja lâmina foi adivinhada (1 ou 2)
     */
    public void checkSlide(int position, int player){
        switch (player){
            case 1:
                mySlidesCheck[position].setVisibility(View.VISIBLE);
                switch (position){
                    case 0:
                        if(PCopponent) slideToGuess = "secondSlide";
                        else{
                            onlineOpponent.myRoomRef.child("slideToGuess").setValue("secondSlide");
                            onlineOpponent.mySlideToGuess = "secondSlide";
                        }
                        break;
                    case 1:
                        if(PCopponent) slideToGuess = "thirdSlide";
                        else{
                            onlineOpponent.myRoomRef.child("slideToGuess").setValue("thirdSlide");
                            onlineOpponent.mySlideToGuess = "thirdSlide";
                        }
                        break;
                    case 2: break;
                }
                break;
            case 2:
                opponentSlidesCheck[position].setVisibility(View.VISIBLE);
                switch (position){
                    case 0:
                        if(PCopponent) slideToGuess = "secondSlide";
                        break;
                    case 1:
                        if(PCopponent) slideToGuess = "thirdSlide";
                        break;
                    case 2: break;
                }
                break;
        }
    }


    /**
     * Método utilizado para lidar com os cliques nos botões da interface do jogo
     * @param v - Objeto da view que recebeu o clique
     */
    @Override
    public void onClick(View v) {
        switch (v.getTag().toString()){
            case "OPPONENT_SLIDE_BUTTON":  // botão de visualizar fotos da lâmina atual
                showSlideImages();
                break;
            case "YES_ANSWER":             // botão de responder pergunta afirmativamente
                if (PCopponent){
                    computerOpponent._estado_C(true);
                }
                else{
                    onlineOpponent._estado_C(true);
                }
                break;
            case "NO_ANSWER":              // botão de responder pergunta negativamente
                if(PCopponent) {
                    computerOpponent._estado_C(false);
                }
                else{
                    onlineOpponent._estado_C(false);
                }
                break;
            case "BACK_MENU":
                endGameDialog.dismiss();
                Intent troca = new Intent(this, MenuActivity.class);
                startActivity(troca);
        }
    }
}