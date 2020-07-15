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
        if (raffledValue % 2 == 0) {
            game_scene.showTextToWaitOpponent("Aguardando oponente selecionar uma pergunta...");
            (new Handler()).postDelayed(this::_estado_000, 5000);
        }
        else {
//            _estado_011();
            _estado_000();
        }
    }

    public void _estado_000(){ // Estado 000: selecionar pergunta para enviar ao jogador
        raffledCategory = generateRaffledValue(game_scene.perguntas.keySet().size(), 0);
        numberOfQuestions = Objects.requireNonNull(game_scene.perguntas.get(game_scene.perguntas.keySet().toArray()[raffledCategory])).size();
        raffledQuestion = generateRaffledValue(numberOfQuestions, 0);
        game_scene.setQuestionForPlayerAnswer(game_scene.getQuestionText(raffledCategory, raffledQuestion));
    }

    public void _estado_001(boolean answer){ // Estado 001: verificar a resposta do jogador
        Object [] keySet = game_scene.mySlides.keySet().toArray();
        switch (slideToGuess){
            case "firstSlide":
                if(game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, Integer.parseInt(keySet[3].toString())) == answer){
                    game_scene.incrementPlayerScore(1);
                }
                break;
            case "secondSlide":
                if(game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, Integer.parseInt(keySet[4].toString())) == answer){
                    game_scene.incrementPlayerScore(1);
                }
                break;
            case "thirdSlide":
                if(game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, Integer.parseInt(keySet[5].toString())) == answer){
                    game_scene.incrementPlayerScore(1);
                }
                break;
        }
        _estado_011();
    }

    protected void _estado_010(){ // Estado 010: tentar adivinhar minha lâmina

    }

    protected void _estado_011(){ // Estado 011: aguardar jogador escolher uma pergunta
        game_scene.showQuestionSelection();
    }


    public void _estado_100(){ // Estado 100: responder pergunta do jogador
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

    protected void _estado_101() { // Estado 101: aguardar oponente tentar adivinhar sua lâmina
        _estado_000();
    }

    protected void _estado_110(){ // Estado 110: fim da máquina de estados

    }

    protected int generateRaffledValue(int limit, int start){
        return rndGenerator.nextInt(limit) + start;
    }
}