package com.example.induztek.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
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
import com.example.induztek.repository.models.EquipoDetalle
import com.example.induztek.repository.models.VisitaResumen
import com.example.induztek.viewmodels.VisitaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Historial de visitas (Room + JOIN).
 * tecnicoId = null -> el gerente ve todas; tecnicoId = X -> el técnico ve solo las suyas.
 */
@Composable
fun HistorialScreen(
    viewModel: VisitaViewModel,
    tecnicoId: Int?,
    puedeEliminar: Boolean
) {
    val flujo = remember(tecnicoId) { viewModel.historial(tecnicoId) }
    val visitas by flujo.collectAsStateWithLifecycle()
    var expandidaId by rememberSaveable { mutableIntStateOf(0) }
    var toDelete by remember { mutableStateOf<VisitaResumen?>(null) }
    val formato = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (visitas.isEmpty()) {
            Text("Aún no hay visitas registradas.", modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(visitas, key = { it.id }) { visita ->
                    val expandida = expandidaId == visita.id
                    Card(
                        onClick = { expandidaId = if (expandida) 0 else visita.id },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(visita.clienteNombre, style = MaterialTheme.typography.titleMedium)
                                    Text(visita.direccionTexto, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${formato.format(Date(visita.fecha))} · ${visita.tecnicoNombre} · " +
                                                "${visita.cantidadEquipos} equipo(s)",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Icon(
                                    if (expandida) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                                if (puedeEliminar) {
                                    IconButton(onClick = { toDelete = visita }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            if (expandida) {
                                // Equipos de la visita (consulta puntual con corrutina)
                                val equipos by produceState(emptyList<EquipoDetalle>(), visita.id) {
                                    value = viewModel.getEquipos(visita.id)
                                }
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                equipos.forEach { equipo ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
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
                                        Column {
                                            Text(equipo.codigoEquipo, fontWeight = FontWeight.Bold)
                                            Text("${equipo.tipoEquipo} - ${equipo.tipoPrueba}")
                                            Text("Resultado: ${equipo.resultado}")
                                            if (equipo.observaciones.isNotBlank()) {
                                                Text("Obs: ${equipo.observaciones}")
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { visita ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar visita") },
            text = { Text("¿Eliminar la visita a ${visita.clienteNombre} y sus equipos?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarVisita(visita.id)
                    toDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}
