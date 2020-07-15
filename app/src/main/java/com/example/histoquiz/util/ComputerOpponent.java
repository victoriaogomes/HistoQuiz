package com.example.histoquiz.util;

import com.example.histoquiz.activities.GameActivity;

import java.util.Objects;
import java.util.Random;
import android.os.Handler;

public class ComputerOpponent {

//Estados:
    // Estado 000: selecionar pergunta para enviar ao oponente
    // Estado 001: aguardar oponente responder minha pergunta
    // Estado 010: tentar adivinhar minha lâmina
    // Estado 011: aguardar oponente escolher uma pergunta
    // Estado 100: responder pergunta do meu oponente
    // Estado 101: aguardar oponente tentar adivinhar sua lâmina
    // Estado 110: fim da máquina de estados

    protected GameActivity game_scene; // Cena do jogo
    public String slideToGuess = "firstSlide";
    protected Random rndGenerator;
    protected int raffledCategory, raffledQuestion, numberOfQuestions, raffledValue;
    boolean myAnswer;

    public ComputerOpponent(GameActivity game_scene){
        this.game_scene = game_scene;
        rndGenerator = new Random();
        raffledValue = generateRaffledValue(100, 1);
//        if (raffledValue % 2 == 0) {
            _estado_0000();
//        }
//        else {
//            _estado_011();
//            _estado_000();
//        }
    }

    public void _estado_0000(){
        game_scene.showTextToWaitOpponent("Aguardando oponente selecionar uma pergunta...");
        (new Handler()).postDelayed(this::_estado_0001, 2000);
    }

    public void _estado_0001(){
        raffledCategory = generateRaffledValue(game_scene.perguntas.keySet().size(), 0);
        numberOfQuestions = Objects.requireNonNull(game_scene.perguntas.get(game_scene.perguntas.keySet().toArray()[raffledCategory])).size();
        raffledQuestion = generateRaffledValue(numberOfQuestions, 0);
        game_scene.setQuestionForPlayerAnswer(game_scene.getQuestionText(raffledCategory, raffledQuestion));
    }

    public void _estado_0010(boolean answer){
        game_scene.showTextToWaitOpponent("Validando resposta...");
        (new Handler()).postDelayed(() -> _estado_0011(answer), 2000);
    }

    public void _estado_0011(boolean answer){
        Object [] keySet = game_scene.mySlides.keySet().toArray();
        String text = "";
        switch (slideToGuess){
            case "firstSlide":
                if(game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, Integer.parseInt(keySet[3].toString())) == answer){
                    game_scene.incrementPlayerScore(1);
                    text = "ganhou 2 pontos!";
                }
                else{
                    game_scene.decrementPlayerScore(1);
                    text = "perdeu 1 ponto!";
                }
                break;
            case "secondSlide":
                if(game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, Integer.parseInt(keySet[4].toString())) == answer){
                    game_scene.incrementPlayerScore(1);
                    text = "ganhou 2 pontos!";
                }
                else{
                    game_scene.decrementPlayerScore(1);
                    text = "perdeu 1 ponto!";
                }
                break;
            case "thirdSlide":
                if(game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, Integer.parseInt(keySet[5].toString())) == answer){
                    game_scene.incrementPlayerScore(1);
                    text = "ganhou 2 pontos!";
                }
                else{
                    game_scene.decrementPlayerScore(1);
                    text = "perdeu 1 ponto!";
                }
                break;
        }
        game_scene.showTextToWaitOpponent("Você " + text + " Aguardando oponente analisar sua resposta...");
        (new Handler()).postDelayed(this::_estado_0100, 2000);
    }

    public void _estado_0100(){
        game_scene.showQuestionSelection();
    }

    public void _estado_0101(){
        game_scene.closeQuestionSelection();
        game_scene.showTextToWaitOpponent("Enviando...");
        (new Handler()).postDelayed(this::_estado_0110, 2000);
    }

    public void _estado_0110(){
        game_scene.showTextToWaitOpponent("Aguardando resposta do oponente...");
        (new Handler()).postDelayed(this::_estado_0111, 2000);
    }

    public void _estado_0111(){
        raffledValue = generateRaffledValue(100, 1);
        myAnswer = raffledValue % 2 == 0;
        Object [] keySet = game_scene.mySlides.keySet().toArray();
        game_scene.closeQuestionSelection();
        switch (game_scene.slideToGuess){
            case "firstSlide":
                if(game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, Integer.parseInt(keySet[0].toString())) == myAnswer){
                    game_scene.incrementPlayerScore(2);
                }
                game_scene.showQuestionFeedback(myAnswer, game_scene.getQuestionRealAnswer(game_scene.getCategory(), game_scene.getQuestion(), Integer.parseInt(keySet[0].toString())));
                break;
            case "secondSlide":
                if(game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, Integer.parseInt(keySet[1].toString())) == myAnswer){
                    game_scene.incrementPlayerScore(2);
                }
                game_scene.showQuestionFeedback(myAnswer, game_scene.getQuestionRealAnswer(game_scene.getCategory(), game_scene.getQuestion(), Integer.parseInt(keySet[1].toString())));
                break;
            case "thirdSlide":
                if(game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, Integer.parseInt(keySet[2].toString())) == myAnswer){
                    game_scene.incrementPlayerScore(2);
                }
                game_scene.showQuestionFeedback(myAnswer, game_scene.getQuestionRealAnswer(game_scene.getCategory(), game_scene.getQuestion(), Integer.parseInt(keySet[2].toString())));
                break;
        }
    }


    public void _estado_1000(){
        game_scene.showGuessSlide();
    }


    public void _estado_1001(String answer){
        Object [] keySet = game_scene.mySlides.keySet().toArray();
        boolean answerValidation = false;
        switch (game_scene.slideToGuess){
            case "firstSlide":
                if(game_scene.mySlides.get(keySet[0]).equals(answer)){
                    game_scene.incrementPlayerScore(2);
                    answerValidation = true;
                    game_scene.slideToGuess = "secondSlide";
                }
                break;
            case "secondSlide":
                if(game_scene.mySlides.get(keySet[1]).equals(answer)){
                    game_scene.incrementPlayerScore(2);
                    answerValidation = true;
                    game_scene.slideToGuess = "thirdSlide";
                }
                break;
            case "thirdSlide":
                if(game_scene.mySlides.get(keySet[2]).equals(answer)){
                    game_scene.incrementPlayerScore(2);
                    answerValidation = true;
                }
                break;
        }
        _estado_1010(answerValidation);
    }

    public void _estado_1010(boolean answerValidation){
        game_scene.closeGuessSlide();
        if(answerValidation){
            game_scene.showTextToWaitOpponent("Resposta correta! Você ganhou 2 pontos! Vamos para a próxima rodada...");
        }
        else{
            game_scene.showTextToWaitOpponent("Resposta incorreta! Vamos para a próxima rodada...");
        }
        (new Handler()).postDelayed(this::_estado_0000, 2000);
    }


    protected int generateRaffledValue(int limit, int start){
        return rndGenerator.nextInt(limit) + start;
    }
}