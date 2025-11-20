package com.tecsup.authfirebaseapp

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen() {
    val context = LocalContext.current

    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    // Si no hay usuario logueado, mostramos mensaje simple
    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay usuario autenticado.")
        }
        return
    }

    val db = FirebaseFirestore.getInstance()
    val cursosCollection = db.collection("cursos")

    // Estados para formulario
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var cursoIdEditando by remember { mutableStateOf<String?>(null) }

    // Lista de cursos del usuario
    var cursos by remember { mutableStateOf<List<Curso>>(emptyList()) }

    // Cargar cursos en tiempo real
    LaunchedEffect(user.uid) {
        cursosCollection
            .whereEqualTo("userId", user.uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Error al leer cursos", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val lista = snapshot.documents.map { doc ->
                        Curso(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            userId = doc.getString("userId") ?: ""
                        )
                    }
                    cursos = lista
                }
            }
    }

    fun limpiarFormulario() {
        nombre = ""
        descripcion = ""
        cursoIdEditando = null
    }

    fun guardarCurso() {
        if (nombre.isBlank()) {
            Toast.makeText(context, "Ingresa el nombre del curso", Toast.LENGTH_SHORT).show()
            return
        }

        val data = mapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "userId" to user.uid
        )

        if (cursoIdEditando == null) {
            // Crear nuevo
            cursosCollection.add(data)
                .addOnSuccessListener {
                    Toast.makeText(context, "Curso creado", Toast.LENGTH_SHORT).show()
                    limpiarFormulario()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al crear curso", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Actualizar existente
            cursosCollection.document(cursoIdEditando!!)
                .update(data)
                .addOnSuccessListener {
                    Toast.makeText(context, "Curso actualizado", Toast.LENGTH_SHORT).show()
                    limpiarFormulario()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al actualizar curso", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun eliminarCurso(curso: Curso) {
        cursosCollection.document(curso.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Curso eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al eliminar curso", Toast.LENGTH_SHORT).show()
            }
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp)
        ) {

            Text(
                text = "Mis Cursos 📚",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Usuario: ${user.email}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // FORMULARIO
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del curso") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { guardarCurso() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (cursoIdEditando == null) "Agregar curso" else "Guardar cambios")
                }

                if (cursoIdEditando != null) {
                    OutlinedButton(
                        onClick = { limpiarFormulario() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider()

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Listado de cursos",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // LISTA
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cursos) { curso ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = curso.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (curso.descripcion.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = curso.descripcion,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        // Cargar datos en el formulario para editar
                                        nombre = curso.nombre
                                        descripcion = curso.descripcion
                                        cursoIdEditando = curso.id
                                    }
                                ) {
                                    Text("Editar")
                                }
                                OutlinedButton(
                                    onClick = { eliminarCurso(curso) },
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }

        // FOOTER ABAJO
        Text(
            text = "Milagros Ramos - Tecsup",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}
