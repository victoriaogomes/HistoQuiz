package com.example.histoquiz.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.histoquiz.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Classe responsável por exibir métricas relativas ao desempenho desse usuário no jogo
 */
public class PerformanceFragment extends Fragment {

    protected Context context;
    protected View performanceView;
    protected FirebaseFirestore firestore;
    protected TextView playedMatches, victoryText, defeatText;
    protected TextView [] reproductorSis, digestSis, cardioSis, osteoSis;
    protected ProgressBar victory, defeat;



    /**
     * Construtor da classe desse fragmento.
     * @param context - contexto da activity na qual ele está sendo criado.
     */
    public PerformanceFragment(Context context) {
        this.context = context;
    }


    /**
     * Método chamado no instante que um fragment desse tipo é instanciado, para que ele instancie
     * a view correspondente a ele na interface do usuário.
     * @param inflater - LayoutInflater que pode ser usado para exibir a view desse fragmento.
     * @param container - Se não for nulo, é a view pai à qual a view desse fragmento deve ser ane-
     *                    xada.
     * @param savedInstanceState - Se não for nulo, este fragmento está sendo reconstruído a partir
     *                             de um estado salvo anteriormente, conforme indicado nesse Bundle.
     * @return - retorna a view criada para esse fragmento.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        performanceView = inflater.inflate(R.layout.fragment_performance, container, false);
        initGui();
        return performanceView;
    }


    /**
     * Método utilizado para obter uma referência para os elementos da view que está sendo exibida,
     * que serão utilizados para mudar algumas de suas configurações. Além disso, inicializa algumas
     * variáveis que serão utilizadas.
     */
    protected void initGui(){
        firestore = FirebaseFirestore.getInstance();
        victory = performanceView.findViewById(R.id.victoryProgress);
        victoryText = performanceView.findViewById(R.id.percentVitorias);
        defeat = performanceView.findViewById(R.id.defeatProgress);
        defeatText = performanceView.findViewById(R.id.percentDerrotas);
        playedMatches = performanceView.findViewById(R.id.numPartidas);
        reproductorSis = new TextView[]{performanceView.findViewById(R.id.erroSisRepro), performanceView.findViewById(R.id.hitSisRepro)};
        digestSis = new TextView[]{performanceView.findViewById(R.id.erroSisDiges), performanceView.findViewById(R.id.hitSisDiges)};
        cardioSis = new TextView[]{performanceView.findViewById(R.id.erroSisCardio), performanceView.findViewById(R.id.hitSisCardio)};
        osteoSis = new TextView[]{performanceView.findViewById(R.id.erroSisOsteo), performanceView.findViewById(R.id.hitSisOsteo)};
        getPerformanceData();
    }


    /**
     * Método utilizado para obter do firebase os dados relativos a performance desse usuário nas
     * partidas que ele jogou
     */
    @SuppressWarnings("unchecked")
    protected void getPerformanceData(){
        firestore.document("desempenho/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(documentSnapshot -> setPerformanceData(documentSnapshot.get("numPartidas").toString(),
                documentSnapshot.get("vitorias").toString(),
                (ArrayList<Long>)documentSnapshot.get("sisCardiopulmonar"),
                (ArrayList<Long>)documentSnapshot.get("sisDigestorio"),
                (ArrayList<Long>)documentSnapshot.get("sisOsteomuscular"),
                (ArrayList<Long>)documentSnapshot.get("sisReprodutor")));
    }


    /**
     * Método utilizado para exibir na interface as informações relativas ao desempenho do jogador
     * nas partidas que ele jogou
     * @param numMatches - número total de partidas jogadas pelo usuário
     * @param vitories - número de partidas que o usuário ganhou
     * @param sisCardio -dados relativos ao desempenho do jogador relativos ao sistema
     *                   Cardiopulmonar
     * @param sisDigest - dados relativos ao desempenho do jogador relativos ao sistema digestório
     * @param sisOsteo - dados relativos ao desempenho do jogador relativos ao sistema osteomuscular
     * @param sisRep - dados relativos ao desempenho do jogador relativos ao sistema reprodutor
     */
    protected void setPerformanceData(String numMatches, String vitories, ArrayList<Long> sisCardio, ArrayList<Long> sisDigest, ArrayList<Long> sisOsteo, ArrayList<Long> sisRep){
        cardioSis[0].setText(String.format(Locale.getDefault(), "%s", sisCardio.get(0).toString()));
        cardioSis[1].setText(String.format(Locale.getDefault(), "%s", sisCardio.get(1).toString()));
        digestSis[0].setText(String.format(Locale.getDefault(), "%s", sisDigest.get(0).toString()));
        digestSis[1].setText(String.format(Locale.getDefault(), "%s", sisDigest.get(1).toString()));
        osteoSis[0].setText(String.format(Locale.getDefault(), "%s", sisOsteo.get(0).toString()));
        osteoSis[1].setText(String.format(Locale.getDefault(), "%s", sisOsteo.get(1).toString()));
        reproductorSis[0].setText(String.format(Locale.getDefault(), "%s", sisRep.get(0).toString()));
        reproductorSis[1].setText(String.format(Locale.getDefault(), "%s", sisRep.get(1).toString()));
        playedMatches.setText(numMatches);
        if(Integer.parseInt(numMatches) == 0){
            victory.setProgress(0);
            defeat.setProgress(0);
        }
        else{
            victory.setProgress((Integer.parseInt(vitories)*100)/Integer.parseInt(numMatches));
            defeat.setProgress(100 - victory.getProgress());
        }
        victoryText.setText(victory.getProgress() + "%");
        defeatText.setText(defeat.getProgress() + "%");
    }
}