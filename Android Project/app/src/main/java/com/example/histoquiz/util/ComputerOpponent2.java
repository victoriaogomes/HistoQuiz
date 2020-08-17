package com.example.histoquiz.util;

import android.os.Handler;

import com.example.histoquiz.activities.GameActivity;
import com.example.histoquiz.model.Slide;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class ComputerOpponent2 {

    protected GameActivity game_scene; // Cena do jogo
    public String slideToGuess = "firstSlide";
    protected Random rndGenerator;
    protected int raffledCategory, raffledQuestion, numberOfQuestions, raffledValue;
    boolean myAnswer;
    protected String currentState;
    protected boolean general = true; // Variável para indicar se o PC já pode selecionar perguntas de outra categoria
    protected HashMap<String, Map<String, Object>> perguntas;
    protected HashMap<Integer, Slide> slides;
    protected boolean trueAnswer;

    public ComputerOpponent2(GameActivity game_scene, HashMap<String, Map<String, Object>> perguntas, HashMap<Integer, Slide> slides){
        this.game_scene = game_scene;
        this.perguntas = perguntas;
        this.slides = slides;
        for(int i=0; i<perguntas.keySet().size(); i++){
            if(perguntas.keySet().toArray()[i].equals("Gerais")){
                raffledCategory = i;
            }
        }
        rndGenerator = new Random();
        raffledValue = generateRaffledValue(100, 1);
        _estado_A();
    }

    public void _estado_A(){
        game_scene.showTextToWaitOpponent("Aguardando oponente selecionar uma pergunta...");
        (new Handler()).postDelayed(this::_estado_B, 2000);
    }

    public void _estado_B(){
        if(!general){
            raffledCategory = generateRaffledValue(perguntas.keySet().size(), 0);
        }
        numberOfQuestions = Objects.requireNonNull(perguntas.get(perguntas.keySet().toArray()[raffledCategory])).size();
        raffledQuestion = generateRaffledValue(numberOfQuestions, 0);
        game_scene.setQuestionForPlayerAnswer(game_scene.getQuestionText(raffledCategory, raffledQuestion));
    }

    public void _estado_C(boolean answer){
        game_scene.showTextToWaitOpponent("Validando resposta...");
        (new Handler()).postDelayed(() -> _estado_D(answer), 2000);
    }

    public void _estado_D(boolean answer){
        Object [] keySet = game_scene.mySlides.keySet().toArray();
        String text = "";
        switch (slideToGuess){
            case "firstSlide":
                trueAnswer = game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, Integer.parseInt(keySet[3].toString()));
                break;
            case "secondSlide":
                trueAnswer = game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, Integer.parseInt(keySet[4].toString()));
                break;
            case "thirdSlide":
                trueAnswer = game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, Integer.parseInt(keySet[5].toString()));
                break;
        }
        if(trueAnswer == answer){
            game_scene.changePlayerScore(1, 1);
            text = "ganhou 2 pontos!";
        }
        else{
            game_scene.changePlayerScore(1, -1);
            text = "perdeu 1 ponto!";
        }
        if(trueAnswer) general = false;
        game_scene.showTextToWaitOpponent("Você " + text + " Aguardando oponente analisar sua resposta...");
        (new Handler()).postDelayed(() ->_estado_E(trueAnswer), 2000);
    }

    public void _estado_E(boolean trueAnswer){
        for (Map.Entry<Integer,Slide> pair : slides.entrySet()) {
            if (game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, pair.getKey()) != trueAnswer){
                slides.remove(pair.getKey());
            }
        }
        if(slides.size() <= 3){
            Object [] keySet = game_scene.mySlides.keySet().toArray();
            String slideName = "";
            switch (slideToGuess){
                case "firstSlide":
                    slideName = game_scene.mySlides.get(Integer.parseInt(keySet[3].toString())).getName();
                    break;
                case "secondSlide":
                    slideName = game_scene.mySlides.get(Integer.parseInt(keySet[4].toString())).getName();
                    break;
                case "thirdSlide":
                    slideName = game_scene.mySlides.get(Integer.parseInt(keySet[5].toString())).getName();
                    break;
            }
            if(slideName.equals(slides.get(slides.keySet().toArray()[0]).getName())){
                game_scene.showTextToWaitOpponent("Seu oponente adivinhou sua lâmina e ganhou 3 pontos!");
                game_scene.changePlayerScore(2, 3);
                slides = game_scene.slides;
            }
            else{
                game_scene.showTextToWaitOpponent("Seu oponente tentou adivinhar sua lâmina e errou. Você ganhou 3 pontos!");
                game_scene.changePlayerScore(1, 3);
                slides.remove(slides.keySet().toArray()[0]);
            }
            (new Handler()).postDelayed(this::_estado_F, 2000);
        }
        _estado_F();
    }

    public void _estado_F(){
        game_scene.showQuestionSelection();
    }

    public void _estado_G(){
        game_scene.closeQuestionSelection();
        game_scene.showTextToWaitOpponent("Enviando...");
        (new Handler()).postDelayed(this::_estado_H, 2000);
    }

    public void _estado_H(){
        game_scene.showTextToWaitOpponent("Aguardando resposta do oponente...");
        (new Handler()).postDelayed(this::_estado_I, 2000);
    }

    public void _estado_I(){
        raffledValue = generateRaffledValue(100, 1);
        myAnswer = raffledValue % 2 == 0;
        int slide = 0;
        Object [] keySet = game_scene.mySlides.keySet().toArray();
        game_scene.closeQuestionSelection();
        switch (game_scene.slideToGuess){
            case "firstSlide":
                slide =  Integer.parseInt(keySet[0].toString());
                break;
            case "secondSlide":
                slide =  Integer.parseInt(keySet[1].toString());
                break;
            case "thirdSlide":
                slide =  Integer.parseInt(keySet[2].toString());
                break;
        }
        if(game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, slide) == myAnswer){
            game_scene.changePlayerScore(2, 1);
        }
        else{
            game_scene.changePlayerScore(2, -1);
        }
        game_scene.showQuestionFeedback(myAnswer, game_scene.getQuestionRealAnswer(game_scene.getCategory(), game_scene.getQuestion(), slide));
    }

    public void _estado_J(){
        game_scene.showGuessSlide();
    }

    public void _estado_K(String answer){
        Object [] keySet = game_scene.mySlides.keySet().toArray();
        boolean answerValidation = false, matchEnded = false;
        String trueSlide = "";
        int position = 0;
        switch (game_scene.slideToGuess){
            case "firstSlide":
                trueSlide = Objects.requireNonNull(game_scene.mySlides.get(keySet[0])).getName().toLowerCase();
                position = 0;
                break;
            case "secondSlide":
                trueSlide = Objects.requireNonNull(game_scene.mySlides.get(keySet[1])).getName().toLowerCase();
                position = 1;
                break;
            case "thirdSlide":
                trueSlide = Objects.requireNonNull(game_scene.mySlides.get(keySet[2])).getName().toLowerCase();
                position = 2;
        }
        if(trueSlide.equals(answer.toLowerCase())){
            game_scene.changePlayerScore(1, 3);
            answerValidation = true;
            if(game_scene.slideToGuess.equals("thirdSlide")) matchEnded = true;
            game_scene.checkMySlide(position);
        }
        else{
            game_scene.changePlayerScore(2, 3);
        }
        _estado_L(answerValidation, matchEnded);
    }

    public void _estado_L(boolean answerValidation, boolean matchEnded){
        game_scene.closeGuessSlide();
        if(answerValidation){
            if(matchEnded) game_scene.showTextToWaitOpponent("Resposta correta! Você ganhou 3 pontos! Fim de jogo...");
            else game_scene.showTextToWaitOpponent("Resposta correta! Você ganhou 3 pontos! Vamos para a próxima rodada...");
        }
        else{
            game_scene.showTextToWaitOpponent("Resposta incorreta! Seu oponente ganhou 3 pontos! Vamos para a próxima rodada...");
        }
        if(matchEnded){
            (new Handler()).postDelayed(this::_estado_M, 2000);
        }
        else{
            (new Handler()).postDelayed(this::_estado_A, 2000);
        }
    }

    public void _estado_M(){
        game_scene.showEndGameDialog();
    }

    protected int generateRaffledValue(int limit, int start){
        return rndGenerator.nextInt(limit) + start;
    }

}
