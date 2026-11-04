package com.example.induztek.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.induztek.repository.models.Cliente
import com.example.induztek.repository.models.TipoEquipo
import com.example.induztek.repository.models.TipoPrueba
import com.example.induztek.view.utils.AnimacionEntradaFormulario
import com.example.induztek.view.utils.CameraCapture
import com.example.induztek.view.utils.PhotoBox
import com.example.induztek.view.utils.SelectorDesplegable
import com.example.induztek.viewmodels.CatalogoViewModel
import com.example.induztek.viewmodels.VisitaViewModel

@Composable
fun NuevaVisitaScreen(
    viewModel: VisitaViewModel,
    clienteViewModel: CatalogoViewModel<Cliente>,
    tipoEquipoViewModel: CatalogoViewModel<TipoEquipo>,
    tipoPruebaViewModel: CatalogoViewModel<TipoPrueba>,
    tecnicoId: Int,
    onGuardada: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val clientes by clienteViewModel.items.collectAsStateWithLifecycle()
    val direcciones by viewModel.direccionesDelCliente.collectAsStateWithLifecycle()
    val tiposEquipo by tipoEquipoViewModel.items.collectAsStateWithLifecycle()
    val tiposPrueba by tipoPruebaViewModel.items.collectAsStateWithLifecycle()
    var showCamera by rememberSaveable { mutableStateOf(false) }
    var visible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    // Visita guardada en Room: se limpia el formulario y se va al historial
    LaunchedEffect(estado.guardada) {
        if (estado.guardada) {
            viewModel.limpiarFormulario()
            onGuardada()
        }
    }

    if (showCamera) {
        CameraCapture(
            carpeta = "equipos",
            onImageCaptured = { uri ->
                viewModel.onFotoChange(uri.toString())
                showCamera = false
            },
            onClose = { showCamera = false }
        )
        return
    }

    // ANIMACIÓN 1 (view/utils/Animations.kt): fade + deslizamiento vertical
    AnimacionEntradaFormulario(visible = visible) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Registrar Nueva Visita",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            if (clientes.isEmpty() || tiposEquipo.isEmpty() || tiposPrueba.isEmpty()) {
                Text(
                    "Faltan datos base: el gerente debe registrar clientes, direcciones, " +
                        "tipos de equipo y tipos de prueba.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Cliente (FK) y dirección/instalación (FK, filtrada por cliente)
            SelectorDesplegable(
                etiqueta = "Cliente",
                opciones = clientes.map { it.id to it.nombre },
                seleccionId = estado.clienteId,
                onSeleccion = { id, _ -> viewModel.onClienteChange(id) },
                error = estado.errores.cliente
            )
            SelectorDesplegable(
                etiqueta = "Dirección / instalación",
                opciones = direcciones.map { it.id to it.texto },
                seleccionId = estado.direccionId,
                onSeleccion = { id, _ -> viewModel.onDireccionChange(id) },
                error = estado.errores.direccion
            )

            Spacer(Modifier.height(4.dp))
            HorizontalDivider()

            Text(
                "Datos del Equipo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            SelectorDesplegable(
                etiqueta = "Tipo de equipo",
                opciones = tiposEquipo.map { it.id to it.nombre },
                seleccionId = estado.tipoEquipoId,
                onSeleccion = { id, nombre -> viewModel.onTipoEquipoChange(id, nombre) },
                error = estado.errores.tipoEquipo
            )
            OutlinedTextField(
                value = estado.codigoEquipo,
                onValueChange = viewModel::onCodigoEquipoChange,
                label = { Text("Código del equipo") },
                isError = estado.errores.codigoEquipo != null,
                supportingText = {
                    estado.errores.codigoEquipo?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            SelectorDesplegable(
                etiqueta = "Tipo de prueba",
                opciones = tiposPrueba.map { it.id to it.nombre },
                seleccionId = estado.tipoPruebaId,
                onSeleccion = { id, nombre -> viewModel.onTipoPruebaChange(id, nombre) },
                error = estado.errores.tipoPrueba
            )
            OutlinedTextField(
                value = estado.resultado,
                onValueChange = viewModel::onResultadoChange,
                label = { Text("Resultado (ej: 850 MΩ)") },
                isError = estado.errores.resultado != null,
                supportingText = {
                    estado.errores.resultado?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.observaciones,
                onValueChange = viewModel::onObservacionesChange,
                label = { Text("Observaciones (opcional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            // Foto del equipo (CameraX)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PhotoBox(
                    imageUri = estado.imageUri,
                    circle = false,
                    onTakePhoto = { showCamera = true }
                )
                Spacer(Modifier.width(12.dp))
                Text("Foto del equipo (opcional)", style = MaterialTheme.typography.bodyMedium)
            }

            OutlinedButton(
                onClick = { viewModel.agregarEquipo() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Agregar equipo") }

            HorizontalDivider()

            Text(
                "Equipos agregados (${estado.equipos.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Error si intenta guardar sin equipos
            estado.errores.equipos?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            estado.equipos.forEachIndexed { indice, equipo ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (equipo.imageUri != null) {
                            AsyncImage(
                                model = equipo.imageUri,
                                contentDescription = equipo.codigoEquipo,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(Modifier.width(12.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(equipo.codigoEquipo, fontWeight = FontWeight.Bold)
                            Text("${equipo.tipoEquipoNombre} - ${equipo.tipoPruebaNombre}")
                            Text("Resultado: ${equipo.resultado}")
                            if (equipo.observaciones.isNotBlank()) {
                                Text("Obs: ${equipo.observaciones}")
                            }
                        }
                        TextButton(onClick = { viewModel.quitarEquipo(indice) }) { Text("Quitar") }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.guardarVisita(tecnicoId) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar Visita") }
        }
    }
}
