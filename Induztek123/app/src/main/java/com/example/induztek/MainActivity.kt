package com.example.induztek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.induztek.navegations.AppNavigation
import com.example.induztek.view.theme.InduztekTheme
import com.example.induztek.viewmodels.ClienteViewModel
import com.example.induztek.viewmodels.DireccionViewModel
import com.example.induztek.viewmodels.LoginViewModel
import com.example.induztek.viewmodels.TipoEquipoViewModel
import com.example.induztek.viewmodels.TipoPruebaViewModel
import com.example.induztek.viewmodels.TipoUsuarioViewModel
import com.example.induztek.viewmodels.UserViewModel
import com.example.induztek.viewmodels.VisitaViewModel

class MainActivity : ComponentActivity() {

    // ViewModels (capa VM de MVVM) con alcance de la Activity
    private val loginViewModel: LoginViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val tipoUsuarioViewModel: TipoUsuarioViewModel by viewModels()
    private val clienteViewModel: ClienteViewModel by viewModels()
    private val direccionViewModel: DireccionViewModel by viewModels()
    private val tipoEquipoViewModel: TipoEquipoViewModel by viewModels()
    private val tipoPruebaViewModel: TipoPruebaViewModel by viewModels()
    private val visitaViewModel: VisitaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InduztekTheme {
                AppNavigation(
                    loginViewModel, userViewModel, tipoUsuarioViewModel, clienteViewModel,
                    direccionViewModel, tipoEquipoViewModel, tipoPruebaViewModel, visitaViewModel
                )
            }
        }
    }
}
