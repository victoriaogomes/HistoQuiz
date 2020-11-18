package com.example.histoquiz.activities;

import com.example.histoquiz.R;
import com.example.histoquiz.adapters.MyAccountAdapter;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager2.widget.ViewPager2;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import java.util.Objects;

/**
 * Classe utilizada para exibir as informações relativas a contado usuário (dados pessoais, amigos,
 * solicitações de amizade e desempenho)
 */
public class MyAccountActivity extends AppCompatActivity {

    protected int tabSelectedIconColor, tabUnselectedIconColor;
    protected TabLayout tabLayout;
    protected TabItem tabPerfil, tabAmigos, tabDesempenho;
    protected ViewPager2 viewPager;
    protected MyAccountAdapter myAccountAdapter;


    /**
     * Método executado no instante em que essa activity é criada, seta qual view será associada a
     * essa classe
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minha_conta);
        initGui();
        tabLayout.selectTab(tabLayout.getTabAt(0));
        tabSelectorManager();
        hideSystemUI();
    }


    /**
     * Método chamado quando a janela atual da activity ganha ou perde o foco, é utilizado para es-
     * conder novamente a barra de status e a navigation bar.
     * @param hasFocus - booleano que indica se a janela desta atividade tem foco.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideSystemUI();
    }


    /**
     * Método utilizado para fazer com que a barra de status e a navigation bar não sejam exibidas
     * na activity. Caso o usuário queira visualizá-las, ele deve realizar um movimento de arrastar
     * para cima (na navigation bar), ou para baixo (na status bar), o que fará com que elas apare-
     * çam por um momento e depois sumam novamente.
     */
    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if(controller != null) {
                controller.hide(WindowInsetsCompat.Type.statusBars());
                controller.hide(WindowInsetsCompat.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
        else {
            //noinspection deprecation
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    /**
     * Método utilizado para obter a referência para certos objetos da view que serão manipulados
     * por essa classe e para inicializar alguns outros objetos utilizados aqui no back-end. Além
     * disso, é utilizado também para configurar a cor de exibição de cada uma das abas da tela de
     * minha conta, bem como para configurar o título delas e a animação decorrente da troca de abas
     */
    protected void initGui(){
        tabLayout = findViewById(R.id.tabBar);
        tabPerfil = findViewById(R.id.tabPerfil);
        tabAmigos = findViewById(R.id.tabAmigos);
        tabDesempenho = findViewById(R.id.tabDesempenho);
        viewPager = findViewById(R.id.viewPager);
        tabSelectedIconColor = ContextCompat.getColor(MyAccountActivity.this, R.color.darkerPurple);
        tabUnselectedIconColor = ContextCompat.getColor(MyAccountActivity.this, R.color.white);
        myAccountAdapter = new MyAccountAdapter(getSupportFragmentManager(), getLifecycle(), tabLayout.getTabCount(), this);
        viewPager.setAdapter(myAccountAdapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position){
                case 0:
                    tab.setIcon(R.drawable.ic_id_card);
                    tab.setText(getString(R.string.perfil));
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(tabSelectedIconColor, PorterDuff.Mode.SRC_IN);
                    break;
                case 1:
                    tab.setIcon(R.drawable.ic_user_friends);
                    tab.setText(getString(R.string.amigos));
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(tabSelectedIconColor, PorterDuff.Mode.SRC_IN);
                    break;
                case 2:
                    tab.setIcon(R.drawable.ic_chart_pie);
                    tab.setText(getString(R.string.desempenho));
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(tabSelectedIconColor, PorterDuff.Mode.SRC_IN);
                    break;
            }
        }).attach();
        Objects.requireNonNull(Objects.requireNonNull(tabLayout.getTabAt(0)).getIcon()).setColorFilter(tabSelectedIconColor, PorterDuff.Mode.SRC_IN);
        Objects.requireNonNull(Objects.requireNonNull(tabLayout.getTabAt(1)).getIcon()).setColorFilter(tabUnselectedIconColor, PorterDuff.Mode.SRC_IN);
        Objects.requireNonNull(Objects.requireNonNull(tabLayout.getTabAt(2)).getIcon()).setColorFilter(tabUnselectedIconColor, PorterDuff.Mode.SRC_IN);
    }


    /**
     * Método utilizado para realizar a animação decorrente da transição entre abas, setando suas
     * cores corretamente, e exibindo o conteúdo solicitado
     */
    protected void tabSelectorManager(){
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                Objects.requireNonNull(tab.getIcon()).setColorFilter(tabSelectedIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Objects.requireNonNull(tab.getIcon()).setColorFilter(tabUnselectedIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}